/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.image.BufferedImage;
import tank.Game;

/**
 *
 * @author Krcma
 */
public enum TankType {

    //HEAVY
    TANK1(1, 400, 50f, 42f, 4.8f, 3f, 2f, 12f, 50, 4, 120, 0, 3 * Game.BLOCK_SCALE, 1),
    TANK2(2, 750, 45f, 40f, 4.1f, 2.9f, 3f, 12f, 100, 4, 100, 0, 3 * Game.BLOCK_SCALE, 1),
    TANK3(3, 1250, 38f, 35f, 3.8f, 2.5f, 4f, 13f, 210, 6, 80, -2 * Game.BLOCK_SCALE, 2 * Game.BLOCK_SCALE, 1),
    TANK4(4, 1700, 32f, 32f, 3.2f, 2.2f, 7f, 14f, 380, 8, 65, -3 * Game.BLOCK_SCALE, 1 * Game.BLOCK_SCALE, 1),
    TANK5(5, 2500, 28f, 30f, 3.1f, 1.8f, 8f, 16f, 620, 10, 45, -5 * Game.BLOCK_SCALE, 1 * Game.BLOCK_SCALE, 1),
    //TD
    TANK6(6, 250, 40f, 30f, 3.5f, 2f, 3f, 13f, 120, 4, 90, 0, 1 * Game.BLOCK_SCALE, 1),
    TANK7(7, 450, 38f, 25f, 3.4f, 1.8f, 6f, 14f, 270, 6, 80, 0, 1 * Game.BLOCK_SCALE, 1),
    TANK8(8, 850, 32f, 20f, 3.0f, 1.6f, 10f, 16f, 520, 8, 65, 0, 2 * Game.BLOCK_SCALE, 1),
    TANK9(9, 1080, 32f, 18f, 2.8f, 1.6f, 13f, 18f, 750, 8, 40, 0, 2 * Game.BLOCK_SCALE, 1),
    TANK10(10, 1450, 30f, 16f, 2.6f, 1.5f, 16f, 20f, 1300, 11, 30, -3 * Game.BLOCK_SCALE, 9 * Game.BLOCK_SCALE, 1),
    //AUTO LOADER
    TANK11(11, 300, 60f, 58f, 5.5f, 3.2f, 5f, 12f, 20, 3, 220, 0, 2 * Game.BLOCK_SCALE, 4),
    TANK12(12, 590, 58f, 57f, 5.3f, 3.1f, 7f, 12f, 35, 3, 190, 0, 2 * Game.BLOCK_SCALE, 4),
    TANK13(13, 950, 56f, 56f, 5.1f, 3f, 18f, 13f, 85, 4, 140, 0, 2 * Game.BLOCK_SCALE, 5),
    TANK14(14, 1150, 50f, 55f, 5.0f, 3f, 22f, 14f, 125, 5, 90, 0, 2 * Game.BLOCK_SCALE, 5),
    TANK15(15, 1400, 49f, 53f, 5.0f, 3f, 25f, 15f, 250, 5, 75, 0, 2 * Game.BLOCK_SCALE, 5),
    //LIGHT
    TANK16(16, 260, 70f, 78f, 6.5f, 3.7f, 2f, 10f, 30, 3, 220, -2 * Game.BLOCK_SCALE, Game.BLOCK_SCALE, 1),
    TANK17(17, 480, 68f, 77f, 6.3f, 3.6f, 2f, 10f, 50, 3, 190, -2 * Game.BLOCK_SCALE, Game.BLOCK_SCALE, 1),
    TANK18(18, 890, 66f, 76f, 6.1f, 3.5f, 3f, 11f, 75, 4, 140, -2 * Game.BLOCK_SCALE, 2 * Game.BLOCK_SCALE, 1),
    TANK19(19, 1100, 60f, 75f, 6.0f, 3.5f, 2f, 11f, 95, 5, 140, -1 * Game.BLOCK_SCALE, 2 * Game.BLOCK_SCALE, 1),
    TANK20(20, 1290, 59f, 73f, 6.0f, 3.5f, 1f, 12f, 105, 5, 155, -1 * Game.BLOCK_SCALE, 2 * Game.BLOCK_SCALE, 1);

    public final int LIFE, AMMO, BULLET_DAMAGE, BULLET_SIZE, ID, MODEL_UP_OFF, MODEL_DOWN_OFF, MAGAZINE;

    public final float TRAVERSE_SPEED, TURRET_TRAVERSE_SPEED, MAX_SPEED, ACCELERATION, LOAD_TIME, BULLET_VELOCITY;

    TankType(int _id, int _life, float _TURRET_TRAVERSE_SPEED, float _TRAVERSE_SPEED, float _MAX_SPEED, float _ACCELERATION,
            float _LOAD_TIME, float _BULLET_VELOCITY, int _BULLET_DAMAGE, int _BULLET_SIZE, int _ammo, int muo, int mdo, int _magazine) {
        this.ID = _id;
        this.LIFE = _life;
        this.TRAVERSE_SPEED = _TRAVERSE_SPEED;
        this.TURRET_TRAVERSE_SPEED = _TURRET_TRAVERSE_SPEED;
        this.MAX_SPEED = _MAX_SPEED;
        this.ACCELERATION = _ACCELERATION;
        this.LOAD_TIME = _LOAD_TIME;
        this.BULLET_VELOCITY = _BULLET_VELOCITY;
        this.BULLET_DAMAGE = _BULLET_DAMAGE;
        this.BULLET_SIZE = _BULLET_SIZE;
        this.AMMO = _ammo;
        this.MODEL_UP_OFF = muo;
        this.MODEL_DOWN_OFF = mdo;
        this.MAGAZINE = _magazine;
    }

    public static final int HEAVY = 1, LIGHT = 2, TD = 3, AUTOLOADER = 4;

    public int getTankType() {
        if (this.ID > 5 && this.ID < 11) {
            return TankType.TD;
        }
        if (this.ID > 10 && this.ID < 16) {
            return TankType.AUTOLOADER;
        }
        if (this.ID > 0 && this.ID < 6) {
            return TankType.HEAVY;
        }
        if (this.ID > 15 && this.ID < 21) {
            return TankType.LIGHT;
        }
        return 0;
    }

    public int getLevel() {
        int lv = this.ID;
        while (lv > 5) {
            lv -= 5;
        }
        return lv;
    }

    public BufferedImage getImage() {
        return Game.IMAGES.tanks[this.ID - 1];
    }

    public String getShotSound() {
        //small gun
        if ((this.ID >= 1 && this.ID <= 3) || (this.ID >= 6 && this.ID <= 7) || (this.ID >= 11 && this.ID <= 14) || (this.ID >= 16 && this.ID <= 20)) {
            return "shot1";
        }
        //medium gun
        if ((this.ID == 4) || (this.ID == 8 || this.ID == 9) || (this.ID == 15)) {
            return "shot2";
        }
        //big gun
        if ((this.ID == 5) || (this.ID == 10)) {
            return "shot3";
        }
        return "";
    }

    public int getBounceAngle() {
        if (this.ID <= 5) {
            return (int) (75 - (this.ID / 5f) * 30);
        } else if (this.ID < 10) {
            return (int) (85 - ((this.ID - 5) / 5f) * 20);
        } else if (this.ID <= 15) {
            return (int) (80 - ((this.ID - 10) / 5f) * 20);
        } else if (this.ID <= 20) {
            return (int) (90 - ((this.ID - 15) / 5f) * 25);
        }
        return 0;
    }

}
