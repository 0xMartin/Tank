/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.engine;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFrame;
import tank.Game;
import tank.objects.Ground;
import tank.objects.Tank;

/**
 *
 * @author Krcma
 */
public class Engine implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    public boolean gameRun = false;

    public enum ObjClass {
        TANKCLASS, BULLETCLASS, BULLETPATHCLASS, TREECLASS, ROCKCLASS, FIRECLASS, FENCECLASS, BUILDINGCLASS;
    }

    public enum Mode {
        PhysicsIN, PhysicsOUT;
    }

    public Mode MODE;

    //JFrame
    private final JFrame frame;

    //FPS(render engine), RPS(Physics engine)
    private final int FPS, RPS;

    public int getFPS() {
        return this.FPS;
    }

    public int getRPS() {
        return this.RPS;
    }

    private final RenderEngine renderEngine;
    private final PhysicsEngine physicsEngine;

    //engine objects
    private Ground[][] GROUND;
    private Dimension MAPSIZE;
    private final List<EngineObject> OBJECTS;
    private final List<EngineEffect> EFFECTS;
    private final List<EngineMenuItem> MENU;

    public static float FX_volume = 10, MUSIC_volume = 10;

    //player
    private Tank PLAYER;

    //sounds
    private final HashMap<String, SoundPlayer> sounds;

    public Tank getPlayer() {
        return this.PLAYER;
    }

    public void setPlayer(Tank eo) {
        this.PLAYER = eo;
    }

    //normal mode
    public Engine(Game game, int fps, int rps) {
        this.frame = game;
        this.FPS = fps;
        this.RPS = rps;
        this.renderEngine = new RenderEngine(this);
        this.physicsEngine = new PhysicsEngine(this);
        this.OBJECTS = new ArrayList<>();
        this.EFFECTS = new ArrayList<>();
        this.MENU = new ArrayList<>();
        this.sounds = new HashMap<>();
        this.MODE = Mode.PhysicsIN;
        this.MAPSIZE = new Dimension(0, 0);
    }

    //server mode
    public Engine(int rps) {
        this.frame = null;
        this.FPS = 0;
        this.RPS = rps;
        this.renderEngine = null;
        this.physicsEngine = new PhysicsEngine(this);
        this.OBJECTS = new ArrayList<>();
        this.EFFECTS = new ArrayList<>();
        this.MENU = new ArrayList<>();
        this.sounds = null;
        this.MODE = Mode.PhysicsIN;
    }

    public void init() throws Exception {
        //init sub engines
        if (this.frame == null) {
            this.physicsEngine.init();
            return;
        }
        this.frame.add(this.renderEngine);
        if (!this.frame.isVisible()) {
            System.out.println("JFrame must be visible");
            System.exit(0);
        }
        this.renderEngine.init();
        this.physicsEngine.init();
        //init listeners
        this.renderEngine.addKeyListener(this);
        this.renderEngine.addMouseListener(this);
        this.renderEngine.addMouseMotionListener(this);
        this.renderEngine.addMouseWheelListener(this);
        //sounds
        if (this.sounds != null) {
            this.sounds.put("shot1", new SoundPlayer(this.getClass().getResource("/tank/src/sounds/shot1.mp3"), 5, false, Engine.FX_volume));
            this.sounds.put("shot2", new SoundPlayer(this.getClass().getResource("/tank/src/sounds/shot2.mp3"), 5, false, Engine.FX_volume));
            this.sounds.put("shot3", new SoundPlayer(this.getClass().getResource("/tank/src/sounds/shot3.mp3"), 5, false, Engine.FX_volume));
            this.sounds.put("explode", new SoundPlayer(this.getClass().getResource("/tank/src/sounds/explode.mp3"), 5, false, Engine.FX_volume));
            this.sounds.put("fence", new SoundPlayer(this.getClass().getResource("/tank/src/sounds/fence.mp3"), 4, false, Engine.FX_volume));
            this.sounds.put("tree", new SoundPlayer(this.getClass().getResource("/tank/src/sounds/tree.mp3"), 4, false, Engine.FX_volume));
            //this.sounds.put("engine", new SoundPlayer(this.getClass().getResource("/tank/src/sounds/engine.mp3"), 5, true, Engine.FX_volume));
            //this.sounds.put("music1", new SoundPlayer(this.getClass().getResource("/SpaceCraft/src/music1.mp3"), 1, true, this.MUSIC_volume));
        }
    }

    public void start() throws Exception {
        if (this.renderEngine != null) {
            this.renderEngine.Thread.start();
        }
        this.physicsEngine.Thread.start();
    }

    public void stop() {
        if (this.renderEngine != null) {
            this.renderEngine.Thread.stop();
        }
        this.physicsEngine.Thread.stop();
    }

    public JFrame getScreen() {
        return this.frame;
    }

    public List<EngineObject> getEngineObjects() {
        return this.OBJECTS;
    }

    public void setGround(Ground[][] ground, Dimension size) {
        this.GROUND = ground;
        this.MAPSIZE.width = size.width;
        this.MAPSIZE.height = size.height;
    }

    public Ground[][] getGround() {
        return this.GROUND;
    }

    public Dimension getMapSize() {
        return this.MAPSIZE;
    }

    public List<EngineEffect> getEngineEffects() {
        return this.EFFECTS;
    }

    public List<EngineMenuItem> getMenuItems() {
        return this.MENU;
    }

    public RenderEngine getRenderEngine() {
        return this.renderEngine;
    }

    public PhysicsEngine getPhysicsEngine() {
        return this.physicsEngine;
    }

    public void playSound(String name) {
        SoundPlayer sp = this.sounds.get(name);
        if (sp != null) {
            sp.playSurroundSound(0, 50);
        }
    }

    public void playSurroundSound(String name, Point position) {
        Point p = new Point(this.frame.getWidth() / 2 - this.renderEngine.xOFF, this.frame.getHeight() / 2 - this.renderEngine.yOFF);
        //calc position of sound (f == 0 -> in center, f==-1 -> left, f==1 -> right)
        double y = -position.y + p.y;
        double a;
        if (y != 0f) {
            a = (position.x - p.x) / y;
        } else {
            a = 1f;
        }
        float f = (float) Math.atan(a);
        f = (float) (f * 2 / Math.PI * (p.x <= position.x ? -1f : 1)) + (p.x <= position.x ? 0.5f : -0.5f);
        f = f < -1f ? -1f : f;
        f = f > 1f ? 1f : f;
        //calc volume
        float volume = (float) Math.sqrt(
                Math.pow(position.x - (this.frame.getWidth() / 2 - this.renderEngine.xOFF), 2)
                + Math.pow(position.y - (this.frame.getHeight() / 2 - this.renderEngine.yOFF), 2)
        );
        volume = (float) (volume != 0f ? Math.log10(10f - volume / (this.frame.getWidth() / 15f)) : 1f);
        volume = volume > 1f ? 1f : volume;
        SoundPlayer sp = this.sounds.get(name);
        if (sp != null) {
            sp.playSurroundSound(f, volume);
        }
    }

    public void stopSound(String name) {
        SoundPlayer sp = this.sounds.get(name);
        if (sp != null) {
            sp.stop();
        }
    }

    public void closeSound(String name) {
        SoundPlayer sp = this.sounds.get(name);
        if (sp != null) {
            sp.close();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //################NONE######################
    }

    private final List<Character> keyHolder = new ArrayList<>();

    @Override
    public void keyPressed(KeyEvent e) {
        for (Character c : this.keyHolder) {
            if (c == e.getKeyChar()) {
                return;
            }
        }
        this.keyHolder.add(e.getKeyChar());
        //objects
        for (int i = 0; i < this.OBJECTS.size(); i++) {
            EngineObject obj = this.OBJECTS.get(i);
            obj.keyAction(EngineObjectAction.KeyPressed, e);
        }
        //menu
        for (int i = 0; i < this.MENU.size(); i++) {
            EngineMenuItem obj = this.MENU.get(i);
            obj.keyAction(EngineObjectAction.KeyPressed, e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        for (Character c : this.keyHolder) {
            if (c == e.getKeyChar()) {
                this.keyHolder.remove(c);
                break;
            }
        }
        //objects
        for (int i = 0; i < this.OBJECTS.size(); i++) {
            EngineObject obj = this.OBJECTS.get(i);
            obj.keyAction(EngineObjectAction.KeyReleased, e);
        }
        //menu
        for (int i = 0; i < this.MENU.size(); i++) {
            EngineMenuItem obj = this.MENU.get(i);
            obj.keyAction(EngineObjectAction.KeyReleased, e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //################NONE######################
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //menu
        for (int i = 0; i < this.MENU.size(); i++) {
            EngineMenuItem obj = this.MENU.get(i);
            obj.mouseAction(EngineObjectAction.MousePressed, e, this.renderEngine.xOFF, this.renderEngine.yOFF);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //menu
        for (int i = 0; i < this.MENU.size(); i++) {
            EngineMenuItem obj = this.MENU.get(i);
            obj.mouseAction(EngineObjectAction.MouseReleased, e, this.renderEngine.xOFF, this.renderEngine.yOFF);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //################NONE######################
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //################NONE######################
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        //################NONE######################
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //################NONE######################
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        //################NONE######################
    }

}
