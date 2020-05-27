/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tank.engine.Engine;
import tank.engine.EngineMenuItem;
import tank.engine.EngineObjectAction;

/**
 *
 * @author Krcma
 */
public class Panel implements EngineMenuItem {

    private int x, y, width, height;
    private Color color;
    private boolean staticPosition, center;

    public Panel(int _x, int _y, int _width, int _height, Color _color, boolean _staticPosition, boolean _center) {
        this.x = _x;
        this.y = _y;
        this.width = _width;
        this.height = _height;
        this.color = _color;
        this.staticPosition = _staticPosition;
        this.center = _center;
    }

    public void setX(int _x) {
        this.x = _x;
    }

    public void setY(int _y) {
        this.y = _y;
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public void setHeight(int h) {
        this.height = h;
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        g2.setColor(this.color);
        if (this.staticPosition) {
            g2.fillRect(this.x - (this.center ? this.width / 2 : 0), this.y - (this.center ? this.height / 2 : 0), this.width, this.height);
        } else {
            g2.fillRect(this.x + xOFF - (this.center ? this.width / 2 : 0), this.y + yOFF - (this.center ? this.height / 2 : 0), this.width, this.height);
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
