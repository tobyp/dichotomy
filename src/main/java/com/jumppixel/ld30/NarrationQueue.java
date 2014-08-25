package com.jumppixel.ld30;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Tom on 25/08/2014.
 */
public class NarrationQueue implements Iterator {
    int current = 0;
    List<Narration> queue;

    public NarrationQueue() {
        queue = new ArrayList<Narration>();
    }

    public void add(Narration n) {
        queue.add(n);
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
}
