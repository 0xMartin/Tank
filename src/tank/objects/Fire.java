/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import tank.Game;
import tank.engine.Engine;
import tank.engine.EngineObjectAction;
import tank.engine.EngineObject;

/**
 *
 * @author Krcma
 */
public class Fire implements EngineObject {

    private int x, y;
    private final boolean up;
    private final BufferedImage[] img;

    private int c = 0, c1 = 0;

    public Fire(int x, int y, boolean _up) {
        this.x = x;
        this.y = y;
        this.up = _up;
        this.img = Game.IMAGES.fire;
    }

    @Override
    public int getZIndex() {
        return this.up ? 1 : 3;
    }

    @Override
    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Point getXY() {
        return new Point(this.x, this.y);
    }
    
    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        //is on the screen ?
        if (this.x + xOFF + this.img[0].getWidth() < 0) {
            return;
        }
        if (this.y + yOFF + this.img[0].getHeight() < 0) {
            return;
        }
        if (this.x + xOFF - this.img[0].getWidth() > Game.SIZE.width) {
            return;
        }
        if (this.y + yOFF - this.img[0].getHeight() > Game.SIZE.width) {
            return;
        }
        g2.drawImage(this.img[(int) (c / 2)], this.x + xOFF - 8 * (Game.BLOCK_SCALE - 1), this.y + yOFF - 8 * (Game.BLOCK_SCALE - 1), null);
        this.c++;
        if ((int) (this.c / 2) > 31) {
            this.c = 0;
        }
    }

    @Override
    public void refresh() {
        if (this.c1 > 20) {
            Game.ENGINE.getEngineEffects().add(
                    new Particle(
                            this.x + (int) (Math.random() * 16 * (Game.BLOCK_SCALE - 1)),
                            this.y + (int) (Math.random() * 16 * (Game.BLOCK_SCALE - 1)),
                            new Color(255, (int) (Math.random() * 255), 0),
                            (int) (Math.random() * 60 - 30),
                            (float) (Math.random() * 1 + 1),
                            4,
                            Game.ENGINE.getRPS()
                    )
            );
            int rgb = (int) (Math.random() * 50 + 40);
            Game.ENGINE.getEngineEffects().add(
                    new Particle(
                            this.x + (int) (Math.random() * 16 * (Game.BLOCK_SCALE - 1)),
                            this.y + (int) (Math.random() * 16 * (Game.BLOCK_SCALE - 1)),
                            new Color(rgb, rgb, rgb),
                            (int) (Math.random() * 60 - 30),
                            (float) (Math.random() * 1 + 1),
                            4,
                            Game.ENGINE.getRPS() * 3 + (int) (Math.random() * Game.ENGINE.getRPS())
                    )
            );
            this.c1 = 0;
        } else {
            this.c1++;
        }
    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {

    }

    @Override
    public Polygon getModel() {
        return new Polygon(
                new int[]{this.x - (Game.BLOCK_SCALE - 1) * 8, this.x + (Game.BLOCK_SCALE - 1) * 8, this.x + (Game.BLOCK_SCALE - 1) * 8, this.x - (Game.BLOCK_SCALE - 1) * 8},
                new int[]{this.y - (Game.BLOCK_SCALE - 1) * 8, this.y - (Game.BLOCK_SCALE - 1) * 8, this.y + (Game.BLOCK_SCALE - 1) * 8, this.y + (Game.BLOCK_SCALE - 1) * 8},
                4
        );
    }

    @Override
    public boolean isDeath() {
        return false;
    }

    @Override
    public void setID(int id) {
    }

    @Override
    public int getID() {
        return Game.ID_NONE;
    }

    @Override
    public Object[] getConfig() {
        return new Object[]{
            Engine.ObjClass.FIRECLASS,
            this.x,
            this.y,
            this.up
        };
    }

    @Override
    public boolean setConfig(Object[] config) {
        return false;
    }

    @Override
    public int getWidth() {
        return this.img[0].getWidth();
    }

}
