package com.github.wyozi.jtexas.server;

import com.esotericsoftware.minlog.Log;
import com.github.wyozi.jtexas.commons.net.*;
import com.github.wyozi.jtexas.commons.net.io.NetInputStream;
import com.github.wyozi.jtexas.commons.net.games.HoldEmOpcodes;
import com.github.wyozi.jtexas.server.auth.AuthService;
import com.github.wyozi.jtexas.server.auth.Authenticator;
import com.github.wyozi.jtexas.server.auth.services.NoAuthService;
import com.github.wyozi.jtexas.server.db.DatabaseAccess;
import com.github.wyozi.jtexas.server.db.conn.DatabaseConnection;
import com.github.wyozi.jtexas.server.db.conn.SqliteDatabaseConnection;
import com.github.wyozi.jtexas.server.games.holdem.FreeHoldEm;
import com.github.wyozi.jtexas.server.games.holdem.PaidHoldEm;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import javax.inject.Named;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

@Module(injects = Server.class)
public class MainServer {

    @Provides @Named("port") int providePort() {
        return 12424;
    }

    @Provides AuthService provideAuthService() {
        return new NoAuthService();
    }

    @Provides DatabaseConnection provideDatabaseConnection() {
        try {
            return new SqliteDatabaseConnection("jdbc:sqlite:server.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) throws IOException {
        Log.DEBUG();

        ObjectGraph graph = ObjectGraph.create(new MainServer());
        Server server = graph.get(Server.class);

        server.init();
    }

}
