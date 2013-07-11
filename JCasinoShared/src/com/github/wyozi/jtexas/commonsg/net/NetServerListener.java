package com.github.wyozi.jtexas.commonsg.net;

import java.io.IOException;
import java.net.Socket;

public interface NetServerListener<T extends NetServerClient> {
    public T connected(Socket socket, ServerPacketHandler<T> packHandler, NetServer<T> server) throws IOException;
    public void disconnected(NetServerClient client); // TODO somehow make generic
}
