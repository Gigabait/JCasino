package com.github.wyozi.jtexas.commonsg.net;

import java.io.IOException;

public interface PFragment {
    public int getSize();
    public void addDataTo(NetOutputStream output) throws IOException;
}
