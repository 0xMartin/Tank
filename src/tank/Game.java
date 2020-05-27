/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import tank.engine.Engine;
import tank.engine.Tools;
import tank.map.MapEditor;
import tank.objects.Tank;
import tank.server.Client;
import tank.server.Server;

/**
 *
 * @author Krcma
 */
public class Game extends JFrame {

    //block scale for variable size of block
    public static int BLOCK_SCALE = 3;
    //game engine
    public static Engine ENGINE;
    //game manager (load all maps, server comunication)
    public static GameManager GAMEMANAGER;
    //images
    public static Images IMAGES;
    //size of frame
    public static Dimension SIZE;
    //pixel fonts
    public static Font ttf15, ttf16, ttf19, ttf26, ttf36, ttf46, ttf56, ttf66, ttf76, ttf100;
    //server
    public static Server server;
    //client
    public static Client client;

    public static int ID_NONE = -1;

    public Game() {
        Game.ENGINE = new Engine(this, 60, 60);
        Game.IMAGES = new Images();
        Game.GAMEMANAGER = new GameManager(Game.ENGINE);
    }

    public void init() throws Exception {
        //font
        Font ttf = Font.createFont(
                Font.TRUETYPE_FONT,
                this.getClass().getResourceAsStream("/tank/src/font.otf")
        );
        Game.ttf15 = ttf.deriveFont(15f);
        Game.ttf16 = ttf.deriveFont(16f);
        Game.ttf19 = ttf.deriveFont(19f);
        Game.ttf26 = ttf.deriveFont(26f);
        Game.ttf36 = ttf.deriveFont(36f);
        Game.ttf46 = ttf.deriveFont(46f);
        Game.ttf56 = ttf.deriveFont(56f);
        Game.ttf66 = ttf.deriveFont(66f);
        Game.ttf76 = ttf.deriveFont(76f);
        Game.ttf100 = ttf.deriveFont(100f);
        //init JFrame
        this.setTitle("Game");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(true);
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                ENGINE.stop();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        this.setVisible(true);
        Game.SIZE = this.getSize();
        //init game engine
        Game.ENGINE.init();
        //init images
        Game.IMAGES.init();
        //load data (default "menu")
        Game.GAMEMANAGER.menu();
    }

    public void start() throws Exception {
        this.ENGINE.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            if (args[0].equals("map_editor")) {
                try {
                    //load images
                    Game.SIZE = Toolkit.getDefaultToolkit().getScreenSize();
                    Game.IMAGES = new Images();
                    Game.IMAGES.init();
                    //start editor
                    MapEditor mapEditor = new MapEditor();
                    mapEditor.run();
                } catch (Exception ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            } else if (args[0].equals("model_editor")) {
                try {
                    //start editor
                    ModelEditor modelE = new ModelEditor();
                    modelE.run();
                } catch (Exception ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
        }
        try {
            //create instance of game
            Game game = new Game();
            //init game (engine, images, ...)
            game.init();
            //start game
            game.start();
            game.revalidate();
        } catch (Exception ex) {
            Logger.getLogger(Tank.class.getName()).log(Level.SEVERE, null, ex);
            Tools.showException(ex);
        }
    }

}
