/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaquizgame;

/**
 *
 * @author Adrian
 */
public class Question {
    
    String text;
    String[] options; // index 0=A, 1=B, 2=C, 3=D
    char correctAnswer;

    public Question(String text, String[] options, char correctAnswer) {
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
}