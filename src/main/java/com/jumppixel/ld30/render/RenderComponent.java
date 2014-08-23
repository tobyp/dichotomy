package com.jumppixel.ld30.render;

import com.jumppixel.ld30.Component;
import com.jumppixel.ld30.base.TransformComponent;

/**
 * Created by tobyp on 8/23/14.
 */
public class RenderComponent implements Component {
    private TransformComponent transform;
    //sprite data, etc

    public TransformComponent getTransform() {
        return transform;
    }
    public void setTransform(TransformComponent transform) {
        this.transform = transform;
    }
}
