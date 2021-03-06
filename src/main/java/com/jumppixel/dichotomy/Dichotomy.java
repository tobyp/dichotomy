package com.jumppixel.dichotomy;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.openal.Audio;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

/**
 * Created by tobyp on 8/23/14.
 */
public class Dichotomy extends BasicGame implements InputListener {
    Logger logger = Logger.getLogger("dichotomy");

    float PLAYER_TILES_PER_MS = 0.005f;

    GameContainer gameContainer;

    //RESOURCES
    SpriteSheet meta_sprites;
    SpriteSheet drop_sprites;
    SpriteSheet player_sprites;
    SpriteSheet zombie_sprites;
    Image dark_overlay;
    SpriteSheetFont font;

    //MAP/WORLD
    Map map;
    World world;
    World wgood, wevil;

    //NARRATION
    NarrationQueue current_narration_queue = null;

    //PLAYER
    Player player;
    String checkpoint = null;

    //MUSIC
    Audio music;

    public List<Notification> notification_buffer  = new ArrayList<Notification>();

    public Dichotomy() {
        super("Dichotomy");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {

        music = GameSound.MUSIC_CONSTANCE.getAudio();
        music.playAsMusic(1, .5f, true);

        this.gameContainer = gameContainer;
        gameContainer.setTargetFrameRate(60);

        font = new SpriteSheetFont(new SpriteSheet("src/main/resources/font/font.png", 15, 21), ' ');
        meta_sprites = new SpriteSheet("src/main/resources/sprite/meta.png", 24, 24);
        drop_sprites = new SpriteSheet("src/main/resources/sprite/drops.png", 24, 24);
        player_sprites = new SpriteSheet("src/main/resources/sprite/protagonist.png", 24, 48);
        zombie_sprites = new SpriteSheet("src/main/resources/sprite/zombie.png", 24, 48);
        dark_overlay = new Image("src/main/resources/overlay/dark_overlay.png");

        reset();

        addNotification(new TimedNotification("Controls: WASD to move, E to interact.", 6000, Notification.Type.INFO));

        gameContainer.getInput().addListener(this);
    }

    public void reset() {
        try {
            map = new Map("src/main/resources/level/world_merge.tmx");
        }
        catch (SlickException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Error loading map", e);
        }

        wgood = new World(this, map, "good");
        wevil = new World(this, map, "evil");

        if (checkpoint == null) checkpoint = map.getMapProperty("start", "");
        Map.MapObject cpo = map.getObject(map.getObjectGroupIndex("checkpoints"), checkpoint);
        world = cpo.getProperty("world", "good").equals("good") ? wgood : wevil;
        logger.info("Spawning at "+cpo.getLocation().div(24.f).toString()+" in "+world.name);
        player = new Player(this, cpo.getLocation().div(24.f), player_sprites, new vec2(-12,-48), PLAYER_TILES_PER_MS, 4);
        executeActions(cpo.getProperty("init", ""));

        wgood.reset();
        wevil.reset();
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

        player.update(world, delta_ms);

        world.update(player, delta_ms);

        checkMovements(gameContainer);

        if (current_narration_queue != null) {
            if (current_narration_queue.current() == null) {
                current_narration_queue.next();
                current_narration_queue.current().playSound();
            }
        }
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
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if (button == 0) { //attack
            player.doAttack(world);
        }
    }

    @Override
    public void keyPressed(int key, char c) {
        switch (key) {
            case Input.KEY_E: {
                vec2 faced_tile = player.getFaced();
                Map.MapObject faced = map.getObject(world.ogroup, faced_tile);
                if (faced == null) return;
                String type = faced.getType();

                logger.info("Interaction with "+faced.toString());

                if (type.equals("button")) {
                    if (faced.getPropertyBool("blocked", "false")) return;;

                    String state = faced.getProperty("state", "false");
                    if (state.equals("true")) {
                        faced.setProperty("state", "false");
                        logger.info("\tBUTTON: disabling");
                        executeActions(faced.getProperty("disable", ""));
                        GameSound.BUTTON_UP.play(0, 0);
                    } else if (state.equals("false")) {
                        faced.setProperty("state", "true");
                        logger.info("\tBUTTON: enabling");
                        executeActions(faced.getProperty("enable", ""));
                        GameSound.BUTTON_DOWN.play(0, 0);
                    }
                }
                else if (type.equals("laser-emitter")) {
                    if (faced.getPropertyBool("manual", "false")) {
                        world.laserEmitterToggle(faced);
                    }
                    else if (faced.getPropertyBool("rotateable", "false")) {
                        world.laserDeviceRotate(faced);
                    }
                }
                else if (type.equals("laser-io") || type.equals("laser-io-inverse") || type.equals("laser-receiver")) {
                    if (faced.getPropertyBool("rotateable", "false")) {
                        world.laserDeviceRotate(faced);
                    }
                }
                else if (type.equals("keycard-reader")) {
                    boolean state = faced.getPropertyBool("state", "false");
                    boolean closeable = faced.getPropertyBool("closeable", "false");
                    int card = faced.getPropertyBeams("card", "00000");
                    if (!state || state && closeable) {
                        if ((player.keycards & card) > 0) {
                            if (faced.getPropertyBool("consumes", "true")) {
                                player.keycards &= ~card;
                            }
                            faced.setPropertyBool("state", !state);
                            if (state) {
                                executeActions(faced.getProperty("lock", ""));
                                GameSound.KEYCARD_LOCK.play(1, 1);
                            }
                            else {
                                executeActions(faced.getProperty("unlock", ""));
                                GameSound.KEYCARD_UNLOCK.play(1, 1);
                            }
                        }
                    }
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
                if (player.debug_mode) player.debug_mode = false;
                else player.debug_mode = true;
                addNotification(new TimedNotification("Toggled debug mode", 2000, Notification.Type.INFO));
            }
            break;
            case Input.KEY_F1: { //about
                NarrationQueue q = new NarrationQueue();
                q.add(new Narration("About Dichotomy"));
                q.add(new Narration("Dichotomy was made in 72 hours by"));
                q.add(new Narration("Tom, Toby, Felix & Lennart"));
                q.add(new Narration("for LD30."));
                q.add(new Narration("\"Did you ever wonder where dropped pens go?\""));
                setCurrentNarrationQueue(q);
            }
            break;
            case Input.KEY_M: { //music
                if (!music.isPlaying()) music.playAsMusic(1, 0.5f, true);
                else music.stop();
                addNotification(new TimedNotification("Toggled music", 2000, Notification.Type.INFO));
            }
            break;
            case Input.KEY_ESCAPE: {
                gameContainer.exit();
            }
            break;
            case Input.KEY_LSHIFT:
            case Input.KEY_RSHIFT: {
                if (player.charge == player.max_charge && player.has_device) player.charge_holding = true;
                else if (player.has_device) GameSound.CHARGE_INCOMPLETE.play(1, 1);
            }
            break;
            case Input.KEY_L: {
                if (player.debug_mode) switchWorld();
            }
            break;
            case Input.KEY_SPACE: {
                if (current_narration_queue != null) {
                    if (current_narration_queue.current() != null) {
                        GameSound.NARRATION_ADVANCE.play(1, 0.5f);
                        current_narration_queue.current().stopSound();
                        if (current_narration_queue.hasNext()) {
                            current_narration_queue.next();
                            current_narration_queue.current().playSound();
                        }else{
                            current_narration_queue = null;
                        }
                    }
                }
            }
            break;
        }
    }

    @Override
    public void keyReleased(int key, char c) {
        switch (key) {
            case Input.KEY_LSHIFT:
            case Input.KEY_RSHIFT: {
                player.charge_holding = false;
            }
            break;
        }
    }

    @Override
    public void mouseMoved(int oldx, int oldy, int newx, int newy) {

    }

    public World getWorld(String name) {
        if (name.equals("good")) return wgood;
        else if (name.equals("evil")) return wevil;
        return null;
    }

    int DOOR_BASE = 961;

    public void executeActions(String actions_string) {
        for (String a : actions_string.split(",")) {
            if (a.isEmpty()) continue;

            String[] aparts = a.split(":");
            for (int i=0; i<aparts.length; i++) {
                aparts[i] = aparts[i].replace("#c", ",");
                aparts[i] = aparts[i].replace("#d", ":");
                aparts[i] = aparts[i].replace("#h", "#");
            }
            if (aparts[0].equals("set-tile")) {
                int tile_x = Integer.parseInt(aparts[1]);
                int tile_y = Integer.parseInt(aparts[2]);
                int tile_l = map.getLayerIndex(aparts[3]);
                int tile_id = Integer.parseInt(aparts[4]);
                logger.info("ACTION: set tile ("+Integer.toString(tile_x)+";"+Integer.toString(tile_y)+";"+Integer.toString(tile_l)+") to "+Integer.toString(tile_id));
                map.setTileId(tile_x, tile_y, tile_l, tile_id);
            }
            if (aparts[0].equals("door-open")) {
                int tile_x = Integer.parseInt(aparts[1]);
                int tile_y = Integer.parseInt(aparts[2]);
                int top_l = map.getLayerIndex(aparts[3]);
                int bot_l = map.getLayerIndex(aparts[4]);
                int walk_l = map.getLayerIndex(aparts[5]);
                logger.info("ACTION: opening door ("+Integer.toString(tile_x)+";"+Integer.toString(tile_y)+")");
                int tile_id = map.getTileId(tile_x, tile_y-1, top_l) - DOOR_BASE;
                System.out.println("tile "+tile_id);
                int variant = tile_id / 2 * 2 + 1;
                map.setTileId(tile_x, tile_y-1, top_l, DOOR_BASE + variant);
                map.setTileId(tile_x, tile_y, bot_l, DOOR_BASE + variant + 40);
                map.setTileId(tile_x, tile_y-1, walk_l, 881);
                map.setTileId(tile_x, tile_y, walk_l, 881);
            }
            if (aparts[0].equals("door-close")) {
                int tile_x = Integer.parseInt(aparts[1]);
                int tile_y = Integer.parseInt(aparts[2]);
                int top_l = map.getLayerIndex(aparts[3]);
                int bot_l = map.getLayerIndex(aparts[4]);
                int walk_l = map.getLayerIndex(aparts[5]);
                logger.info("ACTION: opening door ("+Integer.toString(tile_x)+";"+Integer.toString(tile_y)+")");
                int tile_id = map.getTileId(tile_x, tile_y-1, top_l) - DOOR_BASE;
                int variant = tile_id / 2 * 2;
                map.setTileId(tile_x, tile_y-1, top_l, DOOR_BASE + variant);
                map.setTileId(tile_x, tile_y, bot_l, DOOR_BASE + variant + 40);
                map.setTileId(tile_x, tile_y-1, walk_l, 881+15);
                map.setTileId(tile_x, tile_y, walk_l, 881+15);
            }
            else if (aparts[0].equals("notify")) {
                Notification.Type notify_type = Notification.Type.valueOf(aparts[1]);
                addNotification(new Notification(aparts[2], notify_type));
            }
            else if (aparts[0].equals("notify-timed")) {
                int notify_time = Integer.parseInt(aparts[1]);
                Notification.Type notify_type = Notification.Type.valueOf(aparts[2]);
                addNotification(new TimedNotification(aparts[3], notify_time, notify_type));
            }
            else if (aparts[0].equals("laser-dev-toggle")) {
                World w = getWorld(aparts[1]);
                if (w == null) continue;
                int le_x = Integer.parseInt(aparts[2]);
                int le_y = Integer.parseInt(aparts[3]);

                Map.MapObject object = map.getObject(w.ogroup, le_x, le_y);
                if (object != null) {
                    logger.info("ACTION: toggling "+object.toString());
                    w.laserEmitterToggle(object);
                }
            }
            else if (aparts[0].equals("laser-dev-rotate")) {
                World w = getWorld(aparts[1]);
                if (w == null) continue;
                int le_x = Integer.parseInt(aparts[2]);
                int le_y = Integer.parseInt(aparts[3]);

                Map.MapObject object = map.getObject(w.ogroup, le_x, le_y);
                if (object != null) {
                    logger.info("ACTION: rotating "+object.toString());
                    w.laserDeviceRotate(object);
                }
            }
            else if (aparts[0].equals("keycard-add")) {
                logger.info("ACTION: Giving keycards "+aparts[1]);
                player.keycards |= Integer.parseInt(aparts[1], 2);
            }
            else if (aparts[0].equals("keycard-remove")) {
                logger.info("ACTION: Removing keycards "+aparts[1]);
                player.keycards &= ~Integer.parseInt(aparts[1], 2);
            }
            else if (aparts[0].equals("keycards-clear")) {
                logger.info("ACTION: Clearing keycards.");
                player.keycards = 0;
            }
            else if (aparts[0].equals("keycard-drop")) {
                int cards = Integer.parseInt(aparts[1], 2);
                World w = getWorld(aparts[2]);
                if (w == null) continue;
                vec2 loc = new vec2(Float.parseFloat(aparts[3]),Float.parseFloat(aparts[4]));
                logger.info("ACTION: Dropping Keycard in " + w.name + " " + loc.toString());
                w.addEntity(new KeycardDrop(loc, drop_sprites, player, cards));
            }
            else if (aparts[0].equals("health-drop")) {
                float health = Float.parseFloat(aparts[1]);
                World w = getWorld(aparts[2]);
                if (w == null) continue;
                vec2 loc = new vec2(Float.parseFloat(aparts[3]),Float.parseFloat(aparts[4]));
                logger.info("ACTION: Dropping Healthpack in "+w.name+" "+loc.toString());
                w.addEntity(new HealthDrop(loc, drop_sprites, player, health));
            }
            else if (aparts[0].equals("device-drop")) {
                World w = getWorld(aparts[1]);
                if (w == null) continue;
                vec2 loc = new vec2(Float.parseFloat(aparts[2]),Float.parseFloat(aparts[3]));
                logger.info("ACTION: Dropping Device in "+w.name+" "+loc.toString());
                w.addEntity(new DeviceDrop(loc, drop_sprites, player));
            }
            else if (aparts[0].equals("device-give")) {
                logger.info("ACTION: Giving Device.");
                player.has_device = true;
                player.allow_charging = true;
            }
            else if (aparts[0].equals("set-prop")) {
                World w = getWorld(aparts[1]);
                if (w == null) continue;
                Map.MapObject mo = w.getObject(aparts[2]);
                if (mo != null) {
                    logger.info("ACTION: Setting "+mo.toString()+"."+aparts[3]+"="+aparts[4]);
                    mo.setProperty(aparts[3], aparts[4]);
                }
            }
            else if (aparts[0].equals("narration-queue")) {
                NarrationQueue q = new NarrationQueue();
                for (String s : aparts[1].split(",")) {
                    String text = s;
                    String sound = null;
                    String[] split = s.split("|");
                    if (split.length == 2) {
                        text = split[0];
                        sound = split[1];
                    }
                    Narration narration = new Narration(text, sound);
                    q.add(narration);
                }
                setCurrentNarrationQueue(q);
            }
            else if (aparts[0].equals("narration-clear")) {
                setCurrentNarrationQueue(null);
            }
            else if (aparts[0].equals("checkpoint")) {
                checkpoint = aparts[1];

                Map.MapObject cpo = map.getObject(map.getObjectGroupIndex("checkpoints"), checkpoint);
                executeActions(cpo.getProperty("init", ""));

                logger.info("ACTION: Checkpoint set to "+checkpoint);
                addNotification(new TimedNotification("Checkpoint!", 500, Notification.Type.INFO));
            }
            else if (aparts[0].equals("zombie-spawn")) {
                World w = getWorld(aparts[1]);
                if (w == null) continue;
                Zombie z = new Zombie(
                        new vec2(Float.parseFloat(aparts[2]), Float.parseFloat(aparts[3])),
                        zombie_sprites,
                        new vec2(-12, -48),
                        4,
                        w,
                        player
                );
                logger.info("ACTION: Spawning Zombie at "+z.world.name+" "+z.loc.toString());
                w.addEntity(z);
            }
            else if (aparts[0].equals("zombie-spawn-x")) {
                World w = getWorld(aparts[1]);
                if (w == null) continue;
                Zombie z = new Zombie(
                        new vec2(Float.parseFloat(aparts[2]), Float.parseFloat(aparts[3])),
                        zombie_sprites,
                        new vec2(-12, -48),
                        4,
                        w,
                        player
                );
                z.move_speed = Float.parseFloat(aparts[4]);
                z.attack_duration = Integer.parseInt(aparts[5]);
                z.attack_strength = Float.parseFloat(aparts[6]);
                z.attack_distance = Integer.parseInt(aparts[7]);
                logger.info("ACTION: Spawning Zombie at "+z.world.name+" "+z.loc.toString());
                w.addEntity(z);
            }
        }
    }

    public void setCurrentNarrationQueue(NarrationQueue queue) {
        if (current_narration_queue != null) {
            if (current_narration_queue.current() != null) {
                current_narration_queue.current().stopSound();
            }
        }
        current_narration_queue = queue;
    }



    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        render(world, gameContainer, graphics);
    }

    void render(World w, GameContainer gameContainer, Graphics graphics) throws SlickException {
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
            String layer_visibility = map.getLayerProperty(i, "world", "");
            String layer_name = map.getLayerName(i);
            if (layer_name.equals("player")) {
                player.render(tileOffset.add(0.f, -1.f), gameContainer, graphics);
            }
            else if (layer_name.equals("mobs")) {
                w.renderEntities(tileOffset, gameContainer, graphics);
            }
            else if (layer_visibility.contains(w.name) || layer_visibility.isEmpty()) {
                map.render(render_offset_x, render_offset_y, tileOffsetX, tileOffsetY, render_tile_w, render_tile_h, i, false);
            }
        }

        graphics.scale(.5f, .5f);

        if (player.fade_ms > 0) {
            if (w != wgood) graphics.setColor(new Color(0.4f, 0, 0.4f, ((float)player.fade_ms)/1000));
            else graphics.setColor(new Color(0.7f, 0.7f, 0.7f, ((float) player.fade_ms) / 1000));
            graphics.fillRect(0, 0, gameContainer.getWidth(), gameContainer.getHeight());
        }

        if (w != wgood) dark_overlay.draw(0, 0);

        //GUI

        //Health bar
        player.animations.get(player.rotation.getRotInt()).getImage(0).draw(20, 5, 1.5f, Color.red);
        meta_sprites.getSubImage(19, 1, 20, 2).draw(60, 36, 20*8, 2*8);
        meta_sprites.getSubImage(19, 4, 20, 2).draw(60, 36, Math.round(20*8*player.health/player.max_health), 2*8);

        //KEYCARDS
        int keycard_x = gameContainer.getWidth() - 60;
        int keycard_y = gameContainer.getHeight() - 60;
        if ((player.keycards & Player.KEYCARD_BLUE) > 0) { meta_sprites.getSubImage(54+24*4, 0, 24, 24).draw(keycard_x, keycard_y, 48, 48); keycard_x -= 48; }
        if ((player.keycards & Player.KEYCARD_PINK) > 0) { meta_sprites.getSubImage(54+24*3, 0, 24, 24).draw(keycard_x, keycard_y, 48, 48); keycard_x -= 48; }
        if ((player.keycards & Player.KEYCARD_ORANGE) > 0) { meta_sprites.getSubImage(54+24*2, 0, 24, 24).draw(keycard_x, keycard_y, 48, 48); keycard_x -= 48; }
        if ((player.keycards & Player.KEYCARD_GREEN) > 0) { meta_sprites.getSubImage(54+24, 0, 24, 24).draw(keycard_x, keycard_y, 48, 48); keycard_x -= 48; }
        if ((player.keycards & Player.KEYCARD_CYAN) > 0) { meta_sprites.getSubImage(54, 0, 24, 24).draw(keycard_x, keycard_y, 48, 48); keycard_x -= 48; }

        //Charge bar
        if (player.has_device) {
            int charge_offset_x = 10;
            int charge_offset_y = gameContainer.getHeight() - 78;

            Image teleporter_icon = meta_sprites.getSubImage(40, 7, 14, 17);
            Image charge_empty = meta_sprites.getSubImage(19, 10, 20, 2);
            Image charge_full = meta_sprites.getSubImage(19, 13, 20, 2);
            Image charge_ready = meta_sprites.getSubImage(19, 19, 20, 2);

            Image charge_inst = meta_sprites.getSubImage(19, 38, 25, 6);
            Image charge_status = meta_sprites.getSubImage(19, player.allow_charging ? 44 : 50, 38, 6);

            teleporter_icon.draw(charge_offset_x, charge_offset_y, 14 * 4, 17 * 4);

            teleporter_icon.draw(charge_offset_x, charge_offset_y, 14 * 4, 17 * 4, new Color(.8f, 0f, .8f, player.charge_hold / 2));

            charge_empty.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 - (2 * 8) / 2, 20 * 8, 2 * 8);
            charge_full.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 - (2 * 8) / 2, Math.round(20 * 8 * player.charge / player.max_charge), 2 * 8);
            charge_ready.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 - (2 * 8) / 2, Math.round(20 * 8 * player.charge_hold / 1), 2 * 8);

            if (player.charge == player.max_charge && !player.charge_holding) {
                charge_inst.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 + 10, 38 * 2, 6 * 2);
            }else
            if (player.charge < player.max_charge) {
                charge_status.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 + 10, 38 * 2, 6 * 2);
            }
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

        //Narration
        if (current_narration_queue != null) {
            if (current_narration_queue.current() != null) {
                Image nbg = meta_sprites.getSubImage(57, 26, 61, 12);
                Image nsk = meta_sprites.getSubImage(61, 39, 25, 6);
                int scale = 7;

                nbg.draw(gameContainer.getWidth()/2 - (nbg.getWidth() * scale) / 2, gameContainer.getHeight() - nbg.getHeight()*scale - 10, nbg.getWidth()*scale, nbg.getHeight()*scale);
                int nsk_width = Math.round((float)nsk.getWidth()*scale/3);
                int nsk_height = Math.round((float)nsk.getHeight()*scale/3);
                nsk.draw(gameContainer.getWidth() / 2 + (nbg.getWidth() * scale) / 2 - nsk_width - 4 * scale, gameContainer.getHeight() - 10 - nsk_height - 3 * scale + Math.round((float) scale / 2), nsk_width, nsk_height);

                int text_width = graphics.getFont().getWidth(current_narration_queue.current().text);
                int text_height = graphics.getFont().getHeight(current_narration_queue.current().text);
                graphics.setColor(Color.black);
                graphics.drawString(current_narration_queue.current().text.toUpperCase(), gameContainer.getWidth()/2 - text_width / 2, gameContainer.getHeight() - (nbg.getHeight()*scale)/2 - text_height / 2 - 10);
            }
        }

        //DEBUG

        if (player.debug_mode) {
            graphics.setColor(Color.white);
            graphics.drawString("FPS: " + gameContainer.getFPS(), gameContainer.getWidth() - 150, gameContainer.getHeight() - 30);
        }
    }

    public void switchWorld() {
        world = (world == wgood) ? wevil : wgood;
        GameSound.WORLD_CHANGE.play(1, 1);
    }

    public static void main(String [] args) {
        Logger logger = Logger.getGlobal();
        try {
            AppGameContainer c = new AppGameContainer(new ScalableGame(new Dichotomy(), 800, 600));
            c.setDisplayMode(800, 600, false);
            c.setShowFPS(false);
            c.setIcon("src/main/resources/icon/icon.png");
            c.start();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Game error.", e);
        }
    }

    public void addNotification(Notification notification) {
        notification_buffer.add(notification);
    }
}
