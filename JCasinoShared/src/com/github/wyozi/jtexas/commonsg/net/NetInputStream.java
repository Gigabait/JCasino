package com.github.wyozi.jtexas.commonsg.net;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NetInputStream extends DataInputStream {

    public NetInputStream(final InputStream in) {
        super(in);
    }
    
    public String readString() throws IOException {
        final short length = readShort();
        if (length > 300)
            throw new IOException("Too long string ( > 300 ) ");
        final StringBuffer all = new StringBuffer();
        for (int i = 0;i < length; i++) {
            all.append(readChar());
        }
        return all.toString();
    }
    

}
