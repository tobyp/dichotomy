package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.Log;

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
        map = new Map("src/main/resources/tmx/lasersWithAnS.tmx");
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
        for (Drop drop : new ArrayList<Drop>(map.drops)) {
            if (drop.loc.getMagnitude(player.loc) < 0.5) {
                drop.pickup(player);
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
                charge_ms = 0;
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
                        executeActions(map.getObjectProperty(objects_group, faced_oid, "disable", ""));
                    } else if (state.equals("false")) {
                        map.setObjectProperty(objects_group, faced_oid, "state", "true");
                        executeActions(map.getObjectProperty(objects_group, faced_oid, "enable", ""));
                    }
                }
                else if (type.equals("laser-emitter")) {
                    int beams = Integer.parseInt(map.getObjectProperty(objects_group, faced_oid, "beams", "0000"), 2);
                    laserEmitterToggle(faced_oid, faced_x, faced_y, type, beams);
                }
                else if (type.equals("laser-io") || type.equals("laser-receiver")) {
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
                map.setTileId(Integer.parseInt(aparts[1]), Integer.parseInt(aparts[2]), map.getLayerIndex(aparts[3]), Integer.parseInt(aparts[4]));
            }
            else if (aparts[0].equals("laser-dev-rotate")) {

            }
            else if (aparts[0].equals("laser-emitter-toggle")) {

            }
            else if (aparts[0].equals("notify-timed")) {
                int notify_time = Integer.parseInt(aparts[1]);
                Notification.Type notify_type = Notification.Type.valueOf(aparts[2]);
                addNotification(new TimedNotification(aparts[3], notify_time, notify_type));
            }
            else if (aparts[0].equals("add-drop")) {
                vec2 loc = new vec2(Integer.parseInt(aparts[2]),Integer.parseInt(aparts[3]));
                if (aparts[1].equals("health-1")) {
                    map.addDrop(new HealthDrop(loc, drop_sprites));
                }else
                if (aparts[1].equals("health-2")) {
                    map.addDrop(new HealthDrop(loc, drop_sprites));
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

    private static int complementBeam(int beam) {
        return ((beam >> 2) | (beam << 2)) & 0xF;
    }

    int setBeams(int oid, int beams) {
        map.setObjectProperty(objects_group, oid, "beams", Integer.toBinaryString(beams));
        return beams;
    }

    //a south beam will get itself applied to x,y-1 because that's north, and there the beam enters from the south
    public void laserUpdateTowards(int x, int y, int beam, boolean on) {
        x += beam == 0x2 ? 1 : (beam == 0x8 ? -1 : 0);
        y += beam == 0x1 ? -1 : (beam == 0x4 ? 1 : 0);
        laserUpdate(x, y, beam, on);
    }

    //update a laser device/block.
    //warning: recursive with exactly ZERO protection
    public void laserUpdate(int x, int y, int beam, boolean on) {
        int oid = map.getTileObject(objects_group, x, y);
        if (oid == -1) return;
        String device_type = map.getObjectType(objects_group, oid);
        int old_beams = Integer.parseInt(map.getObjectProperty(objects_group, oid, "beams", "0000"), 2);
        int new_beams = on ? old_beams | beam : old_beams & ~beam;
        if (new_beams == old_beams) return;

        setBeams(oid, new_beams);

        logger.info("LASER update " + device_type + " '" + map.getObjectName(objects_group, oid) + "'@("+Integer.toString(x)+";"+Integer.toString(y)+")[turning " + Integer.toBinaryString(beam) + " " + (on ? "on" : "off") + "]: beams " +Integer.toBinaryString(old_beams)+"->"+Integer.toBinaryString(new_beams));

        int base_tile = BEAM_BASE_TILE;
        if (device_type.equals("laser-beam")) {
            //a normal beam - for any component changed, also change the complementary one - north/south or east/west, and propagate
            int complement_beam = complementBeam(beam);

            new_beams = setBeams(oid, on ? new_beams | complement_beam : new_beams & ~complement_beam); //update the complimentary one as well
            logger.info("\tBEAM: complement="+Integer.toBinaryString(complement_beam)+", ultimate="+Integer.toBinaryString(new_beams));
            laserUpdateTowards(x, y, beam, on);
        }
        else if (device_type.equals("laser-emitter")) {
            int emitter_output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            //a beam was turned on/off on an emitter. we need to propagate the laser changes to the beams appear/disappear
            if (emitter_output == beam) { //check it's actually the beam in the direction this device "fires"
                laserUpdateTowards(x, y, complementBeam(beam), on);
            }
        }
        else if (device_type.equals("laser-receiver")) {
            int receiver_input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            if (receiver_input == beam) {
                if (on) {
                    executeActions(map.getObjectProperty(objects_group, oid, "enable", "")); //AC-TI-VATE!
                }
                else {
                    executeActions(map.getObjectProperty(objects_group, oid, "disable", ""));
                }
            }
        }
        else if (device_type.equals("laser-io")) {
            int input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            int output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            int primaries = Integer.parseInt(map.getObjectProperty(objects_group, oid, "primaries", "0000"), 2);

            int old_primaries = primaries;
            primaries = on ? primaries | beam : primaries & ~beam;

            map.setObjectProperty(objects_group, oid, "primaries", Integer.toBinaryString(primaries));

            if ((primaries & input) > 0) { //we have a primary input, so let's put in the outputs!
                new_beams = setBeams(oid, new_beams | output);
            }
            else { //we have no primary inputs, remove all non-primary outputs!
                new_beams = setBeams(oid, new_beams & ~output | primaries);
            }

            logger.info("\tIOlaser. "+Integer.toString(input)+"->" + Integer.toBinaryString(output) + " primaries="+Integer.toBinaryString(old_primaries)+"->"+Integer.toBinaryString(primaries)+", beams=->"+Integer.toBinaryString(new_beams));

            for (int side : SIDES) {
                if ((side & new_beams) > 0 && (side & old_beams) == 0 && side != beam) { //it was added
                    laserUpdateTowards(x, y, complementBeam(side), true);
                } else if ((side & new_beams) == 0 && (side & old_beams) > 0 && side != beam) { //it was removed
                    laserUpdateTowards(x, y, complementBeam(side), false);
                }
            }
        }
        else if (device_type.equals("laser-blocker")) {
            base_tile = BLOCKER_BASE_TILE;
            //the buck stops here.
        }

        map.setTileId(x, y, laser_beam_layer, base_tile+new_beams);
    }

    private int rotateBeams(int beams) {
        return ((beams >> 3) | (beams << 1)) & 0xF;
    }

    public int laserDeviceTileRotate(int tileid) {
        int device_base_id = (tileid - DEVICE_BASE_TILE) / 4 * 4;
        int device_variant_id = (tileid - DEVICE_BASE_TILE) % 4;
        int device_rotated_id = DEVICE_BASE_TILE + device_base_id + (device_variant_id + 1) % 4;
        return device_rotated_id;
    }

    public void laserDeviceRotate(int oid, int x, int y, String device_type, int beams) {
        if (device_type.equals("laser-emitter")) {
            int emitter_output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            boolean was_on = (beams & emitter_output) > 0;
            logger.info("\trotating emitter, beams="+Integer.toBinaryString(beams)+", output="+Integer.toBinaryString(emitter_output));
            if (was_on) {
                laserUpdate(x, y, emitter_output, false);
            }
            //ROTATIONS START
            emitter_output = rotateBeams(emitter_output);
            map.setObjectProperty(objects_group, oid, "output", Integer.toBinaryString(emitter_output));
            map.setTileId(x, y, laser_dev_layer, laserDeviceTileRotate(map.getTileId(x, y, laser_dev_layer)));
            //ROTATIONS END
            if (was_on) {
                laserUpdate(x, y, emitter_output, true);
            }
            logger.info("\trotated emitter, beams="+Integer.toBinaryString(Integer.parseInt(map.getObjectProperty(objects_group, oid, "beams", "0000"), 2))+", output="+Integer.toBinaryString(emitter_output));
        }
        else if (device_type.equals("laser-receiver")) {
            int receiver_input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            boolean was_on = (beams & receiver_input) > 0;
            //ROTATIONS START
            receiver_input = rotateBeams(receiver_input);
            map.setObjectProperty(objects_group, oid, "input", Integer.toBinaryString(receiver_input));
            map.setTileId(x, y, laser_dev_layer, laserDeviceTileRotate(map.getTileId(x, y, laser_dev_layer)));
            //ROTATIONS END
            boolean now_on = (beams & receiver_input) > 0;
            if (now_on != was_on) {
                if (now_on) {
                    executeActions(map.getObjectProperty(objects_group, oid, "enable", ""));
                }
                else {
                    executeActions(map.getObjectProperty(objects_group, oid, "disable", ""));
                }
            }
        }
        else if (device_type.equals("laser-io")) {
            int input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            int output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            int primaries = Integer.parseInt(map.getObjectProperty(objects_group, oid, "primaries", "0000"), 2);

            //ROTATIONS START
            input = rotateBeams(input);
            output = rotateBeams(output);
            map.setObjectProperty(objects_group, oid, "input", Integer.toBinaryString(input));
            map.setObjectProperty(objects_group, oid, "output", Integer.toBinaryString(output));
            map.setTileId(x, y, laser_dev_layer, laserDeviceTileRotate(map.getTileId(x, y, laser_dev_layer)));
            //ROTATIONS END

            int new_beams = setBeams(oid, (primaries & input) > 0 ? primaries | output : primaries);

            logger.info("Rotating IO "+Integer.toString(input)+"->"+Integer.toBinaryString(output)+" primaries="+Integer.toBinaryString(primaries)+", beams="+Integer.toBinaryString(beams)+"->"+Integer.toBinaryString(new_beams));

            for (int side : SIDES) {
                if ((side & new_beams) > 0 && (side & beams) == 0) { //it was added
                    laserUpdateTowards(x, y, complementBeam(side), true);
                } else if ((side & new_beams) == 0 && (side & beams) > 0) { //it was removed
                    laserUpdateTowards(x, y, complementBeam(side), false);
                }
            }

            map.setTileId(x, y, laser_beam_layer, BEAM_BASE_TILE+new_beams);
        }
    }

    public void laserEmitterToggle(int oid, int x, int y, String device_type, int beams) {
        if (device_type.equals("laser-emitter")) {
            int emitter_output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            logger.info("\tlaser-emitter beams="+Integer.toBinaryString(beams)+"; output="+Integer.toBinaryString(emitter_output));
            if ((beams & emitter_output) > 0) {
                laserUpdate(x, y, emitter_output, false);
            }
            else {
                laserUpdate(x, y, emitter_output, true);
            }
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
