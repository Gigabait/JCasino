package com.github.wyozi.jtexas.server.games.holdem;

import com.github.wyozi.jtexas.commons.net.RankLevel;
import com.github.wyozi.jtexas.server.DBToolkit;
import com.github.wyozi.jtexas.server.MyServerClient;
import com.github.wyozi.jtexas.server.ServerPacketFactory;
import com.github.wyozi.jtexas.server.Table;
import com.github.wyozi.jtexas.server.chiphandler.FreeChipHandler;

import java.io.IOException;

public class FreeHoldEm extends HoldEmBase {

    public FreeHoldEm(DBToolkit db) {
        super(db);
    }

    @Override
    public void setTable(Table table) {
        super.setTable(table);
        this.chipHandler = new FreeChipHandler(1000, table);
    }

    @Override
    public void log_addGame(long startTime) {
        db.log_addGame(startTime);
    }

    @Override
    public void log_addGameEvent(long startTime, String field, String data) {
        db.log_addGameEvent(startTime, field, data);
    }

    @Override
    public void log_error(String error, MyServerClient client) {
        db.log_error("(free)" + error, client);
    }

    public void sendWelcomePacket(MyServerClient client) throws IOException {
        super.sendWelcomePacket(client);
        client.send(ServerPacketFactory.makeChatPacket("You have been given " + chipHandler.getChipAmount(client) + " f2p chips", RankLevel.Server));
    }
}
