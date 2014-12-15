package com.github.wyozi.jtexas.server.games;

import com.github.wyozi.jtexas.commons.net.io.NetInputStream;
import com.github.wyozi.jtexas.commons.net.games.SlotMachineAction;
import com.github.wyozi.jtexas.server.DBToolkit;
import com.github.wyozi.jtexas.server.MyServerClient;

import java.io.IOException;

public class SlotMachine extends GameBase {

    MyServerClient player;

    public SlotMachine(DBToolkit db) {
        super(db);
    }

    @Override
    public void sendWelcomePacket(MyServerClient client) throws IOException {
    }

    @Override
    public boolean isInTable(MyServerClient client) {
        return this.player == client;
    }

    @Override
    public boolean removeTablePlayer(MyServerClient client) {
        if (player == client) {
            player = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean attemptToAddTablePlayer(MyServerClient client)
            throws IOException {
        return this.player == null; // if no player
    }

    @Override
    public void readDoAction(MyServerClient client, NetInputStream input)
            throws IOException {
        SlotMachineAction action = SlotMachineAction.getAction(input.readByte());
        if (action != null) {
            if (action == SlotMachineAction.PullLever) {

            }
        }
    }

    @Override
    public int getPlayerCount() {
        return player == null ? 0 : 1;
    }

    @Override
    public boolean allowSpectators() {
        return false;
    }

    @Override
    public void gameLoop() {

    }

    @Override
    public byte getGameId() {
        return 2;
    }

    @Override
    public String getType() {
        return "SlotMachines";
    }

}
