package com.github.wyozi.jtexas.server.modules;

import com.github.wyozi.jtexas.commons.net.RankLevel;
import com.github.wyozi.jtexas.commons.net.games.HoldEmOpcodes;
import com.github.wyozi.jtexas.server.*;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Wyozi
 * @since 17.12.2014
 */
public class Chat {

    private final Tables tables;

    @Inject
    public Chat(Tables tables) {
        this.tables = tables;
    }

    public void send(MyServerClient client, String msg, RankLevel rankLevel) throws IOException {
        client.send(ServerPacketFactory.makeChatPacket(msg, rankLevel));
    }

    public void addPacketHandling(MyServerPacketHandler packetHandler) {
        packetHandler.addHandler(HoldEmOpcodes.CHAT, (in, client) -> {
            final String msg = in.readString();

            Table t = tables.getClientTable(client);
            if (t != null) {
                final String cMsg = client.getName() + ": " + msg;
                System.out.println(t.getName() + ">" + cMsg);
                t.broadcast(ServerPacketFactory.makeChatPacket(cMsg, client.getRank()));
            }
        });
    }
}
