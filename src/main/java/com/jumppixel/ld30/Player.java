package com.jumppixel.ld30;

import org.newdawn.slick.SpriteSheet;

/**
 * Created by tobyp on 8/23/14.
 */
public class Player extends LivingEntity {
    float charge = 0.0f;
    float max_charge = 1.0f;
    float charge_hold = 0.0f;
    boolean allow_charging = false;
    boolean charge_holding = false;
    boolean has_device = false;
    int charge_ms = 0;
    int charge_interval = 100; //+0.01 charge every interval

    boolean debug_mode = false;

    public Player(vec2 loc, SpriteSheet sprites, vec2 render_offset, float move_speed, int num_ani_frames) {
        super(loc, sprites, render_offset, 1.0f, move_speed, num_ani_frames, null);
    }

    @Override
    public void update(World world, int delta_ms) {
        super.update(world, delta_ms);

        if (this.allow_charging) {
            charge_ms = charge_ms + delta_ms;
            if (charge_ms >= charge_interval) {
                if (this.charge + 0.01f > this.max_charge && this.charge != this.max_charge) {
                    this.charge = this.max_charge;
                } else if (this.charge < this.max_charge) {
                    this.charge = this.charge + 0.01f;
                }
                charge_ms = charge_ms - charge_interval;
            }
        }else{
            charge_ms = 0;
        }

        if (this.charge_holding && this.charge == this.max_charge) {
            if (this.charge_hold + ((float)delta_ms)/1000 > 1) {
                this.charge_hold = 1.0f;
                this.charge_holding = false;
                //TODO: Teleport player
            }else{
                this.charge_hold = this.charge_hold + ((float) delta_ms)/1000;
            }
        }else{
            this.charge_holding = false;
            this.charge_hold = 0;
        }
    }

    @Override
    public void die() {
        charge = 0;
        //TODO: Respawn code
    }
}
