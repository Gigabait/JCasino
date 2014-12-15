package com.github.wyozi.jtexas.server.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * You must initialize {@link Database#con} inside the constructor!
 */
public abstract class Database {

    protected java.sql.Connection con = null;

    public Statement old_stmt() throws SQLException {
        return this.con.createStatement();
    }

    public PreparedStatement pstmt(final String sql) throws SQLException {
        return this.con.prepareStatement(sql);
    }

    public void close() throws SQLException {
        this.con.close();
    }
}
