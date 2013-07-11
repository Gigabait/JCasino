package com.github.wyozi.jtexas.commonsg.net;

import java.io.IOException;

public interface ServerPacketHandler<T extends NetServerClient> {
    public void handlePacket(int opcode, NetInputStream packet, T client) throws IOException;
}
