/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.map;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import tank.Game;
import tank.engine.Tools;
import tank.objects.Fence;
import tank.objects.Ground;
import tank.objects.TankType;

/**
 *
 * @author Krcma
 */
public class ComboBoxRendered extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value == null) {
            this.setIcon(new ImageIcon(new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB)));
        } else if (value instanceof Ground) {
            this.setIcon(new ImageIcon(((Ground) value).getImage()));
        } else if (value instanceof TankType) {
            this.setIcon(new ImageIcon(((TankType) value).getImage()));
        } else if (value instanceof Fence.Orientation) {
            this.setIcon(
                    new ImageIcon(
                            Game.IMAGES.fence[((Fence.Orientation) value).id]
                    )
            );
        } else if (value instanceof BufferedImage) {
            try {
                this.setIcon(new ImageIcon(Tools.resizeImage((BufferedImage) value, 50, 50)));
                this.setText("");
            } catch (Exception ex) {
                Logger.getLogger(ComboBoxRendered.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!isSelected) {
            this.setBackground(index % 2 == 0 ? this.getBackground() : this.getBackground().darker());
        }
        return this;
    }

}
