package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.net.io.NetOutputStream;

import java.io.IOException;

public interface PFragment {
    public int getSize();

    public void addDataTo(NetOutputStream output) throws IOException;
}
