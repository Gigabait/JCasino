package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.util.Filter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetServer<T extends NetServerClient> implements Runnable {

    private final int port;

    private ServerSocket socket;
    private final NetServerListener<T> listener;

    private final List<T> clients = Collections.synchronizedList(new ArrayList<T>());

    private final ServerPacketHandler<T> packHandler;

    public NetServer(final int port, final NetServerListener<T> listener, final ServerPacketHandler<T> packetHandler) {
        this.port = port;
        this.listener = listener;
        this.packHandler = packetHandler;
    }

    public void bind() throws IOException {
        if (socket != null)
            throw new IOException("Socket already bound");

        socket = new ServerSocket(port);

        new Thread(this).start();
    }

    public void broadcast(final Packet packet) throws IOException {
        for (final T client : clients) {
            client.send(packet);
        }
    }

    public void broadcastAllButOne(final Packet packet, final T skip) throws IOException {
        for (final T client : clients) {
            if (client == skip) {
                continue;
            }
            client.send(packet);
        }
    }

    public boolean isOnline(final T client) {
        return clients.contains(client);
    }

    public ArrayList<T> getClients(final Filter<T> filter) {
        final ArrayList<T> ret = new ArrayList<T>();
        for (final T el : clients) {
            if (filter.accept(el)) {
                ret.add(el);
            }
        }
        return ret;
    }

    @Override
    public void run() {
        while (socket.isBound()) {

            try {
                final Socket newSocket = socket.accept();

                final T newClient = listener.connected(newSocket, packHandler, this);
                if (newClient != null) {
                    clients.add(
                            newClient
                    );
                }

            } catch (final IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void disconnectNotify(final NetServerClient client) {
        clients.remove(client);
        listener.disconnected(client);
    }


}
