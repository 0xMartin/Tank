/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.AI;

import tank.Game;
import tank.engine.EngineAI;
import tank.engine.EngineObject;
import tank.objects.Tank;

/**
 *
 * @author Krcma
 */
public class TankAI implements EngineAI {

    private final Tank tank;

    public TankAI(Tank _tank) {
        this.tank = _tank;
    }

    @Override
    public void refresh() {
        //find target
        Tank target = findTarget();
        if (target != null) {
            
        }
    }

    private Tank findTarget() {
        for (int i = 0; i < Game.ENGINE.getEngineObjects().size(); i++) {
            EngineObject eo = Game.ENGINE.getEngineObjects().get(i);
            if (eo instanceof Tank) {
                if (Math.sqrt(Math.pow(this.tank.getXY().x - eo.getXY().x, 2) + Math.pow(this.tank.getXY().y - eo.getXY().y, 2)) < 700) {
                    return (Tank) eo;
                }
            }
        }
        return null;
    }

}
