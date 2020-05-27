/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tank.Game;
import tank.Team;
import tank.engine.Engine;
import tank.engine.EngineObjectAction;
import tank.engine.EngineObject;
import tank.engine.Tools;
import tank.server.Client;

/**
 *
 * @author Krcma
 */
public class Tank implements EngineObject {

    private int ID = 0;
    //type of tank (in this is all parametres of tanks)
    private TankType tankType;
    //name of thi tank
    private String name;
    //x,y position of tank
    private float x, y;
    //life
    private int life;
    //ammo count
    private int ammo;
    //angle of turet, hull and gun offset for gun shot effect
    private float turetAngle, hullAngle, gunOffSet;
    //move per refresh (for acceleration)
    private float movePerTick;
    //loading gun
    private int loading, magazine, loadingMagazine;
    //center of hull and turet [8;15]
    private int centerW, centerH;
    //turet blow up (when tank is destroyed, his turet blow and flew on the ground)
    private float blTa = 0f, blTv = 0f;
    private float blTx = 0f, blTy = 0f;
    //damage list
    private final List<Object[]> damage_list;
    private int kills = 0;
    //team id
    private Team team;

    //control
    //W(forward),A(left),S(backward),D(right),space(shot),left(turet_left),right(turet_right)
    private final boolean[] controler = new boolean[7];

    //image
    private BufferedImage hull, hull_, turet, gun;

    public Tank(int _x, int _y, float _turetAngle, float _hullAngle, TankType _tankType, Team _team) {
        this.tankType = _tankType;
        this.name = "tank";
        this.x = _x;
        this.y = _y;
        this.turetAngle = _turetAngle;
        this.hullAngle = _hullAngle;
        this.movePerTick = 0f;
        this.gunOffSet = 0f;
        this.loading = 0;
        this.magazine = 0;
        this.loadingMagazine = 0;
        this.life = _tankType.LIFE;
        this.ammo = _tankType.AMMO;
        this.team = _team;
        this.damage_list = new ArrayList<>();
        int i = _tankType.ID;
        this.hull = Tools.copyImage(Game.IMAGES.hull[i - 1]);
        this.hull_ = Tools.copyImage(Game.IMAGES.hull_[i - 1]);
        this.turet = Tools.copyImage(Game.IMAGES.turet[i - 1]);
        this.gun = Tools.copyImage(Game.IMAGES.gun[i - 1]);
        this.centerW = Game.IMAGES.hull[i - 1].getWidth() / 2;
        this.centerH = Game.IMAGES.hull[i - 1].getHeight() / 2;
        //image of tank in tank.Images
    }

    public int getKills() {
        return this.kills;
    }

    public void addKill(int i) {
        this.kills += i;
    }

    public void setTeam(Team t) {
        this.team = t;
    }

    public Team getTeam() {
        return this.team;
    }

    public List<Object[]> getDamageList() {
        return this.damage_list;
    }

    public TankType getTankType() {
        return this.tankType;
    }

    public float getGunOffset() {
        return this.gunOffSet;
    }

    public float getHullAngle() {
        return this.hullAngle;
    }

    public float getTuretAngle() {
        return this.turetAngle;
    }

    public void setHullAngle(float angle) {
        this.hullAngle = angle;
    }

    public void setTuretAngle(float angle) {
        this.turetAngle = angle;
    }

    public float getSpeedKm(int rps) {
        return (this.movePerTick * rps) / (Game.BLOCK_SCALE * 16f) * 2.5f * 3.6f;
    }

    public float getGunLoad(int rps) {
        return (this.tankType.LOAD_TIME * rps - (float) this.loading) / (float) rps;
    }

    public int getAmmoCount() {
        return this.ammo;
    }

    private float sX, sY, sH, sT;

    public void backToSafePosition() {
        this.x = this.sX;
        this.y = this.sY;
        this.hullAngle = this.sH;
        this.turetAngle = this.sT;
        this.movePerTick = 0;
    }

