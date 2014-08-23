package com.jumppixel.ld30;

/**
 * Created by tobyp on 8/23/14.
 */
public interface System {
    public void notifyAddEntity(Entity entity);
    public void notifyRemoveEntity(Entity entity);
    public void tick(Engine engine, float delta);
}
