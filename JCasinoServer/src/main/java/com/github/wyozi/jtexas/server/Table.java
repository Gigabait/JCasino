package com.github.wyozi.jtexas.server;

import com.github.wyozi.jtexas.commons.net.io.NetInputStream;
import com.github.wyozi.jtexas.commons.net.Packet;
import com.github.wyozi.jtexas.commons.net.games.HoldEmOpcodes;
import com.github.wyozi.jtexas.server.db.DatabaseAccess;
import com.github.wyozi.jtexas.server.games.GameBase;
import com.github.wyozi.jtexas.server.games.GamePacketHandler;

import java.io.IOException;
import java.util.ArrayList;

public class Table implements GamePacketHandler, HoldEmOpcodes {
    private GameBase game;
    private String tableName;
    private ArrayList<MyServerClient> clients = new ArrayList<MyServerClient>();
    private int id;

    public Table(String tableName, GameBase game, int tableId) {
        this.setTableName(tableName);
        game.setTable(this);
        this.setGame(game);
        this.setId(tableId);
    }

    @Override
    public void handlePacket(int opcode, NetInputStream packet,
                             MyServerClient client) throws IOException {
        if (opcode == LEAVE_TABLE_SEAT) {
            if (getGame().isInTable(client)) {
                getGame().removeTablePlayer(client);
            } else {
                client.send(ServerPacketFactory.makeInfoPacket("You're not in table"));
            }
            client.send(ServerPacketFactory.makeLeaveTableSeatPacket());
        } else if (opcode == JOIN_TABLE) {
            getGame().attemptToAddTablePlayer(client);
            /*

            */
        } else if (opcode == DO_ACTION) {
            getGame().readDoAction(client, packet);
        }
    }

    public void broadcast(Packet packet) throws IOException {
        for (MyServerClient client : getClients()) {
            if (client.isOnline())
                client.send(packet);
        }
    }

    @Override
    public void spectatorJoined(MyServerClient client) {
        this.getClients().add(client);
        try {
            client.send(ServerPacketFactory.makeSpectateTablePacket(this));
            getGame().sendWelcomePacket(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void spectatorLeft(MyServerClient client) {
        this.getClients().remove(client);
    }

    private volatile boolean running = false;
    public void startGameLoop() {
        if (running) return;

        running = true;
        new Thread(game).start();
    }

    public void broadcastAllButOne(Packet packet,
                                   MyServerClient starter) {
        for (MyServerClient client : getClients()) {
            if (client == null)
                continue;
            try {
                client.send(packet);
            } catch (IOException e) {
                spectatorLeft(client);
                System.out.println(client.getName() + "error: ");
                e.printStackTrace();
                continue;
            }
        }
    }

    public String getName() {
        return this.getTableName();
    }

    public int getPlayerCount() {
        return getGame().getPlayerCount();
    }

    public int getMaxPlayerCount() {
        return getGame().getMaxPlayerCount();
    }

    public byte getGameId() {
        return getGame().getGameId();
    }

    public String getGameType() {
        return getGame().getType();
    }

    public int getId() {
        return this.id;
    }

    public GameBase getGame() {
        return game;
    }

    public void setGame(GameBase game) {
        this.game = game;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ArrayList<MyServerClient> getClients() {
        return clients;
    }

    public void setClients(ArrayList<MyServerClient> clients) {
        this.clients = clients;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isFull() {
        return this.getPlayerCount() >= this.getMaxPlayerCount();
    }
}
