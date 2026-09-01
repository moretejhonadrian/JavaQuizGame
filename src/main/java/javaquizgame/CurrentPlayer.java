package javaquizgame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CurrentPlayer {

    private static final String FILE_PATH =
            "src/main/java/files/playerData.json";

    protected Player player;

    public CurrentPlayer() {

        File file = new File(FILE_PATH);

        try {
            // create parent folders if they don't exist
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            // create empty JSON file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{}");
                }

                player = null;
                return;
            }

            // read existing player data
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                player = gson.fromJson(reader, Player.class);
            }

        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            System.err.println(
                "Error loading player data: " + e.getMessage()
            );

            player = null;
        }
    }
    public Player getPlayer() {
        return player;
    }

    public boolean isSet() {
        return player != null
            && player.id != null
            && !player.id.isBlank();
    }

    public void set(String name, String id, int total_score, int rank) {

        // Create the Player object
        player = new Player(
            id,
            name,
            total_score,
            rank
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
    
    public void logout() {

        player = null;

        File file = new File(FILE_PATH);

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Player logged out.");
            } else {
                System.err.println(
                    "Could not delete player data."
                );
            }
        }
    }
}