package com.github.wyozi.jtexas.server.games;

import com.esotericsoftware.minlog.Log;
import com.github.wyozi.jtexas.commons.net.io.NetInputStream;
import com.github.wyozi.jtexas.server.DBToolkit;
import com.github.wyozi.jtexas.server.MyServerClient;
import com.github.wyozi.jtexas.server.Table;

import java.io.IOException;

public abstract class GameBase implements Runnable {

    protected Table table;
    protected DBToolkit db;


    public final byte MAX_PLAYERS_IN_TABLE;

    protected MyServerClient[] tablePlayers;

    public GameBase(DBToolkit db) {
        this.db = db;
        this.MAX_PLAYERS_IN_TABLE = 10;
        this.tablePlayers = new MyServerClient[MAX_PLAYERS_IN_TABLE];
    }

    public void setTable(Table table) {
        this.table = table;
    }

    private volatile boolean running = false;

    public final void startLoop() {
        if (running)
            return;
        running = true;
        new Thread(this).start();
    }

    public abstract void sendWelcomePacket(MyServerClient client) throws IOException;

    public abstract boolean isInTable(MyServerClient client);

    public abstract boolean removeTablePlayer(MyServerClient client);

    public abstract boolean attemptToAddTablePlayer(MyServerClient client) throws IOException;

    public abstract void readDoAction(MyServerClient client, NetInputStream input) throws IOException;

    public int getMaxPlayerCount() {
        return this.MAX_PLAYERS_IN_TABLE;
    }

    public abstract int getPlayerCount();

    public abstract boolean allowSpectators();

    public abstract void gameLoop();

    public final void run() {
        while (running) {
            gameLoop();
        }
    }

    public void debug(String msg) {
        Log.debug(table.getName(), msg);
    }

    public abstract byte getGameId();

    public abstract String getType();
}
