package com.jumppixel.ld30; /**
 * Created by tobyp on 8/23/14.
 */

import com.jumppixel.ld30.render.RenderSystem;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.*;

public class Engine {
    private List<System> systems = new ArrayList<System>();
    private boolean running = false, stopping = false;

    public void addEntity(Entity entity) {
        for (System s : systems) {
            s.notifyAddEntity(entity);
        }
    }
    public void removeEntity(Entity entity) {
        for (System s : systems) {
            s.notifyRemoveEntity(entity);
        }
    }

    public void run() {
        if (running) return;
        running = true;
        while (!stopping) {
            float delta = 1.0f/60.0f; //TODO delta
            for (System s : systems) {
                s.tick(this, delta);
            }
        }
        running = false;
    }
    public void stop() {
        if (running) stopping = true;
    }

    public boolean isRunning() {
        return running;
    }
    public boolean isStopping() {
        return stopping;
    }

    public static void main(String[] args) {
        Logger l = Logger.getGlobal();
        try {
            Engine e = new Engine();

            RenderSystem rs = new RenderSystem("Ludum Dare 30", 800, 600);

            e.systems.add(rs);
            e.run();
        }
        catch (Exception e) {
            l.log(Level.SEVERE, "Game crash.", e);
        }
    }
}
