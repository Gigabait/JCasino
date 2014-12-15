package com.github.wyozi.jtexas.client;

public class Table {
    public String name;
    public int players;
    public int maxPlayers;
    public int id;
    public String type;

    public Table(String n, String type, int id, int p, int mp) {
        this.name = n;
        this.type = type;
        this.id = id;
        this.players = p;
        this.maxPlayers = mp;
    }

    public String toString() {
        return this.name;
    }
}
