package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.net.games.HoldEmOpcodes;

import java.io.IOException;


public class CommonPacketFactory {
    public static Packet makeKickPacket(final String reason) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.KICK);
        packet.addFragment(FragmentFactory.newStringFragment(reason));
        return packet;
    }
}
