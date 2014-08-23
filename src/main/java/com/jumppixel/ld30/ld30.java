package com.jumppixel.ld30;

import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.util.ResourceLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tobyp on 8/23/14.
 */
public class ld30 extends BasicGame {
    float PLAYER_TILES_PER_MS = .15f;

    //RESOURCES
    SpriteSheet player_sprites;

    //MAP/WORLD
    TiledMap map;

    //PLAYER
    Player player = new Player(2*24, 10*24);
    Vector2f tileOffset = new Vector2f(0.f, 0.f);

    public ld30() {
        super("Ludum Dare 30");
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {
        map = new TiledMap("src/main/resources/tmx/level1.tmx");
        player_sprites = new SpriteSheet("src/main/resources/protagonist.png", 24, 48);

        player.animation.addFrame(player_sprites.getSprite(0, 0), 1000);

        gameContainer.setTargetFrameRate(60);
    }

    @Override
    public void update(GameContainer gameContainer, int delta_ms) throws SlickException {
        if (gameContainer.getInput().isKeyDown(Input.KEY_RIGHT)) {
            player.loc.translate(delta_ms * PLAYER_TILES_PER_MS, 0.f);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_LEFT)) {
            player.loc.translate(delta_ms * -PLAYER_TILES_PER_MS, 0.f);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_DOWN)) {
            player.loc.translate(0.f, delta_ms * PLAYER_TILES_PER_MS);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_UP)) {
            player.loc.translate(0.f, delta_ms * -PLAYER_TILES_PER_MS);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_ESCAPE)) {
            gameContainer.exit();
        }
    }

    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        int tileOffsetX = (int)tileOffset.getX();
        int tileOffsetY = (int)tileOffset.getY();

        map.render(-(int)((tileOffset.x-tileOffsetX)*24.f), -(int)((tileOffset.y-tileOffsetY)*24.f), tileOffsetX, tileOffsetY, (gameContainer.getWidth() + 23) / 24, (gameContainer.getHeight() + 23) / 24);

        player.render(tileOffset, gameContainer, graphics);
    }

    public static void main(String [] args) {
        Logger logger = Logger.getGlobal();
        try {
            AppGameContainer c = new AppGameContainer(new ld30(), 800, 600, false);
            c.start();
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Game error.", e);
        }
    }
}
