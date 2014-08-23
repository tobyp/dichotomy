package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tobyp on 8/23/14.
 */
public class ld30 extends BasicGame implements InputListener {
    Logger logger = Logger.getLogger("ld30");

    float PLAYER_TILES_PER_MS = 0.005f;

    //RESOURCES
    SpriteSheet player_sprites;

    //MAP/WORLD
    Map map;
    int walk_layer_index;

    //PLAYER
    Player player;
    vec2 tileOffset;

    boolean[] keystatus;

    public ld30() {
        super("Ludum Dare 30");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {

        gameContainer.getInput().addListener(this);

        int tileCountX = (int)Math.floor(gameContainer.getWidth() / 48.f);
        int tileCountY = (int)Math.floor(gameContainer.getHeight() / 48.f);

        map = new Map("src/main/resources/tmx/level1.tmx");
        walk_layer_index = map.getLayerIndex("walkability");

        String[] spawn_location = map.getMapProperty("p-spawn", "0,0").split(",");
        player = new Player(Integer.parseInt(spawn_location[0]), Integer.parseInt(spawn_location[1]));
        player.render_offset = new vec2(-12,-48);

        tileOffset = player.loc.add(-tileCountX / 2.f - 0.5f, -tileCountY / 2.f - 0.5f); //half screen to char's foot-center

        player_sprites = new SpriteSheet("src/main/resources/protagonist.png", 24, 48);

        player.animation.addFrame(player_sprites.getSprite(0, 0), 100);
        player.animation.addFrame(player_sprites.getSprite(0, 1), 100);
        player.animation.addFrame(player_sprites.getSprite(0, 0), 100);
        player.animation.addFrame(player_sprites.getSprite(0, 2), 100);

        gameContainer.setTargetFrameRate(60);
    }

    @Override
    public void update(GameContainer gameContainer, int delta_ms) throws SlickException {
        if (gameContainer.getInput().isKeyDown(Input.KEY_W)) {
            tryMove(delta_ms * PLAYER_TILES_PER_MS, 0.f);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_A)) {
            tryMove(delta_ms * -PLAYER_TILES_PER_MS, 0.f);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_S)) {
            tryMove(0.f, delta_ms * PLAYER_TILES_PER_MS);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_D)) {
            tryMove(0.f, delta_ms * -PLAYER_TILES_PER_MS);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_ESCAPE)) {
            gameContainer.exit();
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
        if (button == 1) { //object interaction
            int faced = getFacedObject(0);
            if (faced == -1) return;
            String type = map.getObjectType(0, faced);
            if (type.equals("button")) {
                String state = map.getObjectProperty(0, faced, "state", "false");
                if (state.equals("true")) {
                    map.setObjectProperty(0, faced, "state", "false");
                    executeActions(map.getObjectProperty(0, faced, "disable", ""));
                }
                else if (state.equals("false")) {
                    map.setObjectProperty(0, faced, "state", "true");
                    executeActions(map.getObjectProperty(0, faced, "enable", ""));
                }
            }
        }
        else if (button == 0) { //attack

        }
    }

    @Override
    public void keyPressed(int key, char c) {

    }

    @Override
    public void keyReleased(int key, char c) {

    }

    @Override
    public void mouseMoved(int oldx, int oldy, int newx, int newy) {

    }

    public int getFacedObject(int group) {
        int faced_x = player.loc.getFloorX();
        int faced_y = player.loc.getFloorY();

        if (player.rot >= 1 && player.rot <= 3) faced_x -= 1;
        if (player.rot >= 5 && player.rot <= 7) faced_x += 1;
        if (player.rot >= 3 && player.rot <= 5) faced_y -= 1;
        if (player.rot <= 1 || player.rot == 7) faced_y += 1;

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

    public boolean tryMove(float x, float y) {
        vec2 new_loc = player.loc.add(x, y);

        int tile_id = map.getTileId(new_loc.getFloorX(), new_loc.getFloorY(), walk_layer_index);
        if (tile_id != 61) {
            return false;
        }else{
            player.loc = new_loc;
            tileOffset = tileOffset.add(x, y);
        }
        return true;
    }

    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        int tileOffsetX = (int)tileOffset.x;
        int tileOffsetY = (int)tileOffset.y;

        int render_offset_x = -(int)((tileOffset.x-tileOffsetX)*24.f);
        int render_offset_y = -(int)((tileOffset.y-tileOffsetY)*24.f);
        int render_tile_w = (gameContainer.getWidth() / 2 + 48) / 24;
        int render_tile_h = (gameContainer.getHeight() / 2 + 48) / 24;

        for (int i=0; i<map.getLayerCount(); i++) {
            String layer_type = map.getLayerProperty(i, "type", "visible");
            if (layer_type.equals("visible")) {
                map.render(render_offset_x, render_offset_y, tileOffsetX, tileOffsetY, render_tile_w, render_tile_h, i, false);
            }
            else if (layer_type.equals("player")) {
                player.render(tileOffset.add(0.f, -1.f), gameContainer, graphics);
            }
        }
    }

    public static void main(String [] args) {
        Logger logger = Logger.getGlobal();
        try {
            AppGameContainer c = new AppGameContainer(new ScalableGame(new ld30(), 400, 300));
            c.setDisplayMode(800, 600, false);
            c.start();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Game error.", e);
        }
    }
}
