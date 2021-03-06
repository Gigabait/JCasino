package com.github.wyozi.jtexas.server.db;

import com.esotericsoftware.minlog.Log;
import com.github.wyozi.jtexas.commons.net.RankLevel;
import com.github.wyozi.jtexas.server.MyServerClient;
import com.github.wyozi.jtexas.server.ServerPacketFactory;
import com.github.wyozi.jtexas.server.db.conn.DatabaseConnection;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseAccess {
    public static final String TABLE_PREFIX = "jt_";

    public static final String TABLE_USERS = TABLE_PREFIX + "users";

    DatabaseConnection conn;

    @Inject
    public DatabaseAccess(DatabaseConnection conn) {
        this.conn = conn;
    }

    public boolean addChips(final MyServerClient client, final int amount, final String logType, int seat) {
        if (amount < 0) {
            log_error("Trying to add negative chips " + amount, client);
            return false;
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("UPDATE " + TABLE_USERS + " SET chipamount = chipamount + ? WHERE name = ?;");
            stmt.setInt(1, amount);
            stmt.setString(2, client.getName());
            final int rs = stmt.executeUpdate();
            return rs > 0;
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean removeChips(final MyServerClient client, final int amount, final String logType, int seat) {
        if (amount < 0) {
            log_error("Trying to remove negative chips " + amount, client);
            return false;
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("UPDATE " + TABLE_USERS + " SET chipamount = chipamount - ? WHERE name = ?;");
            stmt.setInt(1, amount);
            stmt.setString(2, client.getName());
            final int rs = stmt.executeUpdate();
            return rs > 0;
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean addBankChips(final MyServerClient client, final int amount, final String logType) {
        if (amount < 0) {
            log_error("Trying to add negative chips to bank " + amount, client);
            return false;
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("UPDATE " + TABLE_PREFIX + "bank SET bankchips = bankchips + ? WHERE name = ?;");
            stmt.setInt(1, amount);
            stmt.setString(2, client.getName());
            final int rs = stmt.executeUpdate();
            if (rs > 0) {
                try {
                    client.send(ServerPacketFactory.makeChatPacket("You have been added " + amount + " chips to your bank", RankLevel.Server));
                    client.send(ServerPacketFactory.makeChatPacket("Use /withdraw " + amount + " to withdraw them", RankLevel.Server));
                } catch (final IOException e) {
                    // apparently player quit so no need to do anything
                }
                logChipEvent(client, amount, logType);
            } else {
                log_error("Failed to add " + amount + " bankchips", client);
                Log.warn("Failed to add " + amount + " chips to " + client.getName() + "'s bank?");
            }
            return rs > 0;
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean removeBankChips(final MyServerClient client, final int amount, final String logType) {
        if (amount < 0) {
            log_error("Trying to remove negative amount of chips from bank " + amount, client);
            return false;
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("UPDATE " + TABLE_PREFIX + "bank SET bankchips = bankchips - ? WHERE name = ?;");
            stmt.setInt(1, amount);
            stmt.setString(2, client.getName());
            final int rs = stmt.executeUpdate();
            if (rs > 0) {
                try {
                    client.send(ServerPacketFactory.makeChatPacket("You have been removed " + amount + " chips in bank", RankLevel.Server));
                } catch (final IOException e) {
                    // TODO apparently player quit so no need to do anything
                }
                logChipEvent(client, -amount, logType);
            } else {
                log_error("Failed to remove " + amount + " bankchips", client);
                Log.warn("Failed to remove " + amount + " chips from " + client.getName() + "'s bank?");
            }
            return rs > 0;
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static final int MAX_CHIPS = 50000;

    public void bankTransfer(final MyServerClient client, final int amount, final boolean deposit, int seat) throws IOException {

        if (deposit) {

            if (getChipAmount(client) < amount) {
                client.send(ServerPacketFactory.makeChatPacket("You have too little funds to deposit " + amount, RankLevel.Server));
                return;
            }

            removeChips(client, amount, "Bankdeposit", seat);
            addBankChips(client, amount, "Bankdeposit");

        } else {

            if (getBankChipAmount(client) < amount) {
                client.send(ServerPacketFactory.makeChatPacket("You have too little funds to withdraw " + amount, RankLevel.Server));
                return;
            }
            if (MAX_CHIPS < getChipAmount(client) + amount) {
                client.send(ServerPacketFactory.makeChatPacket("You can have max " + MAX_CHIPS + " chips ingame at time", RankLevel.Server));
                return;
            }

            removeBankChips(client, amount, "Bankwithdraw");
            addChips(client, amount, "Bankwithdraw", seat);

        }

    }

    public int getChipAmount(final MyServerClient client) {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement("SELECT chipamount FROM " + TABLE_USERS + " WHERE name = ?;");
            stmt.setString(1, client.getName());
            rs = stmt.executeQuery();
            if (!rs.next())
                return 0;
            return rs.getInt("chipamount");
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    public int getBankChipAmount(final MyServerClient client) {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement("SELECT bankchips FROM " + TABLE_PREFIX + "bank WHERE name = ?;");
            stmt.setString(1, client.getName());
            rs = stmt.executeQuery();
            if (!rs.next())
                return 0;
            return rs.getInt("bankchips");
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    public RankLevel getRank(final MyServerClient client) {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement("SELECT rank FROM " + TABLE_USERS + " WHERE name = ?;");
            stmt.setString(1, client.getName());
            rs = stmt.executeQuery();
            if (!rs.next())
                return RankLevel.Player;
            return RankLevel.getByLevel(rs.getInt("rank"));
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (rs != null) rs.close();
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }

        return RankLevel.Player;
    }

    public void logChipEvent(final MyServerClient client, final int amount, final String type) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("INSERT INTO " + TABLE_PREFIX + "log_chipevents (name, amount, type) VALUES (?, ?, ?);");
            stmt.setString(1, client.getName());
            stmt.setInt(2, amount);
            stmt.setString(3, type);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void log_addGameEvent(final long startTime, String field, final String data) {

        if (true) // TODO Fix  [SQLITE_ERROR] SQL error or missing database (no such column: events)
            return;

        // TODO sanitize field? It is not user input but developer might've screwed up something.

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("UPDATE " + TABLE_PREFIX + "log_games SET " + field + " = CONCAT(" + field + ", ?) WHERE starttime = ?;");
            stmt.setString(1, data);
            stmt.setLong(2, startTime);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() throws SQLException {
        this.conn.close();
    }

    public void log_error(final String error, final MyServerClient client) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("INSERT INTO " + TABLE_PREFIX + "log_errors (name, time, error) VALUES (?, ?, ?);");
            stmt.setString(1, client.getName());
            stmt.setLong(2, System.currentTimeMillis());
            stmt.setString(3, error);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean nameExistsInDb(final String name) throws SQLException {
        final PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + TABLE_USERS + " WHERE name = ?;");
        stmt.setString(1, name);
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            stmt.close();
            if (rs != null) rs.close();
        }
    }

    public void verifyDbEntry(final String name) throws SQLException {

        if (nameExistsInDb(name)) {
            Log.debug(name + " DOES exist in conn");
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("INSERT INTO " + TABLE_PREFIX + "bank (name, bankchips) VALUES (?, 0);");
            stmt.setString(1, name);
            stmt.executeUpdate();
            stmt = conn.prepareStatement("INSERT INTO " + TABLE_PREFIX + "userdata (name, chipamount) VALUES (?, 0);");
            stmt.setString(1, name);
            stmt.executeUpdate();
        } finally {
            if (stmt != null)
                stmt.close();
        }

        Log.debug(name + " entry created in conn");
    }

    public void createTables() throws SQLException {
        final String[] tableNames = new String[]{
                TABLE_USERS,
                TABLE_PREFIX + "log_games",
                TABLE_PREFIX + "log_gameevents",
                TABLE_PREFIX + "log_chipevents",
                TABLE_PREFIX + "log_errors",
                TABLE_PREFIX + "bank"};
        final String[] tableFields = new String[]{
                "name TEXT, password TEXT, chipamount INT, rank INT",
                "id INTEGER PRIMARY KEY AUTOINCREMENT, gameid TEXT, starttime BIGINT",
                "id INT, eventname TEXT, eventdesc TEXT, time BIGINT",
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, amount INT, type TEXT",
                "name TEXT, time BIGINT, error TEXT",
                "name TEXT, bankchips INT"};

        final Statement stmt = conn.createStatement();
        for (int i = 0; i < tableNames.length; i++) {
            String stmts = "CREATE TABLE IF NOT EXISTS " + tableNames[i] + "(" + tableFields[i] + ");";
            stmt.execute(stmts);
        }

        stmt.close();
    }
}
