package javaquizgame;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Questions {

    private final List<Question> questionPool = new ArrayList<>();

    public void getQuestions() {
        try (FileReader reader = new FileReader("src/main/java/files/questions.json")) {

            Gson gson = new Gson();

            Type questionListType =
                    new TypeToken<List<Question>>() {}.getType();

            List<Question> questions =
                    gson.fromJson(reader, questionListType);

            questionPool.addAll(questions);

            System.out.println(
                    "Loaded " + questionPool.size() + " questions."
            );

        } catch (Exception e) {
            System.out.println("Error loading questions: " + e.getMessage());
        }
    }

    public List<Question> getQuestionPool() {
        getQuestions();
        return questionPool;
    }
}