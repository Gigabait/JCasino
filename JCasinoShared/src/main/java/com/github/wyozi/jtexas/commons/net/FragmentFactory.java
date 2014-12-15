package com.github.wyozi.jtexas.commons.net;

import com.github.wyozi.jtexas.commons.net.io.NetOutputStream;

import java.io.IOException;

public class FragmentFactory {
    private FragmentFactory() {
    }

    public static PFragment newStringFragment(final String string) {
        return new PFragment() {
            @Override
            public int getSize() {
                return 2 + string.length();
            }

            @Override
            public void addDataTo(final NetOutputStream output) throws IOException {
                output.writeString(string);
            }
        };
    }

    public static PFragment newByteFragment(final byte b) {
        return new PFragment() {
            @Override
            public int getSize() {
                return 1;
            }

            @Override
            public void addDataTo(final NetOutputStream output) throws IOException {
                output.writeByte(b);
            }
        };
    }

    public static PFragment newBooleanFragment(final boolean b) {
        return new PFragment() {
            @Override
            public int getSize() {
                return 1;
            }

            @Override
            public void addDataTo(final NetOutputStream output) throws IOException {
                output.writeBoolean(b);
            }
        };
    }

    public static PFragment newIntFragment(final int i) {
        return new PFragment() {
            @Override
            public int getSize() {
                return 4;
            }

            @Override
            public void addDataTo(final NetOutputStream output) throws IOException {
                output.writeInt(i);
            }
        };
    }

    public static PFragment newShortFragment(final short s) {
        return new PFragment() {
            @Override
            public int getSize() {
                return 2;
            }

            @Override
            public void addDataTo(final NetOutputStream output) throws IOException {
                output.writeShort(s);
            }
        };
    }
}
