package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.net.io.NetInputStream;

import java.io.IOException;

public interface ServerPacketHandler<T extends NetServerClient> {
    public void handlePacket(int opcode, NetInputStream packet, T client) throws IOException;
}
