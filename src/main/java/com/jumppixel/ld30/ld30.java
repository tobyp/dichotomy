package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * Created by tobyp on 8/23/14.
 */
public class ld30 extends BasicGame implements InputListener {
    Logger logger = Logger.getLogger("ld30");

    float PLAYER_TILES_PER_MS = 0.005f;

    GameContainer gameContainer;

    //RESOURCES
    SpriteSheet meta_sprites;
    SpriteSheet drop_sprites;
    Image dark_overlay;

    //MAP/WORLD
    Map map;
    int objects_group;
    int laser_dev_layer; //for the laser device tiles
    int laser_beam_layer; //for the laser beam tiles

    //PLAYER
    Player player;
    int charge_ms = 0;
    int charge_interval = 100; //+0.01 charge every interval

    //NOTIFICATIONS
    TrueTypeFont font;
    public List<Notification> notification_buffer  = new ArrayList<Notification>();

    public ld30() {
        super("Dichotomy");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {
        this.gameContainer = gameContainer;
        gameContainer.setTargetFrameRate(60);
        gameContainer.getInput().addListener(this);

        font = new TrueTypeFont(new Font("Verdana", 0, 20), false);
        meta_sprites = new SpriteSheet("src/main/resources/meta.png", 24, 24);
        drop_sprites = new SpriteSheet("src/main/resources/drops.png", 24, 24);
        dark_overlay = new Image("src/main/resources/dark_overlay.png");
        map = new Map("src/main/resources/tmx/lazers.tmx");

        objects_group = map.getObjectGroupIndex("objects");
        laser_beam_layer = map.getLayerIndex("laser-beam");
        laser_dev_layer = map.getLayerIndex("laser-dev");

        String[] spawn_location_string = map.getMapProperty("p-spawn", "0,0").split(",");
        vec2 spawn_location = new vec2(Integer.parseInt(spawn_location_string[0]), Integer.parseInt(spawn_location_string[1]));
        player = new Player(spawn_location, new SpriteSheet("src/main/resources/protagonist.png", 24, 48), new vec2(-12,-48), PLAYER_TILES_PER_MS, 4);
        addNotification(new TimedNotification("Controls: WASD to move, E to interact.", 7500, Notification.Type.INFO));

        for (Handler h : logger.getParent().getHandlers()) {
            h.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord logRecord) {
                    return "["+logRecord.getLevel().toString()+"] "+logRecord.getSourceClassName()+"."+logRecord.getSourceMethodName()+": "+logRecord.getMessage()+"\n";
                }
            });
        }

        map.addEntity(new Zombie(player.loc, new SpriteSheet("src/main/resources/zombie.png", 24, 48), new vec2(-12, -14), 4, map));
    }

    @Override
    public void update(GameContainer gameContainer, int delta_ms) throws SlickException {
        if (notification_buffer.size() > 0) {
            for (Notification n : new ArrayList<Notification>(notification_buffer)) {
                int text_width = gameContainer.getGraphics().getFont().getWidth(n.text);

                n.update(delta_ms);

                if (n.dismissed) {
                    n.offset_x = n.offset_x - 5;
                }
                if (gameContainer.getWidth() - n.offset_x - text_width - 72 > gameContainer.getWidth()) {
                    notification_buffer.remove(n);
                }
            }
        }

        map.update(delta_ms);
        player.update(map, delta_ms);
        for (Entity entity : new ArrayList<Entity>(map.entities)) {
            if (entity instanceof Drop) {
                Drop drop = (Drop) entity;
                if (drop.loc.getDistance(player.loc) < 0.5) {
                    drop.pickup(player);
                    map.entities.remove(drop);
                }
            }
        }

        if (player.allow_charging) {
            charge_ms = charge_ms + delta_ms;
            if (charge_ms >= charge_interval) {
                if (player.charge + 0.01f > player.max_charge && player.charge != player.max_charge) {
                    player.charge = player.max_charge;
                } else if (player.charge < player.max_charge) {
                    player.charge = player.charge + 0.01f;
                }
                charge_ms = charge_ms - charge_interval;
            }
        }else{
            charge_ms = 0;
        }

        if (player.charge_holding && player.charge == player.max_charge) {
            if (player.charge_hold + ((float)delta_ms)/1000 > 1) {
                player.charge_hold = 1.0f;
                player.charge_holding = false;
                //TODO: Teleport player
            }else{
                player.charge_hold = player.charge_hold + ((float) delta_ms)/1000;
            }
        }else{
            player.charge_holding = false;
            player.charge_hold = 0;
        }

        checkMovements(gameContainer);

        //TODO entities here!
    }

    public void checkMovements(GameContainer gameContainer) {
        player.setVelocity(vec2.ZERO);
        if (gameContainer.getInput().isKeyDown(Input.KEY_D)) {
            player.setVelocity(player.velocity.add(vec2.RIGHT));
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_A)) {
            player.setVelocity(player.velocity.add(vec2.LEFT));
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_W)) {
            player.setVelocity(player.velocity.add(vec2.UP));
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_S)) {
            player.setVelocity(player.velocity.add(vec2.DOWN));
        }
    }

    @Override
    public void mousePressed(int button, int x, int y) {
        //0 = Left
        //1 = Right
        //2 = Middle (Refrain from use)
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if (button == 0) { //attack

        }
    }

    @Override
    public void keyPressed(int key, char c) {
        switch (key) {
            case Input.KEY_E: {
                vec2 faced_tile = player.loc.getFaced(player.rotation);
                int faced_x = faced_tile.getFloorX();
                int faced_y = faced_tile.getFloorY();
                int faced_oid = map.getTileObject(objects_group, faced_x, faced_y);
                if (faced_oid == -1) return;
                String type = map.getObjectType(objects_group, faced_oid);

                logger.info("Interaction with "+type+"("+map.getObjectName(objects_group, faced_oid)+")@("+Integer.toString(faced_x)+";"+Integer.toString(faced_y)+")");

                if (type.equals("button")) {
                    String state = map.getObjectProperty(objects_group, faced_oid, "state", "false");
                    if (state.equals("true")) {
                        map.setObjectProperty(objects_group, faced_oid, "state", "false");
                        logger.info("\tBUTTON: disabling");
                        executeActions(map.getObjectProperty(objects_group, faced_oid, "disable", ""));
                    } else if (state.equals("false")) {
                        map.setObjectProperty(objects_group, faced_oid, "state", "true");
                        logger.info("\tBUTTON: enabling");
                        executeActions(map.getObjectProperty(objects_group, faced_oid, "enable", ""));
                    }
                }
                else if (type.equals("laser-emitter")) {
                    int beams = Integer.parseInt(map.getObjectProperty(objects_group, faced_oid, "beams", "0000"), 2);
                    laserEmitterToggle(faced_oid, faced_x, faced_y, type, beams);
                }
                else if (type.equals("laser-io") || type.equals("laser-io-inverse") || type.equals("laser-receiver")) {
                    int beams = Integer.parseInt(map.getObjectProperty(objects_group, faced_oid, "beams", "0000"), 2);
                    laserDeviceRotate(faced_oid, faced_x, faced_y, type, beams);
                }
            }
            break;
            case Input.KEY_F11: { //full screen
                try {
                    if (gameContainer.isFullscreen()) gameContainer.setFullscreen(false);
                    else gameContainer.setFullscreen(true);
                    addNotification(new TimedNotification("Toggled fullscreen mode", 2000, Notification.Type.INFO));
                } catch (SlickException e) {
                    e.printStackTrace();
                }
            }
            break;
            case Input.KEY_F3: { //debug
                if (gameContainer.isShowingFPS()) gameContainer.setShowFPS(false);
                else gameContainer.setShowFPS(true);
                addNotification(new TimedNotification("Toggled debug mode", 2000, Notification.Type.INFO));
            }
            break;
            case Input.KEY_ESCAPE: {
                gameContainer.exit();
            }
            break;
            case Input.KEY_LALT:
            case Input.KEY_Q: {
                if (player.charge == player.max_charge && player.has_device) player.charge_holding = true;
            }
            break;
        }
    }

    @Override
    public void keyReleased(int key, char c) {
        switch (key) {
            case Input.KEY_LALT:
            case Input.KEY_Q: {
                player.charge_holding = false;
            }
            break;
        }
    }

    @Override
    public void mouseMoved(int oldx, int oldy, int newx, int newy) {

    }

    public void executeActions(String actions_string) {
        for (String a : actions_string.split(",")) {
            if (a.isEmpty()) continue;

            String[] aparts = a.split(":");
            if (aparts[0].equals("set-tile")) {
                int tile_x = Integer.parseInt(aparts[1]);
                int tile_y = Integer.parseInt(aparts[2]);
                int tile_l = map.getLayerIndex(aparts[3]);
                int tile_id = Integer.parseInt(aparts[4]);
                logger.info("ACTION: set tile ("+Integer.toString(tile_x)+";"+Integer.toString(tile_y)+";"+Integer.toString(tile_l)+") to "+Integer.toString(tile_id));
                map.setTileId(tile_x, tile_y, tile_l, tile_id);
            }
            else if (aparts[0].equals("laser-dev-rotate")) {
                int le_x = Integer.parseInt(aparts[1]);
                int le_y = Integer.parseInt(aparts[2]);

                int oid = map.getTileObject(objects_group, le_x, le_y);
                if (oid != -1) {
                    logger.info("ACTION: rotating "+Integer.toString(oid)+"@("+Integer.toString(le_x)+";"+Integer.toString(le_y)+")");
                    int beams = Integer.parseInt(map.getObjectProperty(objects_group, oid, "beams", "0000"), 2);
                    String type = map.getObjectType(objects_group, oid);
                    laserDeviceRotate(oid, le_x, le_y, type, beams);
                }
            }
            else if (aparts[0].equals("laser-dev-toggle")) {
                int le_x = Integer.parseInt(aparts[1]);
                int le_y = Integer.parseInt(aparts[2]);

                int oid = map.getTileObject(objects_group, le_x, le_y);
                if (oid != -1) {
                    logger.info("ACTION: toggling "+Integer.toString(oid)+"@("+Integer.toString(le_x)+";"+Integer.toString(le_y)+")");
                    int beams = Integer.parseInt(map.getObjectProperty(objects_group, oid, "beams", "0000"), 2);
                    String type = map.getObjectType(objects_group, oid);
                    laserEmitterToggle(oid, le_x, le_y, type, beams);
                }
            }
            else if (aparts[0].equals("notify-timed")) {
                int notify_time = Integer.parseInt(aparts[1]);
                Notification.Type notify_type = Notification.Type.valueOf(aparts[2]);
                addNotification(new TimedNotification(aparts[3], notify_time, notify_type));
            }
            else if (aparts[0].equals("add-drop")) {
                vec2 loc = new vec2(Float.parseFloat(aparts[2]),Float.parseFloat(aparts[3]));
                if (aparts[1].equals("health-1")) {
                    map.addEntity(new HealthDrop(loc, drop_sprites));
                }else
                if (aparts[1].equals("health-2")) {
                    map.addEntity(new HealthDrop(loc, drop_sprites));
                }
            }
            else if (aparts[0].equals("notify")) {
                Notification.Type notify_type = Notification.Type.valueOf(aparts[1]);
                addNotification(new Notification(aparts[2], notify_type));
            }
        }
    }

    int DEVICE_BASE_TILE = 681;
    int BEAM_BASE_TILE = 761;
    int BLOCKER_BASE_TILE = 801;
    int DIODE_BASE_TILE = 841;

    int SIDES[] = new int[]{0x1, 0x2, 0x4, 0x8};

    //update a laser device/block.
    //warning: recursive with exactly ZERO protection
    //beam=0 for just a refresh
    private void laserUpdate(int x, int y, int beam, boolean on) {
        int oid = map.getTileObject(objects_group, x, y);
        if (oid == -1) return;

        String device_type = map.getObjectType(objects_group, oid);
        int old_beams = Integer.parseInt(map.getObjectProperty(objects_group, oid, "beams", "0000"), 2);
        int primaries = Integer.parseInt(map.getObjectProperty(objects_group, oid, "primaries", "0000"), 2);

        int new_primaries = on ? primaries | beam : primaries & ~beam;
        map.setObjectProperty(objects_group, oid, "primaries", Integer.toBinaryString(new_primaries));

        logger.info("LASER "+device_type+"("+map.getObjectName(objects_group, oid)+")@("+Integer.toString(x)+";"+Integer.toString(y)+") primaries="+Integer.toBinaryString(primaries)+"->"+Integer.toBinaryString(new_primaries)+", beams="+Integer.toBinaryString(old_beams));

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
            int output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            String state = map.getObjectProperty(objects_group, oid, "state", "false");
            if (state == "true") {
                new_beams |= output;
            }
            logger.info("\tEMITTER beams->"+Integer.toBinaryString(new_beams));
        }
        else if (device_type.equals("laser-receiver")) {
            int input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            String state = map.getObjectProperty(objects_group, oid, "state", "false");
            if ((new_beams & input) > 0 && state.equals("false")) {
                logger.info("\tRECEIVER enable");
                map.setObjectProperty(objects_group, oid, "state", "true");
                executeActions(map.getObjectProperty(objects_group, oid, "enable", ""));
            }
            else if ((new_beams & input) == 0 && state.equals("true")) {
                executeActions(map.getObjectProperty(objects_group, oid, "disable", ""));
                map.setObjectProperty(objects_group, oid, "state", "false");
                logger.info("\tRECEIVER disable");
            }
            propagate_mask = 0;
        }
        else if (device_type.equals("laser-io")) {
            base_tile = BEAM_BASE_TILE;
            int input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            int output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);

            if ((new_primaries & input) > 0) { //we have a primary input, so let's put in the outputs!
                new_beams |= output;
            }
            logger.info("\tEMITTER beams->"+Integer.toBinaryString(new_beams));
            propagate_mask &= ~beam;
        }
        else if (device_type.equals("laser-io-inverse")) {
            base_tile = BEAM_BASE_TILE;
            int input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            int output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);

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

        map.setObjectProperty(objects_group, oid, "beams", Integer.toBinaryString(new_beams));
        map.setTileId(x, y, laser_beam_layer, base_tile+new_beams);

        for (int side : SIDES) {
            if ((side & new_beams) > 0 && (side & old_beams) == 0 && (side & propagate_mask) > 0) { //it was added
                //we find the complimentary beam, and the space it would be in, and turn that on too via update.
                laserUpdate(x + (side == 0x2 ? -1 : (side == 0x8 ? 1 : 0)), y + (side == 0x1 ? 1 : (side == 0x4 ? -1 : 0)), ((side >> 2) | (side << 2)) & 0xF, true);

            } else if ((side & new_beams) == 0 && (side & old_beams) > 0 && (side & propagate_mask) > 0) { //it was removed
                //we find the complimentary beam, and the space it would be in, and turn that off too via update.
                laserUpdate(x + (side == 0x2 ? -1 : (side == 0x8 ? 1 : 0)), y + (side == 0x1 ? 1 : (side == 0x4 ? -1 : 0)), ((side >> 2) | (side << 2)) & 0xF, false);
            }
        }
    }

    public void laserDeviceRotate(int oid, int x, int y, String device_type, int beams) {
        if (device_type.equals("laser-emitter")) {
            int emitter_output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            emitter_output = ((emitter_output >> 3) | (emitter_output << 1)) & 0xF;
            map.setObjectProperty(objects_group, oid, "output", Integer.toBinaryString(emitter_output));

            logger.info("\trotated emitter, output="+Integer.toBinaryString(emitter_output));
        }
        else if (device_type.equals("laser-receiver")) {
            int receiver_input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            receiver_input = ((receiver_input >> 3) | (receiver_input << 1)) & 0xF;
            map.setObjectProperty(objects_group, oid, "input", Integer.toBinaryString(receiver_input));

            logger.info("\trotated receiver, input="+Integer.toBinaryString(receiver_input));
        }
        else if (device_type.equals("laser-io") || device_type.equals("laser-io-inverse")) {
            int input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            int output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            input = ((input >> 3) | (input << 1)) & 0xF;
            output = ((output >> 3) | (output << 1)) & 0xF;
            map.setObjectProperty(objects_group, oid, "input", Integer.toBinaryString(input));
            map.setObjectProperty(objects_group, oid, "output", Integer.toBinaryString(output));
        }

        int tileid = map.getTileId(x, y, laser_dev_layer);
        int device_base_id = (tileid - DEVICE_BASE_TILE) / 4 * 4;
        int device_variant_id = (tileid - DEVICE_BASE_TILE) % 4;
        int device_rotated_id = DEVICE_BASE_TILE + device_base_id + (device_variant_id + 1) % 4;
        map.setTileId(x, y, laser_dev_layer, device_rotated_id);
        laserUpdate(x, y, 0, true);
    }

    public void laserEmitterToggle(int oid, int x, int y, String device_type, int beams) {
        if (device_type.equals("laser-emitter")) {
            int emitter_output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            boolean state = map.getObjectProperty(objects_group, oid, "state", "false").equals("true") ? true : false;
            logger.info("TOGGLE ("+Integer.toString(x)+";"+Integer.toString(y)+") to "+(state ? "false" : "true"));
            map.setObjectProperty(objects_group, oid, "state", state ? "false" : "true");
            laserUpdate(x, y, emitter_output, !state);
        }
    }

    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        render(true, gameContainer, graphics);
    }

    void render(boolean good, GameContainer gameContainer, Graphics graphics) throws SlickException {
        if (graphics.getFont() != font)
        graphics.setFont(font);

        int tileCountX = (int)Math.floor(gameContainer.getWidth() / 48.f);
        int tileCountY = (int)Math.floor(gameContainer.getHeight() / 48.f);
        vec2 tileOffset = player.loc.add(-tileCountX / 2.f - 0.5f, -tileCountY / 2.f - 0.5f); //half screen to char's foot-center

        int tileOffsetX = tileOffset.getFloorX();
        int tileOffsetY = tileOffset.getFloorY();

        graphics.scale(2, 2);

        int render_offset_x = -(int)((tileOffset.x-tileOffsetX)*24.f);
        int render_offset_y = -(int)((tileOffset.y-tileOffsetY)*24.f);
        int render_tile_w = (gameContainer.getWidth() / 2 + 48) / 24;
        int render_tile_h = (gameContainer.getHeight() / 2 + 48) / 24;

        for (int i=0; i<map.getLayerCount(); i++) {
            String layer_type = map.getLayerProperty(i, "type", "both");
            if (layer_type.equals("both")  || layer_type.equals(good ? "good" : "bad")) {
                map.render(render_offset_x, render_offset_y, tileOffsetX, tileOffsetY, render_tile_w, render_tile_h, i, false);
            }
            else if (layer_type.equals("player")) {
                map.renderEntities(tileOffset, gameContainer, graphics);
                player.render(tileOffset.add(0.f, -1.f), gameContainer, graphics);
            }
            else if (layer_type.equals("mobs")) {
                //TODO
            }
        }

        graphics.scale(.5f, .5f);

        if (!good) dark_overlay.draw(0, 0);

        //GUI

        //Health bar
        player.animations.get(player.rotation.getRotInt()).getImage(0).draw(20, 5, 1.5f, Color.red);
        meta_sprites.getSubImage(19, 1, 20, 2).draw(60, 36, 20*8, 2*8);
        meta_sprites.getSubImage(19, 4, 20, 2).draw(60, 36, Math.round(20*8*player.health/player.max_health), 2*8);

        //Charge bar
        if (player.has_device) {
            int charge_offset_x = 10;
            int charge_offset_y = gameContainer.getHeight() - 78;

            Image teleporter_icon = meta_sprites.getSubImage(40, 7, 14, 17);
            Image charge_empty = meta_sprites.getSubImage(19, 10, 20, 2);
            Image charge_full = meta_sprites.getSubImage(19, 13, 20, 2);
            Image charge_ready = meta_sprites.getSubImage(19, 19, 20, 2);

            teleporter_icon.draw(charge_offset_x, charge_offset_y, 14 * 4, 17 * 4);
            teleporter_icon.draw(charge_offset_x, charge_offset_y, 14 * 4, 17 * 4, new Color(.8f, 0f, .8f, player.charge_hold / 2));

            charge_empty.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 - (2 * 8) / 2, 20 * 8, 2 * 8);
            charge_full.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 - (2 * 8) / 2, Math.round(20 * 8 * player.charge / player.max_charge), 2 * 8);
            charge_ready.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 - (2 * 8) / 2, Math.round(20 * 8 * player.charge_hold / 1), 2 * 8);
        }

        if (notification_buffer.size() > 0) {
            graphics.setColor(Color.black);

            int sprite_offset_y = 10;

            for (Notification n : new ArrayList<Notification>(notification_buffer)) {
                int sprite_offset_x = 0;

                if (n.type == Notification.Type.INFO) sprite_offset_x = 0;
                else if (n.type == Notification.Type.WARNING) sprite_offset_x = 6;
                else if (n.type == Notification.Type.OBJECTIVE) sprite_offset_x = 12;

                int text_width = graphics.getFont().getWidth(n.text);

                meta_sprites.getSubImage(sprite_offset_x, 0, 6, 12).draw(gameContainer.getWidth() - n.offset_x - text_width - 72, sprite_offset_y, 6);
                meta_sprites.getSubImage(sprite_offset_x, 12, 1, 12).draw(gameContainer.getWidth() - n.offset_x - text_width - 36, sprite_offset_y, text_width, 72);
                graphics.drawString(n.text, gameContainer.getWidth() - n.offset_x - text_width - 36, sprite_offset_y + 36 - graphics.getFont().getHeight(n.text) / 2);
                meta_sprites.getSubImage(sprite_offset_x, 24, 6, 12).draw(gameContainer.getWidth() - n.offset_x - 36, sprite_offset_y, 6);

                sprite_offset_y = sprite_offset_y + 82;
            }
        }
    }

    public static void main(String [] args) {
        Logger logger = Logger.getGlobal();
        try {
            AppGameContainer c = new AppGameContainer(new ScalableGame(new ld30(), 800, 600));
            c.setDisplayMode(800, 600, false);
            c.setShowFPS(false);
            c.start();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Game error.", e);
        }
    }

    public void addNotification(Notification notification) {
        notification_buffer.add(notification);
    }
}
