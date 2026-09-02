package javaquizgame;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;
import javax.swing.JOptionPane;

public class Leaderboard {

    private final String url;
    private final String user;
    private final String password;

    public Leaderboard() {
        Dotenv dotenv = Dotenv.load();

        url = dotenv.get("SUPABASE_DB_URL");
        user = dotenv.get("SUPABASE_DB_USER");
        password = dotenv.get("SUPABASE_DB_PASSWORD");
    }

    public void updateTotalScore(String id, int score) throws Exception {

        String sql =
            "UPDATE players SET total_score = ? WHERE id = ?";

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setInt(1, score);
            statement.setObject(2, UUID.fromString(id));

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Score updated successfully!");
            } else {
                System.out.println("Player not found!");
            }

        }
        
    }
    
    // Returns the generated player's ID
    public String addPlayer(String name) {

        String sql =
            "INSERT INTO players (player_name, total_score) " +
            "VALUES (?, ?) RETURNING id";

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setString(1, name);
            statement.setInt(2, 0);

            try (ResultSet results = statement.executeQuery()) {

                if (results.next()) {

                    String id = results.getString("id");

                    System.out.println("Player added successfully!");
                    System.out.println("Player ID: " + id);

                    return id;
                }
            }

        } catch (SQLException e) {

            // PostgreSQL error code for UNIQUE constraint violation
            if ("23505".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(
                    null,
                    "That player name already exists!"
                );
            } else {
                JOptionPane.showMessageDialog(
                    null,
                    "Error adding player: " + e.getMessage()
                );
            }
        }

        return null;
    }

    public void deletePlayer(String id) {

        String sql = "DELETE FROM players WHERE id = ?";

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setObject(1, UUID.fromString(id));

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Player deleted successfully!");
            } else {
                System.out.println("Player not found!");
            }

        } catch (Exception e) {
            System.err.println("Error deleting player: " + e.getMessage());
        }
    }
    
    public ArrayList<Player> getAllPlayers() throws Exception {

        ArrayList<Player> players = new ArrayList<>();

        String sql = """
            SELECT id, player_name, total_score, rank
            FROM players
            ORDER BY rank ASC
            """;

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement statement = conn.createStatement();
            ResultSet results = statement.executeQuery(sql)
        ) {

            while (results.next()) {

                String id = results.getString("id");
                String playerName = results.getString("player_name");
                int totalScore = results.getInt("total_score");
                int rank = results.getInt("rank");

                Player player = new Player(
                    id,
                    playerName,
                    totalScore,
                    rank
                );

                players.add(player);
            }

        }

        return players;
    }
    
    public Player getPlayer(String name) {

        String sql = """
            SELECT id, player_name, total_score, rank
            FROM players
            WHERE player_name = ?
            LIMIT 1
            """;

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setString(1, name);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    return new Player(
                        result.getString("id"),
                        result.getString("player_name"),
                        result.getInt("total_score"),
                        result.getInt("rank")
                    );
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null,
                "Error getting player: " + e.getMessage()
            );
        }

        return null;
    }
}