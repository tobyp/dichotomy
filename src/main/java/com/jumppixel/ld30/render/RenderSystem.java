package com.jumppixel.ld30.render;

import com.jumppixel.ld30.*;
import com.jumppixel.ld30.System;
import com.jumppixel.ld30.base.TransformComponent;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by tobyp on 8/23/14.
 */
public class RenderSystem implements System {
    private Map<Entity, RenderComponent> entities = new TreeMap<Entity, RenderComponent>();

    private Logger logger = Logger.getLogger("RenderSystem");

    public RenderSystem(String title, int width, int height) throws Exception {
        try {
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.setTitle(title);
            Display.setFullscreen(false);
            Display.create();

            Mouse.setGrabbed(true);

            glEnable(GL_TEXTURE_2D);
            glDisable(GL_DEPTH_TEST);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, width, height, 0, -1, 1);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            glViewport(0, 0, width, height);
        }
        catch (LWJGLException e) {
            logger.log(Level.SEVERE, "Error initializing Render System", e);
            throw new Exception();
        }
    }

    public void close() {
        Display.destroy();
    }

    @Override
    public void notifyAddEntity(Entity entity) {
        RenderComponent rc = entity.getComponent(RenderComponent.class);
        TransformComponent tc = entity.getComponent(TransformComponent.class);
        if (rc != null && tc != null) {
            rc.setTransform(tc);
            entities.put(entity, rc);
        }
    }

    @Override
    public void notifyRemoveEntity(Entity entity) {
        entities.remove(entity);
    }

    @Override
    public void tick(Engine engine, float delta) {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            engine.stop();
        }

        Display.sync(60); //TODO delta?

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        for (Map.Entry<Entity, RenderComponent> e : entities.entrySet()) {
            RenderComponent rc = e.getValue();
            //render!
        }

        Display.update();
    }
}
