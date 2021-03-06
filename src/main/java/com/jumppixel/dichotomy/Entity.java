package com.jumppixel.dichotomy;

import org.newdawn.slick.*;

/**
 * Created by tobyp on 8/23/14.
 */
public class Entity {
    Image image;

    public vec2 loc;
    public vec2 render_offset;
    public vec2 velocity = new vec2();
    public vec2 rotation = new vec2(1, 0);

    float health;
    float max_health = 1.0f;

    public Entity(vec2 loc, Image image, vec2 render_offset, Player player) {
        this.loc = loc;
        this.render_offset = render_offset;
        this.image = image;
    }

    public void render(vec2 view_offset, GameContainer container, Graphics g) {
        vec2 tile_location = loc.add(view_offset.negate());
        vec2 pixel_location = tile_location.mul(24.f);

        image.draw(pixel_location.x+render_offset.x, pixel_location.y+render_offset.y);
    }

    public void setVelocity(vec2 velocity) {
        this.velocity = velocity;
        if (!velocity.isZero()) {
            this.rotation = velocity.getRot();
        }
    }

    public void update(World world, int delta_ms) {

    }

    public vec2 getFaced() {
        return this.loc.getFaced(this.rotation);
    }

    public void spawn() {
        //Called when entity is added to map
    }

    public void expire() {
        //Cleanup
    }
}
