package com.jumppixel.ld30;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import java.util.logging.Logger;

/**
 * Created by tobyp on 8/23/14.
 */
public class Entity {
    public Animation animation = new Animation();
    public vec2 loc = new vec2();
    public float rot = 0.0f;

    public Entity() {

    }

    public void render(vec2 view_offset, GameContainer container, Graphics g) {
        vec2 tile_location = loc.add(view_offset.negate());
        vec2 pixel_location = tile_location.mul(24.f);
        animation.draw(200, 150);
    }
}
