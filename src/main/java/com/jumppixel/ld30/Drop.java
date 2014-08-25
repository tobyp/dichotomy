package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.util.Log;

/**
 * Created by Tom on 24/08/2014.
 */
public class Drop extends Entity {
    public int expire_ms = 1000*10; //Default: 30 seconds

    public Drop(vec2 loc, Image image) {
        super(loc, image, new vec2(0-image.getWidth()/2, 0-image.getHeight()/2));
    }

    public void setExpireTime(int ms) {
        this.expire_ms = ms;
    }

    @Override
    public void render(vec2 view_offset, GameContainer container, Graphics g) {
        vec2 tile_location = loc.add(view_offset.negate());
        vec2 pixel_location = tile_location.mul(24.f);

        if (expire_ms <= 2000) {
            if (expire_ms % 100 < 50)
                image.draw(pixel_location.x + render_offset.x, pixel_location.y + render_offset.y);
        }
        else
            image.draw(pixel_location.x + render_offset.x, pixel_location.y + render_offset.y);
    }

    @Override
    public void update(Player player, World world, int delta_ms) {
        super.update(player, world, delta_ms);

        if (loc.getDistance(player.loc) < 0.5) {
            pickup(player);
            world.entities.remove(this);
        }
        else if (expire_ms - delta_ms <= 0) {
            expire();
            world.entities.remove(this);
        }else{
            expire_ms -= delta_ms;
        }
    }

    public void expire() {
    }

    public void pickup(Player player) {

    }


}
