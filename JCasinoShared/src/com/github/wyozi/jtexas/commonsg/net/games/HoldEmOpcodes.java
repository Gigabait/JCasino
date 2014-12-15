package com.github.wyozi.jtexas.commonsg.net.games;

import com.github.wyozi.jtexas.commonsg.net.GameOpcodes;

public interface HoldEmOpcodes extends GameOpcodes {

    // Server -> Client

    public static final int DEALER_CHOSEN = 0x35;
    public static final int SHARE_HIDDEN_CARDS = 0x36;
    public static final int REVEAL_CARDS = 0x37;
    public static final int REVEAL_FLOP = 0x38;
    public static final int REVEAL_TURN = 0x39;
    public static final int REVEAL_RIVER = 0x40;

    public static final int CHIPS_ADDED = 0x41;
    public static final int CHIPS_REMOVED = 0x42;
    public static final int TURN_CHANGED = 0x43;

    public static final int CLEANUP = 0x44;

    public static final int UPDATE_POT = 0x45;

}
