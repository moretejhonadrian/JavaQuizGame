package javaquizgame;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestionBank {
    
    private final String quizzesPath = "src/main/java/files/quizzes/";
    
    private List<Question> stage1Bank;
    private List<Question> stage2Bank;
    private List<Question> stage3Bank;

    public QuestionBank() {
        loadQuestionBanks();
    }
    
    public List<Question> getQuiz(String filePath) {
        
        List<Question> quiz = new ArrayList<>();
        
        try (FileReader reader = new FileReader(filePath)) {

            Gson gson = new Gson();

            Type questionListType = new TypeToken<List<Question>>() {}.getType();

            List<Question> questions = gson.fromJson(reader, questionListType);

            quiz.addAll(questions);

            System.out.println("Loaded " + quiz.size() + " questions.");
            
            return quiz;

        } catch (Exception e) {
            System.out.println("Error loading questions: " + e.getMessage());
            return null;
        }
    }

    private void loadQuestionBanks() {

        // ---- Stage 1: Java Basics ----
        stage1Bank = getQuiz(quizzesPath + "Java Basics.json");

        // ---- Stage 2: Object-Oriented Programming ----
        stage2Bank = getQuiz(quizzesPath + "Object Oriented Programming.json");

        // ---- Stage 3: Collections, Exceptions & Advanced Topics ----
       stage3Bank = getQuiz(quizzesPath + "Collections, Exceptions & Advanced Topics.json");
    }

    public List<Question> getStage1Bank() {
        return stage1Bank;
    }

    public List<Question> getStage2Bank() {
        return stage2Bank;
    }

    public List<Question> getStage3Bank() {
        return stage3Bank;
    }
}