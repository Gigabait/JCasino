package com.github.wyozi.jtexas.client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public static String byteArrayToHexString(final byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static String toSHA1(final String string) throws Exception {
        final byte[] convertme = string.getBytes("UTF-8");

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return byteArrayToHexString(md.digest(convertme));
    }
}
