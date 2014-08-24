package com.jumppixel.ld30;

import org.newdawn.slick.SpriteSheet;

/**
 * Created by tobyp on 8/23/14.
 */
public class Player extends LivingEntity {
    float charge = 0.0f;
    float max_charge = 1.0f;
    float charge_hold = 0.0f;
    boolean allow_charging = true;
    boolean charge_holding = false;


    public Player(vec2 loc, SpriteSheet sprites, vec2 render_offset, float move_speed, int num_ani_frames) {
        super(loc, sprites, render_offset, 1.0f, move_speed, num_ani_frames);
    }

    @Override
    public void die() {
        charge = 0;
        //TODO: Respawn code
    }
}
