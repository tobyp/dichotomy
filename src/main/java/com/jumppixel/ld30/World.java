package com.jumppixel.ld30;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.PathFindingContext;
import org.newdawn.slick.util.pathfinding.TileBasedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by tobyp on 8/25/14.
 */
public class World implements TileBasedMap {
    ld30 game;
    Logger logger;
    String name;
    Map map;
    int walkability_layer;
    int ldevs; //laser device layer
    int lbeams; //laser beam layer
    int ogroup; //object group
    public List<Entity> entities = new ArrayList<Entity>();

    public World(ld30 game, Map map, String name) {
        this.game = game;
        this.logger = Logger.getLogger(name);
        this.name = name;
        this.map = map;
        walkability_layer = map.getLayerIndex(name+"-walkability");
        ldevs = map.getLayerIndex(name+"-laser-dev");
        lbeams = map.getLayerIndex(name+"-laser-beam");
        ogroup = map.getObjectGroupIndex(name+"-objects");
    }

    int DEVICE_BASE_TILE = 681;
    int BEAM_BASE_TILE = 761;
    int BLOCKER_BASE_TILE = 801;
    int DIODE_BASE_TILE = 841;

    int SIDES[] = new int[]{0x1, 0x2, 0x4, 0x8};

    //update a laser device/block.
    //warning: recursive with exactly ZERO protection
    //beam=0 for just a refresh
    private void laserUpdate(Map.MapObject object, int beam, boolean on) {
        if (object == null) return; //TODO auto-add laser-beam objects where needed

        String device_type = object.getType();
        int old_beams = object.getPropertyBeams("beams", "0000");
        int primaries = object.getPropertyBeams("primaries", "0000");

        int new_primaries = on ? primaries | beam : primaries & ~beam;
        object.setPropertyBeams("primaries", new_primaries);

        logger.info("LASER "+object.toString()+" primaries="+Integer.toBinaryString(primaries)+"->"+Integer.toBinaryString(new_primaries)+", beams="+Integer.toBinaryString(old_beams));

        int base_tile = BEAM_BASE_TILE;
        int propagate_mask = 0xF;

        //primaries -> beams here
        int new_beams = new_primaries;
        if (device_type.equals("laser-beam")) {
            new_beams = ((new_primaries & (0x1 | 0x4)) > 0 ? (0x1 | 0x4) : 0) | ((new_primaries & (0x2 | 0x8)) > 0 ? (0x2 | 0x8) : 0);
            logger.info("\tBEAM beams->"+Integer.toBinaryString(new_beams));
            propagate_mask &= ~beam;
        }
        else if (device_type.equals("laser-emitter")) {
            int output = object.getPropertyBeams("output", "0000");
            boolean state = object.getPropertyBool("state", "false");
            if (state) {
                new_beams |= output;
            }
            logger.info("\tEMITTER beams->"+Integer.toBinaryString(new_beams));
        }
        else if (device_type.equals("laser-receiver")) {
            int input = object.getPropertyBeams("input", "0000");
            boolean state = object.getPropertyBool("state", "false");
            if ((new_beams & input) > 0 && !state) {
                logger.info("\tRECEIVER enable");
                object.setPropertyBool("state", true);
                game.executeActions(object.getProperty("enable", ""));
            }
            else if ((new_beams & input) == 0 && state) {
                logger.info("\tRECEIVER disable");
                object.setPropertyBool("state", false);
                game.executeActions(object.getProperty("disable", ""));
            }
            propagate_mask = 0;
        }
        else if (device_type.equals("laser-io")) {
            base_tile = BEAM_BASE_TILE;
            int input = object.getPropertyBeams("input", "0000");
            int output = object.getPropertyBeams("output", "0000");

            if ((new_primaries & input) > 0) { //we have a primary input, so let's put in the outputs!
                new_beams |= output;
            }
            logger.info("\tEMITTER beams->"+Integer.toBinaryString(new_beams));
            propagate_mask &= ~beam;
        }
        else if (device_type.equals("laser-io-inverse")) {
            base_tile = BEAM_BASE_TILE;
            int input = object.getPropertyBeams("input", "0000");
            int output = object.getPropertyBeams("output", "0000");

            if ((new_primaries & input) == 0) { //we have no primary input, so let's put in the outputs!
                new_beams |= output;
            }
            logger.info("\tEMITTER beams->"+Integer.toBinaryString(new_beams));
            propagate_mask &= ~beam;
        }
        else if (device_type.equals("laser-blocker")) {
            base_tile = BLOCKER_BASE_TILE;
            propagate_mask = 0;
        }
        else if (device_type.equals("laser-portal")) {
            base_tile = BLOCKER_BASE_TILE;
            propagate_mask = 0;

        }

        object.setPropertyBeams("beams", new_beams);
        map.setTileId(object.getX(), object.getY(), lbeams, base_tile+new_beams);

        for (int side : SIDES) {
            if ((side & new_beams) > 0 && (side & old_beams) == 0 && (side & propagate_mask) > 0) { //it was added
                //we find the complimentary beam, and the space it would be in, and turn that on too via update.
                laserUpdate(object.getGroup(), object.getX() + (side == 0x2 ? -1 : (side == 0x8 ? 1 : 0)), object.getY() + (side == 0x1 ? 1 : (side == 0x4 ? -1 : 0)), ((side >> 2) | (side << 2)) & 0xF, true);

            } else if ((side & new_beams) == 0 && (side & old_beams) > 0 && (side & propagate_mask) > 0) { //it was removed
                //we find the complimentary beam, and the space it would be in, and turn that off too via update.
                laserUpdate(object.getGroup(), object.getX() + (side == 0x2 ? -1 : (side == 0x8 ? 1 : 0)), object.getY() + (side == 0x1 ? 1 : (side == 0x4 ? -1 : 0)), ((side >> 2) | (side << 2)) & 0xF, false);
            }
        }
    }

