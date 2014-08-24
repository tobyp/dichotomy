package com.jumppixel.ld30;

import org.newdawn.slick.Animation;
import org.newdawn.slick.SpriteSheet;

/**
 * Created by tobyp on 8/23/14.
 */
public class Player extends Entity {
    public Player(vec2 loc, SpriteSheet sprites, vec2 render_offset, float move_speed, int num_ani_frames) {
        super(loc, sprites, render_offset, 1.0f, move_speed, num_ani_frames);
    }

    @Override
    public void die() {
        //TODO: Respawn code
    }
}
