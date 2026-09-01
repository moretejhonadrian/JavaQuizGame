package javaquizgame;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

// Offline only
public class Scoreboard {

    public static final String DB_URL = "jdbc:sqlite:quizgame.db";

    public Scoreboard() {
        createTable();
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTable() {

        String sql = """
            CREATE TABLE IF NOT EXISTS scoreboard (
                score_id TEXT PRIMARY KEY,
                quiz_stage TEXT NOT NULL,
                score INTEGER NOT NULL
            )
            """;

        try (
            Connection conn = connect();
            Statement statement = conn.createStatement()
        ) {

            statement.execute(sql);
            System.out.println("Offline database is ready.");

        } catch (SQLException e) {
            System.err.println(
                "Error creating offline database: "
                + e.getMessage()
            );
        }
    }

    public void addScore(String quizStage, int score) {

        String scoreId = UUID.randomUUID().toString();

        String sql = """
            INSERT INTO scoreboard
            (score_id, quiz_stage, score)
            VALUES (?, ?, ?)
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement =
                    conn.prepareStatement(sql)
        ) {

            statement.setString(1, scoreId);
            statement.setString(2, quizStage);
            statement.setInt(3, score);

            statement.executeUpdate();

            System.out.println(
                "Score saved! ID: " + scoreId
            );

        } catch (SQLException e) {
            System.err.println(
                "Error saving score: "
                + e.getMessage()
            );
        }
    }
    
    public void deleteScores() {

        String sql = """
            DELETE FROM scoreboard
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement =
                    conn.prepareStatement(sql)
        ) {

            int rows = statement.executeUpdate();

            System.out.println(
                "Deleted " + rows + " score(s)."
            );

        } catch (SQLException e) {

            System.err.println(
                "Error deleting scores: "
                + e.getMessage()
            );
        }
    }
    
    public Map<String, Integer> showAllScores() {

        Map<String, Integer> scores =
                new LinkedHashMap<>();

        String sql = """
            SELECT score_id, score
            FROM scoreboard
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement =
                    conn.prepareStatement(sql)
        ) {

            try (ResultSet result =
                    statement.executeQuery()) {

                while (result.next()) {

                    String id =
                            result.getString("score_id");

                    int score =
                            result.getInt("score");

                    scores.put(id, score);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                "Error getting scores: "
                + e.getMessage()
            );
        }

        return scores;
    }
    
    public static void main(String[] args) {
        Scoreboard scoreboard = new Scoreboard();
        
        scoreboard.addScore("All", 25);
        scoreboard.addScore("All", 24);
        
        Map<String, Integer> scores = scoreboard.showAllScores();
        
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            System.out.println(
                entry.getKey() + ": " + entry.getValue()
            );
        }
        
        scoreboard.deleteScores();
        
        scores = scoreboard.showAllScores();
        
        
        if (scores.isEmpty()) {
            System.out.println("Already deleted");
        }
        
        scoreboard.addScore("All", 25);
        scoreboard.addScore("All", 24);
        
        scores = scoreboard.showAllScores();
        
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            System.out.println(
                entry.getKey() + ": " + entry.getValue()
            );
        }
    }
}