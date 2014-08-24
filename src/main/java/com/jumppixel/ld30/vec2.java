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

    public vec2 add(vec2 o) {
        return new vec2(this.x + o.x, this.y + o.y);
    }

    public vec2 add(float x, float y) {
        return new vec2(this.x + x, this.y + y);
    }

    public vec2 sub(vec2 o) {
        return new vec2(this.x - o.x, this.y - o.y);
    }

    public vec2 sub(float x, float y) {
        return new vec2(this.x - x, this.y - y);
    }

    public vec2 mul(float f) {
        return new vec2(x*f, y*f);
    }

    public vec2 negate() {
        return new vec2(-this.x, -this.y);
    }

    public vec2 floor() {
        return new vec2((float)Math.floor(this.x), (float)Math.floor(this.y));
    }

    public int getFloorX() {
        return (int)Math.floor(this.x);
    }

    public int getFloorY() {
        return (int)Math.floor(this.y);
    }

    public int getRotInt() {
        if (this.x == 0.f) {
            if (this.y == 0.f) {
                return -1;
            }
            else if (this.y < 0.f) {
                return 4;
            }
            else {
                return 0;
            }
        }
        else if (this.x < 0.f) {
            if (this.y == 0.f) {
                return 2;
            }
            else if (this.y < 0.f) {
                return 3;
            }
            else {
                return 1;
            }
        }
        else {
            if (this.y == 0.f) {
                return 6;
            }
            else if (this.y < 0.f) {
                return 5;
            }
            else {
                return 7;
            }
        }
    }

    public vec2 getRot() {
        return new vec2(this.x == 0.f ? 0.f : (this.x < 0.f ? -1.f : 1.f), this.y == 0.f ? 0.f : (this.y < 0.f ? -1.f : 1.f));
    }

    public vec2 getFaced(vec2 rotation) {
        int faced_x = getFloorX();
        int faced_y = getFloorY();

        faced_x += rotation.x;
        faced_y += rotation.y;

        return new vec2(faced_x, faced_y);
    }

    public int walk_dirs() {
        int wd = 0;
        if (x < 0.f) wd |=0x8;
        else if (x > 0.f) wd |=0x2;
        if (y < 0.f) wd |=0x1;
        else if (y > 0.f) wd |=0x4;
        return wd;
    }

    public vec2 withX(float x) {
        return new vec2(x, this.y);
    }

    public vec2 withY(float y) {
        return new vec2(this.x, y);
    }

    public boolean isZero() {
        return x == 0.f && y == 0.f;
    }

    @Override
    public String toString() {
        return "vec2("+Float.toString(x)+"; "+Float.toString(y)+")";
    }

    public static final vec2 ZERO = new vec2(0.f, 0.f);
    public static final vec2 UP = new vec2(0.f, -1.f);
    public static final vec2 DOWN = new vec2(0.f, 1.f);
    public static final vec2 LEFT = new vec2(-1.f, 0.f);
    public static final vec2 RIGHT = new vec2(1.f, 0.f);

}
