package com.github.wyozi.jtexas.commonsg.net;

import java.awt.*;

public enum RankLevel {

    Player(0x00, Color.black),
    Vip(0x01, Color.green),
    Admin(0x02, Color.orange),
    Server(0x03, Color.yellow),
    SuperAdmin(0x04, Color.red);

    int cColor;
    Color color;

    RankLevel(final int rankWorth, final Color chatColor) {
        this.cColor = rankWorth;
        this.color = chatColor;
    }

    public int getRankWorth() {
        return cColor;
    }

    public Color getChatColor() {
        return this.color;
    }

    public static RankLevel getByLevel(final int lvl) {
        for (final RankLevel rl : values()) {
            if (rl.getRankWorth() == lvl)
                return rl;
        }
        return null;
    }
}
