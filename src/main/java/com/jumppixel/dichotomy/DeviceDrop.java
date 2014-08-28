package com.jumppixel.dichotomy;

import org.newdawn.slick.SpriteSheet;

/**
 * Created by tobyp on 8/25/14.
 */
public class DeviceDrop extends Drop {
    public DeviceDrop(vec2 loc, SpriteSheet dropsprites, Player player) {
        super(loc, dropsprites.getSprite(3, 0), player, false);
    }

    @Override
    public void pickup() {
        player.has_device = true;
        player.allow_charging = true;
    }
}
