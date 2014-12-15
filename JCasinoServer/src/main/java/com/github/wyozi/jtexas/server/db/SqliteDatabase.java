package com.github.wyozi.jtexas.server.db;

import com.esotericsoftware.minlog.Log;

import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteDatabase extends Database {
    public SqliteDatabase(String path) throws ClassNotFoundException, SQLException {

        Class.forName("org.sqlite.JDBC");

        this.con = DriverManager.getConnection(path);

        Log.info("Database connection established");

    }
}
