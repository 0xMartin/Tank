/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tank.Game;
import tank.Team;
import tank.engine.Engine;
import tank.engine.EngineMenuItem;
import tank.engine.EngineObject;
import tank.engine.Tools;
import tank.objects.Tank;
import tank.objects.TankType;
import tank.objects.menu.Chat;

/**
 *
 * @author Krcma
 */
public class Client {

    public enum Command {
        CreateTank, ControlTank, ChangeTeam, GetAllPlayers, ChangeTankType, Chat;
        //CreateTank {name,TankType,Team}
        //GetAllPlayers {}
        //ChangeTeam {ID,Team}
        //ControlTank {ID, int[] controler}
        //ChangeTanType {ID,TankType}
    }

    private final Socket clientSocket;
    private final BufferedInputStream inputStream;
    private final BufferedOutputStream outputStream;
    private Thread thread;
    private int player_ID;
    private final List<Tank> players;

    public Client(String address, int port) throws IOException {
        //set engine mode to physics calculating outside
        Game.ENGINE.MODE = Engine.Mode.PhysicsOUT;
        this.clientSocket = new Socket(address, port);
        this.clientSocket.setTcpNoDelay(true);
        this.outputStream = new BufferedOutputStream(this.clientSocket.getOutputStream());
        this.inputStream = new BufferedInputStream(this.clientSocket.getInputStream());
        this.players = new ArrayList<>();
        System.out.println("[CLIENT]>Client created");
    }

    public int getPlayerID() {
        return this.player_ID;
    }

    public void init() {
        //only for data recieving
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Object[] data = (Object[]) readObject();
                        if (data != null) {
                            switch ((Server.Command) data[0]) {
                                case Chat:
                                    //chat ****
                                    Game.ENGINE.getMenuItems().stream().filter((mi) -> (mi instanceof Chat)).forEachOrdered((mi) -> {
                                        ((Chat) mi).writeMessage((Object[]) data[1]);
                                    });
                                    break;

                                case ResizeBuffer:
                                    buffer = new byte[(int) data[1]];
                                    break;
                                case GameStart:
                                    //load all objcets
                                    try {
                                        Game.GAMEMANAGER.loadMap((String) data[1], true);
                                    } catch (Exception ex) {
                                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    //run game
                                    Game.ENGINE.gameRun = true;
                                    break;
                                case TankID:
                                    player_ID = (int) data[1];
                                    System.out.println("[CLIENT]>Player ID: " + player_ID);
                                    break;
                                case AllPlayers:
                                    //all players(tanks) connected on server (for server battle waiting room)
                                    List<Object[]> pl = (List<Object[]>) data[1];
                                    if (pl.size() < players.size()) {
                                        players.clear();
                                    }
                                    for (int i = 0; i < pl.size(); i++) {
                                        Object[] o = pl.get(i);
                                        Tank t = new Tank(0, 0, 0f, 0f, (TankType) o[2], (Team) o[3]);
                                        t.setName((String) o[1]);
                                        t.setID((int) o[0]);
                                        Tools.replace(t.getID(), t, players);
                                    }
                                    break;
                                case GameRefresh:
                                    //ping
                                    ping_c++;
                                    double d = System.nanoTime() - ping_l;
                                    if (d > 1e9) {
                                        ping_l = System.nanoTime();
                                        ping = (int) ((int) (1000f / ping_c) * (d / 1e9));
                                        ping_c = 0;
                                    }
                                    //refresh all engineobjects
                                    List<Object[]> objectsConfig = (List<Object[]>) data[1];
                                    LOOP1:
                                    for (int i = 0; i < Game.ENGINE.getEngineObjects().size(); i++) {
                                        EngineObject eo = Game.ENGINE.getEngineObjects().get(i);
                                        if (eo == null) {
                                            Game.ENGINE.getEngineObjects().remove(i);
                                            i--;
                                            continue;
                                        }
                                        for (Object[] config : objectsConfig) {
                                            if (eo.setConfig(config)) {
                                                if (eo instanceof Tank) {
                                                    //set player for this engine
                                                    if (eo.getID() == player_ID) {
                                                        Game.ENGINE.setPlayer((Tank) eo);
                                                    }
                                                    //kill tank because its death
                                                    if (((Tank) eo).getLife() <= 0) {
                                                        ((Tank) eo).killTank(Game.ENGINE);
                                                    }
                                                }
                                                objectsConfig.remove(config);
                                                continue LOOP1;
                                            }
                                        }
                                        //kill some object
                                        if (objectsConfig.size() < Game.ENGINE.getEngineObjects().size()) {
                                            if (eo.getID() > 0) {
                                                Game.ENGINE.getEngineObjects().remove(eo);
                                                i -= 1;
                                            }
                                        }
                                    }
                                    //unassigned config -> object isnt created
                                    objectsConfig.stream().filter((config) -> (config != null)).forEachOrdered((config) -> {
                                        Game.ENGINE.getEngineObjects().add(Tools.createEngineObjectFromConfig(config));
                                    });
                                    break;
                            }
                        }
                    } catch (IOException ex) {
                        Tools.showException(ex);
                        try {
                            clientSocket.close();
                        } catch (IOException ex1) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    public void send(Object[] obj) throws IOException {
        //send
        if (obj != null) {
            byte[] b = objectToBytes(obj);
            this.outputStream.write(b, 0, b.length);
            this.outputStream.flush();
        }
    }

    public void start() {
        this.thread.start();
    }

    public void stop() throws IOException {
        this.thread.stop();
        this.clientSocket.close();
        Game.ENGINE.MODE = Engine.Mode.PhysicsIN;
        Game.ENGINE.gameRun = false;
        System.out.println("[CLIENT]>Client socket closed");
    }

    public List<Tank> getAllPlayersOnServer() {
        return this.players;
    }

    private double ping_l = 0;
    private int ping = 0, ping_c = 0;

    public int getPing() {
        return this.ping;
    }

    private byte[] buffer = new byte[4096];

    private Object[] readObject() throws IOException {
        this.inputStream.read(this.buffer);
        //read
        ByteArrayInputStream bis = new ByteArrayInputStream(this.buffer);
        try {
            ObjectInputStream out = new ObjectInputStream(bis);
            return (Object[]) out.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
            }
        }
        return null;
    }

    private byte[] objectToBytes(Object[] data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(data);
            out.flush();
            return buffer = bos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
            }
        }
        return null;
    }

}
