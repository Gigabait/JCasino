package com.github.wyozi.jtexas.server.chiphandler;

import com.github.wyozi.jtexas.server.MyServerClient;
import com.github.wyozi.jtexas.server.ServerPacketFactory;
import com.github.wyozi.jtexas.server.Table;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class FreeChipHandler extends ChipHandler {


    public FreeChipHandler(int startWithChips, Table table) {
        super();
        this.startWithChips = startWithChips;
        this.table = table;
    }


    ConcurrentHashMap<MyServerClient, Integer> freechipAmount = new ConcurrentHashMap<MyServerClient, Integer>();

    int startWithChips = 1000;
    Table table;

    @Override
    public void addChips(MyServerClient client, int amount, String type,
                         int seat) {
        if (!freechipAmount.containsKey(client)) {
            freechipAmount.put(client, startWithChips);
        }
        freechipAmount.put(client, freechipAmount.get(client) + amount);
        try {
            table.broadcast(ServerPacketFactory.makeChipsAddedPacket((byte) seat, getChipAmount(client)));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getChipAmount(MyServerClient client) {
        if (!freechipAmount.containsKey(client)) {
            freechipAmount.put(client, startWithChips);
        }
        return freechipAmount.get(client);
    }


    @Override
    public void removeChips(MyServerClient client, int amount, String type,
                            int seat) {
        if (!freechipAmount.containsKey(client)) {
            freechipAmount.put(client, startWithChips);
        }
        freechipAmount.put(client, freechipAmount.get(client) - amount);
        try {
            table.broadcast(ServerPacketFactory.makeChipsRemovedPacket((byte) seat, getChipAmount(client)));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
