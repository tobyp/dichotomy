package com.jumppixel.ld30;

import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

/**
 * Created by tobyp on 8/23/14.
 */
public class Entity {
    public Animation animation = new Animation();
    public Vector2f loc = new Vector2f();
    public float rot = 0.0f;

    public Entity() {

    }

    public void render(Vector2f view_offset, GameContainer container, Graphics g) {
        animation.draw(loc.getX() - view_offset.getX(), loc.getY()-view_offset.getY());
    }
}
