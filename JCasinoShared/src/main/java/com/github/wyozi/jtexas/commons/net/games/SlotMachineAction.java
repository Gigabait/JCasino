package com.github.wyozi.jtexas.commons.net.games;

public enum SlotMachineAction {
    PullLever((byte) 1);

    byte id;

    SlotMachineAction(final byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static SlotMachineAction getAction(final int id) {
        for (final SlotMachineAction act : values()) {
            if (act.getId() == id)
                return act;
        }
        return null;
    }
}
