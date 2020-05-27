/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.engine;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;

/**
 *
 * @author Krcma
 */
public interface EngineObject {
    
    public int getZIndex();
    
    public void setID(int id);
    
    public int getID();
    
    public void setXY(int x, int y);
    
    public Point getXY();
    
    public void render(Graphics2D g2, int xOFF, int yOFF);
    
    public void refresh();
    
    public void keyAction(EngineObjectAction ea, KeyEvent e);
    
    public Polygon getModel();
    
    public boolean isDeath();
    
    public Object[] getConfig();
            
    public boolean setConfig(Object[] config);
    
    public int getWidth();
    
}
