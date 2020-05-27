/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import tank.objects.Tank;
import javax.imageio.ImageIO;
import tank.engine.Tools;
import tank.objects.TankType;

/**
 *
 * @author Krcma
 */
public class Images {

    public BufferedImage grass, dirt, gravel, water;
    public BufferedImage[] fire, waterA, trees, rocks, fence, buildings;
    public BufferedImage[] tanks;
    //tank
    public BufferedImage[] hull, hull_, turet, gun;

    public void init() throws Exception {

        //tank
        this.hull = new BufferedImage[20];
        this.hull_ = new BufferedImage[20];
        this.turet = new BufferedImage[20];
        this.gun = new BufferedImage[20];
        for (int i = 1; i <= hull.length; i++) {
            this.hull[i - 1] = Tools.resizePixelImage(ImageIO.read(getClass().getResource("/tank/objects/Tank" + i + "/Tank" + i + ".png")), Game.BLOCK_SCALE);
            this.hull_[i - 1] = Tools.resizePixelImage(ImageIO.read(getClass().getResource("/tank/objects/Tank" + i + "/Tank" + i + "_.png")), Game.BLOCK_SCALE);
            this.turet[i - 1] = Tools.resizePixelImage(ImageIO.read(getClass().getResource("/tank/objects/Tank" + i + "/Tank" + i + "t.png")), Game.BLOCK_SCALE);
            this.gun[i - 1] = Tools.resizePixelImage(ImageIO.read(getClass().getResource("/tank/objects/Tank" + i + "/Tank" + i + "g.png")), Game.BLOCK_SCALE);
        }

        //tanks icons
        this.tanks = new BufferedImage[20];
        for (int i = 1; i <= tanks.length; i++) {
            BufferedImage img = new BufferedImage(42 * Game.BLOCK_SCALE, 21 * Game.BLOCK_SCALE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) img.getGraphics();
            TankType tt = Tools.getTankType(i);
            if (tt != null) {
                Tank t = new Tank(0, 0, 90f, 90f, tt, Team.NONE);
                t.render(g, 0, -Game.BLOCK_SCALE * 6);
                this.tanks[i - 1] = Tools.resizeImage(img, 82, 42);
            }
        }

        //blocks
        this.grass = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/grass.png")), Game.BLOCK_SCALE);
        this.dirt = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/dirt.png")), Game.BLOCK_SCALE);
        this.gravel = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/gravel.png")), Game.BLOCK_SCALE);
        this.waterA = Tools.loadTiles(
                Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/water.png")), Game.BLOCK_SCALE),
                32
        );
        this.water = this.waterA[0];

        //trees
        this.trees = new BufferedImage[2];
        for (int i = 0; i < this.trees.length; i++) {
            this.trees[i] = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/tree" + (i + 1) + ".png")), (Game.BLOCK_SCALE - 1));
        }

        //rocks (without resize)
        this.rocks = new BufferedImage[4];
        for (int i = 0; i < this.rocks.length; i++) {
            this.rocks[i] = ImageIO.read(this.getClass().getResource("/tank/src/img/rock" + (i + 1) + ".png"));
        }
        
        //buildings
        this.buildings = new BufferedImage[1];
        for (int i = 0; i < this.buildings.length; i++) {
            this.buildings[i] = ImageIO.read(this.getClass().getResource("/tank/src/img/building" + (i + 1) + ".png"));
        }

        //fire
        this.fire = Tools.loadTiles(
                Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/fire.png")), Game.BLOCK_SCALE - 1),
                32
        );

        //fence
        this.fence = new BufferedImage[6];
        this.fence[0] = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/fence/fence_v.png")), Game.BLOCK_SCALE);
        this.fence[1] = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/fence/fence_h.png")), Game.BLOCK_SCALE);
        this.fence[2] = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/fence/fence_ul.png")), Game.BLOCK_SCALE);
        this.fence[3] = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/fence/fence_ur.png")), Game.BLOCK_SCALE);
        this.fence[4] = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/fence/fence_dl.png")), Game.BLOCK_SCALE);
        this.fence[5] = Tools.resizePixelImage(ImageIO.read(this.getClass().getResource("/tank/src/img/fence/fence_dr.png")), Game.BLOCK_SCALE);
    }

    int i = 0;

    public void refershBlockTextures() {
        if (i < 62) {
            i++;
        } else {
            i = 0;
        }
        this.water = this.waterA[i / 2];
    }

}
