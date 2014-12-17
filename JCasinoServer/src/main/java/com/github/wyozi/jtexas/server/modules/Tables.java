package com.github.wyozi.jtexas.server.modules;

import com.github.wyozi.jtexas.commons.net.games.HoldEmOpcodes;
import com.github.wyozi.jtexas.server.*;
import com.github.wyozi.jtexas.server.games.GameBase;
import com.github.wyozi.jtexas.server.games.holdem.PaidHoldEm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * @author Wyozi
 * @since 17.12.2014
 */
public class Tables {
    private List<Table> tableList = new ArrayList<>();
    private AtomicInteger tableIdCounter = new AtomicInteger();

    @Inject
    public Tables() {}

    public Table getTable(Predicate<Table> pred) {
        for (Table t: tableList)
            if (pred.test(t))
                return t;
        return null;
    }

    public Table getClientTable(MyServerClient client) {
        return getTable(t -> t.getClients().contains(client));
    }

    public void sendListTo(MyServerClient client) throws IOException {
        client.send(ServerPacketFactory.makeTableListPacket(tableList));
    }

    public void addPacketHandling(MyServerPacketHandler packetHandler) {
        packetHandler.addHandler(HoldEmOpcodes.REFRESH_TABLES, (in, client) -> sendListTo(client));
        packetHandler.addHandler(HoldEmOpcodes.SPECTATE_TABLE, (in, client) -> {
            final byte tableId = in.readByte();

            if (tableId == -1) {
                Table oldTable = getClientTable(client);
                if (oldTable != null) oldTable.spectatorLeft(client);
            }
            else {
                Table table = getTable(t -> t.getId() == tableId);

                if (!table.getGame().allowSpectators() && table.isFull())
                    client.send(ServerPacketFactory.makeInfoPacket("Table is full"));
                else
                    table.spectatorJoined(client);
            }
        });
    }

    public void create(String title, GameBase game) {
        int id = tableIdCounter.getAndIncrement();

        Table heTable = new Table(title, game, id);
        heTable.startGameLoop();

        tableList.add(heTable);
    }
}
