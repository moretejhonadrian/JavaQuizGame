package javaquizgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.List;


public class QuizGame extends JFrame implements ActionListener {

    // ---------- Color palette ----------
    private static final Color BG_DARK = new Color(22, 26, 46);
    private static final Color CARD_BG = new Color(250, 250, 252);
    private static final Color TEXT_DARK = new Color(30, 32, 46);
    private static final Color TEXT_MUTED = new Color(105, 110, 130);
    private static final Color TEXT_LIGHT = new Color(230, 232, 245);
    private static final Color GOLD = new Color(255, 190, 40);
    private static final Color GREEN = new Color(39, 174, 96);
    private static final Color RED = new Color(214, 69, 65);
    private static final Color GRAY_BTN = new Color(120, 126, 148);

    // Per-stage accent colors and titles
    private static final Color[] STAGE_COLORS = {
            new Color(41, 128, 185),   // Stage 1 - blue
            new Color(155, 89, 182),   // Stage 2 - purple
            new Color(230, 126, 34)    // Stage 3 - orange
    };
    private static final String[] STAGE_TITLES = {
            "STAGE 1: JAVA BASICS",
            "STAGE 2: OBJECT-ORIENTED PROGRAMMING",
            "STAGE 3: COLLECTIONS & ADVANCED TOPICS"
    };

    private static final String FONT_FAMILY = "Segoe UI";
    
    // ---------- Database ----------
    private Database database;
    private Connection dbConnection;
    private static final String ID_PREFIX = "PLY-";

    private static final int QUESTIONS_PER_STAGE = 10;
    private static final int STAGE_COUNT = 3;
    private static final int TOTAL_QUESTIONS = QUESTIONS_PER_STAGE * STAGE_COUNT;

    // Master question banks (never mutated directly; copied + shuffled each play)
    private QuestionBank questionBank;

    // Active shuffled question list for the current play-through
    private List<Question> activeQuestions = new ArrayList<>();

    private int currentIndex = 0;
    private int score = 0;
    private String playerName = "";
    private String playerId = "";
    private long currentSessionId = -1; // -1 = no session currently open
    private final Random random = new Random();

    // ---------- UI components ----------
    private JTextField playerNameField;

    private JLabel stageLabel;
    private JLabel questionNumberLabel;
    private JLabel questionLabel;
    private JProgressBar progressBar;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionGroup;
    private JButton nextButton;
    private JLabel scoreLabel;
    private JLabel feedbackLabel;
    private JLabel playerTagLabel;

    private JLabel resultLabel;
    private JLabel resultDetailLabel;
    private JLabel resultBadgeLabel;

    private DefaultTableModel leaderboardModel;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private static final String START_CARD = "START";
    private static final String QUIZ_CARD = "QUIZ";
    private static final String RESULT_CARD = "RESULT";
    private static final String LEADERBOARD_CARD = "LEADERBOARD";

    public QuizGame() {
        super("Java Programming Quiz Game");
        
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
    
    // ---------- Small styling helpers ----------

    /** A JPanel that paints a solid rounded-rectangle background. */
    private static class RoundedPanel extends JPanel {
        private final Color bg;
        private final int radius;

        RoundedPanel(Color bg, int radius) {
            this.bg = bg;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JButton makeButton(String text, Color bg, Color fg, int fontSize) {
        JButton button = new JButton(text);
        button.setFont(new Font(FONT_FAMILY, Font.BOLD, fontSize));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 26, 12, 26));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private JLabel makeLabel(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FONT_FAMILY, style, size));
        label.setForeground(color);
        return label;
    }

