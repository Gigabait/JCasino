package com.github.wyozi.jtexas.server;

import com.github.wyozi.jtexas.commons.net.*;
import com.github.wyozi.jtexas.commons.net.ServerPacketHandler;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Wyozi
 * @since 16.12.2014
 */
public class MyNetServerListener implements NetServerListener<MyServerClient> {
    @Override
    public MyServerClient connected(Socket socket, ServerPacketHandler<MyServerClient> packHandler, NetServer<MyServerClient> server) throws IOException {
        return new MyServerClient(socket, packHandler, server);
    }

    @Override
    public void disconnected(NetServerClient client) {

    }
}
