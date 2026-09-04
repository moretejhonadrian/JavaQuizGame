package javaquizgame;

public class Player {
    String id;
    String player_name;
    double total_points;

    public Player(String id, String player_name, double total_points) {
        this.id = id;
        this.player_name = player_name;
        this.total_points = total_points;
    }
}
