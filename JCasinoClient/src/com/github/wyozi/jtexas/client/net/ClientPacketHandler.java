package com.github.wyozi.jtexas.client.net;

import com.github.wyozi.jtexas.client.MainClient;
import com.github.wyozi.jtexas.client.Table;
import com.github.wyozi.jtexas.commonsg.net.InputPacketHandler;
import com.github.wyozi.jtexas.commonsg.net.NetInputStream;
import com.github.wyozi.jtexas.commonsg.net.games.HoldEmOpcodes;

import java.io.IOException;
import java.util.ArrayList;

public class ClientPacketHandler implements InputPacketHandler, HoldEmOpcodes {
    
    MainClient client;
    
    public ClientPacketHandler(final MainClient client) {
        this.client = client;
    }

    @Override
    public void handlePacket(final int opcode, final NetInputStream packet) throws IOException {
        if (opcode == LOGIN_DETAILS) {
            client.setLoginStatus("Validating login details on the server..");
        }
        else if (opcode == KICK) {
            final String reason = packet.readString();
            client.showError("You got kicked: " + reason);
            client.gotoLogin();
        }
        else if (opcode == INFO) {
            final String reason = packet.readString();
            client.showInfo("Server message: " + reason);
        }
        else if (opcode == SUCCESFUL_LOGIN) {
            client.gotoServerList();
        }
        else if (opcode == CHAT) {
            final String msg = packet.readString();
            final byte chatLevel = packet.readByte();
            client.addChatMsg(msg, chatLevel);
        }
        else if (opcode == TABLE_LIST) {
        	
        	int count = packet.readByte();
        	ArrayList<Table> tables = new ArrayList<Table>();
        	
        	System.out.println("Received " + count + " tables");
        	while (count-- > 0) {
        		String name = packet.readString();
        		String type = packet.readString();
        		byte id = packet.readByte();
        		byte players = packet.readByte();
        		byte maxPlayers = packet.readByte();
        		tables.add(new Table(name, type, id, players, maxPlayers));
        	}
        	
        	client.setTables(tables);
        }
        else if (opcode == SPECTATE_TABLE) {
        	byte gameId = packet.readByte();
        	byte maxPlayers = packet.readByte();
        	client.openGame(GameIdentifiers.getGameClass(gameId, client), maxPlayers);
        }
        else if (client.getGame() != null) {
        	client.getGame().handlePacket(opcode, packet);
        }
        else {
        	System.out.println("Weird opcode in server " + opcode);
        }
    }

}
