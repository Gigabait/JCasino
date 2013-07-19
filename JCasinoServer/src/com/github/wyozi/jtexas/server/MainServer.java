package com.github.wyozi.jtexas.server;

import com.esotericsoftware.minlog.Log;
import com.github.wyozi.jtexas.commonsg.net.*;
import com.github.wyozi.jtexas.commonsg.net.games.HoldEmOpcodes;
import com.github.wyozi.jtexas.server.db.Database;
import com.github.wyozi.jtexas.server.db.SqliteDatabase;
import com.github.wyozi.jtexas.server.games.holdem.FreeHoldEm;
import com.github.wyozi.jtexas.server.games.holdem.PaidHoldEm;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer implements NetServerListener<MyServerClient> {
    
    public final static short PROTOCOL_VERSION = 3;
    
    NetServer<MyServerClient> server;
    PacketHandler handler;
    
    ExecutorService loginVerifyPool = Executors.newCachedThreadPool();
    final ArrayList<Table> tables = new ArrayList<Table>();
    
    private DBToolkit toolkit;
    
    private int port = 12424;
    
    private boolean hasNext(String[] args, int index) {
    	return index < args.length-1;
    }
    
    public MainServer(String[] args) throws IOException {
    	
    	for (int i = 0;i < args.length; i++) {
    		String w = args[i];
    		if (hasNext(args, i)) {
    			if (w.equals("-port")) {
        			this.port = Integer.valueOf(args[i+=1]);
        			System.out.println("Port set to " + port);
        		}
    		}
    	}
    	
        
        final Scanner inputScanner = new Scanner(System.in);
        
        try {
            Database db = new SqliteDatabase("jdbc:sqlite:server.db");
            this.toolkit = new DBToolkit(db, this);
            
            this.toolkit.createTables();
            
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (final SQLException e) {
            e.printStackTrace();
            return;
        }
        
        handler = new PacketHandler();
        this.server = new NetServer<MyServerClient>(port, this, handler);
        this.server.bind();
        
        Log.info("Server started");

        // Add tables manually

        {
            PaidHoldEm heGame = new PaidHoldEm(toolkit);
            Table heTable = new Table("PaidHoldEm_Beta", heGame, toolkit, 0);

            tables.add(heTable);
            heGame.startLoop();
        }
        
        {
            FreeHoldEm feGame = new FreeHoldEm(toolkit);
            Table feTable = new Table("FreeHoldEm_Beta", feGame, toolkit, 1);

            tables.add(feTable);
            feGame.startLoop();
        }

        /* Don't work properly
        {
            SlotMachine smGame = new SlotMachine(toolkit);
            Table smTable = new Table("SlotMachine_Test", smGame, toolkit, 2);

            tables.add(smTable);
            smGame.startLoop();
        }
        
        {
            FreeBlackjack bjfGame = new FreeBlackjack(toolkit);
            Table bjfTable = new Table("Blackjack_FreeBeta", bjfGame, toolkit, 3);

            tables.add(bjfTable);
            bjfGame.startLoop();
        }
        */
        
        Log.info("Table loops started");
        
        while (inputScanner.hasNext()) {
            final String s = inputScanner.nextLine();
            String[] spl = s.split(" ", 2);
            if (spl[0].equals("quit")) {
                Log.info("Stopping server..");
                try {
                    toolkit.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
                System.exit(0);
                return;
            }
            else if (spl[0].equals("kick")) {
            	if (spl.length > 1) {
            		spl = spl[1].split(" ", 2);
            		if (spl.length < 2) {
                		Log.warn("KickFormat: kick reason name: " + Arrays.toString(spl));
                		continue;
            		}
            		final String user = spl[1].toLowerCase();
            		String reason = spl[0];
                    final ArrayList<MyServerClient> kickable = server.getClients(new Filter<MyServerClient>() {
                        @Override
                        public boolean accept(final MyServerClient element) {
                            if (element.getName() == null)
                                return false;
                            return element.getName().toLowerCase().equals(user);
                        }
                    });
                    if (kickable.size() == 0) {
                    	Log.warn("no " + user + " found to kick");
                    }
                    else {
                    	kickable.get(0).kick(reason);
                    	Log.info(kickable.get(0).getName() + " kicked");
                    }
            	}
            	else {
            		Log.warn("KickFormat: kick reason name");
            	}
            }
            else if (spl[0].equals("say") && spl.length > 1) {
            	server.broadcast(ServerPacketFactory.makeChatPacket("Server: " + spl[1], RankLevel.Server));
            }
        }
        
    }
    

    @Override
    public MyServerClient connected(final Socket socket, final ServerPacketHandler<MyServerClient> handler, final NetServer<MyServerClient> server) throws IOException {
        Log.info(socket.getInetAddress().getHostAddress() + " connected");
        return new MyServerClient(socket, handler, server);
    }

    @Override
    public void disconnected(final NetServerClient clientn) {
        final MyServerClient client = (MyServerClient) clientn;
        removeTablePlayer(client);
        Log.info(client.getName() + " (" + client.getIp() + " disconnected");
    }
    
    private void removeTablePlayer(MyServerClient client) {
		for (Table table : tables) {
			table.game.removeTablePlayer(client);
			table.spectatorLeft(client);
		}
	}

	public class PacketHandler implements ServerPacketHandler<MyServerClient>, HoldEmOpcodes {

        private void handleLoginPacket(final NetInputStream packet,
                                       final MyServerClient client) throws IOException {
            client.send(ServerPacketFactory.makeLoginReceivedPacket());

            final String username = packet.readString();
            final String hash = packet.readString();
            final short protocol = packet.readShort();
            if (protocol != PROTOCOL_VERSION) {
                client.kick("You're using outdated client.");
                return;
            }

            // Get clients with identical name already connected
            final ArrayList<MyServerClient> users = server.getClients(new Filter<MyServerClient>() {
                @Override
                public boolean accept(final MyServerClient element) {
                    if (element.getName() == null)
                        return false;
                    return element.getName().toLowerCase().equals(username.toLowerCase());
                }
            });

            if (users.size() > 0) {
                users.get(0).kick("You have logged in from another location"); // Let's kick the guy already online
            }

            Log.debug(client.getIp() + " identified as " + username);

            loginVerifyPool.execute(new LoginVerifierThread(username, hash,
                    new LoginVerifiedListener() {
                        @Override
                        public void loginVerified(final String user,
                                                  final UserDetails details) {
                            try {
                                if (details == null) {
                                    client.kick("Login server is down. Try again later");
                                } else {
                                    if (!details.valid) {
                                        client.kick("Invalid login details");
                                    } else {
                                        try {
                                            toolkit.verifyDbEntry(user); // See if we exist in database, if not, create new entry
                                        } catch (final SQLException e) {
                                            e.printStackTrace();
                                        }

                                        client.setAuthInfo(details, user);
                                        client.send(ServerPacketFactory
                                                .makeSuccesfulLoginPacket());

                                        final RankLevel userRank = toolkit.getRank(client);

                                        Log.debug("Giving" + user + " rank " + userRank.name());
                                        client.setRank(userRank);

                                        client.send(ServerPacketFactory.makeTableListPacket(tables));

                                        client.send(ServerPacketFactory.makeChatPacket("You've got " + toolkit.getChipAmount(client) + " chips", RankLevel.Server));
                                        client.send(ServerPacketFactory.makeChatPacket("You've got " + toolkit.getBankChipAmount(client) + " chips in bank", RankLevel.Server));
                                        client.send(ServerPacketFactory.makeChatPacket("/deposit amount to store chips to bank", RankLevel.Server));
                                        client.send(ServerPacketFactory.makeChatPacket("/withdraw amount to get chips from bank", RankLevel.Server));

                                        Log.info(user + " (" + client.getIp() + ") connected.");

                                    }
                                }
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }));

        }

        public void handleChatPacket(final NetInputStream packet,
                                     final MyServerClient client) throws IOException {
            final String msg = packet.readString();
            if (msg.length() > 100) {
                client.send(ServerPacketFactory.makeChatPacket("Too long chat message, unable to send.", RankLevel.Server));
            } else {
                if (msg.startsWith("/")) {
                    final String[] cmd = msg.split(" ", 2);
                    if (cmd[0].equals("/kick") && cmd.length > 1 && client.isSuperAdmin()) {
                        final String user = cmd[1].toLowerCase();
                        final ArrayList<MyServerClient> targets = server.getClients(new Filter<MyServerClient>() {
                            @Override
                            public boolean accept(final MyServerClient element) {
                                if (element.getName() == null)
                                    return false;
                                return element.getName().toLowerCase().equals(user);
                            }
                        });
                        if (targets.size() > 0) {
                            final MyServerClient tokick = targets.get(0);
                            tokick.kick("You have been kicked by: " + client.getName());
                            client.send(ServerPacketFactory.makeChatPacket("You kicked " + tokick.getName(), RankLevel.Server));
                        }
                        else {
                            client.send(ServerPacketFactory.makeChatPacket("Couldn't find " + cmd[1] + " to kick", RankLevel.Server));
                        }
                    }
                    else if (cmd[0].equals("/addchips") && cmd.length > 1 && client.isSuperAdmin()) {
                        final String[] spl2 = cmd[1].split(" ");
                        final String user = spl2[0].toLowerCase();
                        int amount;
                        try {
                            amount = Integer.parseInt(spl2[1]);
                        }
                        catch (final NumberFormatException e) {
                            client.send(ServerPacketFactory.makeChatPacket(spl2[1] + " is not an integer", RankLevel.Server));
                            return;
                        }

                        final ArrayList<MyServerClient> targets = server.getClients(new Filter<MyServerClient>() {
                            @Override
                            public boolean accept(final MyServerClient element) {
                                if (element.getName() == null)
                                    return false;
                                return element.getName().toLowerCase().equals(user);
                            }
                        });
                        if (targets.size() > 0) {
                            final MyServerClient player = targets.get(0);
                            toolkit.addBankChips(player, amount, "Given by " + client.getName());
                            client.send(ServerPacketFactory.makeChatPacket("You gave " + player.getName() + " " + amount + " chips", RankLevel.Server));
                        }
                        else {
                            client.send(ServerPacketFactory.makeChatPacket("Couldn't find " + spl2[0] + " to add chips", RankLevel.Server));
                        }
                    }
                    else if ((cmd[0].equals("/deposit") || cmd[0].equals("/withdraw")) && cmd.length > 1) {
                        int amount;
                        try {
                            amount = Integer.parseInt(cmd[1]);
                        }
                        catch (final NumberFormatException e) {
                            client.send(ServerPacketFactory.makeChatPacket(cmd[1] + " is not an integer", RankLevel.Server));
                            return;
                        }
                        final boolean deposit = cmd[0].equals("/deposit");
                        toolkit.bankTransfer(client, amount, deposit, -1);
                    }
                    else {
                        client.send(ServerPacketFactory.makeChatPacket("Command " + cmd[0] + " not found", RankLevel.Server));
                    }
                } else {
                    Table t = getTableOf(client);
                    if (t != null) {
                        final String cMsg = client.getName() + ": " + msg;
                        System.out.println(t.getName() + ">" + cMsg);
                        t.broadcast(ServerPacketFactory.makeChatPacket(cMsg, client.getRank()));
                    }
                }
            }
        }

        @Override
        public void handlePacket(final int opcode, final NetInputStream packet,
                final MyServerClient client) throws IOException {
            
            if (opcode == LOGIN_DETAILS) {
                
                handleLoginPacket(packet, client);
            }
            else if (opcode == REFRESH_TABLES) {
            	Table t = getTableOf(client);
            	if (t != null)
            		t.handlePacket(opcode, packet, client);
            	client.send(ServerPacketFactory.makeTableListPacket(tables));
            }
            else if (opcode == CHAT) {
                handleChatPacket(packet, client);
            }
            else if (opcode == SPECTATE_TABLE) {
            	byte tableId = packet.readByte();
            	Table t = getTableById(tableId);
            	if (t != null) {
            		if (!t.game.allowSpectators() && t.getPlayerCount() >= t.getMaxPlayerCount()) {
            			client.send(ServerPacketFactory.makeInfoPacket("Table is full and doesn't allow spectators"));
            		}
            		else
            			t.spectatorJoined(client);
            	}
            	else {
            		Table oldTable = getTableOf(client);
            		if (oldTable != null) {
            			oldTable.spectatorLeft(client);
            		}
            	}
            }
            else {
            	Table t = getTableOf(client);
            	if (t != null)
            		t.handlePacket(opcode, packet, client);
            }
        }
    }
	
	public Table getTableOf(MyServerClient client) {
		for (Table t : tables) {
			if (t.clients.contains(client))
				return t;
		}
		return null;
	}
	
	public Table getTableById(byte id) {
		for (Table t : tables) {
			if (t.getId() == id)
				return t;
		}
		return null;
	}
    
    public static void main(final String[] args) throws IOException {
        Log.DEBUG();
        new MainServer(args);
    }

}
