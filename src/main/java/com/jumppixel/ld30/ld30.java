package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    //PLAYER
    Player player;
    int charge_ms = 0;
    int charge_interval = 100; //+0.01 charge every interval

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
        map = new Map("src/main/resources/tmx/level0.tmx");
        objects_group = map.getObjectGroupIndex("objects");

        String[] spawn_location_string = map.getMapProperty("p-spawn", "0,0").split(",");
        vec2 spawn_location = new vec2(Integer.parseInt(spawn_location_string[0]), Integer.parseInt(spawn_location_string[1]));
        player = new Player(spawn_location, new SpriteSheet("src/main/resources/protagonist.png", 24, 48), new vec2(-12,-48), PLAYER_TILES_PER_MS, 4);
        addNotification(new TimedNotification("Controls: WASD to move, E to interact.", 6000, Notification.Type.INFO));
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

        int faced_x = faced.getFloorX();
        int faced_y = faced.getFloorY();

        for (int oid = 0; oid < map.getObjectCount(group); oid++) {
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
            if (aparts[0].equals("set_tile")) {
                map.setTileId(Integer.parseInt(aparts[1]), Integer.parseInt(aparts[2]), map.getLayerIndex(aparts[3]), Integer.parseInt(aparts[4]));
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

        //Health bar
        player.animations.get(player.rotation.getRotInt()).getImage(0).draw(20, 5, 1.5f, Color.red);
        meta_sprites.getSubImage(19, 1, 20, 2).draw(60, 36, 20*8, 2*8);
        meta_sprites.getSubImage(19, 4, 20, 2).draw(60, 36, Math.round(20*8*player.health/player.max_health), 2*8);

        //Charge bar
        int charge_offset_x = 10;
        int charge_offset_y = gameContainer.getHeight() - 78;

        Image teleporter_icon = meta_sprites.getSubImage(40, 7, 14, 17);
        Image charge_disabled = meta_sprites.getSubImage(19, 10, 20, 2);
        Image charge_empty = meta_sprites.getSubImage(19, 13, 20, 2);
        Image charge_full = meta_sprites.getSubImage(19, 19, 20, 2);

        teleporter_icon.draw(charge_offset_x, charge_offset_y, 14*4, 17*4);

        //charge_disabled.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 - (2 * 8) / 2, 20 * 8, 2 * 8);

        charge_empty.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 - (2 * 8) / 2, 20 * 8, 2 * 8);
        charge_full.draw(charge_offset_x + 14 * 5, charge_offset_y + (17 * 4) / 2 - (2 * 8) / 2, Math.round(20*8*player.charge/player.max_charge), 2 * 8);

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
