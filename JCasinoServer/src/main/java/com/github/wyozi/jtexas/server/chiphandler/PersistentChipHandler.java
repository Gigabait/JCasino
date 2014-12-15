package com.github.wyozi.jtexas.server.chiphandler;

import com.github.wyozi.jtexas.commons.net.RankLevel;
import com.github.wyozi.jtexas.server.DBToolkit;
import com.github.wyozi.jtexas.server.MyServerClient;
import com.github.wyozi.jtexas.server.ServerPacketFactory;
import com.github.wyozi.jtexas.server.Table;

import java.io.IOException;

public class PersistentChipHandler extends ChipHandler {

    public PersistentChipHandler(DBToolkit db, Table t) {
        super();
        this.db = db;
        this.table = t;
    }

    DBToolkit db;
    Table table;

    @Override
    public void addChips(MyServerClient client, int amount, String type,
                         int seat) {
        if (db.addChips(client, amount, type, seat)) {
            if (seat != -1) {
                try {
                    table.broadcast(ServerPacketFactory.makeChipsAddedPacket((byte) seat, getChipAmount(client)));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                client.send(ServerPacketFactory.makeChatPacket("You have been given " + amount + " chips", RankLevel.Server));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            db.logChipEvent(client, amount, type);
        } else {
            System.out.println("Failed to give " + amount + " chips to " + client.getName() + "?");
            db.log_error("Failed to add " + amount + " chips", client);
        }
    }

    @Override
    public int getChipAmount(MyServerClient client) {
        return db.getChipAmount(client);
    }

    @Override
    public void removeChips(MyServerClient client, int amount, String type,
                            int seat) {
        if (db.removeChips(client, amount, type, seat)) {
            if (seat != -1) {
                try {
                    table.broadcast(ServerPacketFactory.makeChipsRemovedPacket((byte) seat, getChipAmount(client)));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                client.send(ServerPacketFactory.makeChatPacket("You have been removed " + amount + " chips", RankLevel.Server));
            } catch (final IOException e) {
                // TODO apparently player quit so no need to do anything
            }
            db.logChipEvent(client, -amount, type);
        } else {
            db.log_error("Failed to remove " + amount + " chips", client);
            System.out.println("Failed to remove " + amount + " chips from " + client.getName() + "?");
        }
    }


}
