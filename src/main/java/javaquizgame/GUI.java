package javaquizgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableCellRenderer;

public abstract class GUI extends JFrame implements ActionListener {  
    
    protected static final int QUESTIONS_PER_STAGE = 10;
    protected static final int STAGE_COUNT = 3;
    protected static final int TOTAL_QUESTIONS = QUESTIONS_PER_STAGE * STAGE_COUNT;
    
    // ---------- Color palette ----------
    protected static final Color BG_DARK = new Color(22, 26, 46);
    protected static final Color CARD_BG = new Color(250, 250, 252);
    protected static final Color TEXT_DARK = new Color(30, 32, 46);
    protected static final Color TEXT_MUTED = new Color(105, 110, 130);
    protected static final Color TEXT_LIGHT = new Color(230, 232, 245);
    protected static final Color GOLD = new Color(255, 190, 40);
    protected static final Color GREEN = new Color(39, 174, 96);
    protected static final Color RED = new Color(214, 69, 65);
    protected static final Color GRAY_BTN = new Color(120, 126, 148);

    // Per-stage accent colors and titles
    protected static final Color[] STAGE_COLORS = {
            new Color(41, 128, 185),   // Stage 1 - blue
            new Color(155, 89, 182),   // Stage 2 - purple
            new Color(230, 126, 34)    // Stage 3 - orange
    };
    
    protected static final String[] STAGE_TITLES = {
            "STAGE 1: JAVA BASICS",
            "STAGE 2: OBJECT-ORIENTED PROGRAMMING",
            "STAGE 3: COLLECTIONS & ADVANCED TOPICS"
    };

    private static final String FONT_FAMILY = "Segoe UI";
    
    // ---------- UI components ----------
    protected JTextField signupNameField;
    protected JTextField loginNameField;
    protected JTextField playerNameField;
    protected JLabel stageLabel;
    protected JLabel questionNumberLabel;
    protected JLabel questionLabel;
    protected JProgressBar progressBar;
    protected JRadioButton[] optionButtons;
    protected ButtonGroup optionGroup;
    protected JButton nextButton;
    protected JLabel scoreLabel;
    protected JLabel feedbackLabel;
    protected JLabel playerTagLabel;

    protected JLabel resultLabel;
    protected JLabel resultDetailLabel;
    protected JLabel resultBadgeLabel;

    protected DefaultTableModel leaderboardModel;

    protected JPanel cardPanel;
    protected CardLayout cardLayout;

    protected static final String START_CARD = "START";
    protected static final String SIGNUP_CARD = "SIGNUP";
    protected static final String LOGIN_CARD = "LOGIN";
    protected static final String QUIZ_CARD = "QUIZ";
    protected static final String RESULT_CARD = "RESULT";
    protected static final String LEADERBOARD_CARD = "LEADERBOARD";
    
    //Needed Data
    protected Leaderboard leaderboard;
    protected CurrentPlayer currentPlayer;
    protected Scoreboard scoreboard;

