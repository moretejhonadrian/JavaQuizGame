package javaquizgame;

import java.util.*;

public class PROJECTSYSTEM_TRIAL {
    static class Question {
        String text;
        String[] options; // index 0=A, 1=B, 2=C, 3=D
        char correctAnswer;

        Question(String text, String[] options, char correctAnswer) {
            this.text = text;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    private static final int WIDTH = 60; // inner content width
    private final Scanner scanner = new Scanner(System.in);
    private final List<Question> questionPool = new ArrayList<>();
    private List<Question> activeQuestions = new ArrayList<>();
    private int score = 0;

    public static void main(String[] args) {
        PROJECTSYSTEM_TRIAL game = new PROJECTSYSTEM_TRIAL();
        game.loadQuestions();
        game.printIntro();
        game.chooseItemCount();
        game.playQuiz();
        game.printResults();
    }

    private void loadQuestions() {
        // Basic syntax / elements
        questionPool.add(new Question(
                "Which symbol is used to end most statements in Java?",
                new String[]{"Colon 🙂)", "Semicolon (😉", "Period (.)", "Comma (,)"}, 'B'));
        questionPool.add(new Question(
                "Which of these is a valid Java variable name?",
                new String[]{"2total", "total-2", "_total2", "total 2"}, 'C'));
        questionPool.add(new Question(
                "Which keyword declares a constant in Java?",
                new String[]{"const", "static", "final", "constant"}, 'C'));
        questionPool.add(new Question(
                "Which of these is NOT a Java primitive data type?",
                new String[]{"int", "boolean", "String", "char"}, 'C'));
        questionPool.add(new Question(
                "What is the size of an int in Java?",
                new String[]{"8-bit", "16-bit", "32-bit", "64-bit"}, 'C'));
        questionPool.add(new Question(
                "Which data type is used to store true/false values?",
                new String[]{"bit", "boolean", "bool", "flag"}, 'B'));
        questionPool.add(new Question(
                "Which symbol is used for single-line comments in Java?",
                new String[]{"//", "/*", "#", "--"}, 'A'));

        // Methods
        questionPool.add(new Question(
                "Which method is the entry point of a Java program?",
                new String[]{"start()", "run()", "main()", "init()"}, 'C'));
        questionPool.add(new Question(
                "What keyword is used to define a method that returns nothing?",
                new String[]{"null", "void", "empty", "none"}, 'B'));
        questionPool.add(new Question(
                "What is it called when two methods in the same class share a name but differ in parameters?",
                new String[]{"Overriding", "Overloading", "Inheriting", "Cloning"}, 'B'));
        questionPool.add(new Question(
                "Which keyword allows a subclass to redefine a parent class method?",
                new String[]{"@Override", "@Redefine", "@New", "@Replace"}, 'A'));
        questionPool.add(new Question(
                "What do you call the values passed into a method when it is called?",
                new String[]{"Fields", "Arguments", "Attributes", "Returns"}, 'B'));

        // Attributes / fields / OOP basics
        questionPool.add(new Question(
                "What term describes the variables that belong to a class (its properties)?",
                new String[]{"Methods", "Attributes", "Constructors", "Loops"}, 'B'));
        questionPool.add(new Question(
                "Which access modifier makes a member visible only within its own class?",
                new String[]{"public", "protected", "default", "private"}, 'D'));
        questionPool.add(new Question(
                "Which keyword is used to create a subclass in Java?",
                new String[]{"implements", "extends", "inherits", "super"}, 'B'));
        questionPool.add(new Question(
                "What is a special method used to initialize a new object called?",
                new String[]{"Initializer", "Constructor", "Builder", "Setter"}, 'B'));
        questionPool.add(new Question(
                "What keyword refers to the current object instance inside a class?",
                new String[]{"self", "this", "current", "me"}, 'B'));
        questionPool.add(new Question(
                "Which keyword prevents a class from being subclassed?",
                new String[]{"static", "final", "private", "sealed"}, 'B'));
        questionPool.add(new Question(
                "What is the default value of a boolean instance variable?",
                new String[]{"true", "false", "0", "null"}, 'B'));
        questionPool.add(new Question(
                "Which keyword makes a variable or method belong to the class rather than an instance?",
                new String[]{"final", "static", "public", "abstract"}, 'B'));

        // Collections / operators / exceptions
        questionPool.add(new Question(
                "Which collection does NOT allow duplicate elements?",
                new String[]{"ArrayList", "LinkedList", "HashSet", "Vector"}, 'C'));
        questionPool.add(new Question(
                "Which operator is used to compare object references?",
                new String[]{"equals()", "==", "compareTo()", "="}, 'B'));
        questionPool.add(new Question(
                "Which keyword is used to catch exceptions in Java?",
                new String[]{"throw", "except", "catch", "handle"}, 'C'));
        questionPool.add(new Question(
                "What does JVM stand for?",
                new String[]{"Java Virtual Machine", "Java Visual Model",
                        "Java Verified Method", "Java Variable Manager"}, 'A'));
        questionPool.add(new Question(
                "Which interface must a class implement to be used with a for-each loop?",
                new String[]{"Iterator", "Iterable", "Comparable", "Serializable"}, 'B'));
    }

    // ---------- Setup ----------

    private void chooseItemCount() {
        int choice = 0;
        while (choice != 10 && choice != 15 && choice != 20) {
            printLine('+', '-');
            printTextLine("How many questions do you want to answer?");
            printTextLine("1. 10 items");
            printTextLine("2. 15 items");
            printTextLine("3. 20 items");
            printLine('+', '-');
            System.out.print("Choose 1, 2, or 3: ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> choice = 10;
                case "2" -> choice = 15;
                case "3" -> choice = 20;
                default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.\n");
            }
        }

        List<Question> shuffled = new ArrayList<>(questionPool);
        Collections.shuffle(shuffled);
        int count = Math.min(choice, shuffled.size());
        activeQuestions = shuffled.subList(0, count);

        System.out.println();
        printLine('+', '=');
        printCentered("Starting quiz with " + count + " items!");
        printLine('+', '=');
        System.out.println();
    }

    // ---------- Screen drawing helpers ----------

    private void printLine(char corner, char fill) {
        StringBuilder sb = new StringBuilder();
        sb.append(corner);
        for (int i = 0; i < WIDTH + 2; i++) sb.append(fill);
        sb.append(corner);
        System.out.println(sb);
    }

    private void printTextLine(String text) {
        System.out.println("| " + padRight(text, WIDTH) + " |");
    }

    private void printCentered(String text) {
        int totalPad = WIDTH - text.length();
        int left = totalPad / 2;
        int right = totalPad - left;
        String line = " ".repeat(Math.max(left, 0)) + text + " ".repeat(Math.max(right, 0));
        printTextLine(line);
    }

    private String padRight(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        return text + " ".repeat(width - text.length());
    }

    /** Wraps text to WIDTH and prints each resulting line inside the box. */
    private void printWrapped(String text) {
        for (String line : wrap(text, WIDTH)) {
            printTextLine(line);
        }
    }

    private List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            if (current.length() == 0) {
                current.append(word);
            } else if (current.length() + 1 + word.length() <= width) {
                current.append(" ").append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    // ---------- Screens ----------

    private void printIntro() {
        printLine('+', '=');
        printCentered("JAVA PROGRAMMING QUIZ GAME");
        printLine('+', '=');
        printTextLine("Test your knowledge of the Java language!");
        printTextLine("Answer each question by typing A, B, C, or D.");
        printLine('+', '=');
        System.out.println();
    }

    private void playQuiz() {
        for (int i = 0; i < activeQuestions.size(); i++) {
            Question q = activeQuestions.get(i);
            printLine('+', '-');
            printTextLine("Question " + (i + 1) + " of " + activeQuestions.size() + ":");
            printLine('+', '-');
            printWrapped(q.text);
            printWrapped("A. " + q.options[0]);
            printWrapped("B. " + q.options[1]);
            printWrapped("C. " + q.options[2]);
            printWrapped("D. " + q.options[3]);
            printLine('+', '-');
            System.out.println();

            char answer = promptAnswer();
            if (answer == q.correctAnswer) {
                System.out.println(">> Correct!\n");
                score++;
            } else {
                System.out.println(">> Incorrect. The correct answer was " + q.correctAnswer
                        + ". " + q.options[q.correctAnswer - 'A'] + "\n");
            }
        }
    }

    private char promptAnswer() {
        while (true) {
            System.out.print("Your answer (A/B/C/D): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.length() == 1 && "ABCD".contains(input)) {
                return input.charAt(0);
            }
            System.out.println("Please type A, B, C, or D.");
        }
    }

    private void printResults() {
        System.out.println();
        printLine('+', '=');
        printCentered("QUIZ COMPLETE!");
        printLine('+', '=');
        printTextLine("Your score: " + score + " out of " + activeQuestions.size());
        printTextLine("Percentage: " + (score * 100 / activeQuestions.size()) + "%");
        printLine('+', '-');
        printWrapped(getRemark());
        printLine('+', '=');
        scanner.close();
    }

    private String getRemark() {
        double pct = (double) score / activeQuestions.size();
        if (pct == 1.0) return "Perfect score! You know Java inside and out.";
        if (pct >= 0.8 ) return "Great job! You have a strong grasp of Java.";
        if (pct >= 0.5) return "Not bad! A bit more review and you'll master it.";
        return "Keep studying! Practice makes perfect.";
    }
}