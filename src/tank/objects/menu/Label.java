/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tank.engine.Engine;
import tank.engine.EngineMenuItem;
import tank.engine.EngineObjectAction;
import tank.engine.EngineObject;

/**
 *
 * @author Krcma
 */
public class Label implements EngineMenuItem {

    public String text;
    public int x, y;
    public Color color;
    public Font font;
    public boolean staticPosition, center;

    public EngineObject owner;

    public Label(String _text, int _x, int _y, Color _color, Font _f, boolean _staticPosition, boolean _center) {
        this.text = _text;
        this.x = _x;
        this.y = _y;
        this.font = _f;
        this.color = _color;
        this.staticPosition = _staticPosition;
        this.center = _center;
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        g2.setFont(font);
        g2.setColor(this.color);
        if (this.staticPosition) {
            g2.drawString(this.text, this.x - (this.center ? g2.getFontMetrics().stringWidth(this.text) / 2 : 0), this.y);
        } else {
            g2.drawString(this.text, this.x + xOFF - (this.center ? g2.getFontMetrics().stringWidth(this.text) / 2 : 0), this.y + yOFF);
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

    public void setX(int _x) {
        this.x = _x;
    }

    public void setY(int _y) {
        this.y = _y;
    }

    public void setText(String _text) {
        this.text = _text;
    }

    public void setFont(Font _font) {
        this.font = _font;
    }

    public void setColor(Color _color) {
        this.color = _color;
    }

}
