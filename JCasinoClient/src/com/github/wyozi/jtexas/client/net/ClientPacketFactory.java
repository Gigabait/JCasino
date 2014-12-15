package com.github.wyozi.jtexas.client.net;

import com.github.wyozi.jtexas.client.MainClient;
import com.github.wyozi.jtexas.commonsg.net.FragmentFactory;
import com.github.wyozi.jtexas.commonsg.net.Packet;
import com.github.wyozi.jtexas.commonsg.net.PacketBuilder;
import com.github.wyozi.jtexas.commonsg.net.games.HoldEmAction;
import com.github.wyozi.jtexas.commonsg.net.games.HoldEmOpcodes;

import java.io.IOException;


public class ClientPacketFactory {
    public static Packet makeLoginPacket(final String user, final String hash) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.LOGIN_DETAILS);
        packet.addFragment(FragmentFactory.newStringFragment(user));
        packet.addFragment(FragmentFactory.newStringFragment(hash));
        packet.addFragment(FragmentFactory.newShortFragment(MainClient.PROTOCOL_VERSION));
        return packet;
    }

    public static Packet makeChatPacket(final String msg) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.CHAT);
        packet.addFragment(FragmentFactory.newStringFragment(msg));
        return packet;
    }

    public static Packet makeLeaveTableSeatPacket() {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.LEAVE_TABLE_SEAT);
        return packet;
    }

    public static Packet makeJoinTablePacket() {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.JOIN_TABLE);
        return packet;
    }

    public static Packet makeHoldEmActionPacket(final HoldEmAction act, final short bid) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.DO_ACTION);

        packet.addFragment(FragmentFactory.newByteFragment(act.getId()));
        packet.addFragment(FragmentFactory.newShortFragment(bid));

        return packet;
    }

    public static Packet makeSpectateTablePacket(int id) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.SPECTATE_TABLE);

        packet.addFragment(FragmentFactory.newByteFragment((byte) id));

        return packet;
    }

    public static Packet makeRefreshTablesPacket() {
        return PacketBuilder.newPacket(HoldEmOpcodes.REFRESH_TABLES);
    }
}
