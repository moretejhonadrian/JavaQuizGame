package javaquizgame;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.JOptionPane;

//if xampp wont work, delete aria_log.######## files in
//C:\xampp\mysql\data

public class Database {
    
    public Database() {
        initDB();
        createTables();
    }
    
    protected Connection connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/javaquizgamedb",
                "root",
                ""
            );
            
            return conn;

        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
    }
    
    //create javaquizgamedb if not exist
    private void initDB() {
        String url = "jdbc:mysql://localhost:3306/";
        String username = "root";
        String password = "";

        String query = "CREATE DATABASE IF NOT EXISTS javaquizgamedb";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (
                Connection conn = DriverManager.getConnection(url, username, password);
                Statement stmt = conn.createStatement()
            ) {
                stmt.executeUpdate(query);
                System.out.println("Database javaquizgamedb is ready.");

            }

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
        } catch (SQLException e) {
            System.out.println("Error creating database: " + e.getMessage());
        }
    }
    
    private void createTables() {

        String playersSql = """
            CREATE TABLE IF NOT EXISTS players (
                id VARCHAR(36) PRIMARY KEY,
                player_name VARCHAR(100) NOT NULL UNIQUE,
                total_score INTEGER NOT NULL,
                rank INTEGER NOT NULL
            )
            """;

        String scoreboardSql = """
            CREATE TABLE IF NOT EXISTS scoreboard (
                score_id VARCHAR(36) PRIMARY KEY,
                id VARCHAR(100) NOT NULL,
                quiz_stage VARCHAR(100) NOT NULL,
                score INTEGER NOT NULL,
                FOREIGN KEY (id) REFERENCES players(id)
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

            System.out.println("Tables are ready.");

        } catch (SQLException e) {

            System.err.println(
                "Error creating tables: "
                + e.getMessage()
            );
        }
    }
    
    public Player addPlayer(String name) {

        String sql =
            "INSERT INTO players (id, player_name, total_score, rank) " +
            "VALUES (?, ?, ?, ?)";
        
        String id = UUID.randomUUID().toString();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (
                Connection conn = connect();
                PreparedStatement statement = conn.prepareStatement(sql);
            ) {
                
                statement.setString(1, id);
                statement.setString(2, name);
                statement.setInt(3, 0);
                statement.setInt(4, 0);

                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("Player added successfully!");
                    System.out.println("Player ID: " + id);

                    return new Player(
                        id,
                        name,
                        0,
                        0
                    );
                }

            }

        } catch (ClassNotFoundException e) {

            JOptionPane.showMessageDialog(
                null,
                "PostgreSQL driver not found: " + e.getMessage()
            );

        } catch (SQLException e) {

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
    
    public boolean playerExists(String name) {

        String sql = """
            SELECT id, player_name, total_score, rank
            FROM players
            WHERE player_name = ?
            LIMIT 1
            """;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        
            try (
                Connection conn = connect();
                PreparedStatement statement = conn.prepareStatement(sql);
            ) {
                
                statement.setString(1, name);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {

//                        return new Player(
//                            result.getString("id"),
//                            result.getString("player_name"),
//                            result.getInt("total_score"),
//                            result.getInt("rank")
//                        );
                          return true;
                    }
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Error getting player: " + e.getMessage()
                );
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(
                null,
                "PostgreSQL driver not found: " + e.getMessage()
            );
        }

        return false;
    }
    
    public Player getPlayer(String name) {

        String sql = """
            SELECT *
            FROM players
            WHERE player_name = ?
            LIMIT 1
            """;
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        
            try (
                Connection conn = connect();
                PreparedStatement statement = conn.prepareStatement(sql);
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
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(
                null,
                "PostgreSQL driver not found: " + e.getMessage()
            );
        }

        return null;
    }
    
    public List<Player> getAllPlayers() throws Exception {
        
        ArrayList<Player> players = new ArrayList<>();

        String sql = """
            SELECT id, player_name, total_score, rank
            FROM players
            ORDER BY rank ASC
            """;
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql);
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
    
    public void addScore(String id, String quizName, int score) {
        String score_id = UUID.randomUUID().toString();

        String sql = """
            INSERT INTO scoreboard
            (score_id, id, quiz_stage, score)
            VALUES (?, ?, ?, ?)
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setString(1, score_id);
            statement.setString(2, id);
            statement.setString(3, quizName);
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
    
    public Map<String, Integer> showAllScores(String player_id) {

        Map<String, Integer> scores = new LinkedHashMap<>();

        String sql = """
            SELECT score_id, score
            FROM scoreboard
            WHERE id = ?
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {
            
            statement.setString(1, player_id);

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
            WHERE id = ?
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            
            statement.setString(1, player_id);
            
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
    
    public void updateRanking() {

        String selectSql = """
            SELECT id
            FROM players
            ORDER BY total_score DESC
            """;

        String updateSql = """
            UPDATE players
            SET rank = ?
            WHERE id = ?
            """;

        try (
            Connection conn = connect();
            PreparedStatement selectStatement =
                conn.prepareStatement(selectSql);
            PreparedStatement updateStatement =
                conn.prepareStatement(updateSql);
            ResultSet results = selectStatement.executeQuery()
        ) {

            int rank = 1;

            while (results.next()) {

                String id = results.getString("id");

                updateStatement.setInt(1, rank);
                updateStatement.setString(2, id);

                updateStatement.executeUpdate();

                rank++;
            }

            System.out.println("Ranking updated successfully!");

        } catch (SQLException e) {
            System.err.println(
                "Error updating ranking: " + e.getMessage()
            );
        }
    }

    public void updateTotalScore(String id, int score) throws Exception {

        String sql =
            "UPDATE players SET total_score = ? WHERE id = ?";
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {

            statement.setInt(1, score);
            statement.setObject(2, id);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Score updated successfully!");
            } else {
                System.out.println("Player not found!");
            }

        }
        
    }
    
    public static void main(String[] args) throws Exception {
        Database db = new Database();
        CurrentPlayer currentPlayer = new CurrentPlayer();
        
        if (!db.playerExists("jhon")) {
            System.out.println("jhon ain't here.");
            db.addPlayer("jhon");
        }
        
        
        List<Player> players = db.getAllPlayers();

        for (Player player : players) {

            System.out.println(
                player.id + " | "
                + player.player_name + " | "
                + player.total_score + " | "
                + player.rank 
            );
        }
        
        System.out.println("Current Player: ");
        System.out.println(currentPlayer.player.player_name);
        
        Player p = db.getPlayer("adrian");
        System.out.println("Player adrian's id: " + p.id);
        
        System.out.println("Add score: 9");
        db.addScore("1efbd682-48b7-45a0-a6df-aba5a8349b28", "All", 90);
        
        Map<String, Integer> scores =
            db.showAllScores("1efbd682-48b7-45a0-a6df-aba5a8349b28");

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            System.out.println(
                entry.getKey() + " | " + entry.getValue()
            );
        }
        
        System.out.println(db.getHighestScore("1efbd682-48b7-45a0-a6df-aba5a8349b28"));
    }
}
