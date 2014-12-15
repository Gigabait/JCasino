package com.github.wyozi.jtexas.commonsg.net.games;

public enum BlackJackAction {
    Bet((byte) 1),
    Hit((byte) 2),
    Stand((byte) 3),
    Double((byte) 4),
    Split((byte) 5),
    Surrender((byte) 6);

    byte id;

    BlackJackAction(final byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static BlackJackAction getAction(final int id) {
        for (final BlackJackAction act : values()) {
            if (act.getId() == id)
                return act;
        }
        return null;
    }
}
