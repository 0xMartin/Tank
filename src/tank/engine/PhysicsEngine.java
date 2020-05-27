/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.engine;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.logging.Level;
import java.util.logging.Logger;
import tank.Game;
import tank.objects.Bullet;
import tank.objects.DropText;
import tank.objects.Fence;
import tank.objects.Fire;
import tank.objects.Ground;
import tank.objects.Tank;
import tank.objects.Tree;
import tank.objects.Wave;

/**
 *
 * @author Krcma
 */
public class PhysicsEngine {

    protected final Thread Thread;

    private final Engine engine;

    public PhysicsEngine(Engine engine) {
        this.engine = engine;
        //init thread
        this.Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //rps regulation
                double last = System.nanoTime(), ticks = 0, perTick = 1e9 / engine.getRPS();
                while (true) {
                    double now = System.nanoTime();
                    ticks += (now - last) / perTick;
                    last = now;
                    while (ticks >= 1) {
                        ticks--;
                        try {
                            refresh();
                        } catch (Exception ex) {
                            Logger.getLogger(RenderEngine.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
        this.Thread.setName("Physics_Thread");
    }

    public void init() throws Exception {

    }

    private void refresh() {
        //refresh
        if (engine.gameRun) {
            for (int i = 0; i < this.engine.getEngineObjects().size(); i++) {
                EngineObject obj1 = this.engine.getEngineObjects().get(i);
                if (obj1 == null) {
                    this.engine.getEngineObjects().remove(i);
                    i--;
                    continue;
                }
                //collision test
                if (this.engine.MODE == Engine.Mode.PhysicsIN) {
                    for (int j = 0; j < this.engine.getEngineObjects().size(); j++) {
                        EngineObject obj2 = this.engine.getEngineObjects().get(j);
                        //first test (both object mast have model and distance betwen them must be lower the value)
                        if (obj1.getModel() != null && obj2.getModel() != null) {
                            if (distace(obj1.getXY(), obj2.getXY()) < obj1.getWidth() + obj2.getWidth() * 1.5f) {
                                if (collision(obj1, obj2)) {
                                    if (obj1 instanceof Bullet && obj2 instanceof Tank) {
                                        bulletHitTank((Bullet) obj1, (Tank) obj2);
                                    } else if (obj1 instanceof Tank && obj2 instanceof Tank) {
                                        ((Tank) obj1).backToSafePosition();
                                        ((Tank) obj2).backToSafePosition();
                                    } else if (obj1 instanceof Tank && obj2 instanceof Fence) {
                                        //kill fence
                                        ((Tank) obj1).speedDown(0.5f);
                                        this.engine.getEngineObjects().remove(obj2);
                                        Game.ENGINE.playSurroundSound("fence", obj2.getXY());
                                        Tools.particles_fence((Fence) obj2);
                                    } else if (obj1 instanceof Tank && obj2 instanceof Tree) {
                                        //kill fence
                                        ((Tree) obj2).killTree();
                                        ((Tank) obj1).speedDown(0.75f);
                                        Tools.particles_tree((Tree) obj2);
                                    } else if (!(obj2 instanceof Fire)) {
                                        //ob2
                                        if (obj1 instanceof Tank) {
                                            ((Tank) obj1).backToSafePosition();
                                        } else if (obj1 instanceof Bullet) {
                                            Point p = ((Bullet) obj1).getXY();
                                            //remove bullet
                                            this.engine.getEngineObjects().remove(obj1);
                                            //remove fance if is obj2
                                            if (obj2 instanceof Fence) {
                                                this.engine.getEngineObjects().remove(obj2);
                                                Game.ENGINE.playSurroundSound("fence", obj2.getXY());
                                                Tools.particles_fence((Fence) obj2);
                                            }
                                            //small explode
                                            Tools.particles_explode((int) (40 * ((Bullet) obj1).getSize() / 4f), p.x, p.y, 3f, 0.6f, 5);
                                        }
                                    }
                                } else {
                                    if (obj1 instanceof Tank) {
                                        if (!standOnWater((Tank) obj1)) {
                                            ((Tank) obj1).refreshSafePosition();
                                        } else {
                                            ((Tank) obj1).backToSafePosition();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //refresh (must be after collisions because if two tanks are in collision, they must stop)
                obj1.refresh();
                if (obj1.isDeath()) {
                    if (!(obj1 instanceof Tank)) {
                        this.engine.getEngineObjects().remove(obj1);
                    }
                }
            }
            //particles, effects ...
            for (int i = 0; i < this.engine.getEngineEffects().size(); i++) {
                EngineEffect obj = this.engine.getEngineEffects().get(i);
                if (obj == null) {
                    continue;
                }
                obj.refresh();
                if (obj.isDeath()) {
                    this.engine.getEngineEffects().remove(obj);
                }
            }
        }
        //menu items
        for (int i = 0; i < this.engine.getMenuItems().size(); i++) {
            EngineMenuItem mi = this.engine.getMenuItems().get(i);
            mi.refresh();
        }
    }

    public static boolean collision(EngineObject obj1, EngineObject obj2) {
        if (obj1 == obj2) {
            return false;
        }
        Polygon p1 = obj1.getModel();
        Polygon p2 = obj2.getModel();
        Point p;
        for (int i = 0; i < p2.npoints; i++) {
            p = new Point(p2.xpoints[i], p2.ypoints[i]);
            if (p1.contains(p)) {
                return true;
            }
        }
        for (int i = 0; i < p1.npoints; i++) {
            p = new Point(p1.xpoints[i], p1.ypoints[i]);
            if (p2.contains(p)) {
                return true;
            }
        }
        return false;
    }

    private int distace(Point a, Point b) {
        return (int) Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private boolean bulletBounce(Tank t, Bullet bullet) {
        Polygon model = t.getModel();
        Point abest = null, bbest = null;
        float best = Integer.MAX_VALUE;
        for (int i = 0; i < model.npoints; i++) {
            Point a = new Point(model.xpoints[i], model.ypoints[i]);
            Point b = new Point(model.xpoints[i + 1 == model.npoints ? 0 : i + 1], model.ypoints[i + 1 == model.npoints ? 0 : i + 1]);
            int na = b.y - a.y;
            int nb = -b.x + a.x;
            int c = -na * model.xpoints[i] - nb * model.ypoints[i];
            float dist = (float) (Math.abs(na * bullet.getXY().x + nb * bullet.getXY().y + c) / Math.sqrt(na * na + nb * nb));
            if (dist < best) {
                best = dist;
                abest = a;
                bbest = b;
            }
        }
        if (abest != null || bbest != null) {
            //calculate angle between to vectors
            Point mv = new Point(abest.x - bbest.x, abest.y - bbest.y);
            double by = Math.tan(Math.toRadians(bullet.getAngle()));
            float angle = (float) (Math.abs(mv.x + mv.y * by) / (Math.sqrt(mv.x * mv.x + mv.y * mv.y) * Math.sqrt(1 + by * by)));
            angle = (float) Math.toDegrees(Math.acos(angle));
            if (angle >= 52) {
                //calc bounce
                float ha = (float) Math.toDegrees(Math.atan((float) mv.y / (float) mv.x));
                float bnc = bullet.getAngle() - ha;
                if (bnc < -90) {
                    bnc = 2 * angle;
                } else if (bnc < 0) {
                    bnc = -2 * angle;
                } else if (bnc < 90) {
                    bnc = 2 * angle;
                } else {
                    bnc = -2 * angle;
                }
                if (bullet.getAngle() - ha > 0f) {
                    bnc = 180 - bnc;
                } else {
                    bnc = 180 + Math.abs(bnc);
                }
                bullet.changeDirection(
                        bullet.getAngle() + bnc,
                        this.engine.getEngineObjects()
                );
                return true;
            }
        }
        return false;
    }

    private void bulletHitTank(Bullet b, Tank t) {
        //bullet hit the tank
        if (b.getSender() != t) {
            Point p = b.getXY();
            if (!bulletBounce(t, b)) {
                //remove bullet
                this.engine.getEngineObjects().remove(b);
                //small explode
                Tools.particles_explode((int) (40 * b.getSize() / 4f), p.x, p.y, 3f, 0.6f, 5);
                //if is death then break (only kill bullet and make small explode)
                if (t.isDeath()) {
                    return;
                }
                //reduce life (calc damege using random, reduce tank life and show value of damege on drop text)
                int dmg = (int) (b.getDamege() * (Math.random() * 0.5f - 0.25f + 1f));
                dmg = Math.min(dmg, t.getLife());
                ((Tank) b.getSender()).getDamageList().add(new Object[]{t, dmg});
                this.engine.getMenuItems().add(
                        new DropText("-" + dmg, p.x, p.y, Color.RED, Game.ttf15)
                );
                t.reduceLife(dmg);
                //tank death ?
                if (t.isDeath()) {
                    //add score (kills)
                    if (b.getSender().getTeam() != t.getTeam()) {
                        b.getSender().addKill(1);
                    } else {
                        b.getSender().addKill(-1);
                    }
                    //kill tank
                    t.killTank(this.engine);
                    this.engine.getEngineEffects().add(
                            new Wave(
                                    t.getXY().x + t.getSize().width / 2,
                                    t.getXY().y + t.getSize().height / 2,
                                    this.engine.getRPS()/2,
                                    15f
                            ));
                }
            }
        }
    }

    private boolean standOnWater(Tank tank) {
        //calc position of tank (in blocks)
        int xt = tank.getXY().x / (Game.BLOCK_SCALE * 16);
        int yt = tank.getXY().y / (Game.BLOCK_SCALE * 16);
        for (int x = xt - 1; x < xt + 2; x++) {
            for (int y = yt - 1; y < yt + 2; y++) {
                if (x >= 0 && y >= 0) {
                    if (x < this.engine.getMapSize().width && y < this.engine.getMapSize().height) {
                        Ground g = this.engine.getGround()[x][y];
                        if (g != null) {
                            if (g == Ground.WATER) {
                                if (tank.getModel().intersects(
                                        x * Game.BLOCK_SCALE * 16,
                                        y * Game.BLOCK_SCALE * 16,
                                        Game.BLOCK_SCALE * 16,
                                        Game.BLOCK_SCALE * 16
                                )) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

}
