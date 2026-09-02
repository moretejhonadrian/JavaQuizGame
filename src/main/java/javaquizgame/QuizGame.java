package javaquizgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class QuizGame extends GUI {

    // Master question banks (never mutated directly; copied + shuffled each play)
    private final QuestionBank questionBank;

    // Active shuffled question list for the current play-through
    private List<Question> activeQuestions = new ArrayList<>();

    private int currentIndex = 0;
    private int score = 0;
    private String playerName = "";
    private String playerId = "";
    private final Random random = new Random();

    public QuizGame() {
        super("GROUP 2 FINAL PROJECT");
        
        questionBank = new QuestionBank();
        
        buildUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 660);
        setMinimumSize(new Dimension(600, 580));
        setLocationRelativeTo(null);
    }

    // ---------- Login / Logout ----------
    @Override
    protected void handleSignup() {
        String name = signupNameField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter your name!"
            );
            return;
        }

        //save the name
        String id = leaderboard.addPlayer(name);

        if (id != null) {
            
            scoreboard.addPlayer(id, name);
            
            // Save current player
            currentPlayer.set(name, id, 0, -1);

            JOptionPane.showMessageDialog(
                this,
                "Player created successfully!"
            );

            // Remove old START card
            cardPanel.remove(0);

            // Rebuild START card
            cardPanel.add(buildStartPanel(), START_CARD, 0);

            // Show updated START card
            cardLayout.show(cardPanel, START_CARD);

            cardPanel.revalidate();
            cardPanel.repaint();
        } 
    }
    
    @Override
    protected void handleLogin() {
        String name = loginNameField.getText().trim();

        if (name.isEmpty()) {

            JOptionPane.showMessageDialog(
                this,
                "Please enter your name!"
            );

            return;
        }

        // Find player in offline database
        Player player = leaderboard.getPlayer(name);

        if (player != null) {

            // Set logged-in player
            currentPlayer.set(
                player.player_name,
                player.id,
                player.total_score,
                player.rank
            );

            JOptionPane.showMessageDialog(
                this,
                "Welcome back, "
                + player.player_name + "!"
            );

            // Rebuild start screen
            cardPanel.remove(0);

            cardPanel.add(buildStartPanel(), START_CARD, 0);

            cardLayout.show(cardPanel, START_CARD);

            cardPanel.revalidate();
            cardPanel.repaint();

        } 
    }
    
    @Override
    protected void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {

            currentPlayer.logout();

            // Remove old start panel
            cardPanel.remove(0);

            // Rebuild it without the logged-in player
            cardPanel.add(buildStartPanel(), START_CARD, 0);

            cardLayout.show(cardPanel,START_CARD);

            cardPanel.revalidate();
            cardPanel.repaint();
        }
    }
    
