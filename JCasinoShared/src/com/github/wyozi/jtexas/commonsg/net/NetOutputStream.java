package com.github.wyozi.jtexas.commonsg.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NetOutputStream extends DataOutputStream {

    public NetOutputStream(final OutputStream out) {
        super(out);
    }
    
    public void writeString(final String string) throws IOException {
    	if (string == null) {
    		writeShort(0);
    		return;
    	}
        writeShort(string.length());
        for (final char c : string.toCharArray()) {
            writeChar(c);
        }
    }
}
