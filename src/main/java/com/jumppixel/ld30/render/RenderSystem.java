package com.jumppixel.ld30.render;

import com.jumppixel.ld30.*;
import com.jumppixel.ld30.System;
import com.jumppixel.ld30.base.TransformComponent;

import java.util.Map;

/**
 * Created by tobyp on 8/23/14.
 */
public class RenderSystem implements System {
    private Map<Entity, RenderComponent> entities;

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
        for (Map.Entry<Entity, RenderComponent> e : entities.entrySet()) {
            RenderComponent rc = e.getValue();
            //render!
        }
    }
}
