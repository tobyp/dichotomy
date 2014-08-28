package com.jumppixel.dichotomy;

import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

/**
 * Created by Tom on 25/08/2014.
 */
public class Narration {
    String text;
    Sound sound;

    public Narration(String text, String sound_file_name) {
        this.text = text;
        this.sound = null;
        if (sound_file_name != null) {
            try {
                this.sound = new Sound("src/main/resources/narration/" + sound_file_name);
            } catch (SlickException e) {
                //It's already null
            }
        }
    }

    public Narration(String text) {
        this.text = text;
        this.sound = null;
    }

    public Narration(String text, Sound sound) {
        this.text = text;
        this.sound = sound;
    }

    public void playSound() {
        if (sound != null)
        sound.play();
    }

    public boolean isSoundPlaying() {
        return sound != null ? sound.playing() : false;
    }

    public void stopSound() {
        if (sound != null)
        sound.stop();
    }
}
