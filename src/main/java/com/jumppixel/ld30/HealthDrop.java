package com.jumppixel.ld30;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

/**
 * Created by Tom on 24/08/2014.
 */
public class HealthDrop extends Drop {

    public HealthDrop(vec2 loc, SpriteSheet sheet, Player player) {
        super(loc, sheet.getSprite(0, 0), player);
    }

    @Override
    public void pickup() {
        if (player.health + 0.2 > player.max_health) {
            player.health = player.max_health;
        }else{
            player.health = player.health + .2f;
        }
    }


}
