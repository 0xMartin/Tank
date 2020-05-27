/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import tank.Game;
import tank.engine.EngineMenuItem;
import tank.engine.EngineObjectAction;
import tank.engine.EngineObject;

/**
 *
 * @author Krcma
 */
public class DropText implements EngineMenuItem {

    private String text;
    private float x, y;
    private Color color;
    private Font font;
    private float opacity;
    private EngineObject owner;

    public DropText(String text, int x, int y, Color color, Font f) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.font = f;
        this.opacity = 1.0f;
        this.color = color;
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        //is on the screen ?
        if (this.x + xOFF + 100 < 0) {
            return;
        }
        if (this.y + yOFF + 100 < 0) {
            return;
        }
        if (this.x + xOFF - 100 > Game.SIZE.width) {
            return;
        }
        if (this.y + yOFF - 100 > Game.SIZE.width) {
            return;
        }
        if (this.opacity >= 0.0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.opacity));
            g2.setFont(font);
            g2.setColor(this.color);
            g2.drawString(this.text, this.x + xOFF, this.y + yOFF);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    @Override
    public void refresh() {
        this.y += 0.3f;
        this.opacity -= 0.005f;
    }

    @Override
    public void mouseAction(EngineObjectAction ea, MouseEvent e, int xOFF, int yOFF) {

    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {

    }
    /*
    @Override
    public boolean isDeath() {
        return this.opacity < 0f;
    }
     */

}
