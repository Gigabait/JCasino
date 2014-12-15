package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.net.ex.SocketClosedException;
import com.github.wyozi.jtexas.commons.net.io.NetInputStream;

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
