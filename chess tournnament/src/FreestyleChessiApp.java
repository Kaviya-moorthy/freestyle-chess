import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class FreestyleChessiApp extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/freestyle";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "kaviya836";

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTable table;
    private DefaultTableModel model;
    private List<String[]> currentPairs = new ArrayList<>();

    private String playerName = "";
    private String playerDOB = "";
    private String playerEmail = "";
    private String playerPhone = "";

    public FreestyleChessiApp() {
        setTitle("Freestyle Chessi Tournament");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createWelcomePanel(), "Welcome");
        mainPanel.add(createRegisterPanel(), "Register");
        mainPanel.add(createTournamentPanel(), "Tournament");

        add(mainPanel);
        cardLayout.show(mainPanel, "Welcome");
    }

    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon background = new ImageIcon(
                        "C:\\Users\\kaviya\\Desktop\\dbms\\chess tournnament\\src\\chessbg.jpg");
                g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
        welcomePanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("WELCOME TO FREESTYLE CHESS TOURNAMENT", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        welcomePanel.add(title, BorderLayout.CENTER);

        JButton startButton = new JButton("GET STARTED");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        startButton.setBackground(Color.BLACK);
        startButton.setForeground(Color.WHITE);
        startButton.addActionListener(e -> cardLayout.show(mainPanel, "Register"));
        welcomePanel.add(startButton, BorderLayout.SOUTH);

        return welcomePanel;
    }

    private JPanel createRegisterPanel() {
        JPanel registerPanel = new JPanel(new GridBagLayout());
        JPanel formPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        formPanel.setPreferredSize(new Dimension(300, 300));
        formPanel.setBackground(Color.LIGHT_GRAY);
        formPanel.setBorder(BorderFactory.createTitledBorder("Register Player"));

        JButton nameButton = new JButton("Enter Name");
        JButton dobButton = new JButton("Enter Date of Birth");
        JButton emailButton = new JButton("Enter Email");
        JButton phoneButton = new JButton("Enter Phone Number");
        JButton registerButton = new JButton("Register Player");

        formPanel.add(nameButton);
        formPanel.add(dobButton);
        formPanel.add(emailButton);
        formPanel.add(phoneButton);
        formPanel.add(registerButton);

        registerPanel.add(formPanel);

        nameButton.addActionListener(e -> playerName = JOptionPane.showInputDialog(this, "Enter Name:"));
        dobButton.addActionListener(e -> playerDOB = JOptionPane.showInputDialog(this, "Enter DOB (yyyy-mm-dd):"));
        emailButton.addActionListener(e -> playerEmail = JOptionPane.showInputDialog(this, "Enter Email:"));
        phoneButton.addActionListener(e -> playerPhone = JOptionPane.showInputDialog(this, "Enter Phone Number:"));

        registerButton.addActionListener(e -> {
            if (playerName.isEmpty() || playerDOB.isEmpty() || playerEmail.isEmpty() || playerPhone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields before registering.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String id = UUID.randomUUID().toString();
                LocalDate dob = LocalDate.parse(playerDOB);
                int age = LocalDate.now().getYear() - dob.getYear();

                String sql = "INSERT INTO players (id, name, dob, age, email, phone) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, id);
                ps.setString(2, playerName);
                ps.setDate(3, java.sql.Date.valueOf(dob));
                ps.setInt(4, age);
                ps.setString(5, playerEmail);
                ps.setString(6, playerPhone);
                ps.executeUpdate();

                showLoadingAnimation();

                playerName = "";
                playerDOB = "";
                playerEmail = "";
                playerPhone = "";

                if (getPlayerCount() >= 6) {
                    cardLayout.show(mainPanel, "Tournament");
                    loadPlayers();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error registering player");
            }
        });

        return registerPanel;
    }

    private void showLoadingAnimation() {
        JDialog loadingDialog = new JDialog(this, "Registering...", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        loadingDialog.add(BorderLayout.CENTER, progressBar);
        loadingDialog.setSize(300, 100);
        loadingDialog.setLocationRelativeTo(this);

        new Thread(() -> {
            try {
                loadingDialog.setVisible(true);
                Thread.sleep(1500);
                loadingDialog.dispose();
                JOptionPane.showMessageDialog(this, "Player Registered Successfully!");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private JPanel createTournamentPanel() {
        JPanel tournamentPanel = new JPanel(new BorderLayout());

        model = new DefaultTableModel(new String[] {
                "ID", "Name", "DOB", "Age", "Email", "Phone", "R1", "R2", "R3", "Total"
        }, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete Player");
        JButton round1Btn = new JButton("Round 1");
        JButton round2Btn = new JButton("Round 2");
        JButton round3Btn = new JButton("Round 3");
        JButton leaderboardBtn = new JButton("Leaderboard");
        JButton viewPlayersBtn = new JButton("View Players");

        buttonPanel.add(deleteButton);
        buttonPanel.add(round1Btn);
        buttonPanel.add(round2Btn);
        buttonPanel.add(round3Btn);
        buttonPanel.add(leaderboardBtn);
        buttonPanel.add(viewPlayersBtn);

        tournamentPanel.add(buttonPanel, BorderLayout.NORTH);
        tournamentPanel.add(scrollPane, BorderLayout.CENTER);

        deleteButton.addActionListener(e -> deletePlayer());
        round1Btn.addActionListener(e -> generateRound(1));
        round2Btn.addActionListener(e -> generateRound(2));
        round3Btn.addActionListener(e -> generateRound(3));
        leaderboardBtn.addActionListener(e -> showLeaderboard());
        viewPlayersBtn.addActionListener(e -> showRegisteredPlayers());

        return tournamentPanel;
    }

    private int getPlayerCount() {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM players");
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private void loadPlayers() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM players");
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getString("id"), rs.getString("name"), rs.getDate("dob"),
                        rs.getInt("age"), rs.getString("email"), rs.getString("phone"),
                        rs.getDouble("round1_score"), rs.getDouble("round2_score"),
                        rs.getDouble("round3_score"), rs.getDouble("total_score")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePlayer() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a player to delete.");
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM players WHERE id = ?");
            ps.setString(1, id);
            ps.executeUpdate();
            loadPlayers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateRound(int round) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            List<Player> players = new ArrayList<>();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM players");
            while (rs.next()) {
                players.add(new Player(
                        rs.getString("id"), rs.getString("name"), rs.getInt("age"),
                        rs.getDouble("round1_score"), rs.getDouble("round2_score"), rs.getDouble("round3_score")));
            }

            if (players.size() != 6) {
                JOptionPane.showMessageDialog(this, "Exactly 6 players required.");
                return;
            }

            currentPairs.clear();
            if (round == 1)
                players.sort(Comparator.comparingInt(p -> p.age));
            else
                players.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));

            for (int i = 0; i < players.size(); i += 2) {
                currentPairs.add(new String[] { players.get(i).id, players.get(i + 1).id });
            }

            JPanel scorePanel = new JPanel(new GridLayout(currentPairs.size(), 4, 10, 10));
            List<JTextField[]> fieldsList = new ArrayList<>();

            for (String[] pair : currentPairs) {
                Player p1 = players.stream().filter(p -> p.id.equals(pair[0])).findFirst().get();
                Player p2 = players.stream().filter(p -> p.id.equals(pair[1])).findFirst().get();

                JTextField p1Score = new JTextField();
                JTextField p2Score = new JTextField();
                scorePanel.add(new JLabel(p1.name + " vs " + p2.name));
                scorePanel.add(p1Score);
                scorePanel.add(p2Score);

                fieldsList.add(new JTextField[] { p1Score, p2Score });
            }

            int result = JOptionPane.showConfirmDialog(this, scorePanel, "Enter scores Round " + round,
                    JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                for (int i = 0; i < currentPairs.size(); i++) {
                    String[] pair = currentPairs.get(i);
                    JTextField[] fields = fieldsList.get(i);

                    double score1 = Double.parseDouble(fields[0].getText());
                    double score2 = Double.parseDouble(fields[1].getText());

                    PreparedStatement ps1 = conn
                            .prepareStatement("UPDATE players SET round" + round + "_score = ? WHERE id = ?");
                    ps1.setDouble(1, score1);
                    ps1.setString(2, pair[0]);
                    ps1.executeUpdate();

                    PreparedStatement ps2 = conn
                            .prepareStatement("UPDATE players SET round" + round + "_score = ? WHERE id = ?");
                    ps2.setDouble(1, score2);
                    ps2.setString(2, pair[1]);
                    ps2.executeUpdate();
                }
                conn.createStatement()
                        .executeUpdate("UPDATE players SET total_score = round1_score + round2_score + round3_score");
                loadPlayers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLeaderboard() {
        List<String[]> leaderboard = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            leaderboard.add(new String[] {
                    model.getValueAt(i, 1).toString(), model.getValueAt(i, 9).toString()
            });
        }
        leaderboard.sort((a, b) -> Double.compare(Double.parseDouble(b[1]), Double.parseDouble(a[1])));
        StringBuilder result = new StringBuilder("Leaderboard:\n");
        for (String[] entry : leaderboard) {
            result.append(entry[0]).append(" - ").append(entry[1]).append(" points\n");
        }
        JOptionPane.showMessageDialog(this, result.toString());
    }

    private void showRegisteredPlayers() {
        StringBuilder result = new StringBuilder("Registered Players:\n");
        for (int i = 0; i < model.getRowCount(); i++) {
            result.append(model.getValueAt(i, 1).toString()).append("\n");
        }
        JOptionPane.showMessageDialog(this, result.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FreestyleChessiApp().setVisible(true));
    }
}

class Player {
    String id;
    String name;
    int age;
    double round1Score;
    double round2Score;
    double round3Score;

    public Player(String id, String name, int age, double round1Score, double round2Score, double round3Score) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.round1Score = round1Score;
        this.round2Score = round2Score;
        this.round3Score = round3Score;
    }

    public double getTotalScore() {
        return round1Score + round2Score + round3Score;
    }
}