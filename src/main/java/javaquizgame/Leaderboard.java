package javaquizgame;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

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

    public void updateTotalScore(String id, int score) {

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

        } catch (Exception e) {
            System.err.println("Errors updating score: " + e.getMessage());
            System.out.println("Error: " + e);
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

            // Set the ? values FIRST
            statement.setString(1, name);
            statement.setInt(2, 0);

            // THEN execute
            try (ResultSet results = statement.executeQuery()) {

                if (results.next()) {
                    String id = results.getString("id");

                    System.out.println("Player added successfully!");
                    System.out.println("Player ID: " + id);

                    return id;
                }
            }

        } catch (Exception e) {
            System.err.println("Error adding player: " + e.getMessage());
        }

        return null;
    }

    public void deletePlayer(String id) {

        String sql = "DELETE FROM players WHERE id = ?";

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setObject(1, java.util.UUID.fromString(id));

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
    
    public ArrayList<Player> getAllPlayers() {

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

        } catch (Exception e) {
            System.err.println("Error getting players: " + e.getMessage());
        }

        return players;
    }
    
    public static void main(String[] args) {

        Leaderboard leaderboard = new Leaderboard();
        ArrayList<Player> players = leaderboard.getAllPlayers();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            
            System.out.println("ID: " + player.id);
            System.out.println("Username: " + player.player_name);
            System.out.println("Total Score: " + player.total_score);
            System.out.println("Rank: " + player.rank);
        }
        
        leaderboard.updateTotalScore("baef7a3e-aeaf-4a36-955f-d0c6420467d4", 10000);
        
        leaderboard.addPlayer("Player100");
        
        //leaderboard.deletePlayer("28a5653d-2c63-495a-bed0-2876b5044875");
    }
}