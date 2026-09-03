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

    // writable location outside the JAR
    private static final String FILE_PATH =
            System.getProperty("user.home")
            + File.separator
            + "JavaQuizGame"
            + File.separator
            + "playerData.json";

    protected Player player;

    public CurrentPlayer() {

        File file = new File(FILE_PATH);

        try {

            // create parent folder if it doesn't exist
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            // create empty JSON file if it doesn't exist
            if (!file.exists()) {

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{}");
                }

                this.player = null;
                return;
            }

            // read existing player data
            try (FileReader reader = new FileReader(file)) {

                Gson gson = new Gson();

                this.player = gson.fromJson(
                    reader,
                    Player.class
                );
            }

        } catch (
            JsonIOException
            | JsonSyntaxException
            | IOException e
        ) {

            System.err.println(
                "Error loading player data: "
                + e.getMessage()
            );

            this.player = null;
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isSet() {

        return player != null
            && player.id != null
            && !player.id.isBlank();
    }

    public void set(Player p) {

        this.player = new Player(
            p.id,
            p.player_name,
            p.total_points
        );

        save();
    }

    private void save() {

        File file = new File(FILE_PATH);

        try {

            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (FileWriter writer = new FileWriter(file)) {

                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();

                gson.toJson(player, writer);
            }

        } catch (JsonIOException | IOException e) {

            System.err.println(
                "Error saving player data: "
                + e.getMessage()
            );
        }
    }

    public void logout() {

        this.player = null;

        File file = new File(FILE_PATH);

        if (file.exists()) {

            if (file.delete()) {

                System.out.println(
                    "Player logged out."
                );

            } else {

                System.err.println(
                    "Could not delete player data."
                );
            }
        }
    }
}