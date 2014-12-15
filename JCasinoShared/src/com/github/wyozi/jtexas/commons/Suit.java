package com.github.wyozi.jtexas.commons;

public enum Suit {
    DIAMONDS, CLUBS, SPADES, HEARTS;

    public static int getValue(final Suit suit) {
        for (int s = 0; s < Suit.values().length; s++) {
            if (Suit.values()[s] == suit)
                return s;
        }
        return -1;
    }

    public static Suit getSuit(final int value) {
        return Suit.values()[value];
    }
}