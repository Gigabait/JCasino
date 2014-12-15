package com.github.wyozi.jtexas.server;

import com.github.wyozi.jtexas.commonsg.net.NetInputStream;
import com.github.wyozi.jtexas.commonsg.net.Packet;
import com.github.wyozi.jtexas.commonsg.net.games.HoldEmOpcodes;
import com.github.wyozi.jtexas.server.games.GameBase;
import com.github.wyozi.jtexas.server.games.GamePacketHandler;

import java.io.IOException;
import java.util.ArrayList;

public class Table implements GamePacketHandler, HoldEmOpcodes {
    GameBase game;
    String tableName;
    ArrayList<MyServerClient> clients = new ArrayList<MyServerClient>();
    DBToolkit db;
    int id;

    public Table(String tableName, GameBase game, DBToolkit db, int tableId) {
        this.tableName = tableName;
        game.setTable(this);
        this.game = game;
        this.db = db;
        this.id = tableId;
    }

    @Override
    public void handlePacket(int opcode, NetInputStream packet,
                             MyServerClient client) throws IOException {
        if (opcode == LEAVE_TABLE_SEAT) {
            if (game.isInTable(client)) {
                game.removeTablePlayer(client);
            } else {
                client.send(ServerPacketFactory.makeInfoPacket("You're not in table"));
            }
            client.send(ServerPacketFactory.makeLeaveTableSeatPacket());
        } else if (opcode == JOIN_TABLE) {
            game.attemptToAddTablePlayer(client);
            /*

            */
        } else if (opcode == DO_ACTION) {
            game.readDoAction(client, packet);
        }
    }

    public void broadcast(Packet packet) throws IOException {
        for (MyServerClient client : clients) {
            if (client.isOnline())
                client.send(packet);
        }
    }

    @Override
    public void spectatorJoined(MyServerClient client) {
        this.clients.add(client);
        try {
            client.send(ServerPacketFactory.makeSpectateTablePacket(this));
            game.sendWelcomePacket(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void spectatorLeft(MyServerClient client) {
        this.clients.remove(client);
    }

    public void broadcastAllButOne(Packet packet,
                                   MyServerClient starter) {
        for (MyServerClient client : clients) {
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
        return this.tableName;
    }

    public int getPlayerCount() {
        return game.getPlayerCount();
    }

    public int getMaxPlayerCount() {
        return game.getMaxPlayerCount();
    }

    public byte getGameId() {
        return game.getGameId();
    }

    public String getGameType() {
        return game.getType();
    }

    public int getId() {
        return this.id;
    }
}
