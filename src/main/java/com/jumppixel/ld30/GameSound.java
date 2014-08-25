package com.jumppixel.ld30;

import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

import java.util.Random;

/**
 * Created by Tom on 25/08/2014.
 */
public enum GameSound {

    MUSIC_CONSTANCE("music/constance"),
    MUSIC_HITMAN("music/hitman"),
    PLAYER_HURT("player_hurt", "player_hurt_2", "player_hurt_3"),
    PLAYER_HIT("player_hit", "player_hit_2"),
    PLAYER_DIE("player_die"),
    DROP_PICKUP("drop_pickup", "drop_pickup_2"),
    BUTTON_DOWN("button_down"),
    BUTTON_UP("button_up"),
    KEYCARD_LOCK("keycard_lock"),
    KEYCARD_UNLOCK("keycard_unlock"),
    WORLD_CHANGE("world_change"),
    LASER_ON("laser_on"),
    LASER_OFF("laser_off"),
    LASER_CATCH("laser_catch"),
    ROTATE("rotate"),
    CHARGE_COMPLETE("charge_complete"),
    CHARGE_INCOMPLETE("charge_incomplete"),
    CHARGE_HOLD("charge_hold"),
    NARRATION_ADVANCE("narration_advance");

    private Random random = new Random();
    private String[] sound;
    private GameSound(String... sound) {
        this.sound = sound;
    }

    public Audio getAudio() {
        try {
            return AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("src/main/resources/sounds/" + sound[random.nextInt(sound.length)] + ".wav"));
        }catch (Exception e) {
            return null;
        }
    }

    public void play(float pitch, float gain) {
        try {
            Audio a = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("src/main/resources/sounds/" + sound[random.nextInt(sound.length)] + ".wav"));
            a.playAsSoundEffect(pitch, gain, false);
        }catch (Exception e) {
            Log.info("Couldn't play sound " + name() + " - " + e.getMessage());
        }
    }
}
