package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.net.ex.SocketClosedException;
import com.github.wyozi.jtexas.commons.net.io.NetInputStream;
import com.github.wyozi.jtexas.commons.net.io.NetOutputStream;

import java.io.IOException;
import java.net.Socket;

public abstract class NetServerClient implements Runnable {

    private final Socket socket;

    private final NetInputStream input;
    private final NetOutputStream output;
    private final ServerPacketHandler<? extends NetServerClient> packHandler;
    private final ServerDataHandler dataHandler;
    protected final NetServer<? extends NetServerClient> server;

    public NetServerClient(final Socket socket, final ServerPacketHandler<? extends NetServerClient> packetHandler, final NetServer<? extends NetServerClient> server) throws IOException {
        this.socket = socket;
        this.packHandler = packetHandler;
        this.dataHandler = new ServerDataHandler();
        this.server = server;

        input = new NetInputStream(socket.getInputStream());
        output = new NetOutputStream(socket.getOutputStream());

        new Thread(this).start();
    }

    public void send(final Packet packet) throws IOException {
        if (socket.isClosed())
            throw new IOException("Socket closed");
        packet.sendData(output);
    }

    @Override
    public void run() {
        while (socket.isConnected()) {

            try {
                dataHandler.handle(input, packHandler, this);
            } catch (final IOException e) {
                notifyOfDisconnect();
                break;
            } catch (final SocketClosedException e) {
                notifyOfDisconnect();
                break;
            }

        }
    }

    boolean notifiedAlready = false;

    private void notifyOfDisconnect() {
        if (notifiedAlready)
            return;
        notifiedAlready = true;

        notifyServerOfDisconnect();
    }

    protected abstract void notifyServerOfDisconnect();

    public void kick(final String message) throws IOException {
        if (socket.isClosed())
            return;
        send(CommonPacketFactory.makeKickPacket(message));
        socket.close();
        notifyOfDisconnect();
    }

    public Socket getSocket() {
        return this.socket;
    }

    public String getIp() {
        return this.getSocket().getInetAddress().getHostAddress();
    }
}
