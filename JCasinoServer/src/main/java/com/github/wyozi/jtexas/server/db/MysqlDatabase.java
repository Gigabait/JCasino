package com.github.wyozi.jtexas.server.db;

import com.esotericsoftware.minlog.Log;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MysqlDatabase extends Database {
    public MysqlDatabase(String url, String user, String pass) throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.jdbc.Driver");

        this.con = DriverManager.getConnection("jdbc:mysql://" + url /* eeh */ + "?"
                + "user=" + user + "&password=" + pass);

        Log.info("Database connection established");

    }
}
