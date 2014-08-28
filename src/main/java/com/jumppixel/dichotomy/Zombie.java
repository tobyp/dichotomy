package com.jumppixel.dichotomy;

import org.newdawn.slick.SpriteSheet;

/**
 * Created by Tom on 24/08/2014.
 */
public class Zombie extends Monster {
    public Zombie(vec2 loc, SpriteSheet sprites, vec2 render_offset, int num_ani_frames, World world, Player player) {
        super(loc, sprites, render_offset, 0.5f, 0.003f, num_ani_frames, world, player);
    }

    @Override
    public void takeDamage(float attack_strength) {
        super.takeDamage(attack_strength);
    }
}
