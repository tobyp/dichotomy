package com.jumppixel.ld30;

import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by tobyp on 8/23/14.
 */
public class Entity {
    public List<Animation> animations = new ArrayList<Animation>();
    int rot = 0;

    public vec2 loc = new vec2();
    public vec2 render_offset = new vec2();

    public Entity() {

    }

    public void render(vec2 view_offset, GameContainer container, Graphics g) {
        vec2 tile_location = loc.add(view_offset.negate());
        vec2 pixel_location = tile_location.mul(24.f);
        animations.get(rot).draw(pixel_location.x+render_offset.x, pixel_location.y+render_offset.y);
    }

    public void startAnimation() {
        for (Animation a : animations) {
            a.start();
        }
    }
    public void stopAnimation() {
        for (Animation a : animations) {
            a.stopAt(0);
        }
    }
    public boolean isRunningAnimation() {
        return !animations.get(rot).isStopped();
    }

    public Image getStillImage() {
        return animations.get(rot).getImage(0);
    }
}
