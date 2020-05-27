/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import tank.engine.EngineEffect;

/**
 *
 * @author Krcma
 */
public class Wave implements EngineEffect {

    private final int X, Y, TOTALTIME;
    private final float SPEED;
    private int TIME, W = 0;

    public Wave(int x, int y, int time, float speed) {
        this.X = x;
        this.Y = y;
        this.TOTALTIME = time;
        this.TIME = time;
        this.SPEED = speed;
    }

    @Override
    public void refresh() {
        this.TIME--;
        this.W += this.SPEED;
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        float ff = (float) this.TIME / (float) this.TOTALTIME;
        g2.setStroke(new BasicStroke(5 + (1 - ff) * 50f));
        g2.setColor(new Color(120,120,120));
        g2.setComposite(AlphaComposite.SrcOver.derive(ff * 1f));
        g2.drawOval(this.X - W / 2 + xOFF, this.Y - W / 2 + yOFF, this.W, this.W);
    }

    @Override
    public boolean isDeath() {
        return this.TIME < 0;
    }

}
