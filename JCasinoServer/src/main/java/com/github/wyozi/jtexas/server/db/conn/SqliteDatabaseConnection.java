package com.github.wyozi.jtexas.server.db.conn;

import com.esotericsoftware.minlog.Log;

import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteDatabaseConnection extends DatabaseConnection {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Log.error("Failed to load SQLite database driver!");
        }
    }
    public SqliteDatabaseConnection(String path) throws SQLException {
        super(DriverManager.getConnection(path));
    }
}