// ---------- Quiz logic ----------
    
    @Override
    protected void handleReturn() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to leave the quiz?",
            "Return",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            cardLayout.show(cardPanel, START_CARD);
        }
    }

    @Override
    protected void startQuiz() {
        playerName = currentPlayer.player.player_name;
        playerId = currentPlayer.player.id;
        
        score = 0;
        currentIndex = 0;
        activeQuestions = buildShuffledQuestionSet();
        //currentSessionId = beginQuizSession(playerId, playerName);

        playerTagLabel.setText("Player: " + playerName + "   |   ID: " + playerId);

        cardLayout.show(cardPanel, QUIZ_CARD);
        showQuestion();
    }

    private List<Question> buildShuffledQuestionSet() {
        List<Question> combined = new ArrayList<>();
        
        combined.addAll(shuffledStageCopy(questionBank.getStage1Bank()));
        combined.addAll(shuffledStageCopy(questionBank.getStage2Bank()));
        combined.addAll(shuffledStageCopy(questionBank.getStage3Bank()));

        return combined;
    }

    private List<Question> shuffledStageCopy(List<Question> bank) {
        List<Question> copy = new ArrayList<>();
        for (Question q : bank) {
            copy.add(shuffleOptions(q));
        }
        Collections.shuffle(copy, random);
        return copy;
    }

    // Returns a new Question with the same text but options (and correct index) shuffled
    private Question shuffleOptions(Question original) {
        String correctAnswerText = original.choices[original.correctIndex];
        List<String> opts = new ArrayList<>(Arrays.asList(original.choices));
        Collections.shuffle(opts, random);
        int newCorrectIndex = opts.indexOf(correctAnswerText);
        return new Question(original.questionText, opts.toArray(String[]::new), newCorrectIndex);
    }

    private void showQuestion() {
        Question q = activeQuestions.get(currentIndex);
        int stageIndex = currentIndex / QUESTIONS_PER_STAGE; // 0-based
        int questionInStage = (currentIndex % QUESTIONS_PER_STAGE) + 1;
        Color accent = STAGE_COLORS[stageIndex];

        stageLabel.setText(STAGE_TITLES[stageIndex]);
        stageLabel.setForeground(accent);
        progressBar.setForeground(accent);
        progressBar.setValue(currentIndex);

        questionNumberLabel.setText("Question " + questionInStage + " of " + QUESTIONS_PER_STAGE
                + "  (Overall: " + (currentIndex + 1) + " of " + TOTAL_QUESTIONS + ")");
        questionLabel.setText("<html>" + q.questionText + "</html>");

        String[] letters = {"A", "B", "C", "D"};
        optionGroup.clearSelection();
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(letters[i] + ".   " + q.choices[i]);
            optionButtons[i].setEnabled(true);
        }

        feedbackLabel.setText(" ");
        nextButton.setText("SUBMIT ANSWER");
        nextButton.setBackground(accent);
        scoreLabel.setText("Score: " + score + " / " + TOTAL_QUESTIONS);
    }

    private void handleSubmitOrNext() {
        if (nextButton.getText().equals("SUBMIT ANSWER")) {
            int selected = getSelectedOptionIndex();
            if (selected == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select an answer before submitting.",
                        "No answer selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Question q = activeQuestions.get(currentIndex);
            for (JRadioButton b : optionButtons) {
                b.setEnabled(false);
            }

            if (selected == q.correctIndex) {
                score++;
                feedbackLabel.setForeground(GREEN);
                feedbackLabel.setText("Correct!");
            } else {
                feedbackLabel.setForeground(RED);
                feedbackLabel.setText("Incorrect. Correct answer: " + q.choices[q.correctIndex]);
            }

            scoreLabel.setText("Score: " + score + " / " + TOTAL_QUESTIONS);
            nextButton.setText(currentIndex == TOTAL_QUESTIONS - 1 ? "FINISH QUIZ" : "NEXT QUESTION");
        } else {
            currentIndex++;

            if (currentIndex >= TOTAL_QUESTIONS) {
                progressBar.setValue(TOTAL_QUESTIONS);
                finishQuiz();
                return;
            }

            // Announce the start of a new stage
            if (currentIndex == QUESTIONS_PER_STAGE || currentIndex == QUESTIONS_PER_STAGE * 2) {
                int newStage = (currentIndex / QUESTIONS_PER_STAGE) + 1;
                JOptionPane.showMessageDialog(this,
                        "Stage " + (newStage - 1) + " complete! Starting Stage " + newStage + ".",
                        "Stage Complete", JOptionPane.INFORMATION_MESSAGE);
            }

            showQuestion();
        }
    }

    private int getSelectedOptionIndex() {
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].isSelected()) {
                return i;
            }
        }
        return -1;
    }

    private void finishQuiz() {
        double percentage = (score * 100.0) / TOTAL_QUESTIONS;

        String badgeText;
        Color badgeColor;
        if (percentage >= 90) {
            badgeText = "OUTSTANDING - You're a Java pro!";
            badgeColor = GOLD.darker();
        } else if (percentage >= 75) {
            badgeText = "GREAT JOB - Solid Java knowledge!";
            badgeColor = GREEN;
        } else if (percentage >= 50) {
            badgeText = "NOT BAD - Keep practicing!";
            badgeColor = STAGE_COLORS[0];
        } else {
            badgeText = "KEEP STUDYING - You'll get there!";
            badgeColor = RED;
        }
        resultBadgeLabel.setText(badgeText);
        resultBadgeLabel.setForeground(badgeColor);

        resultDetailLabel.setText(
                "<html><div style='text-align:center;'>" + playerName + " (ID: " + playerId + ")"
                        + "<br>You scored " + score + " out of " + TOTAL_QUESTIONS
                        + "<br>(" + String.format("%.0f", percentage) + "%)</div></html>");

        scoreboard.addScore(playerId, "All", score);
        cardLayout.show(cardPanel, RESULT_CARD);
    }
    
    @Override
    protected void refreshLeaderboard() {

        leaderboardModel.setRowCount(0);

        try {
            Player current = currentPlayer.getPlayer();

            // only update online leaderboard if a player is logged in
            if (current != null) {

                // get highest local score
                int highestScore =
                    scoreboard.getHighestScore(current.id);

                // update player's score online
                leaderboard.updateTotalScore(
                    current.id,
                    highestScore
                );
            }

            // get all players from online database
            ArrayList<Player> players =
                leaderboard.getAllPlayers();

            // add online players to table
            for (Player player : players) {

                leaderboardModel.addRow(new Object[] {
                    player.rank,
                    player.id,
                    player.player_name,
                    player.total_score
                });
            }

        } catch (Exception e) {

            System.out.println(
                "Leaderboard unavailable: "
                + e.getMessage()
            );
            
            JOptionPane.showMessageDialog(
                this,
                "Leaderboard unavailable: "
                + e.getMessage()
            );

            // show something in the table instead of crashing
            leaderboardModel.addRow(new Object[] {
                "-",
                "-",
                "No internet connection",
                "-"
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nextButton) {
            handleSubmitOrNext();
        }
    }

    // ---------- Entry point ----------
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ignored) {
            // fall back to default look and feel
        }

        SwingUtilities.invokeLater(() -> {
            QuizGame game = new QuizGame();
            game.setVisible(true);
        });
    }
}