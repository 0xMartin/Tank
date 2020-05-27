/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.engine;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author Krcma
 */
public interface EngineMenuItem {

    public void render(Graphics2D g2, int xOFF, int yOFF);
    
    public void refresh();

    public void mouseAction(EngineObjectAction ea, MouseEvent e, int xOFF, int yOFF);
    
    public void keyAction(EngineObjectAction ea, KeyEvent e);
    
}