    public void refreshSafePosition() {
        //refresh last values of position for colission
        this.sX = this.x;
        this.sY = this.y;
        this.sH = this.hullAngle;
        this.sT = this.turetAngle;
    }

    public void speedDown(float percent) {
        this.movePerTick -= this.movePerTick * percent;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    public String getName() {
        return this.name;
    }

    public void setLife(int _life) {
        this.life = _life;
    }

    public void reduceLife(int hp) {
        this.life -= hp;
        if (this.life < 0) {
            this.life = 0;
        }
    }

    public int getMagazine() {
        return this.magazine;
    }

    public int getLife() {
        return this.life;
    }

    @Override
    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getZIndex() {
        return 2;
    }

    @Override
    public Point getXY() {
        return new Point((int) this.x, (int) this.y);
    }

    private int c = 0;

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        //is on the screen ?
        if (this.x + xOFF + this.centerH * 2 < 0) {
            return;
        }
        if (this.y + yOFF + this.centerH * 2 < 0) {
            return;
        }
        if (this.x + xOFF - this.centerH * 2 > Game.SIZE.width) {
            return;
        }
        if (this.y + yOFF - this.centerH * 2 > Game.SIZE.width) {
            return;
        }
        AffineTransform old = g2.getTransform();
        //hull
        g2.rotate(Math.toRadians(this.hullAngle), this.x + xOFF + this.centerW, this.y + yOFF + this.centerH);
        if (this.movePerTick != 0 || controler[1] || controler[3]) {
            if (this.c < 16) {
                this.c++;
            } else {
                this.c = 0;
            }
        }
        if (this.c < 8) {
            g2.drawImage(this.hull, (int) (this.x + xOFF), (int) (this.y + yOFF), null);
        } else {
            g2.drawImage(this.hull_, (int) (this.x + xOFF), (int) (this.y + yOFF), null);
        }
        g2.setTransform(old);
        //TD red line
        if (Game.ENGINE != null) {
            if (this.tankType.getTankType() == TankType.TD && Game.ENGINE.getPlayer() == this) {
                g2.setColor(new Color(180, 40, 40, 80));
                g2.setStroke(new BasicStroke(4));
                g2.drawLine((int) (this.x + this.centerW + xOFF),
                        (int) (this.y + this.centerH + yOFF),
                        (int) (this.x + this.centerW - Game.SIZE.width * 0.8f * Math.cos(Math.toRadians(this.turetAngle + 90)) + xOFF),
                        (int) (this.y + this.centerH - Game.SIZE.width * 0.8f * Math.sin(Math.toRadians(this.turetAngle + 90)) + yOFF)
                );
            }
        }
        old = g2.getTransform();
        g2.rotate(Math.toRadians(this.turetAngle), this.x + xOFF + this.centerW + this.blTx, this.y + yOFF + this.centerH + this.blTy);
        //gun
        g2.drawImage(this.gun,
                (int) (this.x + xOFF + this.blTx),
                (int) (this.y + yOFF + this.gunOffSet + this.blTy),
                null
        );
        //turet
        g2.drawImage(this.turet, (int) (this.x + xOFF + this.blTx), (int) (this.y + yOFF + this.blTy), null);
        g2.setTransform(old);
        //info
        if (Game.ENGINE != null) {
            if (Game.ENGINE.getPlayer() != this && Game.ENGINE.getPlayer() != null) {
                //life bar
                g2.setColor(new Color(80, 80, 80, 180));
                g2.fillRect((int) (this.x - this.centerW / 2 + xOFF),
                        (int) (this.y - this.centerH + yOFF),
                        this.centerW * 3,
                        15);
                g2.setColor(Game.ENGINE.getPlayer().getTeam() == this.team && this.team != Team.NONE ? new Color(0, 140, 0) : new Color(160, 0, 0));
                g2.fillRect((int) (this.x - this.centerW / 2 + xOFF),
                        (int) (this.y - this.centerH + yOFF),
                        this.centerW * 3 * this.life / this.tankType.LIFE,
                        15);
                if (this.name.length() > 0) {
                    g2.setFont(Game.ttf16);
                    g2.drawString(this.name,
                            this.x + this.centerW - g2.getFontMetrics().stringWidth(this.name) / 2 + xOFF,
                            this.y - this.centerH - 20 + yOFF
                    );
                }
            }
        }
    }

