package com.jumppixel.ld30;

import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tobyp on 8/23/14.
 */
public class Entity {
    private List<Component> components;

    public Entity(Component... components) {
        this.components = new ArrayList<Component>(components.length);
        for (Component comp : components) {
            this.components.add(comp);
        }
    }

    public <T> T getComponent(Class<T> clazz) {
        for (Component c : components) {
            if (clazz.isAssignableFrom(c.getClass())) return (T)c;
        }
        return null;
    }
}
