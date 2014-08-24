package com.jumppixel.ld30;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.Layer;
import org.newdawn.slick.tiled.TiledMap;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by tobyp on 8/24/14.
 */
public class Map extends TiledMap {
    private int walk_layer_index;

    public Map(String ref) throws SlickException {
        super(ref);
        walk_layer_index = getLayerIndex("walkability");
    }

    public Map(String ref, boolean loadTileSets) throws SlickException {
        super(ref, loadTileSets);
        walk_layer_index = getLayerIndex("walkability");
    }

    public Map(String ref, String tileSetsLocation) throws SlickException {
        super(ref, tileSetsLocation);
        walk_layer_index = getLayerIndex("walkability");
    }

    public Map(InputStream in) throws SlickException {
        super(in);
        walk_layer_index = getLayerIndex("walkability");
    }

    public Map(InputStream in, String tileSetsLocation) throws SlickException {
        super(in, tileSetsLocation);
        walk_layer_index = getLayerIndex("walkability");
    }

    public void setObjectProperty(int groupID, int objectID, String propertyName, String value) {
        if (groupID >= 0 && groupID < objectGroups.size()) {
            ObjectGroup grp = (ObjectGroup) objectGroups.get(groupID);
            if (objectID >= 0 && objectID < grp.objects.size()) {
                GroupObject object = (GroupObject) grp.objects.get(objectID);

                if (object == null) {
                    return;
                }
                if (object.props == null) {
                    object.props = new Properties();
                }

                object.props.setProperty(propertyName, value);
            }
        }
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

    public boolean walkable(vec2 from, vec2 to) {
        int tile_id = getTileId(to.getFloorX(), to.getFloorY(), walk_layer_index);
        if (tile_id != 61) { //TODO directional walkability
            return false;
        }
        return true;
    }
}
