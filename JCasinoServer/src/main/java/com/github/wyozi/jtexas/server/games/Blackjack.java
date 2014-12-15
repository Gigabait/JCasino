package com.github.wyozi.jtexas.server.games;

import com.github.wyozi.jtexas.commons.net.io.NetInputStream;
import com.github.wyozi.jtexas.server.DBToolkit;
import com.github.wyozi.jtexas.server.MyServerClient;
import com.github.wyozi.jtexas.server.ServerPacketFactory;
import com.github.wyozi.jtexas.server.chiphandler.ChipHandler;

import java.io.IOException;

public abstract class Blackjack extends GameBase {

    public Blackjack(DBToolkit db) {
        super(db);
    }

    ChipHandler chipHandler;

    @Override
    public void sendWelcomePacket(MyServerClient client) throws IOException {
        byte seat = 0;
        for (final MyServerClient client2 : tablePlayers) {
            if (client2 != null) {
                client.send(ServerPacketFactory.makeSeatJoinedPacket(seat, client2, chipHandler.getChipAmount(client2)));
            }
            seat++;
        }
    }

    @Override
    public boolean isInTable(MyServerClient client) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeTablePlayer(MyServerClient client) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean attemptToAddTablePlayer(MyServerClient client) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void readDoAction(MyServerClient client, NetInputStream input) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public int getPlayerCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean allowSpectators() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void gameLoop() {
        // TODO Auto-generated method stub

    }

    @Override
    public byte getGameId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract void log_error(String error, MyServerClient client);

    public abstract void log_addGame(final long startTime);

    public abstract void log_addGameEvent(final long startTime, String field, final String data);

}
