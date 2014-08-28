package com.jumppixel.dichotomy;

import org.apache.commons.lang.WordUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Tom on 25/08/2014.
 */
public class NarrationQueue implements Iterator {
    int current = -1;
    List<Narration> queue;

    public NarrationQueue() {
        queue = new ArrayList<Narration>();
    }

    public void add(Narration n) {
        String[] s = WordUtils.wrap(n.text, 26, "@#@", true).split("@#@");
        int i = 0;
        for (String string : s) {
            queue.add(new Narration(string, (i == 0 ? n.sound : null)));
            i++;
        }
    }

    @Override
    public boolean hasNext() {
        return current + 1 < queue.size();
    }

    @Override
    public Narration next() {
        current++;
        return queue.get(current);
    }

    @Override
    public void remove() {
        queue.remove(current);
    }

    public Narration current() {
        if (current > -1) return queue.get(current);
        return null;
    }
}
