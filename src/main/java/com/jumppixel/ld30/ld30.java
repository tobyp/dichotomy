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
    World world;
    World wgood, wevil;

    //NARRATION
    NarrationQueue current_narration_queue = null;

    //PLAYER
    Player player;

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

        wgood = new World(this, map, "good");
        wevil = new World(this, map, "evil");
        world = wgood;

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

        current_narration_queue = new NarrationQueue();
        current_narration_queue.add(new Narration("Hello there", "meow1.ogg"));
        current_narration_queue.add(new Narration("How are yo-", "meow2.ogg"));
        current_narration_queue.add(new Narration("oh... goodbye :(", "meow3.ogg"));

        world.addEntity(new Zombie(player.loc, new SpriteSheet("src/main/resources/zombie.png", 24, 48), new vec2(-12, -14), 4, world, player));
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
                vec2 faced_tile = player.getFaced();
                Map.MapObject faced = map.getObject(world.ogroup, faced_tile);
                if (faced == null) return;
                String type = faced.getType();

                logger.info("Interaction with "+faced.toString());

                if (type.equals("button")) {
                    String state = faced.getProperty("state", "false");
                    if (state.equals("true")) {
                        faced.setProperty("state", "false");
                        logger.info("\tBUTTON: disabling");
                        executeActions(faced.getProperty("disable", ""));
                    } else if (state.equals("false")) {
                        faced.setProperty("state", "true");
                        logger.info("\tBUTTON: enabling");
                        executeActions(faced.getProperty("enable", ""));
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
                            if (faced.getPropertyBool("consumes", "false")) {
                                player.keycards &= ~card;
                            }
                            faced.setPropertyBool("state", !state);
                            if (state) {
                                executeActions(faced.getProperty("lock", ""));
                            }
                            else {
                                executeActions(faced.getProperty("unlock", ""));
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
            case Input.KEY_ESCAPE: {
                gameContainer.exit();
            }
            break;
            case Input.KEY_LALT:
            case Input.KEY_Q: {
                if (player.charge == player.max_charge && player.has_device) player.charge_holding = true;
            }
            break;
            case Input.KEY_L: {
                world = (world == wgood) ? wevil : wgood;
            }
            break;
            case Input.KEY_SPACE: {
                if (current_narration_queue != null) {
                    if (current_narration_queue.current() != null) {
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

    public World getWorld(String name) {
        if (name.equals("good")) return wgood;
        else if (name.equals("evil")) return wevil;
        return null;
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
            else if (aparts[0].equals("notify")) {
                Notification.Type notify_type = Notification.Type.valueOf(aparts[1]);
                addNotification(new Notification(aparts[2], notify_type));
            }
            else if (aparts[0].equals("notify-timed")) {
                int notify_time = Integer.parseInt(aparts[1]);
                Notification.Type notify_type = Notification.Type.valueOf(aparts[2]);
                addNotification(new TimedNotification(aparts[3], notify_time, notify_type));
            }
            else if (aparts[0].equals("add-drop")) {
                vec2 loc = new vec2(Float.parseFloat(aparts[2]),Float.parseFloat(aparts[3]));
                if (aparts[1].equals("health-1")) {
                    world.addEntity(new HealthDrop(loc, drop_sprites, player));
                }else
                if (aparts[1].equals("health-2")) {
                    world.addEntity(new MegaHealthDrop(loc, drop_sprites, player));
                }
            }
            else if (aparts[0].equals("laser-dev-toggle")) {
                int le_x = Integer.parseInt(aparts[1]);
                int le_y = Integer.parseInt(aparts[2]);

                Map.MapObject object = map.getObject(world.ogroup, le_x, le_y);
                if (object != null) {
                    logger.info("ACTION: toggling "+object.toString());
                    world.laserEmitterToggle(object);
                }
            }
            else if (aparts[0].equals("laser-dev-rotate")) {
                int le_x = Integer.parseInt(aparts[1]);
                int le_y = Integer.parseInt(aparts[2]);
                //TODO cross-world actions
                Map.MapObject object = map.getObject(world.ogroup, le_x, le_y);
                if (object != null) {
                    logger.info("ACTION: rotating "+object.toString());
                    world.laserDeviceRotate(object);
                }
            }
            else if (aparts[0].equals("keycard-add")) {
                player.keycards |= Integer.parseInt(aparts[1], 2);
            }
            else if (aparts[0].equals("keycard-remove")) {
                player.keycards &= ~Integer.parseInt(aparts[1], 2);
            }
            else if (aparts[0].equals("keycards-clear")) {
                player.keycards = 0;
            }
        }
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
            if (layer_visibility.contains(w.name)) {
                map.render(render_offset_x, render_offset_y, tileOffsetX, tileOffsetY, render_tile_w, render_tile_h, i, false);
            }
            else if (layer_name.equals("player")) {
                player.render(tileOffset.add(0.f, -1.f), gameContainer, graphics);
            }
            else if (layer_name.equals("mobs")) {
                w.renderEntities(tileOffset, gameContainer, graphics);
            }
        }

        graphics.scale(.5f, .5f);

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

        //Narration
        if (current_narration_queue != null) {
            if (current_narration_queue.current() != null) {
                Image nbg = meta_sprites.getSubImage(57, 26, 44, 12);
                Image nsk = meta_sprites.getSubImage(103, 26, 25, 6);
                int scale = 7;

                nbg.draw(gameContainer.getWidth()/2 - (nbg.getWidth() * scale) / 2, gameContainer.getHeight() - nbg.getHeight()*scale - 10, nbg.getWidth()*scale, nbg.getHeight()*scale);
                int nsk_width = Math.round((float)nsk.getWidth()*scale/3);
                int nsk_height = Math.round((float)nsk.getHeight()*scale/3);
                nsk.draw(gameContainer.getWidth() / 2 + (nbg.getWidth() * scale) / 2 - nsk_width - 4 * scale, gameContainer.getHeight() - 10 - nsk_height - 3 * scale, nsk_width, nsk_height);

                int text_width = graphics.getFont().getWidth(current_narration_queue.current().text);
                int text_height = graphics.getFont().getHeight(current_narration_queue.current().text);
                graphics.setColor(Color.black);
                graphics.drawString(current_narration_queue.current().text, gameContainer.getWidth()/2 - text_width / 2, gameContainer.getHeight() - (nbg.getHeight()*scale)/2 - text_height / 2 - 10);
            }
        }

        //DEBUG

        if (player.debug_mode) {
            graphics.setColor(Color.white);
            graphics.drawString("FPS: " + gameContainer.getFPS(), gameContainer.getWidth() - 150, gameContainer.getHeight() - 30);
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
            e.printStackTrace();
            logger.log(Level.SEVERE, "Game error.", e);
        }
    }

    public void addNotification(Notification notification) {
        notification_buffer.add(notification);
    }
}