    // ---------- UI construction ----------
    private void buildUI() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_DARK);

        cardPanel.add(buildStartPanel(), START_CARD);
        cardPanel.add(buildQuizPanel(), QUIZ_CARD);
        cardPanel.add(buildResultPanel(), RESULT_CARD);
        cardPanel.add(buildLeaderboardPanel(), LEADERBOARD_CARD);

        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel buildStartPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        RoundedPanel card = new RoundedPanel(CARD_BG, 28);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(460, 470));

        JLabel title = makeLabel("Technical Education and Skills Development Authority", Font.BOLD, 13, TEXT_MUTED);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title2 = makeLabel("NRG INFO-TECH INC.", Font.BOLD, 14, TEXT_DARK);
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);
        title2.setBorder(new EmptyBorder(2, 0, 16, 0));

        JLabel title3 = makeLabel("JAVA PROGRAMMING QUIZ GAME", Font.BOLD, 24, STAGE_COLORS[0]);
        title3.setAlignmentX(Component.CENTER_ALIGNMENT);
        title3.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = makeLabel("3 stages | 10 questions each ",
                Font.PLAIN, 14, TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(10, 0, 30, 0));

        JLabel nameLabel = makeLabel("Enter your Name:", Font.BOLD, 15, TEXT_DARK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameHintLabel = makeLabel("(your Player ID is assigned automatically)", Font.PLAIN, 11, TEXT_MUTED);
        nameHintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameHintLabel.setBorder(new EmptyBorder(2, 0, 0, 0));

        playerNameField = new JTextField();
        playerNameField.setMaximumSize(new Dimension(260, 42));
        playerNameField.setFont(new Font(FONT_FAMILY, Font.PLAIN, 16));
        playerNameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 203, 214), 1, true),
                new EmptyBorder(6, 10, 6, 10)));

        JButton startButton = makeButton("START QUIZ", STAGE_COLORS[0], Color.WHITE, 16);
        startButton.addActionListener(e -> startQuiz());

        JButton viewLeaderboardButton = makeButton("VIEW LEADERBOARD", GOLD, TEXT_DARK, 14);
        viewLeaderboardButton.addActionListener(e -> {
            refreshLeaderboard();
            cardLayout.show(cardPanel, LEADERBOARD_CARD);
        });

        card.add(title);
        card.add(title2);
        card.add(title3);
        card.add(subtitle);
        card.add(nameLabel);
        card.add(nameHintLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(playerNameField);
        card.add(Box.createRigidArea(new Dimension(0, 26)));
        card.add(startButton);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(viewLeaderboardButton);

        outer.add(card);
        return outer;
    }

    private JPanel buildQuizPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        RoundedPanel card = new RoundedPanel(CARD_BG, 28);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(24, 36, 26, 36));
        card.setPreferredSize(new Dimension(600, 500));

        playerTagLabel = makeLabel("Player: -", Font.PLAIN, 12, TEXT_MUTED);
        playerTagLabel.setHorizontalAlignment(SwingConstants.CENTER);

        stageLabel = makeLabel("STAGE 1: JAVA BASICS", Font.BOLD, 17, STAGE_COLORS[0]);
        stageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressBar = new JProgressBar(0, TOTAL_QUESTIONS);
        progressBar.setValue(0);
        progressBar.setForeground(STAGE_COLORS[0]);
        progressBar.setBackground(new Color(226, 228, 236));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 8));

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.add(playerTagLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(stageLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        headerPanel.add(progressBar);
        card.add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        questionNumberLabel = makeLabel("Question 1 of " + TOTAL_QUESTIONS, Font.PLAIN, 13, TEXT_MUTED);
        questionNumberLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        questionLabel = new JLabel("");
        questionLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 19));
        questionLabel.setForeground(TEXT_DARK);
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionLabel.setBorder(new EmptyBorder(10, 0, 18, 0));

        centerPanel.add(questionNumberLabel);
        centerPanel.add(questionLabel);

        String[] letters = {"A", "B", "C", "D"};
        optionButtons = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font(FONT_FAMILY, Font.PLAIN, 15));
            optionButtons[i].setForeground(TEXT_DARK);
            optionButtons[i].setOpaque(false);
            optionButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            optionButtons[i].setFocusPainted(false);
            optionButtons[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            optionButtons[i].setBorder(new EmptyBorder(6, 4, 6, 4));
            optionGroup.add(optionButtons[i]);
            centerPanel.add(optionButtons[i]);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(feedbackLabel);

        card.add(centerPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        scoreLabel = makeLabel("Score: 0 / " + TOTAL_QUESTIONS, Font.BOLD, 14, TEXT_DARK);

        nextButton = makeButton("SUBMIT ANSWER", STAGE_COLORS[0], Color.WHITE, 15);
        nextButton.addActionListener(this);

        footerPanel.add(scoreLabel, BorderLayout.WEST);
        footerPanel.add(nextButton, BorderLayout.EAST);

        card.add(footerPanel, BorderLayout.SOUTH);

        outer.add(card);
        return outer;
    }

    private JPanel buildResultPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        RoundedPanel card = new RoundedPanel(CARD_BG, 28);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(45, 50, 40, 50));
        card.setPreferredSize(new Dimension(460, 420));

        resultLabel = makeLabel("QUIZ COMPLETE!", Font.BOLD, 24, TEXT_DARK);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultBadgeLabel = new JLabel("");
        resultBadgeLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 16));
        resultBadgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultBadgeLabel.setBorder(new EmptyBorder(14, 0, 10, 0));

        resultDetailLabel = new JLabel("");
        resultDetailLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 16));
        resultDetailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultDetailLabel.setBorder(new EmptyBorder(6, 0, 30, 0));

        JButton leaderboardButton = makeButton("VIEW LEADERBOARD", GOLD, TEXT_DARK, 14);
        leaderboardButton.addActionListener(e -> {
            refreshLeaderboard();
            cardLayout.show(cardPanel, LEADERBOARD_CARD);
        });

        JButton restartButton = makeButton("PLAY AGAIN", GREEN, Color.WHITE, 14);
        restartButton.addActionListener(e -> cardLayout.show(cardPanel, START_CARD));

        card.add(resultLabel);
        card.add(resultBadgeLabel);
        card.add(resultDetailLabel);
        card.add(leaderboardButton);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(restartButton);

        outer.add(card);
        return outer;
    }

    private JPanel buildLeaderboardPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        RoundedPanel card = new RoundedPanel(CARD_BG, 28);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(30, 34, 26, 34));
        card.setPreferredSize(new Dimension(600, 480));

        JLabel title = makeLabel("LEADERBOARD", Font.BOLD, 22, GOLD.darker());
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, BorderLayout.NORTH);

        leaderboardModel = new DefaultTableModel(new Object[]{"Rank", "Player ID", "Name", "Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(leaderboardModel);
        table.setRowHeight(30);
        table.setFont(new Font(FONT_FAMILY, Font.PLAIN, 15));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        table.getTableHeader().setBackground(BG_DARK);
        table.getTableHeader().setForeground(TEXT_LIGHT);
        table.setDefaultRenderer(Object.class, new PodiumRowRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 228, 236)));
        card.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = makeButton("BACK", GRAY_BTN, Color.WHITE, 14);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, START_CARD));
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.add(backButton);
        card.add(footer, BorderLayout.SOUTH);

        outer.add(card);
        return outer;
    }

    /** Highlights rank 1 (gold), rank 2 (silver) and rank 3 (bronze) rows. */
    private static class PodiumRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Object rankValue = table.getValueAt(row, 0);
            // Column 1 = Player ID, column 2 = Name -> left aligned; Rank/Score centered
            setHorizontalAlignment((column == 1 || column == 2) ? SwingConstants.LEFT : SwingConstants.CENTER);

            Color rowColor = (row % 2 == 0) ? Color.WHITE : new Color(244, 245, 249);
            setForeground(TEXT_DARK);

            if (rankValue instanceof Integer) {
                int rank = (Integer) rankValue;
                if (rank == 1) {
                    rowColor = new Color(255, 223, 128);
                } else if (rank == 2) {
                    rowColor = new Color(224, 224, 230);
                } else if (rank == 3) {
                    rowColor = new Color(235, 190, 155);
                }
            }
            setBackground(rowColor);
            return c;
        }
    }

    // ---------- Quiz logic ----------
    private void startQuiz() {
        String enteredName = playerNameField.getText().trim();
        if (enteredName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your name before starting.",
                    "Name required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        playerName = enteredName;
        playerId = getOrCreatePlayerId(playerName);

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
    private void refreshLeaderboard() {
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