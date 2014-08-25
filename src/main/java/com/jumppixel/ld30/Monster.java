package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.PathFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 24/08/2014.
 */
public class Monster extends LivingEntity implements Mover {

    PathFinder path_finder;

    int pathfinder_interval = 100;
    int pathfinder_ms = 0;

    public Monster(vec2 loc, SpriteSheet sprites, vec2 render_offset, float max_health, float walk_speed, int num_ani_frames, Map map) {
        super(loc, sprites, render_offset, max_health, walk_speed, num_ani_frames);

        path_finder = new AStarPathFinder(map, 10, true);
    }

    @Override
    public void update(Map map, int delta_ms) {
        super.update(map, delta_ms);

        pathfinder_ms = pathfinder_ms + delta_ms;
        if (pathfinder_ms >= pathfinder_interval) {
            pathfinder_ms = pathfinder_ms - pathfinder_interval;
            updatePathFinder();
        }
    }

    public void updatePathFinder() {
    }

    @Override
    public void render(vec2 view_offset, GameContainer container, Graphics g) {
        super.render(view_offset, container, g);

        vec2 tile_location = loc.add(view_offset.negate());
        vec2 pixel_location = tile_location.mul(24.f);

        g.setColor(Color.red);
        g.fillRect(pixel_location.x + render_offset.x + 3, pixel_location.y + render_offset.y + 6, 18, 1);
        g.setColor(Color.green);
        g.fillRect(pixel_location.x + render_offset.x + 3, pixel_location.y + render_offset.y + 6, Math.round(18 * health / max_health), 1);
    }
}
