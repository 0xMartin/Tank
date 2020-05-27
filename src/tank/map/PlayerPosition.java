/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.map;

import java.io.Serializable;
import tank.Team;

/**
 *
 * @author Krcma
 */
public class PlayerPosition implements Serializable {

    static final long serialVersionUID = 15613815L;
    
    int x, y;
    float hull, turet;
    Team team;

    public PlayerPosition(int _x, int _y, float _hull, float _turet, Team _team) {
        this.x = _x;
        this.y = _y;
        this.hull = _hull;
        this.team = _team;
        this.turet = _turet;
    }
    
}
