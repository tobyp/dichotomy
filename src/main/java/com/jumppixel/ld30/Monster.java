package com.jumppixel.ld30;

import org.newdawn.slick.*;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;

import java.util.logging.Logger;

/**
 * Created by Tom on 24/08/2014.
 */
public class Monster extends LivingEntity implements Mover {

    AStarPathFinder path_finder;
    Path current_path;
    int current_step = 0;

    int recalc_interval = 500;
    int try_move_interval = 20;

    int recalc_ms = 0;
    int try_move_ms = 0;

    float attack_strength = 0.05f;
    float attack_distance = 1.1f;
    int attack_duration = 1000;
    //time since last attack
    int attack_timer_ms = 0;

    World world;
    Player player;

    public Monster(vec2 loc, SpriteSheet sprites, vec2 render_offset, float max_health, float walk_speed, int num_ani_frames, World world, Player player) {
        super(loc, sprites, render_offset, max_health, walk_speed, num_ani_frames, player);
        this.world = world;
        this.player = player;

        path_finder = new AStarPathFinder(world, 10, false);
    }

    @Override
    public void update(World world, int delta_ms) {
        super.update(world, delta_ms);

        recalc_ms = recalc_ms + delta_ms;
        if (recalc_ms >= recalc_interval) {
            recalc_ms = recalc_ms - recalc_interval;
            if (player.loc.getFloorX() != loc.getFloorX() || player.loc.getFloorY() != loc.getFloorY()) {
                updatePathFinder(player.loc.getFloorX(), player.loc.getFloorY());
            }
        }

        try_move_ms = try_move_ms + delta_ms;
        if (try_move_ms >= try_move_interval) {
            try_move_ms = try_move_ms - try_move_interval;
            if (current_path != null && !(player.loc.getFloorX() == loc.getFloorX() && player.loc.getFloorY() == loc.getFloorY())) {
                Path.Step step = current_path.getStep(current_step);


                vec2 vstep = new vec2(step.getX() + .5f, step.getY() + .5f);
                vec2 difference = vstep.sub(loc);

                setVelocity(difference.getBasicDirection());

                rotation = difference.getBasicDirection();

                if (loc.getFloorX() == step.getX() && loc.getFloorY() == step.getY() && current_step < current_path.getLength() - 1) {
                    advanceStep();
                }
                if (player.loc.x == loc.x && player.loc.y == loc.y && recalc_ms % 50 == 0) {
                    setVelocity(loc.sub(player.loc).getBasicDirection());
                }
            }else{
                setVelocity(vec2.ZERO);
            }
        }

        attack_timer_ms = attack_timer_ms - (attack_timer_ms - delta_ms < 0 ? attack_timer_ms : delta_ms);
        if (attack_timer_ms == 0 && loc.getDistance(player.loc) <= attack_distance && world == player.game.world) {
            attack_timer_ms = attack_duration;
            player.takeDamage(attack_strength);
        }
    }

    public void updatePathFinder(int tx, int ty) {
        current_step = 0;

        //Logger.getLogger("AI").info("Pathfinding from "+loc.toString()+" to ("+Integer.toString(tx)+";"+Integer.toString(ty)+")");
        if (current_path != null) {
            int length = current_path.getLength();
            for(int i = 0; i < length; i++) {
                //world.map.setTileId(current_path.getStep(i).getX(), current_path.getStep(i).getY(), 2, 0);
            }
        }
        current_path = path_finder.findPath(this, loc.getFloorX(), loc.getFloorY(), tx, ty);
        if (current_path != null) {
            int length = current_path.getLength();
            //System.out.println("Path " + length);
            for(int i = 0; i < length; i++) {
                //world.map.setTileId(current_path.getStep(i).getX(), current_path.getStep(i).getY(), 2, 777);
                //System.out.println("\t" + current_path.getX(i) + "," + current_path.getY(i));
            }
        }
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

        if (player.debug_mode) {
            if (recalc_ms <= 10) {
                g.setColor(Color.yellow);
                g.fillRect(pixel_location.x + render_offset.x + 3, pixel_location.y + render_offset.y + 4, 2, 1);
            }
            if (player.loc.getFloorX() != loc.getFloorX() || player.loc.getFloorY() != loc.getFloorY()) {
                g.setColor(Color.green);
                g.fillRect(pixel_location.x + render_offset.x + 3 + 4, pixel_location.y + render_offset.y + 4, 8, 1);
            }
            if (current_path == null) {
                g.setColor(Color.red);
                g.fillRect(pixel_location.x + render_offset.x + 3 + 12, pixel_location.y + render_offset.y + 4, 6, 1);
            }
        }
    }

    @Override
    public void die() {
        world.entities.remove(this);
    }
}