    private int e = 0;

    @Override
    public void refresh() {

        //turet blow up (this is before deth test because this is active whe is this tank death)
        if (this.blTv >= 0f) {
            if (this.blTa > 0) {
                this.blTa -= 1f;
            } else {
                this.blTa += 1f;
            }
            this.blTv -= 0.1f;
            this.blTx += Math.cos(Math.toRadians(this.blTa)) * this.blTv;
            this.blTy += Math.sin(Math.toRadians(this.blTa)) * this.blTv;
            this.turetAngle += this.blTa / 100f;
        }

        //if is death then quit
        if (this.life <= 0) {
            return;
        }

        if (Game.ENGINE.MODE == Engine.Mode.PhysicsIN) {
            //gun (loading, magazine loading)
            if (this.loading < this.tankType.LOAD_TIME * Game.ENGINE.getRPS()) {
                this.loading++;
                if (this.loading == this.tankType.LOAD_TIME * Game.ENGINE.getRPS()) {
                    this.magazine = this.tankType.MAGAZINE;
                }
            }
            if (this.loadingMagazine < Game.ENGINE.getRPS() / 2) {
                this.loadingMagazine++;
            }
        }
        //gun offset (shoting effect)
        if (this.gunOffSet > 0f) {
            this.gunOffSet -= 0.8f;
        }

        //control
        boolean acceleration_off = true;
        for (int i = 0; i < this.controler.length; i++) {
            if (this.controler[i]) {
                switch (i) {
                    case 0:
                        //forward
                        if (this.movePerTick < this.tankType.MAX_SPEED) {
                            this.movePerTick += this.tankType.ACCELERATION / Game.ENGINE.getRPS();
                        }
                        acceleration_off = false;
                        break;
                    case 1:
                        //turn left
                        int s = (int) Math.signum(this.movePerTick);
                        s = s == 0 ? 1 : s;
                        this.hullAngle -= this.tankType.TRAVERSE_SPEED / Game.ENGINE.getRPS() * s;
                        this.turetAngle -= this.tankType.TRAVERSE_SPEED / Game.ENGINE.getRPS() * s;
                        break;
                    case 2:
                        //backward
                        if (this.movePerTick * 2 > -this.tankType.MAX_SPEED) {
                            this.movePerTick -= this.tankType.ACCELERATION / Game.ENGINE.getRPS();
                        }
                        acceleration_off = false;
                        break;
                    case 3:
                        //turn right
                        s = (int) Math.signum(this.movePerTick);
                        s = s == 0 ? 1 : s;
                        this.hullAngle += this.tankType.TRAVERSE_SPEED / Game.ENGINE.getRPS() * s;
                        this.turetAngle += this.tankType.TRAVERSE_SPEED / Game.ENGINE.getRPS() * s;
                        break;
                    case 4:
                        //shot
                        if (this.ammo > 0) {
                            if (this.loading == this.tankType.LOAD_TIME * Game.ENGINE.getRPS() && this.loadingMagazine == Game.ENGINE.getRPS() / 2) {
                                this.ammo--;
                                //set controler[4] on false for stop shoting
                                this.controler[4] = false;
                                //set loading to 0 for next gun loading if magazine is empty (if magazine is not empty the set
                                //loadingMagazine on 0)
                                this.magazine--;
                                this.loadingMagazine = 0;
                                if (this.magazine <= 0) {
                                    this.loading = 0;
                                }
                                //position of gun
                                int gx = (int) (this.x + this.centerW - this.centerH * Math.cos(Math.toRadians(this.turetAngle + 90d)));
                                int gy = (int) (this.y + this.centerH - this.centerH * Math.sin(Math.toRadians(this.turetAngle + 90d)));
                                if (Game.ENGINE.MODE == Engine.Mode.PhysicsIN) {
                                    //create bullet
                                    Bullet b = new Bullet(
                                            gx,
                                            gy,
                                            this,
                                            this.tankType.BULLET_VELOCITY,
                                            this.tankType.BULLET_DAMAGE,
                                            this.turetAngle,
                                            this.tankType.BULLET_SIZE
                                    );
                                    //create id for bullet
                                    String sid = "" + this.ID;
                                    for (int j = 0; j < 5; j++) {
                                        sid += (int) (Math.random() * 10);
                                    }
                                    b.setID(Integer.parseInt(sid));
                                    Game.ENGINE.getEngineObjects().add(b);
                                    //sound
                                    Game.ENGINE.playSurroundSound(this.tankType.getShotSound(), this.getXY());
                                }
                                //set gun offset for shot effect
                                this.gunOffSet = 15f;
                                //spawn particles
                                Tools.particles_shot((int) (20 * this.tankType.BULLET_SIZE / 4f), gx, gy, this.turetAngle);
                                //shake with screen
                                if (this == Game.ENGINE.getPlayer()) {
                                    Game.ENGINE.getRenderEngine().getShakeUtility().shake(20, this.turetAngle, Game.ENGINE.getRPS() / 3);
                                }
                            }
                        }
                        break;
                    case 5:
                        //turet left
                        this.turetAngle -= this.tankType.TURRET_TRAVERSE_SPEED / Game.ENGINE.getRPS();
                        break;
                    case 6:
                        //turet right
                        this.turetAngle += this.tankType.TURRET_TRAVERSE_SPEED / Game.ENGINE.getRPS();
                        break;
                }
            }
        }

        //angle correction
        //only for TD
        if (this.tankType.getTankType() == TankType.TD) {
            if (Math.abs(this.hullAngle - this.turetAngle) > 40) {
                this.turetAngle += this.hullAngle - this.turetAngle + 40 * Math.signum(this.turetAngle - this.hullAngle);
            }
        }
        if (this.turetAngle > 360f || this.hullAngle > 360f) {
            this.hullAngle -= 360f;
            this.turetAngle -= 360f;
        } else if (this.turetAngle < -360 || this.hullAngle < -360f) {
            this.hullAngle += 360f;
            this.turetAngle += 360f;
        }

        //acceleration stoping (720 -> 0;400 -> 80)
        if (acceleration_off) {
            if (Math.abs(movePerTick) <= (this.tankType.ACCELERATION / Game.ENGINE.getRPS()) * 2) {
                this.movePerTick = 0f;
            } else {
                if (movePerTick > 0) {
                    this.movePerTick -= (this.tankType.ACCELERATION / Game.ENGINE.getRPS()) * 2;
                } else {
                    this.movePerTick += (this.tankType.ACCELERATION / Game.ENGINE.getRPS()) * 2;
                }
            }
        }

        //movement
        this.x -= this.movePerTick * Math.cos(Math.toRadians(this.hullAngle + 90d));
        this.y -= this.movePerTick * Math.sin(Math.toRadians(this.hullAngle + 90d));

        //end of map
        this.x = this.x + this.centerW < 0 ? -this.centerW : this.x;
        this.y = this.y + this.centerH < 0 ? -this.centerH : this.y;
        this.x = this.x + this.centerW > Game.ENGINE.getMapSize().width * Game.BLOCK_SCALE * 16 ? Game.ENGINE.getMapSize().width * Game.BLOCK_SCALE * 16 - this.centerW : this.x;
        this.y = this.y + this.centerH > Game.ENGINE.getMapSize().height * Game.BLOCK_SCALE * 16 ? Game.ENGINE.getMapSize().height * Game.BLOCK_SCALE * 16 - this.centerH : this.y;

        //particles (engine)
        if (this.e > 5) {
            this.e = 0;
            Tools.particles_engine(
                    3,
                    (int) (this.x + this.centerW + this.centerH * (2f / 3f) * Math.cos(Math.toRadians(this.hullAngle + 90d))),
                    (int) (this.y + this.centerH + this.centerH * (2f / 3f) * Math.sin(Math.toRadians(this.hullAngle + 90d)))
            );
        } else {
            this.e++;
        }

    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {
        if (Game.ENGINE.getPlayer() == this) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                    this.controler[0] = ea == EngineObjectAction.KeyPressed;
                    break;
                case KeyEvent.VK_A:
                    this.controler[1] = ea == EngineObjectAction.KeyPressed;
                    break;
                case KeyEvent.VK_S:
                    this.controler[2] = ea == EngineObjectAction.KeyPressed;
                    break;
                case KeyEvent.VK_D:
                    this.controler[3] = ea == EngineObjectAction.KeyPressed;
                    break;
                case KeyEvent.VK_SPACE:
                    this.controler[4] = ea == EngineObjectAction.KeyPressed;
                    break;
                case KeyEvent.VK_LEFT:
                    this.controler[5] = ea == EngineObjectAction.KeyPressed;
                    break;
                case KeyEvent.VK_RIGHT:
                    this.controler[6] = ea == EngineObjectAction.KeyPressed;
                    break;
            }
            //control tank (send data on server)
            if (Game.ENGINE.MODE == Engine.Mode.PhysicsOUT) {
                if (this.life > 0) {
                    if (Game.client != null) {
                        try {
                            Game.client.send(new Object[]{Client.Command.ControlTank, this.ID, this.controler.clone()});
                        } catch (IOException ex) {
                            Logger.getLogger(Tank.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Polygon getModel() {
        Polygon model = new Polygon();
        Point p1 = rotateModelPoint(
                this.x,
                this.y - this.tankType.MODEL_UP_OFF,
                this.centerW + this.x,
                this.centerH + this.y,
                this.hullAngle
        );
        Point p2 = rotateModelPoint(
                this.x + this.centerW * 2,
                this.y - this.tankType.MODEL_UP_OFF,
                this.centerW + this.x,
                this.centerH + this.y,
                this.hullAngle
        );
        Point p3 = rotateModelPoint(
                this.x + this.centerW * 2,
                this.y + this.centerH * 2 - this.tankType.MODEL_DOWN_OFF,
                this.centerW + this.x,
                this.centerH + this.y,
                this.hullAngle
        );
        Point p4 = rotateModelPoint(
                this.x,
                this.y + this.centerH * 2 - this.tankType.MODEL_DOWN_OFF,
                this.centerW + this.x,
                this.centerH + this.y,
                this.hullAngle
        );
        model.xpoints = new int[]{p1.x, p2.x, p3.x, p4.x};
        model.ypoints = new int[]{p1.y, p2.y, p3.y, p4.y};
        model.npoints = 4;
        return model;
    }

    private Point rotateModelPoint(float _x, float _y, float centerX, float centerY, double angle) {
        angle = Math.toRadians(angle);
        return new Point(
                (int) (centerX + (_x - centerX) * Math.cos(angle) - (_y - centerY) * Math.sin(angle)),
                (int) (centerY + (_x - centerX) * Math.sin(angle) + (_y - centerY) * Math.cos(angle))
        );
    }

    @Override
    public boolean isDeath() {
        return this.life <= 0;
    }

    private boolean isAlive = true;

    public void killTank(Engine engine) {
        if (this.isAlive) {
            //sound
            Game.ENGINE.playSurroundSound("explode", this.getXY());
            this.isAlive = false;
            engine.getEngineObjects().remove(this);
            engine.getEngineObjects().add(0, this);
            //particles (explode)
            Tools.particles_explode(90, (int) this.x + this.centerW, (int) this.y + this.centerH, 7, 1, 7);
            //change images
            this.hull = Tools.breakTankImg(this.hull);
            this.hull_ = this.hull;
            this.turet = Tools.breakTankImg(this.turet);
            this.gun = Tools.breakTankImg(this.gun);
            //fire
            engine.getEngineObjects().add(
                    new Fire(
                            (int) (this.x + this.centerW),
                            (int) (this.y + this.centerH - 10),
                            true
                    )
            );
            //turet blow
            this.blTa = (float) (Math.random() * 160f + 160f) * (Math.random() > 0.5 ? 1 : -1);
            this.blTv = (float) (Math.random() * 3f + 4f);
            //shake with screen
            //calc angle (shaking must come from this tank)
            if (engine.getPlayer() != null) {
                float a = 1f / (float) (Math.tan(Math.toRadians(
                        (engine.getPlayer().getXY().x - this.x) / -(engine.getPlayer().getXY().y - this.y)
                )));
                if (engine.getPlayer().getXY().x - this.x > 0) {
                    a = 90 - a;
                } else {
                    a = 270 - a;
                }
                //calc intensity of shake
                float d = (float) Math.sqrt(Math.pow(engine.getPlayer().getXY().y - this.y, 2) + Math.pow(engine.getPlayer().getXY().x - this.x, 2));
                d = 80 * d / Game.SIZE.width / 2;
                d = Math.max(d, 120);
                engine.getRenderEngine().getShakeUtility().shake(d, a, engine.getRPS() / 3);
            }
        }
    }

    @Override
    public void setID(int id) {
        this.ID = id;
    }

    @Override
    public int getID() {
        return this.ID;
    }

    /**
     * Set controler for this tank (for server mode)
     *
     * @param _controler
     */
    public void setControler(boolean[] _controler) {
        System.arraycopy(_controler, 0, this.controler, 0, this.controler.length);
    }

    public boolean[] getControler() {
        return this.controler;
    }

    /**
     * ID;x;y;movePerTick;hullAngle;turetAngle;loading;loadingMagazine;gunOffSet;life;magazine;tankType,team,kills
     *
     * @return Object[] config
     */
    @Override
    public Object[] getConfig() {
        return new Object[]{
            Engine.ObjClass.TANKCLASS,
            this.ID,
            this.x,
            this.y,
            this.movePerTick,
            this.hullAngle,
            this.turetAngle,
            this.loading,
            this.loadingMagazine,
            this.gunOffSet,
            this.life,
            this.magazine,
            this.tankType,
            this.team,
            this.kills,
            this.name
        };
    }

    @Override
    public boolean setConfig(Object[] config) {
        if (config != null && config.length != 0) {
            if (this.ID == (int) config[1]) {
                this.x = (float) config[2];
                this.y = (float) config[3];
                this.movePerTick = (float) config[4];
                this.hullAngle = (float) config[5];
                this.turetAngle = (float) config[6];
                this.loading = (int) config[7];
                this.loadingMagazine = (int) config[8];
                this.gunOffSet = (float) config[9];
                this.life = (int) config[10];
                this.magazine = (int) config[11];
                this.tankType = (TankType) config[12];
                this.team = (Team) config[13];
                this.kills = (int) config[14];
                this.name = (String) config[15];
                return true;
            }
        }
        return false;
    }

    /**
     * Change TankType for this tank and change his skin, if TankType tt is null
     * then only refresh skin
     *
     * @param tt
     */
    public void changeTankType(TankType tt) {
        if (tt != null) {
            this.tankType = tt;
        }
        this.hull = Tools.copyImage(Game.IMAGES.hull[this.tankType.ID - 1]);
        this.hull_ = Tools.copyImage(Game.IMAGES.hull_[this.tankType.ID - 1]);
        this.turet = Tools.copyImage(Game.IMAGES.turet[this.tankType.ID - 1]);
        this.gun = Tools.copyImage(Game.IMAGES.gun[this.tankType.ID - 1]);
        this.centerW = Game.IMAGES.hull[this.tankType.ID - 1].getWidth() / 2;
        this.centerH = Game.IMAGES.hull[this.tankType.ID - 1].getHeight() / 2;
        this.life = this.tankType.LIFE;
        this.ammo = this.tankType.AMMO;
    }

    @Override
    public int getWidth() {
        return this.hull.getHeight();
    }

    public Dimension getSize() {
        return new Dimension(this.hull.getWidth(), this.hull.getHeight());
    }

}
