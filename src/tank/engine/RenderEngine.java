/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.engine;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import tank.Game;
import tank.Team;
import tank.objects.Ground;
import tank.objects.Tank;

/**
 *
 * @author Krcma
 */
public class RenderEngine extends Canvas {

    //Zindex 0(up)<->3(down)
    protected final Thread Thread;

    private final Engine engine;

    protected int xOFF, yOFF;

    private final ShakeUtility shakeUtility;

    public RenderEngine(Engine engine) {
        this.engine = engine;
        this.shakeUtility = new ShakeUtility();
        //init thread
        this.Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //fps regulation
                double last = System.nanoTime(), ticks = 0, perTick = 1e9 / engine.getFPS();
                while (true) {
                    double now = System.nanoTime();
                    ticks += (now - last) / perTick;
                    last = now;
                    while (ticks >= 1) {
                        ticks--;
                        try {
                            render();
                        } catch (Exception ex) {
                            Logger.getLogger(RenderEngine.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
        this.Thread.setName("Render_Thread");
    }

    public void init() throws Exception {
        this.setBackground(Color.black);
        this.createBufferStrategy(3);
    }

    public ShakeUtility getShakeUtility() {
        return this.shakeUtility;
    }

    private void render() {
        int _xoff = this.xOFF;
        int _yoff = this.yOFF;
        BufferStrategy buffer = this.getBufferStrategy();
        Graphics2D g = (Graphics2D) buffer.getDrawGraphics();
        //clear
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        //render all
        //ground (grass, dirt, watter,  ...)
        if (this.engine.getGround() != null) {
            int sx = -_xoff / (Game.BLOCK_SCALE * 16);
            sx = sx >= 0 ? sx : 0;
            int sy = -_yoff / (Game.BLOCK_SCALE * 16);
            sy = sy >= 0 ? sy : 0;
            int ex = (-_xoff + this.getWidth()) / (Game.BLOCK_SCALE * 16) + 1;
            ex = ex < this.engine.getMapSize().width ? ex : this.engine.getMapSize().width - 1;
            int ey = (-_yoff + this.getHeight()) / (Game.BLOCK_SCALE * 16) + 1;
            ey = ey < this.engine.getMapSize().height ? ey : this.engine.getMapSize().height - 1;
            for (int x = sx; x <= ex; x++) {
                for (int y = sy; y <= ey; y++) {
                    Ground gr = this.engine.getGround()[x][y];
                    if (gr != null) {
                        g.drawImage(
                                gr.getImage(),
                                this.xOFF + x * 16 * Game.BLOCK_SCALE,
                                this.yOFF + y * 16 * Game.BLOCK_SCALE,
                                this
                        );
                    }
                }
            }
        }
        Game.IMAGES.refershBlockTextures();
        //all objects (tank, bullet, tree, stone, building ...)
        if (engine.gameRun) {
            for (int z = 3; z >= 0; z--) {
                for (int i = 0; i < this.engine.getEngineObjects().size(); i++) {
                    EngineObject obj = this.engine.getEngineObjects().get(i);
                    if (obj == null) {
                        continue;
                    }
                    if (obj.getZIndex() != z) {
                        continue;
                    }
                    obj.render(g, _xoff, _yoff);
                    //for mini map
                    if (this.engine.getPlayer() != null) {
                        if (obj instanceof Tank) {
                            if (obj != this.engine.getPlayer()) {
                                int x = (int) ((obj.getXY().x - this.engine.getPlayer().getXY().x) / 15f);
                                int y = (int) ((obj.getXY().y - this.engine.getPlayer().getXY().y) / 15f);
                                if (x > -113 && x < 113) {
                                    if (y > -113 && y < 113) {
                                        if (obj.isDeath()) {
                                            g.setColor(Color.black);
                                        } else {
                                            if (((Tank) obj).getTeam() != this.engine.getPlayer().getTeam() || ((Tank) obj).getTeam() == Team.NONE) {
                                                g.setColor(Color.red);
                                            } else {
                                                g.setColor(Color.green);
                                            }
                                        }
                                        g.fillOval(this.getWidth() - 112 + x, this.getHeight() - 112 + y, 10, 10);
                                    }
                                }
                            }
                        }
                    }
                    /*
                    Polygon p = obj.getModel();
                    g.setColor(Color.red);
                    if (p != null) {
                        int[] xp = new int[p.npoints];
                        int[] yp = new int[p.npoints];
                        for (int j = 0; j < p.npoints; j++) {
                            xp[j] = p.xpoints[j] + this.xOFF;
                            yp[j] = p.ypoints[j] + this.yOFF;
                        }
                        g.drawPolygon(xp, yp, p.npoints);
                    }
                    */
                }
            }
            //effects (particles, explosions ...)
            for (int i = 0; i < this.engine.getEngineEffects().size(); i++) {
                EngineEffect obj = this.engine.getEngineEffects().get(i);
                if (obj == null) {
                    continue;
                }
                obj.render(g, _xoff, _yoff);
            }
        }
        //menu items
        for (int i = 0; i < this.engine.getMenuItems().size(); i++) {
            EngineMenuItem mi = this.engine.getMenuItems().get(i);
            mi.render(g, _xoff, _yoff);
        }
        //info
        if (engine.gameRun) {
            if (this.engine.getPlayer() != null) {
                //player info
                renderInfo(g);
                //team list
                renderTeamList(g);
                //recalc xoff and y off
                this.shakeUtility.refresh();
                this.xOFF = -this.engine.getPlayer().getXY().x + this.getWidth() / 2 + this.shakeUtility.getXOffSet();
                this.yOFF = -this.engine.getPlayer().getXY().y + this.getHeight() / 2 + this.shakeUtility.getYOffSet();
            } else {
                this.xOFF = 0;
                this.yOFF = 0;
            }
            if (this.engine.MODE == Engine.Mode.PhysicsOUT) {
                g.drawString("Ping: " + Game.client.getPing() + " ms", 20, 20);
            }
        }
        g.drawString(this.engine.getEngineObjects().size()+"", 500, 20);
        //show buffer
        buffer.show();
    }

    private void renderInfo(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(60, 60, 60, 200));
        g.fillRect(5, this.getHeight() - 210, 300, 205);
        g.setColor(new Color(30, 30, 30, 255));
        g.drawRect(5, this.getHeight() - 210, 300, 205);
        g.setColor(new Color(240, 240, 240, 200));
        g.setFont(Game.ttf19);
        g.drawString("Name: ", 10, this.getHeight() - 185);
        g.drawString("Speed: ", 10, this.getHeight() - 155);
        g.drawString("Gun load: ", 10, this.getHeight() - 125);
        g.drawString("Magazine: ", 10, this.getHeight() - 95);
        g.drawString("Life: ", 10, this.getHeight() - 65);
        g.drawString("Ammo: ", 10, this.getHeight() - 35);
        g.setFont(Game.ttf26);
        String s = this.engine.getPlayer().getName();
        g.drawString(s, 300 - g.getFontMetrics().stringWidth(s), this.getHeight() - 185);
        s = (int) this.engine.getPlayer().getSpeedKm(engine.getRPS()) + " km/h";
        g.drawString(s, 300 - g.getFontMetrics().stringWidth(s), this.getHeight() - 155);
        s = "";
        for (int i = 0; i < this.engine.getPlayer().getMagazine(); i++) {
            s += " |";
        }
        g.drawString(s, 300 - g.getFontMetrics().stringWidth(s), this.getHeight() - 95);
        s = (int) this.engine.getPlayer().getAmmoCount() + "";
        g.drawString(s, 300 - g.getFontMetrics().stringWidth(s), this.getHeight() - 35);
        float f = this.engine.getPlayer().getGunLoad(engine.getRPS());
        if (f == 0f) {
            s = "READY";
            g.setColor(Color.green);
            g.drawString(s, 300 - g.getFontMetrics().stringWidth(s), this.getHeight() - 125);
        } else {
            s = String.format("%.1f", this.engine.getPlayer().getGunLoad(engine.getRPS())) + " s";
            g.drawString(s, 300 - g.getFontMetrics().stringWidth(s), this.getHeight() - 125);
        }
        s = (int) this.engine.getPlayer().getLife() + " HP";
        g.setColor(Color.red);
        g.drawString(s, 300 - g.getFontMetrics().stringWidth(s), this.getHeight() - 65);
        g.setFont(Game.ttf19);
        int j = 0;
        for (int i = Math.max(0, this.engine.getPlayer().getDamageList().size() - 9); i < this.engine.getPlayer().getDamageList().size(); i++) {
            Object[] hit = this.engine.getPlayer().getDamageList().get(i);
            s = ((Tank) hit[0]).getName() + " [" + hit[1] + "]";
            g.drawString(s, 310, this.getHeight() - 200 + j * 20);
            j++;
        }
        int total = 0;
        total = this.engine.getPlayer().getDamageList().stream().map((hit) -> (int) hit[1]).reduce(total, Integer::sum);
        g.drawString("Total: " + total, 310, this.getHeight() - 200 + 180);
        //mini map
        g.setColor(new Color(60, 60, 60, 150));
        g.fillRect(this.getWidth() - 225, this.getHeight() - 225, 220, 220);
        g.setColor(new Color(30, 30, 30, 255));
        g.drawRect(this.getWidth() - 225, this.getHeight() - 225, 220, 220);
        g.setColor(Color.white);
        g.fillOval(this.getWidth() - 112, this.getHeight() - 112, 10, 10);
    }

    private void renderTeamList(Graphics2D g) {
        int a = 0, b = 0;
        int maxa = 0, maxb = 0;
        for (int i = 0; i < this.engine.getEngineObjects().size(); i++) {
            EngineObject obj = this.engine.getEngineObjects().get(i);
            if (obj != null) {
                if (obj instanceof Tank) {
                    Tank t = (Tank) obj;
                    if (!t.isDeath()) {
                        if (t.getTeam() == Team.A) {
                            maxa = Math.max(maxa, 20 + g.getFontMetrics().stringWidth(t.getName() + t.getKills()) + (int) (t.getTankType().getImage().getWidth() * 0.8));
                            a++;
                        } else if (t.getTeam() == Team.B) {
                            maxb = Math.max(maxb, 20 + g.getFontMetrics().stringWidth(t.getName() + t.getKills()) + (int) (t.getTankType().getImage().getWidth() * 0.8));
                            b++;
                        }
                    }
                }
            }
        }
        g.setPaint(new Color(20, 20, 20, 100));
        if (maxa != 0) {
            g.fillRect(2, 35, maxa + 10, a * 40);
        }
        if (maxb != 0) {
            g.fillRect(this.getWidth() - maxb - 12, 35, maxb + 10, b * 40);
        }
        g.setColor(new Color(220, 220, 220));
        a = 0;
        b = 0;
        for (int i = 0; i < this.engine.getEngineObjects().size(); i++) {
            EngineObject obj = this.engine.getEngineObjects().get(i);
            if (obj != null) {
                if (obj instanceof Tank) {
                    Tank t = (Tank) obj;
                    if (!t.isDeath()) {
                        if (t.getTeam() == Team.A) {
                            BufferedImage img = t.getTankType().getImage();
                            g.drawImage(img, 5, 35 + a * 40, this);
                            g.setFont(Game.ttf15);
                            g.drawString(t.getTankType().getLevel() + "", 10 + (int) (img.getWidth() * 0.5), 72 + a * 40);
                            g.setFont(Game.ttf19);
                            g.drawString(t.getName(), 10 + (int) (img.getWidth() * 0.8), 65 + a * 40);
                            g.drawString("" + t.getKills(), 20 + g.getFontMetrics().stringWidth(t.getName()) + (int) (img.getWidth() * 0.8), 65 + a * 40);
                            a++;
                        } else if (t.getTeam() == Team.B) {
                            BufferedImage img = t.getTankType().getImage();
                            g.drawImage(img, this.getWidth() - maxb - 5, 35 + b * 40, this);
                            g.setFont(Game.ttf15);
                            g.drawString(t.getTankType().getLevel() + "", this.getWidth() - maxb + 10 + (int) (img.getWidth() * 0.5), 72 + b * 40);
                            g.setFont(Game.ttf19);
                            g.drawString(t.getName(), this.getWidth() - maxb + 10 + (int) (img.getWidth() * 0.8), 65 + b * 40);
                            g.drawString("" + t.getKills(), this.getWidth() - maxb + 20 + g.getFontMetrics().stringWidth(t.getName()) + (int) (img.getWidth() * 0.8), 65 + b * 40);
                            b++;
                        }
                    }
                }
            }
        }
    }

    //utilities
    public class ShakeUtility {

        private float dist, angle, time, timeF;
        private double dl;
        private float xoff, yoff;

        public ShakeUtility() {
            this.xoff = 0f;
            this.yoff = 0f;
        }

        public int getXOffSet() {
            return (int) this.xoff;
        }

        public int getYOffSet() {
            return (int) this.yoff;
        }

        public void refresh() {
            //shake
            if (this.time > this.timeF / 2f || dl > 0.2f) {
                double d = this.dist * Math.pow(Math.E, -Math.pow(this.time - this.timeF / 2, 2) / this.dist);
                this.dl = d;
                this.xoff = (float) (Math.cos(Math.toRadians(this.angle + 90f)) * d);
                this.yoff = (float) (Math.sin(Math.toRadians(this.angle + 90f)) * d);
                this.time--;
            } else {
                this.xoff = 0f;
                this.yoff = 0f;
                this.dl = 0f;
            }
        }

        public void shake(float _dist, float _angle, float _time) {
            this.dist = _dist;
            this.angle = _angle;
            this.time = _time;
            this.timeF = time;
        }

    }

}
