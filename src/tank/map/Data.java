/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.map;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import tank.engine.EngineObject;
import tank.engine.Tools;
import tank.objects.Ground;

/**
 *
 * @author Krcma
 */
public class Data implements Serializable {

    protected final String name;
    protected final Dimension mapSize;
    protected final Ground[][] ground;
    protected final List<PlayerPosition> players;
    protected final List<Object[]> obj;

    public Data(String _name, Dimension _mapSize) {
        this.name = _name;
        this.players = new ArrayList<>();
        this.obj = new ArrayList<>();
        this.mapSize = _mapSize;
        this.ground = new Ground[_mapSize.width][_mapSize.height];
    }

    public void save(File file, List<EngineObject> objects) throws FileNotFoundException, IOException {
        objects.forEach((eo) -> {
            obj.add(eo.getConfig());
        });
        FileOutputStream f = new FileOutputStream(file.toString() + ".map");
        ObjectOutputStream o = new ObjectOutputStream(f);
        o.writeObject(this);
        o.flush();
        o.close();
    }

    public void buildObjectList(List<EngineObject> list) {
        obj.forEach((o) -> {
            try {
                list.add(Tools.createEngineObjectFromConfig(o));
            } catch (Exception ex) {
            }
        });
    }

}
