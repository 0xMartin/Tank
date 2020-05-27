/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tank.engine.Engine;
import tank.engine.EngineMenuItem;
import tank.engine.EngineObject;
import tank.engine.Tools;
import tank.map.Data;
import tank.map.Map;
import tank.map.MapEditor;
import tank.objects.Tank;
import tank.objects.TankType;
import tank.objects.menu.Button;
import tank.objects.menu.Chat;
import tank.objects.menu.Image;
import tank.objects.menu.Label;
import tank.objects.menu.SocketPlayerList;
import tank.objects.menu.TextField;
import tank.server.Client;
import tank.server.Server;

/**
 *
 * @author Krcma
 */
public class GameManager {

    private final Engine engine;

    public GameManager(Engine _engine) {
        this.engine = _engine;
    }

    public void loadMap(String name, boolean serverMode) throws Exception {
        clearAll(false);
        //load map
        try {
            FileInputStream f = new FileInputStream("maps/" + name);
            ObjectInputStream o = new ObjectInputStream(f);
            Data d = (Data) o.readObject();
            Map map = new Map("", new Dimension(1, 1));
            map.setData(d);
            o.close();
            //ground
            this.engine.setGround(map.getGround(), map.getMapSize());
            //object
            List<EngineObject> list = map.getObjects(this.engine.getEngineObjects());
            this.engine.getEngineObjects().clear();
            list.forEach((obj) -> {
                this.engine.getEngineObjects().add(obj);
            });
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MapEditor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(MapEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //chat
        //this.engine.getMenuItems().add(new Chat(20, (int) (this.engine.getRenderEngine().getHeight() * 0.75f)));
    }

    public void clearAll(boolean killPlayers) {
        this.engine.getEngineEffects().clear();
        if (killPlayers) {
            this.engine.getEngineObjects().clear();
        } else {
            for (int i = 0; i < this.engine.getEngineObjects().size(); i++) {
                if (this.engine.getEngineObjects().get(i).getID() < 0) {
                    this.engine.getEngineObjects().remove(i);
                    i--;
                }
            }
        }
        this.engine.setGround(null, new Dimension(0, 0));
        this.engine.getMenuItems().clear();
    }

    public void menu() {
        clearAll(true);
        List<EngineMenuItem> objects = this.engine.getMenuItems();
        Button b;
        //menu label
        objects.add(new Label("Tanks", Game.SIZE.width / 2 + 10, 210, new Color(190, 170, 120, 80), Game.ttf100, true, true));
        objects.add(new Label("Tanks", Game.SIZE.width / 2, 200, new Color(120, 180, 130, 220), Game.ttf100, true, true));
        //single player
        b = new Button(
                "Singleplayer",
                50,
                400,
                new Color(170, 170, 130),
                new Color(80, 80, 30, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                engine.MODE = Engine.Mode.PhysicsIN;
                engine.gameRun = true;
                try {
                    //create player
                    Tank t = new Tank(0, 0, 0, 0, TankType.TANK15, Team.A);
                    engine.getEngineObjects().add(t);
                    engine.setPlayer(t);
                    //load map
                    loadMap("map_1.map", false);
                } catch (Exception ex) {
                    Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        objects.add(b);
        //multiplayer
        b = new Button(
                "Multiplayer",
                50,
                (int) (440 + Game.ttf46.getSize2D()),
                new Color(170, 130, 130),
                new Color(80, 30, 30, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server();
            }
        });
        objects.add(b);
        //info
        b = new Button(
                "Settings",
                50,
                (int) (480 + Game.ttf46.getSize2D() * 2),
                new Color(130, 130, 170),
                new Color(30, 30, 80, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        objects.add(b);
        //info
        b = new Button(
                "Info",
                50,
                (int) (520 + Game.ttf46.getSize2D() * 3),
                new Color(130, 170, 170),
                new Color(30, 80, 80, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        objects.add(b);
        //info
        b = new Button(
                "Quit",
                50,
                (int) (560 + Game.ttf46.getSize2D() * 4),
                new Color(130, 170, 170),
                new Color(30, 80, 80, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        objects.add(b);
    }

    public void server() {
        clearAll(true);
        List<EngineMenuItem> objects = this.engine.getMenuItems();
        Button b;
        //menu label
        objects.add(new Label("Multiplayer", Game.SIZE.width / 2 + 10, 210, new Color(150, 110, 110, 80), Game.ttf66, true, true));
        objects.add(new Label("Multiplayer", Game.SIZE.width / 2, 200, new Color(170, 130, 130, 210), Game.ttf66, true, true));
        //join
        b = new Button(
                "Join",
                50,
                400,
                new Color(170, 130, 130),
                new Color(80, 30, 30, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serverJoin();
            }
        });
        objects.add(b);
        //host
        b = new Button(
                "Host",
                50,
                (int) (440 + Game.ttf46.getSize2D()),
                new Color(130, 170, 130),
                new Color(30, 80, 30, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serverHost();
            }
        });
        objects.add(b);
        //back
        b = new Button(
                "Back",
                50,
                (int) (480 + Game.ttf46.getSize2D() * 2),
                new Color(170, 170, 130),
                new Color(80, 80, 30, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu();
            }
        });
        objects.add(b);
    }

    public void serverHost() {
        clearAll(true);
        List<EngineMenuItem> objects = this.engine.getMenuItems();
        Button b;
        TextField t;
        //menu label
        objects.add(new Label("Host", Game.SIZE.width / 2 + 10, 210, new Color(150, 110, 110, 80), Game.ttf66, true, true));
        objects.add(new Label("Host", Game.SIZE.width / 2, 200, new Color(170, 130, 130, 210), Game.ttf66, true, true));
        //port
        objects.add(new Label("Port:", 50, 400, new Color(110, 180, 110, 200), Game.ttf46, true, false));
        t = new TextField(
                "10000",
                500,
                405,
                400,
                (int) (Game.ttf36.getSize() * 1.5f),
                new Color(70, 140, 70, 200),
                new Color(40, 110, 40, 50),
                Game.ttf36,
                true,
                false
        );
        objects.add(t);
        //limit
        objects.add(new Label("Players limit:", 50, (int) (440 + Game.ttf46.getSize2D()), new Color(110, 110, 180, 200), Game.ttf46, true, false));
        t = new TextField(
                "10",
                500,
                (int) (445 + Game.ttf46.getSize2D()),
                400,
                (int) (Game.ttf36.getSize() * 1.5f),
                new Color(70, 70, 140, 200),
                new Color(40, 40, 110, 50),
                Game.ttf36,
                true,
                false
        );
        objects.add(t);
        //name
        objects.add(new Label("Name:", 50, (int) (480 + Game.ttf46.getSize2D() * 2), new Color(110, 110, 180, 200), Game.ttf46, true, false));
        t = new TextField(
                "",
                500,
                (int) (485 + Game.ttf46.getSize2D() * 2),
                400,
                (int) (Game.ttf36.getSize() * 1.5f),
                new Color(70, 70, 140, 200),
                new Color(40, 40, 110, 50),
                Game.ttf36,
                true,
                false
        );
        objects.add(t);
        //Create server
        b = new Button(
                "Create server [BATTLE]",
                50,
                (int) (580 + Game.ttf46.getSize2D() * 2),
                new Color(130, 170, 130),
                new Color(30, 80, 30, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //create client server
                    int port = 0, limit = 0;
                    String name = "";
                    int i = 0;
                    OUTER:
                    for (EngineMenuItem obj : objects) {
                        if (obj instanceof TextField) {
                            switch (i) {
                                case 0:
                                    port = Integer.parseInt(((TextField) obj).getText());
                                    break;
                                case 1:
                                    limit = Integer.parseInt(((TextField) obj).getText());
                                    break;
                                default:
                                    name = ((TextField) obj).getText();
                                    break OUTER;
                            }
                            i++;
                        }
                    }
                    engine.MODE = Engine.Mode.PhysicsIN;
                    Game.server = new Server(port, limit);
                    Game.server.init();
                    Game.server.startServer();
                    Game.server.addLeader(new Tank(0, 0, 0f, 0f, TankType.TANK1, Team.NONE), name);
                    //show menu
                    serverRoomBattleHost();
                } catch (IOException ex) {
                    Tools.showException(ex);
                } catch (Exception ex) {
                    Tools.showException(ex);
                }
            }
        });
        objects.add(b);
        //Create server
        b = new Button(
                "Create server [MAP]",
                50,
                (int) (620 + Game.ttf46.getSize2D() * 3),
                new Color(170, 130, 170),
                new Color(80, 30, 80, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        objects.add(b);
        //back
        b = new Button(
                "Back",
                50,
                (int) (660 + Game.ttf46.getSize2D() * 4),
                new Color(170, 170, 170),
                new Color(80, 80, 80, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server();
            }
        });
        objects.add(b);
    }

    public void serverRoomBattleHost() {
        clearAll(false);
        List<EngineMenuItem> objects = this.engine.getMenuItems();
        Button b;
        //menu label
        objects.add(new Label("Server [Battle]", Game.SIZE.width / 2 + 10, 210, new Color(150, 110, 150, 80), Game.ttf66, true, true));
        objects.add(new Label("Server [Battle]", Game.SIZE.width / 2, 200, new Color(170, 130, 170, 210), Game.ttf66, true, true));
        try {
            objects.add(new Label("IP: " + Inet4Address.getLocalHost().getHostAddress(), 10, 10 + Game.ttf36.getSize(), new Color(150, 110, 150, 80), Game.ttf36, true, false));
            objects.add(new Label("PORT: " + Game.server.getPort(), 10, 20 + Game.ttf36.getSize() * 2, new Color(150, 110, 150, 80), Game.ttf36, true, false));
        } catch (UnknownHostException ex) {
            Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //socket playyer list
        objects.add(new SocketPlayerList((int) (Game.SIZE.width * 0.2), 250, (int) (Game.SIZE.width * 0.6), 500, Game.client, true));
        //Stop server
        b = new Button(
                "Stop server",
                50,
                (int) (Game.SIZE.height - Game.ttf36.getSize2D()),
                new Color(170, 170, 170),
                new Color(80, 80, 80, 100),
                Game.ttf36,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Game.server.closeServer();
                Game.server = null;
                menu();
            }
        });
        objects.add(b);
        //Choose map
        b = new Button(
                "Choose map",
                550,
                (int) (Game.SIZE.height - Game.ttf36.getSize2D()),
                new Color(170, 170, 170),
                new Color(80, 80, 80, 100),
                Game.ttf36,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        objects.add(b);
        //Choose tank
        b = new Button(
                "Choose tank",
                1050,
                (int) (Game.SIZE.height - Game.ttf36.getSize2D()),
                new Color(170, 170, 170),
                new Color(80, 80, 80, 100),
                Game.ttf36,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseTankMenu(true);
            }
        });
        objects.add(b);
        //Start
        b = new Button(
                "Start",
                1550,
                (int) (Game.SIZE.height - Game.ttf36.getSize2D()),
                new Color(170, 170, 170),
                new Color(80, 80, 80, 100),
                Game.ttf36,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //start game
                if (Game.server != null) {
                    try {
                        Game.server.MAP = "map_1.map";
                        Game.server.startGame();
                    } catch (Exception ex) {
                        Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
                        Tools.showException(ex);
                    }
                }
            }
        });
        objects.add(b);
    }

    public void serverJoin() {
        clearAll(true);
        List<EngineMenuItem> objects = this.engine.getMenuItems();
        Button b;
        TextField t;
        //menu label
        objects.add(new Label("Join", Game.SIZE.width / 2 + 10, 210, new Color(150, 110, 110, 80), Game.ttf66, true, true));
        objects.add(new Label("Join", Game.SIZE.width / 2, 200, new Color(170, 130, 130, 210), Game.ttf66, true, true));
        //IP
        objects.add(new Label("IP:", 50, 400, new Color(110, 180, 110, 200), Game.ttf46, true, false));
        t = new TextField(
                "localhost",
                300,
                405,
                400,
                (int) (Game.ttf36.getSize() * 1.5f),
                new Color(70, 140, 70, 200),
                new Color(40, 110, 40, 50),
                Game.ttf36,
                true,
                false
        );
        objects.add(t);
        //Port
        objects.add(new Label("Port:", 50, (int) (440 + Game.ttf46.getSize2D()), new Color(110, 110, 180, 200), Game.ttf46, true, false));
        t = new TextField(
                "10000",
                300,
                (int) (445 + Game.ttf46.getSize2D()),
                400,
                (int) (Game.ttf36.getSize() * 1.5f),
                new Color(70, 70, 140, 200),
                new Color(40, 40, 110, 50),
                Game.ttf36,
                true,
                false
        );
        objects.add(t);
        //Name
        objects.add(new Label("Name:", 50, (int) (480 + Game.ttf46.getSize2D() * 2), new Color(110, 110, 180, 200), Game.ttf46, true, false));
        t = new TextField(
                "",
                300,
                (int) (485 + Game.ttf46.getSize2D() * 2),
                400,
                (int) (Game.ttf36.getSize() * 1.5f),
                new Color(70, 70, 140, 200),
                new Color(40, 40, 110, 50),
                Game.ttf36,
                true,
                false
        );
        objects.add(t);
        //Join
        b = new Button(
                "Join",
                50,
                (int) (580 + Game.ttf46.getSize2D() * 2),
                new Color(130, 170, 130),
                new Color(30, 80, 30, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //create client server
                String ip = "", name = "";
                int port = 0;
                int i = 0;
                OUTER:
                for (EngineMenuItem obj : objects) {
                    if (obj instanceof TextField) {
                        switch (i) {
                            case 0:
                                ip = ((TextField) obj).getText();
                                break;
                            case 1:
                                port = Integer.parseInt(((TextField) obj).getText());
                                break;
                            default:
                                name = ((TextField) obj).getText();
                                break OUTER;
                        }
                        i++;
                    }
                }
                try {
                    Game.client = new Client(ip, port);
                    Game.client.init();
                    Game.client.start();
                    Game.client.send(
                            new Object[]{
                                Client.Command.CreateTank,
                                name,
                                TankType.TANK1,
                                Team.NONE
                            });
                } catch (IOException ex) {
                    Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
                    Tools.showException(ex);
                }
                serverRoomBattleJoin();
            }
        });
        objects.add(b);
        //back
        b = new Button(
                "Back",
                50,
                (int) (620 + Game.ttf46.getSize2D() * 3),
                new Color(170, 170, 170),
                new Color(80, 80, 80, 100),
                Game.ttf46,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server();
            }
        });
        objects.add(b);
    }

    public void serverRoomBattleJoin() {
        clearAll(false);
        List<EngineMenuItem> objects = this.engine.getMenuItems();
        Button b;
        //menu label
        objects.add(new Label("Server [Battle]", Game.SIZE.width / 2 + 10, 210, new Color(150, 110, 150, 80), Game.ttf66, true, true));
        objects.add(new Label("Server [Battle]", Game.SIZE.width / 2, 200, new Color(170, 130, 170, 210), Game.ttf66, true, true));
        //socket playyer list
        objects.add(new SocketPlayerList((int) (Game.SIZE.width * 0.2), 250, (int) (Game.SIZE.width * 0.6), 500, Game.client, false));
        //Stop server
        b = new Button(
                "Quit",
                50,
                (int) (Game.SIZE.height - Game.ttf36.getSize2D()),
                new Color(170, 170, 170),
                new Color(80, 80, 80, 100),
                Game.ttf36,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Game.client.stop();
                    Game.client = null;
                    menu();
                } catch (IOException ex) {
                    Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        objects.add(b);
        //Choose tank
        b = new Button(
                "Change tank",
                550,
                (int) (Game.SIZE.height - Game.ttf36.getSize2D()),
                new Color(170, 170, 170),
                new Color(80, 80, 80, 100),
                Game.ttf36,
                true,
                false
        );
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseTankMenu(false);
            }
        });
        objects.add(b);
    }

    private boolean chooseTankMenu_host;

    public void chooseTankMenu(boolean host) {
        this.chooseTankMenu_host = host;
        clearAll(false);
        List<EngineMenuItem> objects = this.engine.getMenuItems();
        Image img;
        Button b;
        //menu label
        objects.add(new Label("Tank list", Game.SIZE.width / 2 + 10, 210, new Color(110, 150, 150, 80), Game.ttf66, true, true));
        objects.add(new Label("Tank list", Game.SIZE.width / 2, 200, new Color(130, 170, 170, 210), Game.ttf66, true, true));

        objects.add(new Label("Heavy", 50, 310, new Color(130, 170, 170, 210), Game.ttf36, true, false));
        objects.add(new Label("TD", 350, 310, new Color(130, 170, 170, 210), Game.ttf36, true, false));
        objects.add(new Label("Auto loader", 650, 310, new Color(130, 170, 170, 210), Game.ttf36, true, false));
        objects.add(new Label("Light", 950, 310, new Color(130, 170, 170, 210), Game.ttf36, true, false));

        for (int i = 0; i < Game.IMAGES.tanks.length; i++) {
            int j = (i / 5);
            img = new Image(
                    50 + j * 300,
                    (Game.IMAGES.tanks[i].getHeight() + 5) * (i - j * 5) + 365 - Game.IMAGES.tanks[i].getHeight(),
                    Game.IMAGES.tanks[i],
                    true,
                    false
            );
            objects.add(img);
            b = new Button(
                    "Choose",
                    Game.IMAGES.tanks[i].getWidth() + 40 + j * 300,
                    (Game.IMAGES.tanks[i].getHeight() + 5) * (i - j * 5) + 365,
                    new Color(170, 170, 170),
                    new Color(80, 80, 80, 100),
                    Game.ttf26,
                    true,
                    false
            );
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        int j = 0;
                        for (int i = 0; i < engine.getMenuItems().size(); i++) {
                            EngineMenuItem emi = engine.getMenuItems().get(i);
                            if (emi instanceof Button) {
                                j++;
                                if (e.getSource() == emi) {
                                    if (engine.MODE == Engine.Mode.PhysicsOUT) {
                                        Game.client.send(new Object[]{
                                            Client.Command.ChangeTankType,
                                            Game.client.getPlayerID(),
                                            Tools.getTankType(j)});
                                    } else {
                                        for (EngineObject eo : engine.getEngineObjects()) {
                                            if (eo == engine.getPlayer()) {
                                                ((Tank) eo).changeTankType(Tools.getTankType(j));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(GameManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (chooseTankMenu_host) {
                        serverRoomBattleHost();
                    } else {
                        serverRoomBattleJoin();
                    }
                }
            });
            objects.add(b);
        }
    }

}
