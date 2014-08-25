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

/**
 * Created by tobyp on 8/24/14.
 */
public class Map extends TiledMap implements TileBasedMap {
    private int walk_layer_index;
    public int WALKABLE_BASE = 881;

    public List<Entity> entities = new ArrayList<Entity>();

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

    public int getTileObject(int groupID, int x, int y) {
        x *= 24;
        y = (y+1) * 24; //some reason, object coords are bottom left
        ObjectGroup group = (ObjectGroup) objectGroups.get(groupID);
        if (group == null) return -1;

        for (Object go : group.objects) {
            GroupObject o = (GroupObject)go;

            if (o.x == x && o.y == y) {
                return o.index;
            }
        }
        return -1;
    }

    public int getObjectTileX(int gid, int oid) {
        ObjectGroup group = (ObjectGroup) objectGroups.get(gid);
        if (group == null) return -1;

        for (Object go : group.objects) {
            GroupObject o = (GroupObject) go;
            if (o.index == oid) return o.x / 24;
        }

        return -1;
    }

    public int getObjectTileY(int gid, int oid) {
        ObjectGroup group = (ObjectGroup) objectGroups.get(gid);
        if (group == null) return -1;

        for (Object go : group.objects) {
            GroupObject o = (GroupObject) go;
            if (o.index == oid) return o.y / 24 - 1;
        }

        return -1;
    }

    public boolean walkable(vec2 from, vec2 to) {
        int tile_id = getTileId(to.getFloorX(), to.getFloorY(), walk_layer_index) - WALKABLE_BASE;
        if (tile_id < 0) tile_id = 0x0; //completely walkable
        int needed = to.sub(from).walk_dirs();
        //Logger.getGlobal().info("needed " + Integer.toBinaryString(needed) + " | " + Integer.toBinaryString(~tile_id) + " got");
        if ((~tile_id & needed) == needed) {
            return true;
        }
        return false;
    }

    public void reset() {
        for (Entity entity : entities) {
            entity.expire();
        }
        entities.clear();
    }

    public void update(int delta_ms) {
        for (Entity entity : new ArrayList<Entity>(entities)) {
            if (entity instanceof Drop) {
                Drop drop = (Drop) entity;
                if (drop.expire_ms - delta_ms <= 0) {
                    drop.expire();
                    entities.remove(drop);
                }else{
                    drop.expire_ms = drop.expire_ms - delta_ms;
                    drop.update(this, delta_ms);
                }
            }
        }
    }

    public void renderEntities(vec2 view_offset, GameContainer gameContainer, Graphics graphics) {
        for (Entity entity : entities) {
            entity.render(view_offset, gameContainer, graphics);
        }
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        entity.spawn();
    }

    //Pathfinder stuff

    @Override
    public int getWidthInTiles() {
        return getWidth();
    }

    @Override
    public int getHeightInTiles() {
        return getHeight();
    }

    @Override
    public void pathFinderVisited(int x, int y) {

    }

    @Override
    public boolean blocked(PathFindingContext context, int tx, int ty) {
        return (!(getTileId(tx, ty, walk_layer_index) == WALKABLE_BASE));
    }

    @Override
    public float getCost(PathFindingContext context, int tx, int ty) {
        if (getTileId(tx, ty, walk_layer_index) == WALKABLE_BASE) return 0;
        return 1;
    }
}
