package javaquizgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.List;


public class QuizGame extends GUI {
    
    // ---------- Database ----------
    private Database database;
    private final Connection dbConnection;
    private static final String ID_PREFIX = "PLY-";

    // Master question banks (never mutated directly; copied + shuffled each play)
    private final QuestionBank questionBank;

    // Active shuffled question list for the current play-through
    private List<Question> activeQuestions = new ArrayList<>();

    private int currentIndex = 0;
    private int score = 0;
    private final String playerName = "";
    private final String playerId = "";
    private long currentSessionId = -1; // -1 = no session currently open
    private final Random random = new Random();

    public QuizGame() {
        super("GROUP 2 FINAL PROJECT");
        
        database = new Database();
        dbConnection = database.getConnection();

        questionBank = new QuestionBank();
        
        buildUI();

        // Safety net: if the program is closed (X button, IDE stop, terminal
        // close) while a quiz is in progress, mark that session ABANDONED
        // instead of leaving it as a resumable/repeatable IN_PROGRESS row.
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> database.abandonActiveSessionOnExit(currentSessionId))
        );

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 660);
        setMinimumSize(new Dimension(600, 580));
        setLocationRelativeTo(null);
    }
    
    // ---------- Quiz logic ----------
    @Override
    protected void startQuiz() {
//        String enteredName = playerNameField.getText().trim();
//        if (enteredName.isEmpty()) {
//            JOptionPane.showMessageDialog(this,
//                    "Please enter your name before starting.",
//                    "Name required", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//        playerName = enteredName;
//        playerId = getOrCreatePlayerId(playerName);

        score = 0;
        currentIndex = 0;
        activeQuestions = buildShuffledQuestionSet();
        currentSessionId = beginQuizSession(playerId, playerName);

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
        String correctAnswerText = original.options[original.correctIndex];
        List<String> opts = new ArrayList<>(Arrays.asList(original.options));
        Collections.shuffle(opts, random);
        int newCorrectIndex = opts.indexOf(correctAnswerText);
        return new Question(original.questionText, opts.toArray(new String[0]), newCorrectIndex);
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
            optionButtons[i].setText(letters[i] + ".   " + q.options[i]);
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
                feedbackLabel.setText("Incorrect. Correct answer: " + q.options[q.correctIndex]);
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

        completeQuizSession(currentSessionId, score);
        currentSessionId = -1; // session is closed out - nothing left to abandon
        cardLayout.show(cardPanel, RESULT_CARD);
    }

    // ---------- Database: players ----------

    /**
     * Looks up the player's ID by name (case-insensitive, via SQLite's
     * COLLATE NOCASE). If the name has never played before, a new sequential
     * ID is generated and stored permanently in the "players" table - so it
     * is remembered the next time the program runs, since the database file
     * is never dropped or recreated.
     */
    private String getOrCreatePlayerId(String name) {
        String selectSql = "SELECT player_id FROM players WHERE player_name = ? COLLATE NOCASE";
        try (PreparedStatement ps = dbConnection.prepareStatement(selectSql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("player_id");
                }
            }
        } catch (SQLException ex) {
            database.showDbError("looking up your player record", ex);
        }

        int nextNumber = 1;
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM players")) {
            if (rs.next()) {
                nextNumber = rs.getInt("total") + 1;
            }
        } catch (SQLException ex) {
            database.showDbError("counting registered players", ex);
        }

        String newId = ID_PREFIX + String.format("%04d", nextNumber);
        String insertSql = "INSERT INTO players (player_id, player_name) VALUES (?, ?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(insertSql)) {
            ps.setString(1, newId);
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException ex) {
            database.showDbError("registering your player ID", ex);
        }
        return newId;
    }

    // ---------- Database: quiz sessions / leaderboard ----------

    /** Opens a new IN_PROGRESS session row and returns its generated ID. */
    private long beginQuizSession(String id, String name) {
        String sql = "INSERT INTO quiz_sessions (player_id, player_name, score, total_questions, status, started_at) "
                + "VALUES (?, ?, 0, ?, 'IN_PROGRESS', ?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setInt(3, TOTAL_QUESTIONS);
            ps.setString(4, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException ex) {
            database.showDbError("starting a new quiz session", ex);
        }
        return -1;
    }

    /** Marks a session COMPLETED with its final score - this is what counts for the leaderboard. */
    private void completeQuizSession(long sessionId, int finalScore) {
        if (sessionId == -1) {
            return;
        }
        String sql = "UPDATE quiz_sessions SET score = ?, status = 'COMPLETED', finished_at = ? WHERE session_id = ?";
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setInt(1, finalScore);
            ps.setString(2, Instant.now().toString());
            ps.setLong(3, sessionId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            database.showDbError("saving your final score", ex);
        }
    }

    /** Reads the top completed sessions straight from the database. */
    @Override
    protected void refreshLeaderboard() {
        leaderboardModel.setRowCount(0);
        String sql = "SELECT player_id, player_name, score FROM quiz_sessions "
                + "WHERE status = 'COMPLETED' ORDER BY score DESC, finished_at ASC LIMIT 20";

        boolean any = false;
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int rank = 1;
            while (rs.next()) {
                any = true;
                leaderboardModel.addRow(new Object[]{rank, rs.getString("player_id"),
                        rs.getString("player_name"), rs.getInt("score")});
                rank++;
            }
        } catch (SQLException ex) {
            database.showDbError("loading the leaderboard", ex);
        }

        if (!any) {
            leaderboardModel.addRow(new Object[]{"-", "-", "No scores yet", "-"});
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
        } catch (Exception ignored) {
            // fall back to default look and feel
        }

        SwingUtilities.invokeLater(() -> {
            QuizGame game = new QuizGame();
            game.setVisible(true);
        });
    }
}