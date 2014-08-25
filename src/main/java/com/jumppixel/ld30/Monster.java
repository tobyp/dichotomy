package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;

/**
 * Created by Tom on 24/08/2014.
 */
public class Monster extends LivingEntity implements Mover {

    AStarPathFinder path_finder;
    Path current_path;
    int current_step = 0;

    int pathfinder_interval = 100;
    int pathfinder_ms = 0;

    public Monster(vec2 loc, SpriteSheet sprites, vec2 render_offset, float max_health, float walk_speed, int num_ani_frames, Map map) {
        super(loc, sprites, render_offset, max_health, walk_speed, num_ani_frames);

        path_finder = new AStarPathFinder(map, 10, true);
    }

    @Override
    public void update(Player player, Map map, int delta_ms) {
        super.update(player, map, delta_ms);

        pathfinder_ms = pathfinder_ms + delta_ms;
        if (pathfinder_ms >= pathfinder_interval) {
            pathfinder_ms = pathfinder_ms - pathfinder_interval;
            updatePathFinder(player.loc.getFloorX(), player.loc.getFloorY());
        }

        if (current_path != null) {
            Path.Step step = current_path.getStep(current_step);

            setVelocity(vec2.ZERO);

            vec2 difference = loc.sub(new vec2(step.getX() + .5f, step.getY() + .5f));

            Log.info(difference.x + ", " + difference.y);

            switch (difference.getFloorX()) {
                case 1: {
                    setVelocity(velocity.add(vec2.RIGHT));
                }
                break;
                case -1: {
                    setVelocity(velocity.add(vec2.LEFT));
                }
                break;
            }
            switch (difference.getFloorY()) {
                case 1: {
                    setVelocity(velocity.add(vec2.UP));
                }
                break;
                case -1: {
                    setVelocity(velocity.add(vec2.DOWN));
                }
                break;
            }

            if (loc.getFloorX() == step.getX() && loc.getFloorY() == step.getY() && current_step < current_path.getLength() - 1) {
                advanceStep();
            }
        }
    }

    public void updatePathFinder(int tx, int ty) {
        current_step = 0;

        current_path = path_finder.findPath(this, loc.getFloorX(), loc.getFloorY(), tx, ty);
    }

    public void advanceStep() {
        current_step++;
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
