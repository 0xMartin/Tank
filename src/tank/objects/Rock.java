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
public class Rock implements EngineObject {

    private int x, y;
    private final int rock_index, scale;
    private final BufferedImage img;

    public Rock(int _x, int _y, int _rock_index, int _scale) {
        this.x = _x;
        this.y = _y;
        this.scale = _scale;
        this.rock_index = _rock_index;
        this.img = Tools.resizePixelImage(Game.IMAGES.rocks[_rock_index], _scale);
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
        switch (this.rock_index) {
            case 0:
                xp = new int[]{(int) (0.03 * this.img.getWidth()), (int) (0.13 * this.img.getWidth()), (int) (0.28 * this.img.getWidth()), (int) (0.48 * this.img.getWidth()), (int) (0.69 * this.img.getWidth()), (int) (0.85 * this.img.getWidth()), (int) (0.95 * this.img.getWidth()), (int) (0.92 * this.img.getWidth()), (int) (0.17 * this.img.getWidth()), (int) (0.05 * this.img.getWidth())};
                yp = new int[]{(int) (0.65 * this.img.getHeight()), (int) (0.72 * this.img.getHeight()), (int) (0.97 * this.img.getHeight()), (int) (0.99 * this.img.getHeight()), (int) (0.89 * this.img.getHeight()), (int) (0.93 * this.img.getHeight()), (int) (0.85 * this.img.getHeight()), (int) (0.70 * this.img.getHeight()), (int) (0.50 * this.img.getHeight()), (int) (0.57 * this.img.getHeight())};
                n = 10;
                break;
            case 1:
                xp = new int[]{(int) (0.03 * this.img.getWidth()), (int) (0.31 * this.img.getWidth()), (int) (0.44 * this.img.getWidth()), (int) (0.71 * this.img.getWidth()), (int) (0.93 * this.img.getWidth()), (int) (0.95 * this.img.getWidth()), (int) (0.86 * this.img.getWidth()), (int) (0.29 * this.img.getWidth()), (int) (0.06 * this.img.getWidth())};
                yp = new int[]{(int) (0.68 * this.img.getHeight()), (int) (0.85 * this.img.getHeight()), (int) (0.96 * this.img.getHeight()), (int) (0.97 * this.img.getHeight()), (int) (0.83 * this.img.getHeight()), (int) (0.73 * this.img.getHeight()), (int) (0.62 * this.img.getHeight()), (int) (0.52 * this.img.getHeight()), (int) (0.56 * this.img.getHeight())};
                n = 9;
                break;
            case 2:
                xp = new int[]{(int) (0.07 * this.img.getWidth()), (int) (0.26 * this.img.getWidth()), (int) (0.45 * this.img.getWidth()), (int) (0.59 * this.img.getWidth()), (int) (0.70 * this.img.getWidth()), (int) (0.71 * this.img.getWidth()), (int) (0.86 * this.img.getWidth()), (int) (0.94 * this.img.getWidth()), (int) (0.92 * this.img.getWidth()), (int) (0.80 * this.img.getWidth()), (int) (0.31 * this.img.getWidth()), (int) (0.10 * this.img.getWidth())};
                yp = new int[]{(int) (0.76 * this.img.getHeight()), (int) (0.92 * this.img.getHeight()), (int) (0.93 * this.img.getHeight()), (int) (0.98 * this.img.getHeight()), (int) (0.96 * this.img.getHeight()), (int) (0.87 * this.img.getHeight()), (int) (0.88 * this.img.getHeight()), (int) (0.86 * this.img.getHeight()), (int) (0.77 * this.img.getHeight()), (int) (0.66 * this.img.getHeight()), (int) (0.64 * this.img.getHeight()), (int) (0.70 * this.img.getHeight())};
                n = 12;
                break;
            case 3:
                xp = new int[]{(int) (0.05 * this.img.getWidth()), (int) (0.20 * this.img.getWidth()), (int) (0.51 * this.img.getWidth()), (int) (0.63 * this.img.getWidth()), (int) (0.86 * this.img.getWidth()), (int) (0.89 * this.img.getWidth()), (int) (0.78 * this.img.getWidth()), (int) (0.12 * this.img.getWidth())};
                yp = new int[]{(int) (0.68 * this.img.getHeight()), (int) (0.91 * this.img.getHeight()), (int) (0.95 * this.img.getHeight()), (int) (0.99 * this.img.getHeight()), (int) (0.95 * this.img.getHeight()), (int) (0.80 * this.img.getHeight()), (int) (0.66 * this.img.getHeight()), (int) (0.57 * this.img.getHeight())};
                n = 8;
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
            Engine.ObjClass.ROCKCLASS,
            this.x,
            this.y,
            this.rock_index,
            this.scale
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
