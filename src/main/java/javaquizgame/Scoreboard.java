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

        String playersSql = """
            CREATE TABLE IF NOT EXISTS players (
                player_id TEXT PRIMARY KEY,
                player_name TEXT NOT NULL UNIQUE,
                current_player BOOLEAN DEFAULT FALSE,
                total_score INTEGER NOT NULL,
                rank INTERGER NOT NULL
            )
            """;

        String scoreboardSql = """
            CREATE TABLE IF NOT EXISTS scoreboard (
                score_id TEXT PRIMARY KEY,
                player_id TEXT NOT NULL,
                quiz_stage TEXT NOT NULL,
                score INTEGER NOT NULL,
                FOREIGN KEY (player_id) REFERENCES players(id)
            )
            """;

        try (
            Connection conn = connect();
            Statement statement = conn.createStatement()
        ) {

            // create players table
            statement.execute(playersSql);

            // create scoreboard table
            statement.execute(scoreboardSql);

            System.out.println("Offline database is ready.");

        } catch (SQLException e) {

            System.err.println(
                "Error creating offline database: "
                + e.getMessage()
            );
        }
    }

    public void addScore(String player_id, String quiz_stage, int score) {

        String score_id = UUID.randomUUID().toString();

        String sql = """
            INSERT INTO scoreboard
            (score_id, player_id, quiz_stage, score)
            VALUES (?, ?, ?, ?)
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setString(1, score_id);
            statement.setString(2, player_id);
            statement.setString(3, quiz_stage);
            statement.setInt(4, score);

            statement.executeUpdate();

            System.out.println(
                "Score saved! ID: " + score_id
            );

        } catch (SQLException e) {
            System.err.println(
                "Error saving score: "
                + e.getMessage()
            );
        }
    }
    
    public void deleteScores(String player_id) {

        String sql = """
            DELETE FROM scoreboard WHERE player_id = ?
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement =
                    conn.prepareStatement(sql)
        ) {
            
            statement.setObject(1, UUID.fromString(player_id));
            
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
    
    public Map<String, Integer> showAllScores(String player_id) {

        Map<String, Integer> scores = new LinkedHashMap<>();

        String sql = """
            SELECT score_id, score
            FROM scoreboard
            WHERE player_id = ?
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {
            
            statement.setObject(1, UUID.fromString(player_id));

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    String id = result.getString("score_id");

                    int score = result.getInt("score");

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
    
    public int getHighestScore(String player_id) {

        String sql = """
            SELECT MAX(score) AS highest_score
            FROM scoreboard
            WHERE player_id = ?
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            
            statement.setObject(1, UUID.fromString(player_id));
            
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int highestScore = result.getInt("highest_score");

                    if (result.wasNull()) {
                        return 0;
                    }

                    return highestScore;
                }
            }

        } catch (SQLException e) {

            System.err.println(
                "Error getting highest score: "
                + e.getMessage()
            );
        }

        return 0;
    }
    
    public void addPlayer(Player player) {

        String sql = """
            INSERT INTO players
            (player_id, player_name, current_player, total_score, rank)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setString(1, player.id);
            statement.setString(2, player.player_name);
            statement.setInt(3, 1); //current player
            statement.setInt(4, player.total_score);
            statement.setInt(5, player.rank);

            statement.executeUpdate();

            System.out.println("Player saved! Name: " + player.player_name);

        } catch (SQLException e) {

            // SQLite error code for UNIQUE constraint violation
            if (e.getMessage().contains("UNIQUE constraint failed")) {

                System.err.println(
                    "That player name already exists!"
                );

            } else {

                System.err.println(
                    "Error saving player: " + e.getMessage()
                );
            }
        }
    }
}