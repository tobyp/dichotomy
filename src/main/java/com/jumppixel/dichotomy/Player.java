package com.jumppixel.dichotomy;

import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.openal.Audio;

/**
 * Created by tobyp on 8/23/14.
 */
public class Player extends LivingEntity {
    public final static int KEYCARD_CYAN = 0x1;
    public final static int KEYCARD_GREEN = 0x2;
    public final static int KEYCARD_ORANGE = 0x4;
    public final static int KEYCARD_PINK = 0x8;
    public final static int KEYCARD_BLUE = 0x10;
    Dichotomy game;
    float charge = 0.0f;
    float max_charge = 1.0f;
    float charge_hold = 0.0f;
    boolean allow_charging = false;
    boolean charge_holding = false;
    boolean has_device = false;
    int charge_ms = 0;
    int charge_interval = 100; //+0.01 charge every interval
    int keycards = 0;

    int fade_ms = 0;

    Audio charge_hold_sound;

    boolean debug_mode = false;

    float attack_strength = 0.075f;
    float attack_distance = 1.3f;
    int attack_duration = 800;
    //time since last attack
    int attack_timer_ms = attack_duration;

    public Player(Dichotomy game, vec2 loc, SpriteSheet sprites, vec2 render_offset, float move_speed, int num_ani_frames) {
        super(loc, sprites, render_offset, 1.0f, move_speed, num_ani_frames, null);
        this.game = game;
        this.charge_hold_sound = GameSound.CHARGE_HOLD.getAudio();
    }

    @Override
    public void update(World world, int delta_ms) {
        super.update(world, delta_ms);

        if (fade_ms > 0) {
            fade_ms = fade_ms - delta_ms;
            if (fade_ms < 0) {
                fade_ms = 0;
            }
        }

        if (this.allow_charging) {
            charge_ms = charge_ms + delta_ms;
            if (charge_ms >= charge_interval) {
                if (this.charge + 0.01f > this.max_charge && this.charge != this.max_charge) {
                    this.charge = this.max_charge;
                    GameSound.CHARGE_COMPLETE.play(1, 1);
                } else if (this.charge < this.max_charge) {
                    this.charge = this.charge + 0.01f;
                }
                charge_ms = charge_ms - charge_interval;
            }
        }else{
            charge_ms = 0;
        }

        if (this.charge_holding && this.charge == this.max_charge) {
            if (this.charge_hold == 0) {
                this.charge_hold_sound.playAsSoundEffect(1, 1, false);
            }
            if (this.charge_hold + ((float)delta_ms)/1000 > 1) {
                this.charge_hold = 1.0f;
                this.charge_holding = false;
                this.charge = 0.f;
                this.fade_ms = 1000;
                game.switchWorld();
            }else{
                this.charge_hold = this.charge_hold + ((float) delta_ms)/1000;
            }
        }else{
            if (this.charge_hold_sound.isPlaying() && this.charge_hold > 0) {
                charge_hold_sound.stop();
            }
            this.charge_holding = false;
            this.charge_hold = 0;
        }

        attack_timer_ms = Math.max(attack_timer_ms + delta_ms, attack_duration);
    }

    public Entity getTargetedEntity(World world) {
        Entity candidate = null;
        float rotangle = rotation.getRotAngle();
        for (Entity e : world.entities) {
            if (e.loc.getDistance(loc) <= attack_distance) {
                if (candidate != null) {
                    if (Math.abs(e.rotation.getRotAngle() - rotangle) > Math.abs(candidate.rotation.getRotAngle() - rotangle)) {
                        continue;
                    }
                }
                candidate = e;
            }
        }
        return candidate;
    }

    public void doAttack(World world) {
        if (attack_timer_ms < attack_duration) return;
        Entity e = getTargetedEntity(world);
        if (e == null || !(e instanceof LivingEntity)) return;
        attack_timer_ms = 0;
        ((LivingEntity)e).takeDamage(attack_strength);
        GameSound.PLAYER_HIT.play(1, 1);
    }

    @Override
    public void onTileChange(World world, vec2 old_loc, vec2 new_loc) {
        Map.MapObject mo = world.map.getObject(world.ogroup, new_loc);
        if (mo != null && mo.getType().equals("trigger")) {
            if (mo.getPropertyBool("consumed", "false")) return;
            if (mo.getPropertyBool("onetime", "false")) mo.setPropertyBool("consumed", true);
            world.game.executeActions(mo.getProperty("actions", ""));
        }
    }

    @Override
    public void takeDamage(float attack_strength) {
        super.takeDamage(attack_strength);
        GameSound.PLAYER_HURT.play(1, 1);
    }

    @Override
    public void die() {
        GameSound.PLAYER_DIE.play(1, 1);
        game.reset();
    }
}
