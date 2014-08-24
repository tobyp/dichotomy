package com.jumppixel.ld30;

/**
 * Created by Tom on 24/08/2014.
 */
public class Notification {

    public String text;
    public Type type;

    public boolean dismissed = false;
    public int offset_x = 20;

    public Notification(String text, Type type) {
        this.text = text;
        this.type = type;
    }

    public void update(int delta_ms) {

    }

    public enum Type {
        INFO,
        WARNING,
        OBJECTIVE
    }
}