    private void laserUpdate(int groupID, int x, int y, int beam, boolean on) {
        Map.MapObject propagate_to = map.getObject(groupID, x, y);
        //logger.info("propagating to "+propagate_to.toString());
        laserUpdate(propagate_to, beam, on);
    }

    public void laserDeviceRotate(Map.MapObject object) {
        int beams = object.getPropertyBeams("beams", "0000");
        String device_type = object.getType();
        if (device_type.equals("laser-emitter")) {
            int emitter_output = object.getPropertyBeams("output", "0000");
            emitter_output = ((emitter_output >> 3) | (emitter_output << 1)) & 0xF;
            object.setPropertyBeams("output", emitter_output);

            logger.info("\trotated emitter, output="+Integer.toBinaryString(emitter_output));
        }
        else if (device_type.equals("laser-receiver")) {
            int receiver_input = object.getPropertyBeams("input", "0000");
            receiver_input = ((receiver_input >> 3) | (receiver_input << 1)) & 0xF;
            object.setPropertyBeams("input", receiver_input);

            logger.info("\trotated receiver, input="+Integer.toBinaryString(receiver_input));
        }
        else if (device_type.equals("laser-io") || device_type.equals("laser-io-inverse")) {
            int input = object.getPropertyBeams("input", "0000");
            int output = object.getPropertyBeams("output", "0000");
            input = ((input >> 3) | (input << 1)) & 0xF;
            output = ((output >> 3) | (output << 1)) & 0xF;
            object.setPropertyBeams("input", input);
            object.setPropertyBeams("output", output);
        }

        int tileid = map.getTileId(object.getX(), object.getY(), ldevs);
        int device_base_id = (tileid - DEVICE_BASE_TILE) / 4 * 4;
        int device_variant_id = (tileid - DEVICE_BASE_TILE) % 4;
        int device_rotated_id = DEVICE_BASE_TILE + device_base_id + (device_variant_id + 1) % 4;
        map.setTileId(object.getX(), object.getY(), ldevs, device_rotated_id);
        laserUpdate(object, 0, true);
    }

    public void laserEmitterToggle(Map.MapObject object) {

        int beams = object.getPropertyBeams("beams", "0000");
        if (object.getType().equals("laser-emitter")) {
            int emitter_output = object.getPropertyBeams("output", "0000");
            boolean state = object.getPropertyBool("state", "false");
            logger.info("TOGGLE "+object.toString()+" to "+ !state);
            object.setPropertyBool("state", !state);
            laserUpdate(object, emitter_output, !state);
        }
    }

    public int WALKABLE_BASE = 881;

    public boolean walkable(vec2 from, vec2 to) {
        int tile_id = map.getTileId(to.getFloorX(), to.getFloorY(), walkability_layer) - WALKABLE_BASE;
        if (tile_id < 0) tile_id = 0x0; //completely walkable
        int needed = to.sub(from).walk_dirs();
        //Logger.getGlobal().info("needed " + Integer.toBinaryString(needed) + " | " + Integer.toBinaryString(~tile_id) + " got");
        if ((~tile_id & needed) == needed) {
            return true;
        }
        return false;
    }

    public void update(Player player, int delta_ms) {
        for (Entity entity : new ArrayList<Entity>(entities)) {
            entity.update(player, this, delta_ms);
        }
    }

    public void reset() {
        for (Entity entity : entities) {
            entity.expire();
        }
        entities.clear();
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

    @Override
    public int getWidthInTiles() {
        return map.getWidth();
    }

    @Override
    public int getHeightInTiles() {
        return map.getHeight();
    }

    @Override
    public void pathFinderVisited(int x, int y) {

    }

    @Override
    public boolean blocked(PathFindingContext context, int tx, int ty) {
        int tile_id = map.getTileId(tx, ty, walkability_layer);
        if (tile_id < WALKABLE_BASE) tile_id = WALKABLE_BASE;
        //Logger.getLogger("AI").info("Tile at ("+Integer.toString(tx)+";"+Integer.toString(ty)+") is "+Integer.toString(tile_id)+" ("+(tile_id==WALKABLE_BASE ? "free" : "blocked")+")");
        return (tile_id != WALKABLE_BASE);
    }

    @Override
    public float getCost(PathFindingContext context, int tx, int ty) {
        return 1.f;
        //return (getTileId(tx, ty, walk_layer_index) == WALKABLE_BASE ? 0 : 1);
    }
}
