package com.jumppixel.ld30;

import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

/**
 * Created by tobyp on 8/25/14.
 */
public class DeviceDrop extends Drop {
    public DeviceDrop(vec2 loc, SpriteSheet dropsprites, Player player) {
        super(loc, dropsprites.getSprite(0, 3), player);
    }

    @Override
    public void pickup() {
        player.has_device = true;
    }
}
