package javaquizgame;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import javax.swing.JOptionPane;

public final class Database {

    public static final String DB_URL = "jdbc:sqlite:quizgame.db";
    public Connection dbConnection;

    public Database() {
        initializeDatabase();
    }

    public void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null,
                    "The SQLite JDBC driver was not found on the classpath.\n"
                            + "Add the sqlite-jdbc dependency to Maven and run again.",
                    "Missing Database Driver",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        try {
            dbConnection = DriverManager.getConnection(DB_URL);

            try (Statement stmt = dbConnection.createStatement()) {

                stmt.execute("CREATE TABLE IF NOT EXISTS players ("
                        + "player_id TEXT PRIMARY KEY, "
                        + "player_name TEXT NOT NULL UNIQUE COLLATE NOCASE)");

                stmt.execute("CREATE TABLE IF NOT EXISTS quiz_sessions ("
                        + "session_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "player_id TEXT NOT NULL, "
                        + "player_name TEXT NOT NULL, "
                        + "score INTEGER NOT NULL DEFAULT 0, "
                        + "total_questions INTEGER NOT NULL, "
                        + "status TEXT NOT NULL DEFAULT 'IN_PROGRESS', "
                        + "started_at TEXT NOT NULL, "
                        + "finished_at TEXT)");

            }

            markStaleSessionsAsAbandoned();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not open the quiz database:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void markStaleSessionsAsAbandoned() {
        String sql = "UPDATE quiz_sessions "
                + "SET status = 'ABANDONED', finished_at = ? "
                + "WHERE status = 'IN_PROGRESS'";

        try (PreparedStatement ps =
                     dbConnection.prepareStatement(sql)) {

            ps.setString(1, Instant.now().toString());
            ps.executeUpdate();

        } catch (SQLException ex) {
            // Non-fatal
        }
    }

    public Connection getConnection() {
        return dbConnection;
    }

    public void closeConnection() {
        try {
            if (dbConnection != null) {
                dbConnection.close();
            }
        } catch (SQLException ignored) {
        }
    }
    
    public void abandonActiveSessionOnExit(long currentSessionId) {
        if (currentSessionId != -1 && dbConnection != null) {
            try {
                String sql = "UPDATE quiz_sessions SET status = 'ABANDONED', finished_at = ? "
                        + "WHERE session_id = ? AND status = 'IN_PROGRESS'";
                try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
                    ps.setString(1, Instant.now().toString());
                    ps.setLong(2, currentSessionId);
                    ps.executeUpdate();
                }
            } catch (SQLException ignored) {
                // best effort during shutdown
            }
        }
        try {
            if (dbConnection != null) {
                dbConnection.close();
            }
        } catch (SQLException ignored) {
        }
    }
    
    public void showDbError(String action, SQLException ex) {
        JOptionPane.showMessageDialog(null,
                "A database error occurred while " + action + ":\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}