package javaquizgame;

public class Question {

    String questionText;
    String[] choices;
    int correctIndex;

    public Question(String questionText, String[] options, int correctIndex) {
        this.questionText = questionText;
        this.choices = options;
        this.correctIndex = correctIndex;
    }
}
