package com.jumppixel.ld30;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.util.pathfinding.Mover;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Tom on 24/08/2014.
 */
public class LivingEntity extends Entity {
    public List<Animation> animations = new ArrayList<Animation>();
    SpriteSheet sprites;
    float move_speed;

    float health;
    float max_health = 1.0f;

    public LivingEntity(vec2 loc, SpriteSheet sprites, vec2 render_offset, float max_health, float move_speed, int num_ani_frames, Player player) {
        super(loc, sprites, render_offset, player);
        this.max_health = max_health;
        this.move_speed = move_speed;
        this.health = max_health;
        this.sprites = sprites;

        for (int rot = 0; rot<8; rot++) {
            Animation a = new Animation();
            for (int i=0; i<num_ani_frames; i++) {
                a.addFrame(sprites.getSprite(rot, i), 200);
            }
            animations.add(a);
        }
    }

    @Override
    public void update(World world, int delta_ms) {
        Animation rot_ani = animations.get(rotation.getRotInt());
        if (velocity.isZero()) {
            rot_ani.setCurrentFrame(0);
            rot_ani.setAutoUpdate(false);
        }
        else {
            rot_ani.setAutoUpdate(true);
        }

        vec2 new_loc = loc.add(move_speed * velocity.x * delta_ms, move_speed * velocity.y * delta_ms);

        if (world.walkable(loc, new_loc)) {
            vec2 old_loc = new vec2(loc);
            loc = new_loc;
            if (new_loc.getFloorX() != old_loc.getFloorX() || new_loc.getFloorY() != old_loc.getFloorY()) {
                onTileChange(world, old_loc, new_loc);
            }
        }
    }

    public void onTileChange(World world, vec2 old_loc, vec2 new_loc) {

    }

    @Override
    public void render(vec2 view_offset, GameContainer container, Graphics g) {
        vec2 tile_location = loc.add(view_offset.negate());
        vec2 pixel_location = tile_location.mul(24.f);

        animations.get(rotation.getRotInt()).draw(pixel_location.x + render_offset.x, pixel_location.y + render_offset.y);
    }

    public void takeDamage(float damage) {
        if (health - damage < 0) {
            health = 0;
            die();
        }else{
            health = health - damage;
        }
    }

    public void die() {

    }
}
