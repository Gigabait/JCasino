package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.net.io.NetOutputStream;

import java.io.IOException;
import java.util.ArrayList;

public class Packet {

    private final int opcode;
    private final ArrayList<PFragment> fragments = new ArrayList<PFragment>();
    private boolean dataSent = false;

    Packet(final int opcode) {
        this.opcode = opcode;
    }

    public int getSize() {
        int w = 1;
        for (final PFragment frag : fragments) {
            w += frag.getSize();
        }
        return w;
    }

    public boolean isDataSent() {
        return dataSent;
    }

    public Packet addFragment(final PFragment frag) throws IOException {
        if (dataSent)
            throw new IOException("Data already sent in this packet");
        this.fragments.add(frag);
        return this;
    }

    public void sendData(final NetOutputStream output) throws IOException {
        output.writeByte(opcode);
        for (final PFragment frag : fragments) {
            frag.addDataTo(output);
        }
        dataSent = true;
        //output.flush(); // TODO
    }

}
