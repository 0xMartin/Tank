/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import tank.Game;
import tank.engine.Engine;
import tank.engine.EngineObject;
import tank.engine.EngineObjectAction;

/**
 *
 * @author Krcma
 */
public class BulletPath implements EngineObject {

    private float x, y, xl, yl;
    private final float angle;
    private final Bullet owner;
    private boolean end = false, delete = false;
    private int time, ID;

    public BulletPath(Bullet b) {
        this.owner = b;
        this.angle = b.getAngle();
        this.x = b.getXY().x;
        this.y = b.getXY().y;
        this.time = 0;
    }

    public void delete() {
        this.delete = true;
    }

    @Override
    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getZIndex() {
        return 1;
    }

    @Override
    public void refresh() {
        //changing start position of effect
        if (Game.ENGINE.MODE == Engine.Mode.PhysicsIN) {
            if (this.time > Game.ENGINE.getRPS() / 2) {
                this.end = true;
                this.x -= Math.cos(Math.toRadians(this.angle + 90)) * this.owner.getVelocity();
                this.y -= Math.sin(Math.toRadians(this.angle + 90)) * this.owner.getVelocity();
            } else {
                this.time++;
            }
        }
        if (!this.delete) {
            this.xl = this.owner.getXY().x;
            this.yl = this.owner.getXY().y;
        }
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Point2D f1 = new Point2D.Float(this.x + xOFF, this.y + yOFF);
        Point2D f2 = new Point2D.Float(
                (int) (xOFF + (this.delete ? this.xl : this.owner.getXY().x)),
                (int) (yOFF + (this.delete ? this.yl : this.owner.getXY().y))
        );
        if ((int) (f1.getX()) - (int) (f2.getX()) != 0 && (int) (f1.getY()) - (int) (f2.getY()) != 0) {
            LinearGradientPaint lgp = new LinearGradientPaint(
                    f1,
                    f2,
                    new float[]{0.0f, 1.0f},
                    new Color[]{
                        new Color(190, 150, 150, 50),
                        new Color(190, 150, 150, 220)
                    }
            );
            g2.setPaint(lgp);
            g2.drawLine((int) (this.x + xOFF),
                    (int) (this.y + yOFF),
                    (int) (xOFF + (this.delete ? this.xl : this.owner.getXY().x)),
                    (int) (yOFF + (this.delete ? this.yl : this.owner.getXY().y))
            );
        }
    }

    @Override
    public boolean isDeath() {
        return Math.sqrt(
                Math.pow(this.x - (this.delete ? this.xl : this.owner.getXY().x), 2)
                + Math.pow(this.y - (this.delete ? this.yl : this.owner.getXY().y), 2)) < this.owner.getVelocity() && this.end;
    }

    @Override
    public void setID(int id) {
        this.ID = id;
    }

    @Override
    public int getID() {
        return this.ID;
    }

    @Override
    public Point getXY() {
        return new Point((int) this.x, (int) this.y);
    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {
    }

    @Override
    public Polygon getModel() {
        return null;
    }

    /**
     * ID,x,y,owner.ID, delete
     *
     * @return
     */
    @Override
    public Object[] getConfig() {
        return new Object[]{
            Engine.ObjClass.BULLETPATHCLASS,
            this.ID,
            this.x,
            this.y,
            this.owner.getID(),
            this.delete
        };
    }

    @Override
    public boolean setConfig(Object[] config) {
        if (config != null && config.length != 0) {
            if (this.ID == (int) config[1]) {
                this.x = (float) config[2];
                this.y = (float) config[3];
                this.delete = (boolean) config[5];
                return true;
            }
        }
        return false;
    }

    @Override
    public int getWidth() {
        return 0;
    }

}
