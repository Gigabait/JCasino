package com.github.wyozi.jtexas.commonsg.net.games;

public enum HoldEmAction {
    Check((byte) 1),
    Call((byte) 2),
    Raise((byte) 3),
    Bet((byte) 4),
    Fold((byte) 5),
    AllIn((byte) 6);

    byte id;

    HoldEmAction(final byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static HoldEmAction getAction(final int id) {
        for (final HoldEmAction act : values()) {
            if (act.getId() == id)
                return act;
        }
        return null;
    }
}
