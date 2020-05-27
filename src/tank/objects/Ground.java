/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import tank.Game;

/**
 *
 * @author Krcma
 */
public enum Ground implements Serializable {

    GRASS, DIRT, WATER, SAND, GRAVEL, SANDSTONE;

    public BufferedImage getImage() {
        switch (this) {
            case GRASS:
                return Game.IMAGES.grass;
            case DIRT:
                return Game.IMAGES.dirt;
            case WATER:
                return Game.IMAGES.water;
            case GRAVEL:
                return Game.IMAGES.gravel;
        }
        return new BufferedImage(16 * Game.BLOCK_SCALE, 16 * Game.BLOCK_SCALE, BufferedImage.TYPE_INT_ARGB);
    }

}
