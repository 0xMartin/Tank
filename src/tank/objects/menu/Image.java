/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects.menu;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import tank.engine.Engine;
import tank.engine.EngineMenuItem;
import tank.engine.EngineObjectAction;

/**
 *
 * @author Krcma
 */
public class Image implements EngineMenuItem {

    private int x, y;
    private BufferedImage img;
    private boolean staticPosition, center;

    public Image(int _x, int _y, BufferedImage _img, boolean _staticPosition, boolean _center) {
        this.x = _x;
        this.y = _y;
        this.img = _img;
        this.staticPosition = _staticPosition;
        this.center = _center;
    }

    public void setX(int _x) {
        this.x = _x;
    }

    public void setY(int _y) {
        this.y = _y;
    }

    public void setImage(BufferedImage _img) {
        this.img = _img;
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        if (this.staticPosition) {
            g2.drawImage(this.img, this.x + (center ? -this.img.getWidth() / 2 : 0), this.y + (center ? -this.img.getHeight() / 2 : 0), null);
        } else {
            g2.drawImage(this.img, this.x + xOFF + (center ? -this.img.getWidth() / 2 : 0), this.y + yOFF + (center ? -this.img.getHeight() / 2 : 0), null);
        }
    }

    @Override
    public void refresh() {

    }

    @Override
    public void mouseAction(EngineObjectAction ea, MouseEvent e, int xOFF, int yOFF) {

    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {

    }

}
