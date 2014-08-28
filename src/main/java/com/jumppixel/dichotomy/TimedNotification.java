package com.jumppixel.dichotomy;

/**
 * Created by Tom on 24/08/2014.
 */
public class TimedNotification extends Notification{

    int ms_ran = 0;
    int dismiss_thresh = 0;

    public TimedNotification(String text, int ms, Type type) {
        super(text, type);
        dismiss_thresh = ms;
    }

    @Override
    public void update(int delta_ms) {
        ms_ran = ms_ran + delta_ms;
        if (ms_ran>dismiss_thresh && !dismissed) {
            dismissed = true;
        }
    }
}
