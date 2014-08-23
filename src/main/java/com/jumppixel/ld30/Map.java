package com.jumppixel.ld30;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by tobyp on 8/24/14.
 */
public class Map extends TiledMap {


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
}
