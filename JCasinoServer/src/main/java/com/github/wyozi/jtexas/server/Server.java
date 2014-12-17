package com.github.wyozi.jtexas.server;

import com.esotericsoftware.minlog.Log;
import com.github.wyozi.jtexas.commons.net.NetServer;
import com.github.wyozi.jtexas.commons.net.RankLevel;
import com.github.wyozi.jtexas.commons.net.games.HoldEmOpcodes;
import com.github.wyozi.jtexas.server.games.holdem.FreeHoldEm;
import com.github.wyozi.jtexas.server.games.holdem.PaidHoldEm;
import com.github.wyozi.jtexas.server.modules.Chat;
import com.github.wyozi.jtexas.server.auth.Authenticator;
import com.github.wyozi.jtexas.server.db.DatabaseAccess;
import com.github.wyozi.jtexas.server.modules.Tables;
import dagger.Module;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Wyozi
 * @since 16.12.2014
 */
@Module
@Singleton
public class Server {
    public final static short PROTOCOL_VERSION = 3;

    @Inject
    Chat chat;

    @Inject
    public Tables tables;

    @Inject
    @Named("port")
    int port;

    @Inject
    Authenticator authenticator;

    @Inject
    public
    DatabaseAccess db;

    NetServer<MyServerClient> netServer;

    @Inject
    MyServerPacketHandler packetHandler;

    MyNetServerListener netServerListener = new MyNetServerListener();

    public void init() throws IOException {
        Log.info("Creating tables");
        try {
            db.createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Log.info("Starting socket");
        netServer = new NetServer<>(port, netServerListener, packetHandler);
        netServer.bind();

        Log.info("Adding packet handlers");
        addHandlers();

        Log.info("Adding tables");

        {
            tables.create("Paid Hold em", new PaidHoldEm(db));
            tables.create("Free Hold em", new FreeHoldEm(db));
        }

    }

    private void addHandlers() {
        packetHandler.addHandler(HoldEmOpcodes.LOGIN_DETAILS, (in, client) -> {
            client.send(ServerPacketFactory.makeLoginReceivedPacket());

            final String username = in.readString();
            final String hash = in.readString();
            final short protocol = in.readShort();
            if (protocol != Server.PROTOCOL_VERSION) {
                client.kick("Outdated client");
                return;
            }

            if (netServer.getClients().stream().filter(c -> username.equals(c.getName())).findAny().isPresent()) {
                client.kick("Duplicate username");
                return;
            }

            authenticator.auth(username, hash, res -> {
                if (res == null) {
                    client.kick("AuthRes null. Login server might be down.");
                    return;
                }

                if (!res.valid) {
                    client.kick("Invalid login");
                    return;
                }

                try {
                    db.verifyDbEntry(res.user);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }

                client.setAuthInfo(res, username);
                client.send(ServerPacketFactory.makeSuccesfulLoginPacket());

                final RankLevel userRank = db.getRank(client);

                Log.debug("Giving" + username + " rank " + userRank.name());
                client.setRank(userRank);

                tables.sendListTo(client);

                chat.send(client, "You've got " + db.getChipAmount(client) + " chips", RankLevel.Server);
                chat.send(client, "You've got " + db.getBankChipAmount(client) + " chips in bank", RankLevel.Server);
                chat.send(client, "/deposit amount to store chips to bank", RankLevel.Server);
                chat.send(client, "/withdraw amount to get chips from bank", RankLevel.Server);

                Log.info(res.user + " (" + client.getIp() + ") connected.");
            });
        });

        chat.addPacketHandling(packetHandler);
        tables.addPacketHandling(packetHandler);

        packetHandler.addGenericHandler((op, in, client) -> {
            Table clientTable = tables.getClientTable(client);
            clientTable.handlePacket(op, in, client);

            return true;
        });
    }
}