package com.github.wyozi.jtexas.client.net;

import com.github.wyozi.jtexas.client.MainClient;
import com.github.wyozi.jtexas.client.gamescript.Game;
import com.github.wyozi.jtexas.client.gamescript.holdem.HoldEm;

public class GameIdentifiers {
    public static Game getGameClass(int id, MainClient client) {
        switch (id) {
            case 1:
                return new HoldEm(client);
            case 2:
                return new com.github.wyozi.jtexas.client.gamescript.SlotMachines(client);
        }
        return null;
    }
}
