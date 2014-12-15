package com.github.wyozi.jtexas.server.games;

import com.github.wyozi.jtexas.commonsg.net.ServerPacketHandler;
import com.github.wyozi.jtexas.server.MyServerClient;

public interface GamePacketHandler extends ServerPacketHandler<MyServerClient> {
    public void spectatorJoined(MyServerClient client);

    public void spectatorLeft(MyServerClient client);
}
