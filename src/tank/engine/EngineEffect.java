/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.engine;

import java.awt.Graphics2D;

/**
 *
 * @author Krcma
 */
public interface EngineEffect {
    
    public void refresh();
    
    public void render(Graphics2D g2, int xOFF, int yOFF);
    
    public boolean isDeath();
    
}
