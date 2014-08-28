package com.jumppixel.dichotomy;

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
    PLAYER_HURT("sound/player_hurt", "sound/player_hurt_2", "sound/player_hurt_3"),
    PLAYER_HIT("sound/player_hit", "sound/player_hit_2"),
    PLAYER_DIE("sound/player_die"),
    DROP_PICKUP("sound/drop_pickup", "sound/drop_pickup_2"),
    BUTTON_DOWN("sound/button_down"),
    BUTTON_UP("sound/button_up"),
    KEYCARD_LOCK("sound/keycard_lock"),
    KEYCARD_UNLOCK("sound/keycard_unlock"),
    WORLD_CHANGE("sound/world_change"),
    LASER_ON("sound/laser_on"),
    LASER_OFF("sound/laser_off"),
    LASER_CATCH("sound/laser_catch"),
    ROTATE("sound/rotate"),
    CHARGE_COMPLETE("sound/charge_complete"),
    CHARGE_INCOMPLETE("sound/charge_incomplete"),
    CHARGE_HOLD("sound/charge_hold"),
    NARRATION_ADVANCE("sound/narration_advance");

    private Random random = new Random();
    private String[] sound;
    private GameSound(String... sound) {
        this.sound = sound;
    }

    public Audio getAudio() {
        try {
            return AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("src/main/resources/" + sound[random.nextInt(sound.length)] + ".wav"));
        }catch (Exception e) {
            return null;
        }
    }

    public void play(float pitch, float gain) {
        try {
            Audio a = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("src/main/resources/" + sound[random.nextInt(sound.length)] + ".wav"));
            a.playAsSoundEffect(pitch, gain, false);
        }catch (Exception e) {
            Log.info("Couldn't play sound " + name() + " - " + e.getMessage());
        }
    }
}
