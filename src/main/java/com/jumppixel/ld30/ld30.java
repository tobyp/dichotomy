package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tobyp on 8/23/14.
 */
public class ld30 extends BasicGame {
    Logger logger = Logger.getLogger("ld30");

    float PLAYER_TILES_PER_MS = 0.0105f;

    //RESOURCES
    SpriteSheet player_sprites;

    //MAP/WORLD
    TiledMap map;
    int walk_layer_index;

    //PLAYER
    Player player;
    vec2 tileOffset;

    public ld30() {
        super("Ludum Dare 30");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {
        int tileCountX = (int)Math.floor(gameContainer.getWidth() / 48.f);
        int tileCountY = (int)Math.floor(gameContainer.getHeight() / 48.f);

        map = new TiledMap("src/main/resources/tmx/level1.tmx");
        walk_layer_index = map.getLayerIndex("walkability");

        String[] spawn_location = map.getMapProperty("p-spawn", "0,0").split(",");
        player = new Player(Integer.parseInt(spawn_location[0]), Integer.parseInt(spawn_location[1]));
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
        if (gameContainer.getInput().isKeyDown(Input.KEY_RIGHT)) {
            tryMove(delta_ms * PLAYER_TILES_PER_MS, 0.f);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_LEFT)) {
            tryMove(delta_ms * -PLAYER_TILES_PER_MS, 0.f);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_DOWN)) {
            tryMove(0.f, delta_ms * PLAYER_TILES_PER_MS);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_UP)) {
            tryMove(0.f, delta_ms * -PLAYER_TILES_PER_MS);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_ESCAPE)) {
            gameContainer.exit();
        }
    }

    public boolean tryMove(float x, float y) {
        vec2 new_loc = player.loc.add(x, y);
        if (Math.floor(new_loc.x) < 0 || Math.floor(new_loc.y) < 0) return false;
        int tile_id = map.getTileId((int)Math.floor(new_loc.x), (int)Math.floor(new_loc.y), walk_layer_index);
        logger.info(Integer.toString(tile_id));
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
