package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.util.Log;

/**
 * Created by Tom on 24/08/2014.
 */
public class Drop extends Entity {
    int expire_ms = 1000*10; //Default: 10 seconds
    Player player;
    boolean expires = true;

    public Drop(vec2 loc, Image image, Player player) {
        super(loc, image, new vec2(0-image.getWidth()/2, 0-image.getHeight()/2), player);
        this.player = player;
    }

    public Drop(vec2 loc, Image image, Player player, boolean expires) {
        super(loc, image, new vec2(0-image.getWidth()/2, 0-image.getHeight()/2), player);
        this.player = player;
        this.expires = false;
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
    public void update(World world, int delta_ms) {
        super.update(world, delta_ms);

        if (loc.getDistance(player.loc) < 0.5) {
            pickup();
            GameSound.DROP_PICKUP.play(1, 1);
            world.entities.remove(this);
        }
        else if (expires) {
            if (expire_ms - delta_ms <= 0) {
                expire();
                world.entities.remove(this);
            } else {
                expire_ms -= delta_ms;
            }
        }
    }

    public void expire() {
    }

    public void pickup() {

    }


}
