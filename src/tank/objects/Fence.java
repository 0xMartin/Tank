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
import java.awt.image.BufferedImage;
import java.io.Serializable;
import tank.Game;
import tank.engine.Engine;
import tank.engine.EngineObject;
import tank.engine.EngineObjectAction;

/**
 *
 * @author Krcma
 */
public class Fence implements EngineObject {

    public enum Orientation implements Serializable{
        VERTICAL(0), HORISONTAL(1), UP_LEFT(2), UP_RIGHT(3), DOWN_LEFT(4), DOWN_RIFHT(5);
        public final int id;

        Orientation(int _id) {
            this.id = _id;
        }
    }

    private int x, y;
    private final Orientation orient;
    private final BufferedImage img;

    public Fence(int _x, int _y, Orientation _orient) {
        this.x = _x;
        this.y = _y;
        this.orient = _orient;
        this.img = Game.IMAGES.fence[_orient.id];
    }

    @Override
    public int getZIndex() {
        return 3;
    }

    @Override
    public void setID(int id) {
    }

    @Override
    public int getID() {
        return Game.ID_NONE;
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
        g2.drawImage(this.img, this.x + xOFF, this.y + yOFF, null);
    }

    @Override
    public void refresh() {
    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {
    }

    @Override
    public Polygon getModel() {
        int[] xp = new int[]{
            (int)(this.x+this.img.getWidth()*0.2f),
            (int)(this.x+this.img.getWidth()*0.8f),
            (int)(this.x+this.img.getWidth()*0.8f),
            (int)(this.x+this.img.getWidth()*0.2f)
        };
        int[] yp = new int[]{
            (int)(this.y+this.img.getHeight()*0.8f),
            (int)(this.y+this.img.getHeight()*0.8f),
            (int)(this.y+this.img.getHeight()*0.2f),
            (int)(this.y+this.img.getHeight()*0.2f)
        };
        return new Polygon(xp, yp, 4);
    }

    @Override
    public boolean isDeath() {
        return false;
    }

    @Override
    public Object[] getConfig() {
        return new Object[]{
            Engine.ObjClass.FENCECLASS,
            this.x,
            this.y,
            this.orient
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

}
