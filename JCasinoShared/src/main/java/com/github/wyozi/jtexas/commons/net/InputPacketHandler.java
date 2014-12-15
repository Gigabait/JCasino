package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.net.io.NetInputStream;

import java.io.IOException;

public interface InputPacketHandler {
    public void handlePacket(int opcode, NetInputStream packet) throws IOException;
}
