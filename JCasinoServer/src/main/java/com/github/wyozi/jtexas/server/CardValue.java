package com.github.wyozi.jtexas.server;

public enum CardValue {

    StraightFlush(10),
    FourOfKind(9),
    FullHouse(8),
    Flush(7),
    Straight(6),
    ThreeOfKind(5),
    TwoPairs(4),
    Pair(3),
    High(1);

    int value;

    CardValue(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
