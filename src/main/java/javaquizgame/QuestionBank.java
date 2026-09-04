package javaquizgame;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestionBank {
    
    //save the quizzes here
    private final List<List<Question>> quizzes = new ArrayList<>();
    private final Map<String, String> quizzesNames = new LinkedHashMap<>();
    
    private final String quizzesPath = "/files/quizzes/";
    
    public QuestionBank() {
        loadQuestionBanks();
    }
    
    public List<Question> getQuiz(String filePath) {

        InputStream input = getClass().getResourceAsStream(filePath);

        if (input == null) {
            System.out.println("Quiz file not found: " + filePath);
            return new ArrayList<>();
        }

        try (
            InputStreamReader reader = new InputStreamReader(input)
        ) {

            Gson gson = new Gson();

            Type questionListType =
                    new TypeToken<List<Question>>() {}.getType();

            List<Question> questions =
                    gson.fromJson(reader, questionListType);

            if (questions == null) {
                return new ArrayList<>();
            }

            System.out.println(
                    "Loaded " + questions.size() + " questions."
            );

            return questions;

        } catch (Exception e) {
            System.out.println(
                    "Error loading questions: " + e
            );

            return new ArrayList<>();
        }
    }

    private void loadQuestionBanks() {

        String[] quizFiles = {
            "JP1 - Java Basics.json",
            "JP2 - Object Oriented Programming.json",
            "JP3 - Collections, Exceptions & Advanced Topics.json"
        };

        for (String fileName : quizFiles) {

            List<Question> quiz = getQuiz(quizzesPath + fileName);

            if (quiz != null && !quiz.isEmpty()) {

                quizzes.add(quiz);

                String name = fileName.replace(".json", "");

                String[] parts = name.split(" ", 2);

                String key = parts[0];

                quizzesNames.put(key, name);

                System.out.println("Loaded quiz: " + key + " -> " + name);
            }
        }
    }
    
    public List<List<Question>> getQuizzes() {
        return quizzes;
    }
    
    public Map<String, String> getQuizzesNames() {
        return quizzesNames;
    }
    
    public int getQuizzesSize() {
        return quizzes.size();
    }
    
    public String getQuizTitle(String key) {
        return quizzesNames.get(key);
    }
}