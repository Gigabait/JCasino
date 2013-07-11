package com.github.wyozi.jtexas.commonsg.net;

import java.io.IOException;

public interface InputPacketHandler {
    public void handlePacket(int opcode, NetInputStream packet) throws IOException;
}
