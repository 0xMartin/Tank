/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import tank.engine.EngineEffect;

/**
 *
 * @author Krcma
 */
public class Particle implements EngineEffect {

    private float x, y;
    private final float angle, velocity;
    private final int size, MaxTime;
    private final Color color;
    private int time;

    public Particle(int x, int y, Color c, float angle, float velocity, int size, int time) {
        this.x = x;
        this.y = y;
        this.color = c;
        this.angle = angle;
        this.size = size;
        this.time = time;
        this.MaxTime = time;
        this.velocity = velocity;
    }

    @Override
    public void refresh() {
        this.x -= Math.cos(Math.toRadians(angle + 90)) * this.velocity;
        this.y -= Math.sin(Math.toRadians(angle + 90)) * this.velocity;
        this.time--;
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        g2.setColor(this.color);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * this.time / this.MaxTime));
        g2.fillRect((int) (xOFF + this.x), (int) (yOFF + this.y), this.size, this.size);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    @Override
    public boolean isDeath() {
        return this.time <= 0;
    }

}
