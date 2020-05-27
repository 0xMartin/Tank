/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.map;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import tank.Game;
import tank.engine.EngineObject;
import tank.objects.Ground;
import tank.objects.Tank;

/**
 *
 * @author Krcma
 */
public class Map {

    private Data data;
    private final List<EngineObject> object;

    public Data getData() {
        return this.data;
    }

    public void setData(Data d) {
        this.data = d;
        this.object.clear();
        this.data.buildObjectList(this.object);
    }

    public Map(String _name, Dimension _mapSize) {
        this.data = new Data(_name, _mapSize);
        this.object = new ArrayList<>();
    }

    public Dimension getMapSize() {
        return this.data.mapSize;
    }

    public Ground[][] getGround() {
        return this.data.ground;
    }

    public List<EngineObject> getObjects(List<EngineObject> players) {
        List<EngineObject> list = new ArrayList<>();
        //add all objects
        this.object.forEach((o) -> {
            list.add(o);
        });
        //add player
        for (int i = 0; i < players.size(); i++) {
            EngineObject player = players.get(i);
            if (player instanceof Tank) {
                for (PlayerPosition pp : this.data.players) {
                    if (pp.team == ((Tank) player).getTeam()) {
                        player.setXY(pp.x-Game.IMAGES.hull[0].getWidth()/2, pp.y-Game.IMAGES.hull[0].getHeight()/2);
                        ((Tank) player).setHullAngle(pp.hull);
                        ((Tank) player).setTuretAngle(pp.turet);
                        list.add(player);
                    }
                }
            }
        }
        return list;
    }

    protected List<EngineObject> getObjects() {
        return this.object;
    }

    public String getName() {
        return this.data.name;
    }

    protected List<PlayerPosition> getPlayerPositions() {
        return this.data.players;
    }

    protected void clear() {
        for (int x = 0; x < this.data.mapSize.width; x++) {
            for (int y = 0; y < this.data.mapSize.height; y++) {
                this.data.ground[x][y] = null;
            }
        }
        this.object.clear();
    }

}