    public GUI(String title) {
        super(title);
        
        leaderboard = new Leaderboard();
        scoreboard = new Scoreboard();
        currentPlayer = new CurrentPlayer();
    }
    
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
    protected void buildUI() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_DARK);

        cardPanel.add(buildStartPanel(), START_CARD);
        cardPanel.add(buildSignupPanel(), SIGNUP_CARD);
        cardPanel.add(buildLoginPanel(), LOGIN_CARD);
        cardPanel.add(buildQuizPanel(), QUIZ_CARD);
        cardPanel.add(buildResultPanel(), RESULT_CARD);
        cardPanel.add(buildLeaderboardPanel(), LEADERBOARD_CARD);

        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
    }

    protected JPanel buildStartPanel() {
        
        JButton signupButton = makeButton("SIGN UP", GREEN, Color.WHITE, 14);
        signupButton.addActionListener(e -> cardLayout.show(cardPanel, SIGNUP_CARD));
        
        JButton loginButton = makeButton("LOGIN", STAGE_COLORS[0], Color.WHITE, 14);
        loginButton.addActionListener(e -> cardLayout.show(cardPanel, LOGIN_CARD));
        
        JButton logoutButton = makeButton("LOGOUT", RED, Color.WHITE, 14);
        logoutButton.addActionListener(e -> handleLogout());
        
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        RoundedPanel card = new RoundedPanel(CARD_BG, 28);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(460, 520));

        JLabel title = makeLabel("Technical Education and Skills Development Authority", Font.BOLD, 13, TEXT_MUTED);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title2 = makeLabel("NRG INFO-TECH INC.", Font.BOLD, 14, TEXT_DARK);
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);
        title2.setBorder(new EmptyBorder(2, 0, 16, 0));

        JLabel title3 = new JLabel(
            "<html><div style='text-align: center;'>"
            + "JAVA PROGRAMMING<br>QUIZ GAME"
            + "</div></html>"
        );

        title3.setFont(new Font(FONT_FAMILY, Font.BOLD, 24));
        title3.setForeground(STAGE_COLORS[0]);
        title3.setAlignmentX(Component.CENTER_ALIGNMENT);
        title3.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = makeLabel("3 stages | 10 questions each ",
                Font.PLAIN, 14, TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(10, 0, 30, 0));

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
        
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        if (currentPlayer.isSet()) {

            Player player = currentPlayer.getPlayer();

            JLabel welcomeLabel = new JLabel(
                "<html>Welcome, <b><font color='#27AE60'>"
                + player.player_name
                + "!</font></b></html>"
            );

            welcomeLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 16));
            welcomeLabel.setForeground(TEXT_MUTED);
            welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

            card.add(welcomeLabel);

            JLabel messageLabel = makeLabel("Ready to test your Java skills?", Font.PLAIN, 13, TEXT_MUTED);
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(welcomeLabel);
            card.add(Box.createRigidArea(new Dimension(0, 4)));
            card.add(messageLabel);
            card.add(Box.createRigidArea(new Dimension(0, 30)));
            card.add(startButton);
            card.add(Box.createRigidArea(new Dimension(0, 12)));
            card.add(viewLeaderboardButton);
            card.add(Box.createRigidArea(new Dimension(0, 12)));
            card.add(logoutButton);

    } else {

        JLabel messageLabel = makeLabel("Create an account or log in to start!", Font.PLAIN, 13, TEXT_MUTED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(messageLabel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(signupButton);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(loginButton);
    }

        outer.add(card);
        return outer;
    }
    
    private JPanel buildSignupPanel() {

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        RoundedPanel card = new RoundedPanel(CARD_BG, 28);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(460, 400));

        JLabel title = makeLabel("SIGN UP", Font.BOLD, 26, STAGE_COLORS[0]);

        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = makeLabel("Enter your Name:", Font.BOLD, 15, TEXT_DARK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        signupNameField = new JTextField();

        signupNameField.setMaximumSize(new Dimension(260, 42));

        signupNameField.setFont(new Font(FONT_FAMILY, Font.PLAIN, 16));

        signupNameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton signupButton = makeButton("CREATE PLAYER", GREEN, Color.WHITE, 15);

        signupButton.addActionListener(e -> handleSignup());

        JButton backButton = makeButton("BACK", GRAY_BTN, Color.WHITE, 14);

        backButton.addActionListener(e -> {
            signupNameField.setText("");
            cardLayout.show(cardPanel, START_CARD);
        });

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 30)));
        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(signupNameField);
        card.add(Box.createRigidArea(new Dimension(0, 25)));
        card.add(signupButton);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(backButton);
        outer.add(card);

        return outer;
    }
    
    private JPanel buildLoginPanel() {

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_DARK);

        RoundedPanel card = new RoundedPanel(CARD_BG, 28);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setPreferredSize(new Dimension(460, 400));

        JLabel title = makeLabel("LOGIN", Font.BOLD, 26, STAGE_COLORS[0]);

        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = makeLabel("Enter your Name:", Font.BOLD, 15, TEXT_DARK);

        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginNameField = new JTextField();

        loginNameField.setMaximumSize(new Dimension(260, 42));

        loginNameField.setFont(new Font(FONT_FAMILY, Font.PLAIN, 16));

        loginNameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = makeButton("LOGIN", STAGE_COLORS[0], Color.WHITE, 15);

        loginButton.addActionListener(e -> handleLogin());

        JButton backButton = makeButton("BACK", GRAY_BTN, Color.WHITE, 14);

        backButton.addActionListener(e -> {
            loginNameField.setText("");
            cardLayout.show(cardPanel, START_CARD);
        });

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 30)));
        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(loginNameField);
        card.add(Box.createRigidArea(new Dimension(0, 25)));
        card.add(loginButton);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(backButton);
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

        // Return button
        JButton returnButton = new JButton("←");
        returnButton.setFont(new Font(FONT_FAMILY, Font.BOLD, 20));
        returnButton.setForeground(TEXT_DARK);
        returnButton.setBackground(CARD_BG);
        returnButton.setBorderPainted(false);
        returnButton.setFocusPainted(false);
        returnButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        returnButton.addActionListener(e -> handleReturn());

        // Player label
        playerTagLabel = makeLabel("Player: -", Font.PLAIN, 12, TEXT_MUTED);
        playerTagLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Stage label
        stageLabel = makeLabel("STAGE 1: JAVA BASICS", Font.BOLD, 17, STAGE_COLORS[0]);
        stageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Progress bar
        progressBar = new JProgressBar(0, TOTAL_QUESTIONS);
        progressBar.setValue(0);
        progressBar.setForeground(STAGE_COLORS[0]);
        progressBar.setBackground(new Color(226, 228, 236));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 8));

        // Top row with return button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(returnButton, BorderLayout.WEST);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        playerTagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(playerTagLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(stageLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        headerPanel.add(progressBar);

        topPanel.add(headerPanel, BorderLayout.CENTER);

        card.add(topPanel, BorderLayout.NORTH);

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
        table.setDefaultRenderer(Object.class, new GUI.PodiumRowRenderer());
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
    public static class PodiumRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Object rankValue = table.getValueAt(row, 0);
            // Column 1 = Player ID, column 2 = Name -> left aligned; Rank/Score centered
            setHorizontalAlignment((column == 1 || column == 2) ? SwingConstants.LEFT : SwingConstants.CENTER);

            Color rowColor = (row % 2 == 0) ? Color.WHITE : new Color(244, 245, 249);
            setForeground(TEXT_DARK);

            if (rankValue instanceof Integer rank) {
                if (null != rank) switch (rank) {
                    case 1 -> rowColor = new Color(255, 223, 128);
                    case 2 -> rowColor = new Color(224, 224, 230);
                    case 3 -> rowColor = new Color(235, 190, 155);
                    default -> {
                    }
                }
            }
            setBackground(rowColor);
            return c;
        }
    }
    
    protected abstract void handleSignup();
    protected abstract void handleLogin();
    protected abstract void handleLogout();
    protected abstract void startQuiz();
    protected abstract void handleReturn();
    protected abstract void refreshLeaderboard();       
}
