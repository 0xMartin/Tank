/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.util.List;
import tank.Game;
import tank.engine.Engine;
import tank.engine.EngineObjectAction;
import tank.engine.EngineObject;

/**
 *
 * @author Krcma
 */
public class Bullet implements EngineObject {

    private int ID = 0;
    private float x, y;
    private float angle;
    private final float velocity;
    private final Tank sender;
    private final int damage, size;
    private int time;
    private BulletPath bp;

    public Bullet(float x, float y, Tank sender, float velocity, int damage, float angle, int size) {
        this.x = x;
        this.y = y;
        this.velocity = velocity;
        this.sender = sender;
        this.angle = angle;
        this.damage = damage;
        this.size = size;
        this.time = Game.ENGINE.getRPS() * 4;
        this.bp = new BulletPath(this);
        Game.ENGINE.getEngineObjects().add(this.bp);
    }

    public int getDamege() {
        return this.damage;
    }

    public float getVelocity() {
        return this.velocity;
    }

    public void changeDirection(float angle, List<EngineObject> objects) {
        this.bp.delete();
        this.angle = angle;
        this.bp = new BulletPath(this);
        objects.add(this.bp);
    }

    public float getAngle() {
        return this.angle;
    }

    public Tank getSender() {
        return this.sender;
    }

    @Override
    public int getZIndex() {
        return 1;
    }

    @Override
    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Point getXY() {
        return new Point((int) this.x, (int) this.y);
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        g2.setColor(Color.BLACK);
        g2.fillOval((int) (xOFF + this.x) - this.size / 2,
                (int) (yOFF + this.y) - this.size / 2,
                this.size,
                this.size
        );
    }

    @Override
    public void refresh() {
        this.x -= this.velocity * Math.cos(Math.toRadians(this.angle + 90d));
        this.y -= this.velocity * Math.sin(Math.toRadians(this.angle + 90d));
        this.time--;
    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {

    }

    @Override
    public Polygon getModel() {
        Polygon model = new Polygon();
        model.xpoints = new int[]{
            (int) (this.x - 4),
            (int) (this.x + 4),
            (int) (this.x + 4),
            (int) (this.x - 4)
        };
        model.ypoints = new int[]{
            (int) (this.y - 4),
            (int) (this.y - 4),
            (int) (this.y + 4),
            (int) (this.y + 4)
        };
        model.npoints = 4;
        return model;
    }

    @Override
    public boolean isDeath() {
        return this.time < 0;
    }

    public int getSize() {
        return this.size;
    }

    @Override
    public int getID() {
        return this.ID;
    }

    @Override
    public void setID(int id) {
        this.ID = id;
    }

    /**
     * ID;x;y;velocity;damage;angle;size
     *
     * @return Object[] config
     */
    @Override
    public Object[] getConfig() {
        return new Object[]{
            Engine.ObjClass.BULLETCLASS,
            this.ID,
            this.x,
            this.y,
            this.velocity,
            this.damage,
            this.angle,
            this.size
        };
    }

    @Override
    public boolean setConfig(Object[] config) {
        if (config != null && config.length != 0) {
            if (this.ID == (int) config[1]) {
                this.x = (float) config[2];
                this.y = (float) config[3];
                this.angle = (float) config[6];
                return true;
            }
        }
        return false;
    }

    @Override
    public int getWidth() {
        return this.size * 3;
    }

}
