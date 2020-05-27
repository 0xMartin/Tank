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
import tank.Game;
import tank.engine.Engine;
import tank.engine.EngineObject;
import tank.engine.EngineObjectAction;
import tank.engine.Tools;

/**
 *
 * @author Krcma
 */
public class Building implements EngineObject {

    private int x, y;
    private final int building_index;
    private final BufferedImage img;

    public Building(int _x, int _y, int _building_index) {
        this.x = _x;
        this.y = _y;
        this.building_index = _building_index;
        this.img = Tools.resizePixelImage(Game.IMAGES.buildings[this.building_index], 3);
    }

    @Override
    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getZIndex() {
        return 0;
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
        int[] xp = null, yp = null;
        int n = 0;
        switch (this.building_index) {
            case 0:
                xp = new int[]{(int) (0.14 * this.img.getWidth()), (int) (0.14 * this.img.getWidth()), (int) (0.34 * this.img.getWidth()), (int) (0.43 * this.img.getWidth()), (int) (0.58 * this.img.getWidth()), (int) (0.95 * this.img.getWidth()), (int) (0.93 * this.img.getWidth()), (int) (0.74 * this.img.getWidth()), (int) (0.27 * this.img.getWidth())};
                yp = new int[]{(int) (0.67 * this.img.getHeight()), (int) (0.78 * this.img.getHeight()), (int) (0.89 * this.img.getHeight()), (int) (0.87 * this.img.getHeight()), (int) (0.98 * this.img.getHeight()), (int) (0.75 * this.img.getHeight()), (int) (0.66 * this.img.getHeight()), (int) (0.60 * this.img.getHeight()), (int) (0.62 * this.img.getHeight())};
                n = 9;
                break;
        }
        if (xp == null || yp == null || n < 3) {
            return null;
        }
        for (int i = 0; i < n; i++) {
            xp[i] += this.x;
            yp[i] += this.y;
        }
        return new Polygon(xp, yp, n);
    }

    @Override
    public boolean isDeath() {
        return false;
    }

    @Override
    public Object[] getConfig() {
        return new Object[]{
            Engine.ObjClass.BUILDINGCLASS,
            this.x,
            this.y,
            this.building_index
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
