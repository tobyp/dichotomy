package com.jumppixel.dichotomy;

import org.newdawn.slick.SpriteSheet;

/**
 * Created by Tom on 24/08/2014.
 */
public class HealthDrop extends Drop {
    float health;

    public HealthDrop(vec2 loc, SpriteSheet sheet, Player player, float health) {
        super(loc, sheet.getSprite(0, health > 0.5f ? 1 : 0), player);
        this.health = health;
    }

    @Override
    public void pickup() {
        if (player.health + health > player.max_health) {
            player.health = player.max_health;
        }else{
            player.health += this.health;
        }
    }


}
