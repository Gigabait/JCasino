package com.github.wyozi.jtexas.server;

import com.esotericsoftware.minlog.Log;
import com.github.wyozi.jtexas.commons.net.NetServerClient;
import com.github.wyozi.jtexas.commons.net.ServerPacketHandler;
import com.github.wyozi.jtexas.commons.net.io.NetInputStream;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wyozi
 * @since 16.12.2014
 */
public class MyServerPacketHandler implements ServerPacketHandler<MyServerClient> {

    @Inject
    public MyServerPacketHandler() {}

    private final Map<Integer, SinglePacketHandler> handlers = new HashMap<>();
    private final List<GenericSinglePacketHandler> genericHandlers = new ArrayList<>();

    public void addHandler(int packetOpcode, SinglePacketHandler handler) {
        handlers.put(packetOpcode, handler);
    }
    public void addGenericHandler(GenericSinglePacketHandler gsph) { genericHandlers.add(gsph); }

    @Override
    public void handlePacket(int opcode, NetInputStream packet, MyServerClient client) throws IOException {
        SinglePacketHandler handler = handlers.get(opcode);
        if (handler != null) {
            handler.handle(packet, client);
            return;
        }

        for (GenericSinglePacketHandler gsph : genericHandlers) {
            if (gsph.handle(opcode, packet, client))
                return;
        }

        Log.debug("Unhandled packet " + opcode);
    }

    public static interface SinglePacketHandler {
        public void handle(NetInputStream in, MyServerClient client) throws IOException;
    }
    public static interface GenericSinglePacketHandler {
        public boolean handle(int opcode, NetInputStream in, MyServerClient client) throws IOException;
    }
}
