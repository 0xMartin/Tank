/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects.menu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tank.engine.Engine;
import tank.engine.EngineMenuItem;
import tank.engine.EngineObjectAction;

/**
 *
 * @author Krcma
 */
public class TextField implements EngineMenuItem {

    //width and height of button
    private int width, height;
    private String text;
    private int x, y;
    private Color foreground, background;
    private Font font;
    private final boolean staticPosition, center;
    private boolean selected;

    public TextField(String _text, int _x, int _y, int _width, int _height, Color _foreground, Color _background, Font _f, boolean _staticPosition, boolean _center) {
        this.text = _text;
        this.x = _x;
        this.y = _y;
        this.font = _f;
        this.foreground = _foreground;
        this.background = _background;
        this.staticPosition = _staticPosition;
        this.center = _center;
        this.width = _width;
        this.height = _height;
        this.selected = false;
    }

    public void setText(String _text) {
        this.text = _text;
    }

    public String getText() {
        return this.text;
    }

    public void setX(int _x) {
        this.x = _x;
    }

    public void setY(int _y) {
        this.y = _y;
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

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        g2.setFont(this.font);
        g2.setColor(this.background);
        if (this.staticPosition) {
            g2.fillRect(
                    this.x - (this.center ? this.width / 2 : 0),
                    this.y - this.height + (this.center ? this.height / 2 : 0),
                    this.width,
                    this.height
            );
            g2.setStroke(new BasicStroke(5));
            g2.drawRect(
                    this.x - (this.center ? this.width / 2 : 0),
                    this.y - this.height + (this.center ? this.height / 2 : 0),
                    this.width,
                    this.height
            );
            g2.setColor(this.foreground);
            g2.drawString(
                    this.text,
                    this.x + 10 - (this.center ? this.width / 2 : 0),
                    this.y - (int) (this.height / 7f) * 2 + (this.center ? this.height / 2 : 0)
            );
        } else {
            g2.fillRect(this.x + xOFF - (this.center ? this.width / 2 : 0),
                    this.y + yOFF - this.height + (this.center ? this.height / 2 : 0),
                    this.width,
                    this.height
            );
            g2.setStroke(new BasicStroke(5));
            g2.drawRect(
                    this.x - (this.center ? this.width / 2 : 0),
                    this.y - this.height + (this.center ? this.height / 2 : 0),
                    this.width,
                    this.height
            );
            g2.setColor(this.foreground);
            g2.drawString(this.text,
                    this.x + xOFF + 10 - (this.center ? this.width / 2 : 0),
                    this.y + yOFF - (int) (this.height / 7f) * 2 + (this.center ? this.height / 2 : 0)
            );
        }
        if (this.selected) {
            this.c++;
            if (this.c > 30) {
                this.c = 0;
            }
            if (this.c > 15) {
                if (this.staticPosition) {
                    g2.setStroke(new BasicStroke(4));
                    g2.setColor(this.background.brighter().brighter());
                    g2.drawLine(this.x + 10 - (this.center ? this.width / 2 : 0) + g2.getFontMetrics().stringWidth(this.text),
                            y - this.font.getSize() - (int) (this.height / 7f) + (this.center ? this.height / 2 : 0),
                            this.x + 10 - (this.center ? this.width / 2 : 0) + g2.getFontMetrics().stringWidth(this.text),
                            y - (int) (this.height / 7f) + (this.center ? this.height / 2 : 0)
                    );
                } else {
                    g2.drawLine(xOFF + this.x + 10 - (this.center ? this.width / 2 : 0) + g2.getFontMetrics().stringWidth(this.text),
                            yOFF + y - this.font.getSize() - (int) (this.height / 7f) + (this.center ? this.height / 2 : 0),
                            xOFF + this.x + 10 - (this.center ? this.width / 2 : 0) + g2.getFontMetrics().stringWidth(this.text),
                            yOFF + y - (int) (this.height / 7f) + (this.center ? this.height / 2 : 0)
                    );
                }
            }
        }
    }
    int c = 0;

    @Override
    public void refresh() {

    }

    @Override
    public void mouseAction(EngineObjectAction ea, MouseEvent e, int xOFF, int yOFF) {
        if (EngineObjectAction.MousePressed == ea) {
            //centers
            int cW = this.center ? this.width / 2 : 0;
            int cH = this.center ? this.height / 2 : 0;
            //test click
            this.selected = false;
            if (this.staticPosition) {
                if (e.getX() >= this.x - cW && e.getY() >= this.y + cH - this.height) {
                    if (e.getX() <= this.x - cW + this.width && e.getY() <= this.y + cH) {
                        this.selected = true;
                    }
                }
            } else {
                if (e.getX() + xOFF >= this.x - cW && e.getY() + yOFF >= this.y + cH - this.height) {
                    if (e.getX() + xOFF <= this.x - cW + this.width && e.getY() + yOFF <= this.y + cH) {
                        this.selected = true;
                    }
                }
            }
        }
    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {
        if (this.selected) {
            if (EngineObjectAction.KeyPressed == ea) {
                char c = e.getKeyChar();
                if (e.isShiftDown()) {
                    c = Character.toUpperCase(c);
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_BACK_SPACE:
                        if (this.text.length() > 0) {
                            this.text = this.text.substring(0, this.text.length() - 1);
                        }
                        break;
                    case KeyEvent.VK_DELETE:
                        if (this.text.length() > 0) {
                            this.text = this.text.substring(0, this.text.length() - 1);
                        }
                        break;
                    default:
                        if (c > 64 && c < 91 || c > 96 && c < 123 || c > 47 && c < 58 || c == '.') {
                            this.text += c;
                        }
                        break;
                }
            }
        }
    }

}
