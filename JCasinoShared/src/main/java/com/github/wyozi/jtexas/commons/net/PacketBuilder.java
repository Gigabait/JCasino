package com.github.wyozi.jtexas.commons.net;

public class PacketBuilder {
    private PacketBuilder() {
    }

    public static Packet newPacket(final int opcode) {
        return new Packet(opcode);
    }
}
