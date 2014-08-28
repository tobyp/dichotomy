package com.jumppixel.dichotomy;

import org.newdawn.slick.SpriteSheet;

/**
 * Created by tobyp on 8/25/14.
 */
public class KeycardDrop extends Drop {
    int keycard;

    private static int spriteForCard(int card) {
        switch (card) {
            case Player.KEYCARD_CYAN:
                return 0;
            case Player.KEYCARD_GREEN:
                return 1;
            case Player.KEYCARD_ORANGE:
                return 2;
            case Player.KEYCARD_PINK:
                return 3;
            case Player.KEYCARD_BLUE:
                return 4;
            default:
                return 5;
        }
    }

    public KeycardDrop(vec2 loc, SpriteSheet spriteSheet, Player player, int keycard) {
        super(loc, spriteSheet.getSprite(spriteForCard(keycard), 1), player, false);
        this.keycard = keycard;
    }

    @Override
    public void pickup() {
        player.keycards |= this.keycard;
    }
}
