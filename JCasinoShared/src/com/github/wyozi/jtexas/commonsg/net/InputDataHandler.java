package com.github.wyozi.jtexas.commonsg.net;

import com.github.wyozi.jtexas.commonsg.net.ex.SocketClosedException;

import java.io.IOException;

public class InputDataHandler {

    public void handle(final NetInputStream input, final InputPacketHandler inp) throws SocketClosedException, IOException {
        int opcode;
        try {
            opcode = input.readByte();
        } catch (final IOException e) {
            throw new SocketClosedException();
        }
        if (opcode == -1)
            throw new SocketClosedException();
        inp.handlePacket(opcode, input);
    }
}
