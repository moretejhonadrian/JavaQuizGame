package javaquizgame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;

public class CurrentPlayer {

    private static final String FILE_PATH =
            "src/main/java/files/playerData.json";

    protected Player player;

    public CurrentPlayer() {

        try (FileReader reader = new FileReader(FILE_PATH)) {

            Gson gson = new Gson();

            player = gson.fromJson(reader, Player.class);

        } catch (Exception e) {
            // No saved player yet
            player = null;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isSet() {
        return player != null && player.id != null;
    }

    public void set(String name, String id) {

        // Create the Player object
        player = new Player(
            id,
            name,
            0,
            -1
        );

        save();
    }

    private void save() {

        try (FileWriter writer = new FileWriter(FILE_PATH)) {

            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            gson.toJson(player, writer);

        } catch (Exception e) {
            System.err.println(
                "Error saving player data: " + e.getMessage()
            );
        }
    }
}