package com.github.wyozi.jtexas.server.games.holdem;

import com.github.wyozi.jtexas.server.db.DatabaseAccess;
import com.github.wyozi.jtexas.server.MyServerClient;
import com.github.wyozi.jtexas.server.Table;
import com.github.wyozi.jtexas.server.chiphandler.PersistentChipHandler;

public class PaidHoldEm extends HoldEmBase {


    public PaidHoldEm(DatabaseAccess db) {
        super(db);
    }

    @Override
    public void setTable(Table table) {
        super.setTable(table);
        this.chipHandler = new PersistentChipHandler(db, table);
    }

    @Override
    public void log_addGame(long startTime) {
    }

    @Override
    public void log_addGameEvent(long startTime, String field, String data) {
        db.log_addGameEvent(startTime, field, data);
    }

    @Override
    public void log_error(String error, MyServerClient client) {
        db.log_error(error, client);
    }
}
