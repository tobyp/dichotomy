package com.jumppixel.ld30;

/**
 * Created by tobyp on 8/23/14.
 */
public class vec2 {
    public final float x, y;

    public vec2() {
        this.x = 0.f;
        this.y = 0.f;
    }

    public vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public vec2 add(float x, float y) {
        return new vec2(this.x + x, this.y + y);
    }

    public vec2 add(vec2 o) {
        return new vec2(this.x + o.x, this.y + o.y);
    }

    public vec2 mul(float f) {
        return new vec2(x*f, y*f);
    }

    public vec2 negate() {
        return new vec2(-this.x, -this.y);
    }

    @Override
    public String toString() {
        return "vec2("+Float.toString(x)+"; "+Float.toString(y)+")";
    }
}
