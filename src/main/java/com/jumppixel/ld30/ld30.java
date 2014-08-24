package com.jumppixel.ld30;

import com.sun.org.apache.bcel.internal.generic.LAND;
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

    //MAP/WORLD
    Map map;
    int objects_group;
    int laser_dev_layer; //for the laser device tiles
    int laser_beam_layer; //for the laser beam tiles

    //PLAYER
    Player player;

    //NOTIFICATIONS
    TrueTypeFont font;
    public List<Notification> notification_buffer  = new ArrayList<Notification>();

    public ld30() {
        super("Ludum Dare 30");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {
        this.gameContainer = gameContainer;
        gameContainer.setTargetFrameRate(60);
        gameContainer.getInput().addListener(this);

        font = new TrueTypeFont(new Font("Verdana", 0, 20), false);
        meta_sprites = new SpriteSheet("src/main/resources/meta.png", 24, 24);
        map = new Map("src/main/resources/tmx/lazers.tmx");
        objects_group = map.getObjectGroupIndex("objects");
        laser_beam_layer = map.getLayerIndex("laser-beam");
        laser_dev_layer = map.getLayerIndex("laser-dev");

        String[] spawn_location_string = map.getMapProperty("p-spawn", "0,0").split(",");
        vec2 spawn_location = new vec2(Integer.parseInt(spawn_location_string[0]), Integer.parseInt(spawn_location_string[1]));
        player = new Player(spawn_location, new SpriteSheet("src/main/resources/zombie.png", 24, 48), new vec2(-12,-48), PLAYER_TILES_PER_MS, 4);        addNotification(new TimedNotification("Controls: WASD to move, E to interact.", 6000, Notification.Type.INFO));
        addNotification(new Notification("Objective: Explore!", Notification.Type.OBJECTIVE));

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

        player.update(map, delta_ms);

        //TODO entities here!
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
                int faced = getFacedObject(objects_group);
                if (faced == -1) return;
                String type = map.getObjectType(objects_group, faced);
                if (type.equals("button")) {
                    String state = map.getObjectProperty(objects_group, faced, "state", "false");
                    if (state.equals("true")) {
                        map.setObjectProperty(objects_group, faced, "state", "false");
                        executeActions(map.getObjectProperty(objects_group, faced, "disable", ""));
                    } else if (state.equals("false")) {
                        map.setObjectProperty(objects_group, faced, "state", "true");
                        executeActions(map.getObjectProperty(objects_group, faced, "enable", ""));
                    }
                }
                else if (type.startsWith("laser-")) {
                    laserDeviceInteract(map.getObjectTileX(objects_group, faced), map.getObjectTileY(objects_group, faced), faced);
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
            case Input.KEY_D: {
                player.setVelocity(player.velocity.add(vec2.RIGHT));
            }
            break;
            case Input.KEY_A: {
                player.setVelocity(player.velocity.add(vec2.LEFT));
            }
            break;
            case Input.KEY_S: {
                player.setVelocity(player.velocity.add(vec2.DOWN));
            }
            break;
            case Input.KEY_W: {
                player.setVelocity(player.velocity.add(vec2.UP));
            }
            break;
            case Input.KEY_ESCAPE: {
                gameContainer.exit();
            }
            break;
        }
    }

    @Override
    public void keyReleased(int key, char c) {
        switch (key) {
            case Input.KEY_D: {
                player.setVelocity(player.velocity.sub(vec2.RIGHT));
            }
            break;
            case Input.KEY_A: {
                player.setVelocity(player.velocity.sub(vec2.LEFT));
            }
            break;
            case Input.KEY_S: {
                player.setVelocity(player.velocity.sub(vec2.DOWN));
            }
            break;
            case Input.KEY_W: {
                player.setVelocity(player.velocity.sub(vec2.UP));
            }
        }
    }

    @Override
    public void mouseMoved(int oldx, int oldy, int newx, int newy) {

    }

    public int getFacedObject(int group) {
        vec2 faced = player.loc.getFaced(player.rotation).mul(24.f);

        //logger.info(faced.toString());

        int faced_x = faced.getFloorX();
        int faced_y = faced.getFloorY();

        for (int oid = 0; oid < map.getObjectCount(group); oid++) {
            //logger.info(">> " + Float.toString(map.getObjectX(group, oid)) + ";" + Float.toString(map.getObjectY(group, oid)));
            if (Math.floor(map.getObjectX(group, oid)) != faced_x) continue;
            if (Math.floor(map.getObjectY(group, oid)) != faced_y) continue;
            return oid;
        }
        return -1;
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
            else if (aparts[0].equals("notify")) {
                Notification.Type notify_type = Notification.Type.valueOf(aparts[1]);
                addNotification(new Notification(aparts[2], notify_type));
            }
        }
    }

    int BEAM_BASE_TILE = 761;
    int BLOCKER_BASE_TILE = 801;
    int DIODE_BASE_TILE = 841;

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
            logger.info("\tComplement: "+Integer.toBinaryString(complement_beam)+"; ultimate beam "+Integer.toBinaryString(new_beams));
            laserUpdateTowards(x, y, beam, on);
        }
        else if (device_type.equals("laser-diode")) {
            base_tile = DIODE_BASE_TILE;
            int diode_input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            if (diode_input == beam) { //exactly like a normal beam, except with this extra condition
                int complement_beam = complementBeam(beam);
                new_beams = setBeams(oid, on ? new_beams | complement_beam : new_beams & ~complement_beam);
                laserUpdateTowards(x, y, beam, on);
            }
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
        else if (device_type.equals("laser-mirror")) {
            int mirror_io = Integer.parseInt(map.getObjectProperty(objects_group, oid, "io", "0000"), 2);
            if ((mirror_io & beam) > 0) {
                new_beams = setBeams(oid, new_beams | mirror_io);
                for (int i=0; i<4; i++) {
                    int b = 0x1 << i;
                    if ((new_beams & b) > 0 && b != beam) {
                        laserUpdateTowards(x, y, b, on);
                    }
                }
            }
        }
        else if (device_type.equals("laser-prism")) {
            int prism_input = Integer.parseInt(map.getObjectProperty(objects_group, oid, "input", "0000"), 2);
            int prism_output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            if (prism_input == beam) {
                new_beams = setBeams(oid, on ? new_beams | prism_output : new_beams & ~prism_output | prism_input);
                for (int i=0; i<4; i++) {
                    int b = 0x1 << i;
                    if ((new_beams & b) > 0 && b != beam) {
                        laserUpdateTowards(x, y, b, on);
                    }
                }
            }
        }
        else if (device_type.equals("laser-blocker")) {
            base_tile = BLOCKER_BASE_TILE;
            //the buck stops here.
        }

        map.setTileId(x, y, laser_beam_layer, base_tile+new_beams);
    }

    public void laserDeviceInteract(int x, int y, int oid) {
        int beams = Integer.parseInt(map.getObjectProperty(objects_group, oid, "beams", "0000"), 2);
        String device_type = map.getObjectType(objects_group, oid);
        logger.info("Interacting with " + device_type + " '" + map.getObjectName(objects_group, oid) + "'@("+Integer.toString(x)+";"+Integer.toString(y)+") - beams were " + Integer.toBinaryString(beams));
        if (device_type.equals("laser-emitter")) {
            int emitter_output = Integer.parseInt(map.getObjectProperty(objects_group, oid, "output", "0000"), 2);
            logger.info("\tlaser-emitter output="+Integer.toBinaryString(emitter_output));
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
                player.render(tileOffset.add(0.f, -1.f), gameContainer, graphics);
            }
            else if (layer_type.equals("mobs")) {
                //TODO
            }
        }

        graphics.scale(.5f, .5f);

        if (notification_buffer.size() > 0) {
            graphics.setColor(Color.black);

            int offset_y = 10;

            for (Notification n : new ArrayList<Notification>(notification_buffer)) {
                int sprite_offset_x = 0;

                if (n.type == Notification.Type.INFO) sprite_offset_x = 0;
                else if (n.type == Notification.Type.WARNING) sprite_offset_x = 6;
                else if (n.type == Notification.Type.OBJECTIVE) sprite_offset_x = 12;

                int text_width = graphics.getFont().getWidth(n.text);

                meta_sprites.getSubImage(sprite_offset_x, 0, 6, 12).draw(gameContainer.getWidth() - n.offset_x - text_width - 72, offset_y, 6);
                meta_sprites.getSubImage(sprite_offset_x, 12, 1, 12).draw(gameContainer.getWidth() - n.offset_x - text_width - 36, offset_y, text_width, 72);
                graphics.drawString(n.text, gameContainer.getWidth() - n.offset_x - text_width - 36, offset_y + 36 - graphics.getFont().getHeight(n.text) / 2);
                meta_sprites.getSubImage(sprite_offset_x, 24, 6, 12).draw(gameContainer.getWidth() - n.offset_x - 36, offset_y, 6);

                offset_y = offset_y + 82;
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
