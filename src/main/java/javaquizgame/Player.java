package javaquizgame;

public class Player {
    String id;
    String player_name;
    int total_score;
    int rank;

    public Player(String id, String player_name, int total_score, int rank) {
        this.id = id;
        this.player_name = player_name;
        this.total_score = total_score;
        this.rank = rank;
    }
}
