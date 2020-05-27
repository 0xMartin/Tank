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
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tank.Game;
import tank.Team;
import tank.engine.Engine;
import tank.engine.EngineObject;
import tank.objects.Tank;
import tank.objects.TankType;

/**
 *
 * @author Krcma
 */
public class Server {

    public enum Command {
        TankID, BulletID, AllPlayers, GameStart, GameRefresh, ResizeBuffer, Chat;
        //TankID{ID}
        //AllPlayers{List<Object[]>->new Object[]{id,name,TankType,Team}}
        //GameStart{mapname}
        //GameRefresh{List<Object[]>->new Object[]{ID,engineObject.getTankConfig()}}
    }

    private Thread acceptThread, dataSender;
    private final ServerSocket serverSocket;
    private final List<ClientManager> clients;
    private int limit;
    public String MAP;

    private final List<Object[]> chatBuffer;

    public void writeToChat(String text) {
        this.chatBuffer.add(new Object[]{Game.ENGINE.getPlayer().getName(), text});
    }

    public Server(int port, int _limit) throws IOException {
        //create server
        this.limit = _limit;
        this.serverSocket = new ServerSocket(port);
        //this.serverSocket.setPerformancePreferences(_limit, limit, _limit);
        System.out.println("[SERVER]>Server created");
        System.out.println("[SERVER]>IP:" + Inet4Address.getLocalHost().getHostAddress());
        System.out.println("[SERVER]>Port:" + this.serverSocket.getLocalPort());
        System.out.println("[SERVER]>Limit:" + _limit);
        this.clients = new ArrayList<>();
        this.chatBuffer = new ArrayList<>();
    }

