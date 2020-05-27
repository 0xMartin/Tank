/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;
import tank.Game;
import tank.engine.Engine;
import tank.engine.EngineObject;
import tank.engine.EngineObjectAction;

/**
 *
 * @author Krcma
 */
public class Tree implements EngineObject {

    private int x, y;
    private float angle = 0f;
    private final int wCenterM, hCenterM;
    private final BufferedImage img;

    public Tree(int _x, int _y) {
        this.x = _x;
        this.y = _y;
        this.img = Game.IMAGES.trees[(new Random()).nextInt(Game.IMAGES.trees.length)];
        this.wCenterM = this.img.getWidth() / 2 - 12;
        this.hCenterM = (int) (this.img.getHeight() * 0.85f - 12);
    }

    @Override
    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private int zIndex = 0;

    @Override
    public int getZIndex() {
        return zIndex;
    }

    @Override
    public void setID(int id) {
    }

    @Override
    public int getID() {
        return Game.ID_NONE;
    }

    @Override
    public Point getXY() {
        return new Point(this.x, this.y);
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        //is on the screen ?
        if (this.x + xOFF + this.img.getWidth() < 0) {
            return;
        }
        if (this.y + yOFF + this.img.getHeight() < 0) {
            return;
        }
        if (this.x + xOFF - this.img.getWidth() > Game.SIZE.width) {
            return;
        }
        if (this.y + yOFF - this.img.getHeight() > Game.SIZE.width) {
            return;
        }
        if (this.angle == 0f) {
            g2.drawImage(this.img, this.x + xOFF, this.y + yOFF, null);
        } else {
            AffineTransform old = g2.getTransform();
            //hull
            g2.rotate(Math.toRadians(this.angle), this.x + xOFF + this.img.getWidth() / 2, this.y + yOFF + this.img.getHeight() * 0.85f);
            g2.drawImage(this.img, this.x + xOFF, this.y + yOFF, null);
            g2.setTransform(old);
        }
    }

    @Override
    public void refresh() {
        if (this.kill) {
            if (Math.abs(this.angle) >= 90 || Math.abs(this.angle) > 78 && Math.random() > 0.7f) {
                this.kill = false;
            }
            this.angle += fallSpeed;
        }
    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {
    }

    @Override
    public Polygon getModel() {
        if (Math.abs(this.angle) > 20) {
            return null;
        }
        return new Polygon(
                new int[]{this.x + this.wCenterM, this.x + 24 + this.wCenterM, this.x + 24 + this.wCenterM, this.x + this.wCenterM},
                new int[]{this.y + this.hCenterM, this.y + this.hCenterM, this.y + 24 + this.hCenterM, this.y + 24 + this.hCenterM},
                4
        );
    }

    @Override
    public boolean isDeath() {
        return false;
    }

    @Override
    public Object[] getConfig() {
        return new Object[]{
            Engine.ObjClass.TREECLASS,
            this.x,
            this.y
        };
    }

    @Override
    public boolean setConfig(Object[] config) {
        return false;
    }

    @Override
    public int getWidth() {
        return this.img.getWidth();
    }

    private boolean kill = false;
    private float fallSpeed;

    public void killTree() {
        if (!this.kill) {
            Game.ENGINE.playSurroundSound("tree", new Point(this.getXY().x + this.img.getWidth() / 2, this.getXY().y + this.img.getHeight() / 2));
            this.zIndex = 3;
            this.fallSpeed = (float) (Math.random() * 1f + 1.5f) * (Math.random() > 0.5 ? 1 : -1);
            this.kill = true;
        }
    }

    public int getHeight() {
        return this.img.getHeight();
    }

}
