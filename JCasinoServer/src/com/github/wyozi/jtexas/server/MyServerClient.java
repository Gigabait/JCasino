package com.github.wyozi.jtexas.server;

import com.esotericsoftware.minlog.Log;
import com.github.wyozi.jtexas.commons.Card;
import com.github.wyozi.jtexas.commonsg.net.NetServer;
import com.github.wyozi.jtexas.commonsg.net.NetServerClient;
import com.github.wyozi.jtexas.commonsg.net.RankLevel;
import com.github.wyozi.jtexas.commonsg.net.ServerPacketHandler;
import com.github.wyozi.jtexas.commonsg.net.games.HoldEmAction;

import java.io.IOException;
import java.net.Socket;

public class MyServerClient extends NetServerClient {

    private UserDetails details = null;
    private String name;
    private Card[] hand = null;

    private RankLevel rank = RankLevel.Player;

    private HoldEmAction gAction = null;

    private NetServer<MyServerClient> oServer;

    public MyServerClient(final Socket socket, final ServerPacketHandler<MyServerClient> packetHandler,
                          final NetServer<MyServerClient> server) throws IOException {
        super(socket, packetHandler, server);
        this.oServer = server;
    }

    public boolean isAuthenticated() {
        return details != null;
    }

    public void setAuthInfo(final UserDetails details, final String name) {
        this.details = details;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Card[] getHand() {
        return hand;
    }

    public void setHand(final Card[] hand) {
        this.hand = hand;
    }


    public void setRank(final RankLevel rankLevel) {
        this.rank = rankLevel;
    }

    public RankLevel getRank() {
        return this.rank;
    }

    public boolean isVip() {
        return this.rank.getRankWorth() >= RankLevel.Vip.getRankWorth();
    }

    public boolean isAdmin() {
        return this.rank.getRankWorth() >= RankLevel.Admin.getRankWorth();
    }

    public boolean isSuperAdmin() {
        return this.rank.getRankWorth() >= RankLevel.SuperAdmin.getRankWorth();
    }

    public HoldEmAction getAction() {
        return this.gAction;
    }

    public void setGAction(final HoldEmAction act) {
        this.gAction = act;
    }

    @Override
    public void kick(final String message) throws IOException {
        super.kick(message);
        Log.info("Kicking " + getName() + " for " + message);
    }

    public boolean isOnline() {
        return this.oServer.isOnline(this) && this.getSocket().isConnected();
    }

    @Override
    protected void notifyServerOfDisconnect() {
        server.disconnectNotify(this);
    }

}
