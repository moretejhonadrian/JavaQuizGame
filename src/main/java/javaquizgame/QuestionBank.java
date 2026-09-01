package javaquizgame;

import java.util.ArrayList;
import java.util.List;

public class QuestionBank {

    private final List<Question> stage1Bank = new ArrayList<>();
    private final List<Question> stage2Bank = new ArrayList<>();
    private final List<Question> stage3Bank = new ArrayList<>();

    public QuestionBank() {
        loadQuestionBanks();
    }

    private void loadQuestionBanks() {

        // ---- Stage 1: Java Basics ----
        stage1Bank.add(new Question(
                "Which keyword is used to define a constant in Java?",
                new String[]{"const", "final", "static", "readonly"}, 1));
        stage1Bank.add(new Question(
                "Which of these is NOT a primitive data type in Java?",
                new String[]{"int", "boolean", "String", "double"}, 2));
        stage1Bank.add(new Question(
                "What is the default value of an int variable in Java?",
                new String[]{"null", "undefined", "1", "0"}, 3));
        stage1Bank.add(new Question(
                "Which method is the entry point of a Java application?",
                new String[]{"start()", "main()", "run()", "init()"}, 1));
        stage1Bank.add(new Question(
                "Which operator is used for string concatenation in Java?",
                new String[]{"&", "+", "*", "%"}, 1));
        stage1Bank.add(new Question(
                "Which loop is guaranteed to run at least once?",
                new String[]{"for", "while", "do-while", "for-each"}, 2));
        stage1Bank.add(new Question(
                "Which symbol is used to end most Java statements?",
                new String[]{"colon (:)", "semicolon (;)", "period (.)", "comma (,)"}, 1));
        stage1Bank.add(new Question(
                "What does JVM stand for?",
                new String[]{"Java Virtual Machine", "Java Verified Method",
                        "Java Variable Manager", "Java Visual Model"}, 0));
        stage1Bank.add(new Question(
                "Which access modifier makes a member visible only within its own class?",
                new String[]{"public", "protected", "private", "default"}, 2));
        stage1Bank.add(new Question(
                "Which of these correctly declares an array of integers?",
                new String[]{"int arr[] = new int[5];", "array int arr = new(5);",
                        "int[5] arr;", "new int arr[5];"}, 0));

        // ---- Stage 2: Object-Oriented Programming ----
        stage2Bank.add(new Question(
                "Which keyword is used to inherit a class in Java?",
                new String[]{"implements", "extends", "inherits", "super"}, 1));
        stage2Bank.add(new Question(
                "Which keyword prevents a class from being subclassed?",
                new String[]{"static", "private", "final", "abstract"}, 2));
        stage2Bank.add(new Question(
                "What is method overriding?",
                new String[]{"Defining multiple methods with the same name but different parameters",
                        "Redefining a superclass method in a subclass with the same signature",
                        "Calling a method more than once", "Hiding a variable in a subclass"}, 1));
        stage2Bank.add(new Question(
                "Which keyword is used to implement an interface?",
                new String[]{"extends", "implements", "interface", "abstract"}, 1));
        stage2Bank.add(new Question(
                "Which OOP principle allows an object to take many forms?",
                new String[]{"Encapsulation", "Inheritance", "Polymorphism", "Abstraction"}, 2));
        stage2Bank.add(new Question(
                "What is the purpose of a constructor in Java?",
                new String[]{"To destroy an object", "To initialize a new object",
                        "To define a class", "To override a method"}, 1));
        stage2Bank.add(new Question(
                "Which keyword refers to the current object instance?",
                new String[]{"this", "self", "current", "instance"}, 0));
        stage2Bank.add(new Question(
                "Can an abstract class have a constructor in Java?",
                new String[]{"No, never", "Yes, and it runs when a subclass is instantiated",
                        "Only if it has no fields", "Only static constructors"}, 1));
        stage2Bank.add(new Question(
                "Which keyword is used to call a superclass constructor from a subclass?",
                new String[]{"this()", "super()", "base()", "parent()"}, 1));
        stage2Bank.add(new Question(
                "What is encapsulation in Java?",
                new String[]{"Hiding internal state behind methods and controlling access",
                        "Creating multiple classes with the same name",
                        "Running code on multiple threads", "Converting one type to another"}, 0));

        // ---- Stage 3: Collections, Exceptions & Advanced Topics ----
       stage3Bank.add(new Question(
                "Which collection class allows duplicate elements and maintains insertion order?",
                new String[]{"HashSet", "TreeSet", "ArrayList", "HashMap"}, 2));
        stage3Bank.add(new Question(
                "Which interface does the HashMap class implement?",
                new String[]{"List", "Set", "Map", "Queue"}, 2));
        stage3Bank.add(new Question(
                "Which keyword is used to handle exceptions in Java?",
                new String[]{"throw", "try-catch", "catch-only", "handle"}, 1));
        stage3Bank.add(new Question(
                "Which block always executes whether or not an exception occurs?",
                new String[]{"catch", "finally", "throws", "try"}, 1));
        stage3Bank.add(new Question(
                "Which of these is a checked exception in Java?",
                new String[]{"NullPointerException", "ArithmeticException",
                        "IOException", "ArrayIndexOutOfBoundsException"}, 2));
        stage3Bank.add(new Question(
                "What does the 'synchronized' keyword help prevent in multithreading?",
                new String[]{"Memory leaks", "Race conditions", "Compilation errors", "Null pointers"}, 1));
        stage3Bank.add(new Question(
                "Which Java feature allows classes to work with any data type?",
                new String[]{"Generics", "Reflection", "Annotations", "Enums"}, 0));
        stage3Bank.add(new Question(
                "Which Java 8 feature allows functional-style operations on collections?",
                new String[]{"Lambda blocks", "Streams API", "Reflection API", "Servlets"}, 1));
        stage3Bank.add(new Question(
                "What is the purpose of the 'static' keyword on a method?",
                new String[]{"It belongs to the class rather than any instance",
                        "It can only be called once", "It runs on a separate thread",
                        "It cannot be overridden"}, 0));
        stage3Bank.add(new Question(
                "Which operator is used to compare object references in Java?",
                new String[]{"equals()", "==", ".compare()", "==="}, 1));
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