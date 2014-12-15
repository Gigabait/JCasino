package com.github.wyozi.jtexas.server.db;

import com.github.wyozi.jtexas.server.DBToolkit;
import com.github.wyozi.jtexas.server.games.GameBase;
import net.sf.persism.Command;
import net.sf.persism.Query;
import net.sf.persism.annotations.NotMapped;
import net.sf.persism.annotations.TableName;

@TableName(DBToolkit.TABLE_PREFIX + "log_games")
public class GameLogger {

    public GameLogger(Query query, Command command, GameBase game) {
        super();
        this.query = query;
        this.command = command;
        this.game = game;

        this.gameid = game.getType();
        this.starttime = System.currentTimeMillis();
    }

    @NotMapped
    private Query query;
    @NotMapped
    private Command command;
    @NotMapped
    private GameBase game;

    private int id;
    private String gameid;
    private long starttime;

    public void logEvent(String eventname, String eventdesc) {
        command.executeSQL("INSERT INTO " + DBToolkit.TABLE_PREFIX + "log_gameevents (id, eventname, eventdesc, time) VALUES (?, ?, ?, ?)", id, eventname, eventdesc, System.currentTimeMillis());
    }

}
