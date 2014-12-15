package com.github.wyozi.jtexas.commons.net;

public interface GameOpcodes {
    // Client -> Server
    public static final int LEAVE_GAME = 0x04;
    public static final int LEAVE_TABLE = 0x06;
    public static final int REFRESH_TABLES = 0x07;

    // Server -> Client
    public static final int KICK = 0x30;
    public static final int SUCCESFUL_LOGIN = 0x31;
    public static final int INFO = 0x32;
    public static final int SEAT_JOINED = 0x33;
    public static final int SEAT_LEFT = 0x34;
    public static final int TABLE_LIST = 0x46;

    // Both
    public static final int LOGIN_DETAILS = 0x61;
    public static final int CHAT = 0x62;
    public static final int JOIN_TABLE = 0x63;
    public static final int LEAVE_TABLE_SEAT = 0x64;
    public static final int DO_ACTION = 0x65;
    public static final int SPECTATE_TABLE = 0x66;
}
