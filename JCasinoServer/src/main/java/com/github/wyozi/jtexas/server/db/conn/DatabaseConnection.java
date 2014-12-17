package com.github.wyozi.jtexas.server.db.conn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DatabaseConnection {
    private java.sql.Connection con;

    protected DatabaseConnection(Connection con) {
        this.con = con;
    }

    public Statement createStatement() throws SQLException {
        return this.con.createStatement();
    }

    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return this.con.prepareStatement(sql);
    }

    public void close() throws SQLException {
        this.con.close();
    }
}
