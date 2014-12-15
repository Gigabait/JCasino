package com.github.wyozi.jtexas.server;

import com.github.wyozi.jtexas.commons.Card;
import com.github.wyozi.jtexas.commons.net.FragmentFactory;
import com.github.wyozi.jtexas.commons.net.Packet;
import com.github.wyozi.jtexas.commons.net.PacketBuilder;
import com.github.wyozi.jtexas.commons.net.RankLevel;
import com.github.wyozi.jtexas.commons.net.games.HoldEmAction;
import com.github.wyozi.jtexas.commons.net.games.HoldEmOpcodes;

import java.io.IOException;
import java.util.List;


public class ServerPacketFactory {
    public static Packet makeLoginReceivedPacket() {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.LOGIN_DETAILS);
        return packet;
    }

    public static Packet makeSuccesfulLoginPacket() throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.SUCCESFUL_LOGIN);
        return packet;
    }

    public static Packet makeChatPacket(final String msg, final RankLevel player) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.CHAT);
        packet.addFragment(FragmentFactory.newStringFragment(msg));
        packet.addFragment(FragmentFactory.newByteFragment((byte) player.getRankWorth()));
        return packet;
    }

    public static Packet makeInfoPacket(final String msg) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.INFO);
        packet.addFragment(FragmentFactory.newStringFragment(msg));
        return packet;
    }

    public static Packet makeLeaveTableSeatPacket() {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.LEAVE_TABLE_SEAT);
        return packet;
    }

    public static Packet makeJoinTablePacket(final boolean finalAdd) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.JOIN_TABLE);
        packet.addFragment(FragmentFactory.newBooleanFragment(finalAdd));
        return packet;
    }

    public static Packet makeSpectateTablePacket(Table t) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.SPECTATE_TABLE);
        packet.addFragment(FragmentFactory.newByteFragment(t.getGameId()));
        packet.addFragment(FragmentFactory.newByteFragment((byte) t.getMaxPlayerCount()));
        return packet;
    }

    public static Packet makeTableListPacket(final List<Table> tables) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.TABLE_LIST);
        packet.addFragment(FragmentFactory.newByteFragment((byte) tables.size()));
        for (Table t : tables) {
            packet.addFragment(FragmentFactory.newStringFragment(t.getName()));
            packet.addFragment(FragmentFactory.newStringFragment(t.getGameType()));
            packet.addFragment(FragmentFactory.newByteFragment((byte) t.getId()));
            packet.addFragment(FragmentFactory.newByteFragment((byte) t.getPlayerCount()));
            packet.addFragment(FragmentFactory.newByteFragment((byte) t.getMaxPlayerCount()));
        }
        return packet;
    }

    public static Packet makeSeatJoinedPacket(final byte seat, final MyServerClient client, final int chips) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.SEAT_JOINED);
        packet.addFragment(FragmentFactory.newByteFragment(seat));
        packet.addFragment(FragmentFactory.newStringFragment(client.getName()));
        packet.addFragment(FragmentFactory.newIntFragment(chips));
        return packet;
    }

    public static Packet makeSeatLeftPacket(final byte seat) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.SEAT_LEFT);
        packet.addFragment(FragmentFactory.newByteFragment(seat));
        return packet;
    }

    public static Packet makeDealerChosenPacket(final byte seat) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.DEALER_CHOSEN);
        packet.addFragment(FragmentFactory.newByteFragment(seat));
        return packet;
    }

    public static Packet makeShareCardsPacket() {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.SHARE_HIDDEN_CARDS);
        return packet;
    }

    public static Packet makeCleanupPacket() {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.CLEANUP);
        return packet;
    }

    public static Packet makeRevealCardsPacket(final byte seat, final Card card1, final Card card2) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.REVEAL_CARDS);
        packet.addFragment(FragmentFactory.newByteFragment(seat));
        packet.addFragment(FragmentFactory.newByteFragment(Card.toByte(card1)));
        packet.addFragment(FragmentFactory.newByteFragment(Card.toByte(card2)));
        return packet;
    }

    public static Packet makeRevealFlopPacket(final Card card1, final Card card2, final Card card3) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.REVEAL_FLOP);
        packet.addFragment(FragmentFactory.newByteFragment(Card.toByte(card1)));
        packet.addFragment(FragmentFactory.newByteFragment(Card.toByte(card2)));
        packet.addFragment(FragmentFactory.newByteFragment(Card.toByte(card3)));
        return packet;
    }

    public static Packet makeRevealTurnPacket(final Card card1) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.REVEAL_TURN);
        packet.addFragment(FragmentFactory.newByteFragment(Card.toByte(card1)));
        return packet;
    }

    public static Packet makeRevealRiverPacket(final Card card1) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.REVEAL_RIVER);
        packet.addFragment(FragmentFactory.newByteFragment(Card.toByte(card1)));
        return packet;
    }

    public static Packet makeChipsAddedPacket(final byte seat, final int newAmount) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.CHIPS_ADDED);
        packet.addFragment(FragmentFactory.newByteFragment(seat));
        packet.addFragment(FragmentFactory.newIntFragment(newAmount));
        return packet;
    }

    public static Packet makeChipsRemovedPacket(final byte seat, final int newAmount) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.CHIPS_REMOVED);
        packet.addFragment(FragmentFactory.newByteFragment(seat));
        packet.addFragment(FragmentFactory.newIntFragment(newAmount));
        return packet;
    }

    public static Packet makeTurnChangedPacket(final byte seat, final int bid, final boolean allowRaises) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.TURN_CHANGED);
        packet.addFragment(FragmentFactory.newByteFragment(seat));
        packet.addFragment(FragmentFactory.newIntFragment(bid));
        packet.addFragment(FragmentFactory.newBooleanFragment(allowRaises));
        return packet;
    }

    public static Packet makeActionDonePacket(final byte seat, final HoldEmAction action) throws IOException {
        final Packet packet = PacketBuilder.newPacket(HoldEmOpcodes.DO_ACTION);
        packet.addFragment(FragmentFactory.newByteFragment(seat));
        packet.addFragment(FragmentFactory.newIntFragment(action.getId()));
        return packet;
    }
}
