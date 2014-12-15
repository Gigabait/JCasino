package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.net.ex.SocketClosedException;
import com.github.wyozi.jtexas.commons.net.io.NetInputStream;

import java.io.IOException;

public class ServerDataHandler {

    public void handle(final NetInputStream input, final ServerPacketHandler inp, final NetServerClient netClient) throws IOException, SocketClosedException {
        final int opcode = input.readByte();
        if (opcode == -1)
            throw new SocketClosedException();

        inp.handlePacket(opcode, input, netClient);
    }
}
