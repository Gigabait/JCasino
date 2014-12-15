package com.github.wyozi.jtexas.server.chiphandler;

import com.github.wyozi.jtexas.server.MyServerClient;

public abstract class ChipHandler {
    public abstract void addChips(MyServerClient client, int amount, String type,
                                  int seat);

    public abstract int getChipAmount(MyServerClient client);

    public abstract void removeChips(MyServerClient client, int amount, String type,
                                     int seat);
}
