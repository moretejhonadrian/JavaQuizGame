package javaquizgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class QuizGame extends GUI {
    
    // Active shuffled question list for the current play-through
    private List<Question> activeQuestions = new ArrayList<>();

    private int currentIndex = 0;
    private int score = 0;
    private String quizId;
    private String playerName;
    private String playerId;
    private final Random random = new Random();

    public QuizGame() {
        super("GROUP 2 FINAL PROJECT");
        
        if (currentPlayer.player != null) {
            setPlayer();
        }
        
        buildUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 660);
        setMinimumSize(new Dimension(600, 580));
        setLocationRelativeTo(null);
    }

    // ---------- Login / Logout ----------
    
    private void setPlayer() {
        playerName = currentPlayer.player.player_name;
        playerId = currentPlayer.player.id;
    } 
    
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

        //save player
        Player player = db.addPlayer(name);

        if (player != null) {
            
            currentPlayer.set(player);
            setPlayer();

            JOptionPane.showMessageDialog(
                this,
                "Player created successfully!"
            );
            
            signupNameField.setText("");
            
            // Rebuild start screen
            rebuildScreen(buildStartPanel(), START_CARD, 0);
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

        // Find player in database
        Player player = db.getPlayer(name);

        if (player != null) {

            // Set logged-in player
            currentPlayer.set(player);
            setPlayer();

            JOptionPane.showMessageDialog(
                this,
                "Welcome back, "
                + player.player_name + "!"
            );
            
            loginNameField.setText("");
            
            // Rebuild start screen
            rebuildScreen(buildStartPanel(), START_CARD, 0);

        } else {
            JOptionPane.showMessageDialog(
                this,
                "Player \"" + name + "\" doesn't exists!"
                + "\nEnter an existing name."
            );
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

            // Rebuild start screen
            rebuildScreen(buildStartPanel(), START_CARD, 0);
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
    protected void startQuiz(int quizIndex, String id) { 
        score = 0;
        currentIndex = 0;
        quizId = id;
        activeQuestions = buildShuffledQuestionSet(quizIndex);
        
        playerTagLabel.setText("Player: " + playerName + "   |   ID: " + playerId);

        cardLayout.show(cardPanel, QUIZ_CARD);
        showQuestion();
    }

    private List<Question> buildShuffledQuestionSet(int quizIndex) {
        List<Question> quiz = questionBank.getQuizzes().get(quizIndex);
        
        return shuffledStageCopy(quiz);
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
        int quizIndex = currentIndex / QUESTIONS_PER_QUIZ; // 0-based
        int questionInQuiz = (currentIndex % QUESTIONS_PER_QUIZ) + 1;
        Color accent = STAGE_COLORS[quizIndex];

        stageLabel.setText(questionBank.getQuizTitle(quizId));
        stageLabel.setForeground(accent);
        progressBar.setForeground(accent);
        progressBar.setValue(currentIndex);

        questionNumberLabel.setText("Question " + questionInQuiz + " of " + QUESTIONS_PER_QUIZ);
                //+ "  (Overall: " + (currentIndex + 1) + " of " + TOTAL_QUESTIONS + ")");
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
        scoreLabel.setText("Score: " + score + " / " + QUESTIONS_PER_QUIZ);
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

            scoreLabel.setText("Score: " + score + " / " + QUESTIONS_PER_QUIZ);
            nextButton.setText(currentIndex == QUESTIONS_PER_QUIZ - 1 ? "FINISH QUIZ" : "NEXT QUESTION");
        } else {
            currentIndex++;

            if (currentIndex >= QUESTIONS_PER_QUIZ) {
                progressBar.setValue(QUESTIONS_PER_QUIZ);
                finishQuiz();
                return;
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
        double percentage = (score * 100.0) / QUESTIONS_PER_QUIZ;

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
                        + "<br>You scored " + score + " out of " + QUESTIONS_PER_QUIZ
                        + "<br>(" + String.format("%.0f", percentage) + "%)</div></html>");

        db.addScore(playerId, quizId, score);
        cardLayout.show(cardPanel, RESULT_CARD);
    }
    
    
    /*
        Ranking basis:

        quizResultPercentage = score / 10 * 100

        totalPoints += quizResultPercentage / quizzesSize
    
    */
    
    @Override
    protected void refreshLeaderboard() {

        leaderboardModel.setRowCount(0);

        try {
            
            updateTotalPoints();
                
            List<Player> players = db.getAllPlayers();
            
            int rank = 1;
            
            for (Player player : players) {

                leaderboardModel.addRow(new Object[] {
                    rank,
                    player.id,
                    player.player_name,
                    player.total_points
                });
                
                rank++;
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
    
    private void updateTotalPoints() {
        Map<String, String> quizzesNames = questionBank.getQuizzesNames();
        
        double totalPoints = 0;
        
        for (Map.Entry<String, String> quiz : quizzesNames.entrySet()) {
            
            String quizKey = quiz.getKey();
            
            double highestScore = db.getHighestScore(playerId, quizKey);
            
            double quizResultPercentage = (highestScore / QUESTIONS_PER_QUIZ) * 100;
            
            totalPoints += quizResultPercentage / questionBank.getQuizzesSize();
        }
        
        try {
            //update totalpoints of user
            db.updateTotalScore(playerId, totalPoints);

        } catch (Exception ex) {

            System.getLogger(QuizGame.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }
    
    @Override
    protected void quizzesButtons(Map<String, String> quizzesNames, JPanel contentPanel) {
        
        for (Map.Entry<String, String> quiz : quizzesNames.entrySet()) {

            String quizKey = quiz.getKey();
            String quizName = quiz.getValue();
            
            int quizIndex = Integer.parseInt(quizKey.substring(2)) - 1;

            // Quiz button
            JButton quizButton = makeButton(quizName, STAGE_COLORS[0], Color.WHITE, 15);

            quizButton.setAlignmentX(Component.LEFT_ALIGNMENT);

            quizButton.addActionListener(e -> {
                startQuiz(quizIndex, quizKey);
            });

            // Progress bar
            JProgressBar bar = new JProgressBar(0, QUESTIONS_PER_QUIZ);
           
            int highestScore = db.getHighestScore(playerId, quizKey);
            
            bar.setValue(highestScore);
            bar.setString(
                    highestScore + " / " + QUESTIONS_PER_QUIZ
            );
            bar.setStringPainted(true);
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Container for button + progress bar
            JPanel quizPanel = new JPanel();
            quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
            quizPanel.setOpaque(false);
            quizPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            quizPanel.add(quizButton);
            quizPanel.add(Box.createVerticalStrut(5));
            quizPanel.add(bar);

            contentPanel.add(quizPanel);
            contentPanel.add(Box.createVerticalStrut(15));
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