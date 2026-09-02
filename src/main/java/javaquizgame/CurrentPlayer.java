package javaquizgame;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrentPlayer extends Scoreboard {

    protected Player player;

    public CurrentPlayer() {
        this.player = getCurrentPlayer();
    }
    
    public final Player getCurrentPlayer() {
        String sql = """
            SELECT *
            FROM players
            WHERE current_player = 1
            LIMIT 1
            """;
        
        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            
            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    
                    return new Player (
                        result.getString("player_id"),
                        result.getString("player_name"),
                        result.getInt("total_score"),
                        result.getInt("rank")
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                "Error setting current player "
                + e.getMessage()
            );
        }
        
        return null;
    }
    
    public Player getPlayer() {
        return player;
    }

    public boolean isSet() {
        return player != null
            && player.id != null
            && !player.id.isBlank();
    }
    
    public void logout(String id) {

        this.player = null;

        setNoCurrentPlayer(id);
    }
    
    public void setNoCurrentPlayer(String id) {
        String sql = "UPDATE players SET current_player = 0 WHERE player_id = ?";
        
        System.out.println(id); 
        
        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql);
        ) {
            
            statement.setObject(1, id);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Player is no longer current!");
            } else {
                System.out.println("Player not found!");
            }

        } catch (SQLException e) {

            System.err.println(
                "Error setting current player "
                + e.getMessage()
            );
        }
    }
    
    public void setCurrentPlayer(Player p) {
        
        if (!isPlayerInDB(p.id)) {
            System.out.println("not in the local db");
            addPlayer(p);
        } else {
            updateCurrentPlayer(p.id);
        }
        
        this.player = getCurrentPlayer();
    }
    
    public void updateCurrentPlayer(String id) {
        String sql =
            "UPDATE players SET current_player = 1 WHERE player_id = ?";

        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setString(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println(
                "Error setting current player: " + e.getMessage()
            );
        }
    }
    
    public boolean isPlayerInDB(String id) {

        String sql = """
            SELECT 1
            FROM players
            WHERE player_id = ?
            LIMIT 1
            """;

        try (
            Connection conn = connect();
            PreparedStatement statement = conn.prepareStatement(sql)
        ) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {

                return result.next();
            }

        } catch (SQLException e) {

            System.err.println(
                "Error checking player: " + e.getMessage()
            );

            return false;
        }
    }
}