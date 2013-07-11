package com.github.wyozi.jtexas.commonsg.net;

import com.github.wyozi.jtexas.commonsg.net.ex.SocketClosedException;

import java.io.IOException;
import java.net.Socket;

public class NetClient implements Runnable {
    
    private final String host;
    private final int port;
    
    private Socket socket;
    
    private NetInputStream input;
    private NetOutputStream output;
    private final InputPacketHandler packHandler;
    private final InputDataHandler dataHandler;
    
    public NetClient(final String host, final int port, final InputPacketHandler packetHandler) {
        this.host = host;
        this.port = port;
        this.packHandler = packetHandler;
        this.dataHandler = new InputDataHandler();
    }
    
    public void connect() throws IOException {
        if (socket != null)
            throw new IOException("Socket already connected");
        
        socket = new Socket(host, port);
        
        input = new NetInputStream(socket.getInputStream());
        output = new NetOutputStream(socket.getOutputStream());
        
        new Thread(this).start();
    }
    
    public void send(final Packet packet) throws IOException {
        packet.sendData(output);
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            
            try {
                dataHandler.handle(input, packHandler);
            } catch (final SocketClosedException e) {
                e.printStackTrace();
                return; // TODO add notifiers
            } catch (final IOException e) {
                e.printStackTrace();
                return; // TODO add notifiers
            }
            
        }
    }
    
    
}
