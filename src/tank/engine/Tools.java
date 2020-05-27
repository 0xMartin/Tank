/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JOptionPane;
import tank.Game;
import tank.Team;
import tank.objects.Building;
import tank.objects.Bullet;
import tank.objects.BulletPath;
import tank.objects.Fence;
import tank.objects.Fire;
import tank.objects.Particle;
import tank.objects.Rock;
import tank.objects.Tank;
import tank.objects.TankType;
import tank.objects.Tree;

/**
 *
 * @author Krcma
 */
public class Tools {

    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    public static BufferedImage resizePixelImage(BufferedImage input, int size) {
        if (size <= 0) {
            return new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB);
        }
        if (size == 1) {
            return input;
        }
        BufferedImage output = new BufferedImage(input.getWidth() * size, input.getHeight() * size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        for (int x = 0; x < input.getWidth(); x++) {
            for (int y = 0; y < input.getHeight(); y++) {
                g.setColor(new Color(input.getRGB(x, y), true));
                g.fillRect(x * size, y * size, size, size);
            }
        }
        return output;
    }

    public static BufferedImage resizeImage(BufferedImage input, int w, int h) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(input, 0, 0, w, h, null);
        return img;
    }

    public static void particles_shot(int number, int x, int y, float angle) {
        for (int i = 0; i < number; i++) {
            Game.ENGINE.getEngineEffects().add(
                    new Particle(
                            (int) (x + Math.random() * 20 - 10),
                            (int) (y + Math.random() * 20 - 10),
                            new Color(255, (int) (Math.random() * 255), 0),
                            (float) (Math.random() * 40 - 20 + angle),
                            (float) (Math.random() * 5 + 8),
                            (int) (Math.random() * 4 + 3),
                            (int) (Math.random() * Game.ENGINE.getRPS() / 2 + Game.ENGINE.getRPS() / 2)
                    )
            );
        }
    }

    public static void particles_engine(int number, int x, int y) {
        for (int i = 0; i < number; i++) {
            int c = (int) (Math.random() * 50 + 40);
            Game.ENGINE.getEngineEffects().add(
                    new Particle(
                            (int) (x + Math.random() * 20 - 10),
                            (int) (y + Math.random() * 20 - 10),
                            new Color(c, c, c),
                            (float) (Math.random() * 360),
                            (float) (Math.random() * 1 + 1),
                            (int) (Math.random() * 5 + 4),
                            (int) (Math.random() * Game.ENGINE.getRPS() / 2 + Game.ENGINE.getRPS() / 2)
                    )
            );
        }
    }

    public static void particles_explode(int number, int x, int y, float velocity, float time, int size) {
        for (int i = 0; i < number; i++) {
            Game.ENGINE.getEngineEffects().add(
                    new Particle(
                            (int) (x + Math.random() * 20 - 10),
                            (int) (y + Math.random() * 20 - 10),
                            new Color(255, (int) (Math.random() * 255), 0),
                            (float) (Math.random() * 360),
                            (float) ((Math.random() + 0.5) * velocity),
                            (int) (Math.random() * size / 2 + size),
                            (int) (Math.random() * Game.ENGINE.getRPS() / 2 * time + Game.ENGINE.getRPS() / 2 * time)
                    )
            );
        }
    }

    public static void particles_fence(Fence f) {
        for (int i = 0; i < 20; i++) {
            int r = (int) (Math.random() * 120) + 100;
            Game.ENGINE.getEngineEffects().add(
                    new Particle(
                            (int) (f.getXY().x + 8 * Game.BLOCK_SCALE + Math.random() * 20 - 10),
                            (int) (f.getXY().y + 8 * Game.BLOCK_SCALE + Math.random() * 20 - 10),
                            new Color(r, (int) (r / 1.2f), (int) (r / 1.6f)),
                            (float) (Math.random() * 360),
                            (float) ((Math.random() + 0.5) * 0.5f),
                            (int) (Math.random() * 8 / 2 + 8),
                            (int) (Math.random() * Game.ENGINE.getRPS() / 2 * 1f + Game.ENGINE.getRPS() / 2 * 1f)
                    )
            );
        }
    }

    public static void particles_tree(Tree tree) {
        int r = (int) (Math.random() * 150) + 50;
        Game.ENGINE.getEngineEffects().add(
                new Particle(
                        (int) (tree.getXY().x + tree.getWidth() / 2 + Math.random() * 20 - 10),
                        (int) (tree.getXY().y + tree.getHeight() * 0.8f + Math.random() * 20 - 10),
                        new Color(r, (int) (r * 0.6f), (int) (r * 0.38f)),
                        (float) (Math.random() * 360),
                        (float) ((Math.random() + 0.5) * 0.5f),
                        (int) (Math.random() * 8 / 2 + 8),
                        (int) (Math.random() * Game.ENGINE.getRPS() / 2 * 1.5f + Game.ENGINE.getRPS() / 2 * 1.5f)
                )
        );
    }

    public static BufferedImage[] loadTiles(BufferedImage tilesimage, int tiles) {
        BufferedImage[] out = new BufferedImage[tiles];
        for (int i = 0; i < tiles; i++) {
            out[i] = tilesimage.getSubimage(0, (int) (tilesimage.getHeight() / tiles * i), tilesimage.getWidth(), (int) (tilesimage.getHeight() / tiles));
        }
        return out;
    }

    public static void fireSpawn(Engine engine, int x, int y, int number, int radius) {
        for (int i = 0; i < number; i++) {
            engine.getEngineObjects().add(0,
                    new Fire((int) (x + radius * Math.cos(Math.random() * Math.PI * 2)), (int) (y + radius * Math.sin(Math.random() * Math.PI * 2)), false)
            );
        }
    }

    public static BufferedImage breakTankImg(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color c = new Color(img.getRGB(x, y), true);
                if (c.getAlpha() != 0) {
                    float f = (float) (Math.random() * 1.6f);
                    img.setRGB(
                            x,
                            y,
                            (new Color(
                                    (int) (120f * (c.getRed() / 255f) * f),
                                    (int) (120f * (c.getGreen() / 255f) * f),
                                    (int) (120f * (c.getBlue() / 255f) * f))).getRGB()
                    );
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            breakImage(
                    img,
                    (int) (img.getWidth() * Math.random()),
                    (int) (img.getHeight() * Math.random()),
                    (int) (Math.random() * 2 + 3)
            );
        }
        return img;
    }

    private static void breakImage(BufferedImage img, int x, int y, int depth) {
        if (Math.random() > 0.7 || depth == 0 || x < 0 || y < 0 || x + 1 > img.getWidth() || y + 1 > img.getHeight()) {
            return;
        }
        img.setRGB(x, y, (new Color(0f, 0f, 0f, 0f)).getRGB());
        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                if (j != 0 || k != 0) {
                    breakImage(img, x + j, y + k, depth - 1);
                }
            }
        }
    }

    public static TankType getTankType(int id) {
        TankType tt = null;
        switch (id) {
            case 1:
                tt = TankType.TANK1;
                break;
            case 2:
                tt = TankType.TANK2;
                break;
            case 3:
                tt = TankType.TANK3;
                break;
            case 4:
                tt = TankType.TANK4;
                break;
            case 5:
                tt = TankType.TANK5;
                break;
            case 6:
                tt = TankType.TANK6;
                break;
            case 7:
                tt = TankType.TANK7;
                break;
            case 8:
                tt = TankType.TANK8;
                break;
            case 9:
                tt = TankType.TANK9;
                break;
            case 10:
                tt = TankType.TANK10;
                break;
            case 11:
                tt = TankType.TANK11;
                break;
            case 12:
                tt = TankType.TANK12;
                break;
            case 13:
                tt = TankType.TANK13;
                break;
            case 14:
                tt = TankType.TANK14;
                break;
            case 15:
                tt = TankType.TANK15;
                break;
            case 16:
                tt = TankType.TANK16;
                break;
            case 17:
                tt = TankType.TANK17;
                break;
            case 18:
                tt = TankType.TANK18;
                break;
            case 19:
                tt = TankType.TANK19;
                break;
            case 20:
                tt = TankType.TANK20;
                break;
        }
        return tt;
    }

    public static void showException(Exception ex) {
        String message = "";
        for (StackTraceElement ste : ex.getStackTrace()) {
            message += ste.toString() + "\n";
        }
        JOptionPane.showMessageDialog(null, message);
    }

    public static void replace(int id, Tank tank, List<Tank> list) {
        for (int i = 0; i < list.size(); i++) {
            Tank t = list.get(i);
            if (t.getID() == id) {
                list.set(i, tank);
                return;
            }
        }
        list.add(tank);
    }

    public static EngineObject createEngineObjectFromConfig(Object[] config) {
        if (config != null && config.length != 0) {
            switch ((Engine.ObjClass) config[0]) {
                case TANKCLASS:
                    Tank t = new Tank(0, 0, 0f, 0f, TankType.TANK1, Team.NONE);
                    t.setID((int) config[1]);
                    t.setConfig(config);
                    t.changeTankType(null);
                    return t;
                case BULLETCLASS:
                    Bullet b = new Bullet(
                            (float) config[2],
                            (float) config[3],
                            null,
                            (float) config[4],
                            (int) config[5],
                            (float) config[6],
                            (int) config[7]
                    );
                    b.setID((int) config[1]);
                    b.setConfig(config);
                    return b;
                case BULLETPATHCLASS:
                    for (int i = 0; i < Game.ENGINE.getEngineObjects().size(); i++) {
                        EngineObject o = Game.ENGINE.getEngineObjects().get(i);
                        if (o instanceof Bullet) {
                            if (o.getID() == (int) config[4]) {
                                BulletPath bp = new BulletPath((Bullet) o);
                                bp.setConfig(config);
                                return bp;
                            }
                        }
                    }
                    break;
                case ROCKCLASS:
                    return new Rock((int) config[1], (int) config[2], (int) config[3], (int) config[4]);
                case TREECLASS:
                    return new Tree((int) config[1], (int) config[2]);
                case FIRECLASS:
                    return new Fire((int) config[1], (int) config[2], (boolean) config[3]);
                case FENCECLASS:
                    return new Fence((int) config[1], (int) config[2], (Fence.Orientation) config[3]);
                case BUILDINGCLASS:
                    return new Building((int) config[1], (int) config[2], (int) config[3]);
            }
        }
        return null;
    }

}
