package com.jumppixel.ld30;

import org.newdawn.slick.SpriteSheet;

/**
 * Created by Tom on 24/08/2014.
 */
public class MegaHealthDrop extends Drop {

    public MegaHealthDrop(vec2 loc, SpriteSheet sheet, Player player) {
        super(loc, sheet.getSprite(1, 0), player);
    }

    @Override
    public void pickup() {
        if (player.health + 0.4 > player.max_health) {
            player.health = player.max_health;
        }else{
            player.health = player.health + .4f;
        }
    }


}
