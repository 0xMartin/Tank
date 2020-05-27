/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects.menu;

import tank.engine.EngineMenuItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tank.engine.EngineObjectAction;

/**
 *
 * @author Krcma
 */
public class Button implements EngineMenuItem {

    //width and height of button
    private int btn_w, btn_h;

    private String text;
    private int x, y;
    private Color foreground, background;
    private Font font;
    private boolean staticPosition, center;
    protected ActionListener actionListener;

    public Button(String _text, int _x, int _y, Color _foreground, Color _background, Font _f, boolean _staticPosition, boolean _center) {
        this.text = _text;
        this.x = _x;
        this.y = _y;
        this.font = _f;
        this.foreground = _foreground;
        this.background = _background;
        this.staticPosition = _staticPosition;
        this.center = _center;
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        g2.setFont(this.font);
        g2.setColor(this.background);
        this.btn_w = (int) (g2.getFontMetrics().stringWidth(this.text) * 1.4f);
        this.btn_h = (int) (g2.getFontMetrics().getHeight() * 1.4f);
        if (this.staticPosition) {
            g2.fillRect(
                    this.x - (this.center ? this.btn_w / 2 : 0),
                    this.y - this.btn_h + (this.center ? this.btn_h / 2 : 0),
                    this.btn_w,
                    this.btn_h
            );
            g2.setColor(this.foreground);
            g2.drawString(
                    this.text,
                    this.x + (int) (this.btn_w / 7f) - (this.center ? this.btn_w / 2 : 0),
                    this.y - (int) (this.btn_h / 7f) * 2 + (this.center ? this.btn_h / 2 : 0)
            );
        } else {
            g2.fillRect(this.x + xOFF - (this.center ? this.btn_w / 2 : 0),
                    this.y + yOFF - this.btn_h + (this.center ? this.btn_h / 2 : 0),
                    this.btn_w,
                    this.btn_h
            );
            g2.setColor(this.foreground);
            g2.drawString(this.text,
                    this.x + xOFF + (int) (this.btn_w / 7f) - (this.center ? this.btn_w / 2 : 0),
                    this.y + yOFF - (int) (this.btn_h / 7f) * 2 + (this.center ? this.btn_h / 2 : 0)
            );
        }
    }

    @Override
    public void refresh() {

    }

    @Override
    public void mouseAction(EngineObjectAction ea, MouseEvent e, int xOFF, int yOFF) {
        if (EngineObjectAction.MousePressed == ea) {
            //centers
            int cW = this.center ? this.btn_w / 2 : 0;
            int cH = this.center ? this.btn_h / 2 : 0;
            //test click
            if (this.staticPosition) {
                if (e.getX() >= this.x - cW && e.getY() >= this.y + cH - this.btn_h) {
                    if (e.getX() <= this.x - cW + this.btn_w && e.getY() <= this.y + cH) {
                        this.actionListener.actionPerformed(new ActionEvent(this, 0, ""));
                    }
                }
            } else {
                if (e.getX() + xOFF >= this.x - cW && e.getY() + yOFF >= this.y + cH - this.btn_h) {
                    if (e.getX() + xOFF <= this.x - cW + this.btn_w && e.getY() + yOFF <= this.y + cH) {
                       this.actionListener.actionPerformed(new ActionEvent(this, 0, ""));
                    }
                }
            }
        }
    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {

    }

    public void addActionListener(ActionListener l) {
        this.actionListener = l;
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

    public void setForeground(Color _color) {
        this.foreground = _color;
    }

    public void setBackground(Color _color) {
        this.background = _color;
    }

}