    //init server
    public void init() throws Exception {
        //this thread accepting clients
        this.acceptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientManager cm = new ClientManager(clientSocket, limit);
                        cm.init();
                        clients.add(cm);
                        cm.getThread().start();
                        System.out.println("[SERVER]>Client joined " + clientSocket.getInetAddress());
                        limit--;
                        if (limit <= 0) {
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.acceptThread.setName("Tank_Serve_Accept");
        //data sender
        this.dataSender = new Thread(new Runnable() {
            @Override
            public void run() {
                int buffSize = 4096;
                while (true) {
                    //rps regulation
                    double last = System.nanoTime(), ticks = 0, perTick = 1e9 / Game.ENGINE.getRPS();
                    while (true) {
                        double now = System.nanoTime();
                        ticks += (now - last) / perTick;
                        last = now;
                        while (ticks >= 1) {
                            ticks--;
                            List<Object[]> objectsConfig = new ArrayList<>();
                            for (int i = 0; i < Game.ENGINE.getEngineObjects().size(); i++) {
                                Object[] cnf = Game.ENGINE.getEngineObjects().get(i).getConfig();
                                if ((Engine.ObjClass) cnf[0] == Engine.ObjClass.TANKCLASS
                                        || (Engine.ObjClass) cnf[0] == Engine.ObjClass.BULLETCLASS
                                        || (Engine.ObjClass) cnf[0] == Engine.ObjClass.BULLETPATHCLASS) {
                                    objectsConfig.add(cnf);
                                }
                            }
                            //conver object to bytes
                            byte[] buff = objectToBytes(new Object[]{Server.Command.GameRefresh, objectsConfig});
                            byte[] chat = null;
                            int bSize = buff.length;
                            if (!chatBuffer.isEmpty()) {
                                chat = objectToBytes(new Object[]{Server.Command.Chat, chatBuffer.get(0)});
                                chatBuffer.remove(0);
                                bSize = Math.max(bSize, chat.length);
                            }
                            //resize buffer (only if buffer size owerflow or is enough lower then preset)
                            if (buffSize < bSize || buffSize - bSize > buffSize / 5) {
                                buffSize = bSize;
                                byte[] rbuf = objectToBytes(new Object[]{Server.Command.ResizeBuffer, buffSize});
                                for (int i = 0; i < clients.size(); i++) {
                                    ClientManager cm = clients.get(i);
                                    try {
                                        cm.getBufferedOutputStream().write(rbuf, 0, rbuf.length);
                                        cm.getBufferedOutputStream().flush();
                                    } catch (IOException ex) {
                                    }
                                }
                            }
                            //send data to client
                            for (int i = 0; i < clients.size(); i++) {
                                ClientManager cm = clients.get(i);
                                try {
                                    cm.getBufferedOutputStream().write(buff, 0, buff.length);
                                    if (chat != null) {
                                        cm.getBufferedOutputStream().write(chat, 0, chat.length);
                                    }
                                    cm.getBufferedOutputStream().flush();
                                } catch (IOException ex) {
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public int getPort() {
        return this.serverSocket.getLocalPort();
    }

    public void addLeader(Tank tank, String name) {
        tank.setName(name);
        tank.setID(this.limit);
        this.limit--;
        Game.ENGINE.setPlayer(tank);
        Game.ENGINE.getEngineObjects().add(tank);
    }

    //start game (physics engine
    public void startGame() throws Exception {
        //load map
        Game.GAMEMANAGER.loadMap(this.MAP, true);
        //run game
        Game.ENGINE.gameRun = true;
        //***
        for (int i = 0; i < this.clients.size(); i++) {
            ClientManager cm = this.clients.get(i);
            byte[] buff = objectToBytes(new Object[]{Server.Command.GameStart, this.MAP});
            cm.getBufferedOutputStream().write(buff, 0, buff.length);
            cm.getBufferedOutputStream().flush();
        }
        //run datasender thread
        this.dataSender.start();
        System.out.println("[SERVER]>Game start");
    }

    //start server
    public void startServer() throws Exception {
        this.acceptThread.start();
        System.out.println("[SERVER]>Server run");
    }

    public void closeServer() {
        this.acceptThread.stop();
        this.dataSender.stop();
        for (int i = 0; i < this.clients.size(); i++) {
            ClientManager c = this.clients.get(i);
            try {
                c.socket.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            c.getThread().stop();
        }
        try {
            this.serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        //stop game
        Game.ENGINE.gameRun = false;
        System.out.println("[SERVER]>Server closed");
    }

    private byte[] buffer = new byte[4096];

    private Object[] readObject(BufferedInputStream inputStrem) throws IOException {
        inputStrem.read(this.buffer);
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

    //class for sending and receiving (for on client)
    private class ClientManager {

        private Socket socket;
        private Thread thread;
        private String name;
        private final BufferedInputStream inputStrem;
        private final BufferedOutputStream outputStream;
        private final int id;

        private byte[] clBuff;

        public ClientManager(Socket _socket, int _id) throws IOException {
            _socket.setTcpNoDelay(true);
            this.outputStream = new BufferedOutputStream(_socket.getOutputStream());
            this.inputStrem = new BufferedInputStream(_socket.getInputStream());
            this.id = _id;
            this.socket = _socket;
        }

        public void init() {
            this.thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            //if client send data then he is ready for data from server
                            Object[] data = (Object[]) readObject(inputStrem);
                            if (data != null) {
                                //read (put data)
                                switch ((Client.Command) data[0]) {
                                    case CreateTank:
                                        //create tank in engine
                                        Tank t = new Tank(getID() * 150, getID() * 150, 0f, 0f, (TankType) data[2], (Team) data[3]);
                                        name = (String) data[1];
                                        t.setName((String) data[1]);
                                        t.setID(getID());
                                        Game.ENGINE.getEngineObjects().add(t);
                                        //send back tank id
                                        clBuff = objectToBytes(new Object[]{Server.Command.TankID, getID()});
                                        outputStream.write(clBuff, 0, clBuff.length);
                                        outputStream.flush();
                                        break;
                                    case Chat:
                                        chatBuffer.add(new Object[]{name, (String) data[1]});
                                        break;
                                    case ChangeTeam:
                                        //change team of player
                                        int _id = (int) data[1];
                                        for (int i = 0; i < Game.ENGINE.getEngineObjects().size(); i++) {
                                            EngineObject o = Game.ENGINE.getEngineObjects().get(i);
                                            if (o instanceof Tank) {
                                                if (o.getID() == _id) {
                                                    System.out.println("[SERVER]>Player " + ((Tank) o).getName() + " join to Team " + (Team) data[2]);
                                                    ((Tank) o).setTeam((Team) data[2]);
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    case ChangeTankType:
                                        //change team of player
                                        _id = (int) data[1];
                                        for (int i = 0; i < Game.ENGINE.getEngineObjects().size(); i++) {
                                            EngineObject o = Game.ENGINE.getEngineObjects().get(i);
                                            if (o instanceof Tank) {
                                                if (o.getID() == _id) {
                                                    ((Tank) o).changeTankType((TankType) data[2]);
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    case ControlTank:
                                        //control tank
                                        _id = (int) data[1];
                                        for (int i = 0; i < Game.ENGINE.getEngineObjects().size(); i++) {
                                            EngineObject o = Game.ENGINE.getEngineObjects().get(i);
                                            if (o instanceof Tank) {
                                                if (o.getID() == _id) {
                                                    ((Tank) o).setControler((boolean[]) data[2]);
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    case GetAllPlayers:
                                        //send all players on this server (Their names, selected tanks and team)
                                        List<Object[]> pl = new ArrayList<>();
                                        for (int i = 0; i < Game.ENGINE.getEngineObjects().size(); i++) {
                                            EngineObject o = Game.ENGINE.getEngineObjects().get(i);
                                            if (o instanceof Tank) {
                                                if (o.getID() > 0) {
                                                    pl.add(
                                                            new Object[]{
                                                                ((Tank) o).getID(),
                                                                ((Tank) o).getName(),
                                                                ((Tank) o).getTankType(),
                                                                ((Tank) o).getTeam()
                                                            }
                                                    );
                                                }
                                            }
                                        }
                                        clBuff = objectToBytes(new Object[]{Server.Command.AllPlayers, pl});
                                        outputStream.write(clBuff, 0, clBuff.length);
                                        outputStream.flush();
                                        break;
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("[SERVER]> Client " + socket.getInetAddress() + " left game (" + e.toString() + ")");
                            close();
                            return;
                        }
                    }
                }
            });
            clients.remove(this);
        }

        public Thread getThread() {
            return this.thread;
        }

        public int getID() {
            return this.id;
        }

        public BufferedInputStream getBufferedInputStream() {
            return this.inputStrem;
        }

        public BufferedOutputStream getBufferedOutputStream() {
            return this.outputStream;
        }

        public void close() {
            try {
                getBufferedOutputStream().close();
                getBufferedInputStream().close();
                for (int i = 0; i < Game.ENGINE.getEngineObjects().size(); i++) {
                    EngineObject o = Game.ENGINE.getEngineObjects().get(i);
                    if (o instanceof Tank) {
                        if (o.getID() == getID()) {
                            Game.ENGINE.getEngineObjects().remove(o);
                        }
                    }
                }
                clients.remove(this);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
