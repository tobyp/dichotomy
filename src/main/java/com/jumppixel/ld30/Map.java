package com.jumppixel.ld30;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.Layer;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by tobyp on 8/24/14.
 */
public class Map extends TiledMap {
    public class MapObject {
        ObjectGroup group;
        GroupObject object;

        public MapObject(ObjectGroup grp, GroupObject obj) {
            this.group = grp;
            this.object = obj;
        }

        public String getProperty(String name, String def) {
            if (object.props == null) return def;
            return object.props.getProperty(name, def);
        }
        public int getPropertyInt(String name, String def) {
            return Integer.parseInt(getProperty(name, def));
        }
        public int getPropertyBeams(String name, String def) {
            return Integer.parseInt(getProperty(name, def), 2);
        }
        public boolean getPropertyBool(String name, String def) {
            return getProperty(name, def).equals("true");
        }
        public void setProperty(String name, String value) {
            if (object.props == null) object.props = new Properties();
            object.props.setProperty(name, value);
        }
        public void setPropertyInt(String name, int value) {
            setProperty(name, Integer.toString(value));
        }
        public void setPropertyBeams(String name, int value) {
            setProperty(name, Integer.toBinaryString(value));
        }
        public void setPropertyBool(String name, boolean value) {
            setProperty(name, value ? "true" : "false");
        }
        public int getX() {
            return (int)Math.floor(object.x / 24.f);
        }
        public int getY() {
            return (int)Math.floor(object.y / 24.f - 1.f);
        }
        public String getName() {
            return object.name;
        }
        public String getType() {
            return object.type;
        }
        public int getGroup() {
            return group.index;
        }
        public String toString() {
            return object.type + (object.name.isEmpty() ? "" : ("('"+object.name+"')")) + "@(" + getX() + ";" + getY() + ")";
        }
    }



    public Map(String ref) throws SlickException {
        super(ref);
    }

    public Map(String ref, boolean loadTileSets) throws SlickException {
        super(ref, loadTileSets);
    }

    public Map(String ref, String tileSetsLocation) throws SlickException {
        super(ref, tileSetsLocation);
    }

    public Map(InputStream in) throws SlickException {
        super(in);
    }

    public Map(InputStream in, String tileSetsLocation) throws SlickException {
        super(in, tileSetsLocation);
    }

    public MapObject getObject(int groupID, vec2 pos) {
        return  getObject(groupID, pos.getFloorX(), pos.getFloorY());
    }
    public MapObject getObject(int groupID, int x, int y) {
        x *= 24;
        y = (y+1) * 24; //some reason, object coords are bottom left
        ObjectGroup group = (ObjectGroup) objectGroups.get(groupID);

        if (group != null) {
            for (Object go : group.objects) {
                GroupObject o = (GroupObject)go;

                if (o.x == x && o.y == y) {
                    return new MapObject(group, o);
                }
            }
        }
        return null;
    }
    public MapObject getObject(int groupID, int objectID) {
        if (groupID >= 0 && groupID < objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup) objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject) grp.objects.get(objectID);
                return new MapObject(grp, object);
            }
        }
        return null;
    }

    public String getLayerName(int layerIndex) {
        Layer layer = (Layer) layers.get(layerIndex);
        if (layer == null) return null;
        return layer.name;
    }

    public int getObjectGroupIndex(String name) {
        int idx = 0;

        for (int i=0;i<objectGroups.size();i++) {
            ObjectGroup group = (ObjectGroup) objectGroups.get(i);

            if (group.name.equals(name)) {
                return i;
            }
        }

        return -1;
    }
}
