package com.github.wyozi.jtexas.server.db.conn;

import com.esotericsoftware.minlog.Log;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlDatabaseConnection extends DatabaseConnection {
    static {
        // Initialize mysql database driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Log.error("Failed to load MySQL database driver!");
        }
    }
    public MysqlDatabaseConnection(String url, String user, String pass) throws SQLException {
        super(DriverManager.getConnection("jdbc:mysql://" + url /* eeh */ + "?"
                + "user=" + user + "&password=" + pass));

    }
}
