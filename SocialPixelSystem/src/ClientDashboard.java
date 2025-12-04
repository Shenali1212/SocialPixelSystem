import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;

// Main class for the client dashboard window
public class ClientDashboard extends javax.swing.JFrame {
    // Declare UI components and variables
    private JProgressBar campaignProgress;
    private JTextArea notificationsArea;
    private JTextArea changeRequestArea;
    private JTextArea feedbackArea;
    private JPanel clientDetailsPanel;
    private JTextField nameField, emailField, phoneField, industryField;
    private JButton saveDetailsButton;
    private int clientId;
    private JButton logout;
    private String clientName = "";

    // Constructor: initializes dashboard for a specific client
    public ClientDashboard(int clientId) {
        this.clientId = clientId; // Store client ID
        fetchClientName(); // Get client name from DB
        initComponents(); // Set up window basics
        initializeCustomComponents(); // Build custom UI
    }

    // Set up window properties
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE); // Close app on exit
        setTitle("SocialPixel - Client Dashboard"); // Window title
        setSize(800, 500); // Window size
        setLocationRelativeTo(null); // Center window
        getContentPane().setLayout(new BorderLayout()); // Use BorderLayout
    }

    // Build the main UI components
    private void initializeCustomComponents() {
        // Sidebar panel for logout button
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(120, 0));
        leftPanel.setBackground(new Color(240, 240, 240));
        logout = new JButton("Logout");
        logout.setBackground(new Color(0, 102, 204));
        logout.setForeground(Color.WHITE);
        logout.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        logout.setFont(new Font("Arial", Font.BOLD, 14));
        leftPanel.add(logout, BorderLayout.SOUTH);
        getContentPane().add(leftPanel, BorderLayout.WEST);

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tabbed pane for different sections
        JTabbedPane tabbedPane = new JTabbedPane();
        // --- Client Details Tab ---
        clientDetailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; clientDetailsPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; nameField = new JTextField(20); clientDetailsPanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; clientDetailsPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; emailField = new JTextField(20); clientDetailsPanel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; clientDetailsPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; phoneField = new JTextField(20); clientDetailsPanel.add(phoneField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; clientDetailsPanel.add(new JLabel("Industry:"), gbc);
        gbc.gridx = 1; industryField = new JTextField(20); clientDetailsPanel.add(industryField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        saveDetailsButton = new JButton("Save Details");
        clientDetailsPanel.add(saveDetailsButton, gbc);
        // Show latest campaign status only (no admin remarks)
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;
        JLabel campaignStatusLabel = new JLabel("Campaign Status: ");
        clientDetailsPanel.add(campaignStatusLabel, gbc);
        tabbedPane.addTab("Client Details", clientDetailsPanel);
        saveDetailsButton.addActionListener(e -> saveClientDetails());
        loadClientDetails();
        loadLatestCampaignStatus(campaignStatusLabel);

        // --- Feedback Tab ---
        JPanel feedbackPanel = new JPanel(new BorderLayout());
        JTable feedbackTable = new JTable(new DefaultTableModel(new Object[]{"Message", "Admin Reply", "Submitted At"}, 0));
        feedbackPanel.add(new JScrollPane(feedbackTable), BorderLayout.CENTER);
        JPanel feedbackInputPanel = new JPanel(new BorderLayout());
        feedbackInputPanel.add(new JLabel("Enter your feedback:"), BorderLayout.NORTH);
        feedbackArea = new JTextArea(5, 30);
        feedbackInputPanel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
        JButton submitFeedback = new JButton("Submit Feedback");
        feedbackInputPanel.add(submitFeedback, BorderLayout.SOUTH);
        feedbackPanel.add(feedbackInputPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Feedback", feedbackPanel);
        loadClientFeedback((DefaultTableModel) feedbackTable.getModel());
        submitFeedback.addActionListener(e -> submitFeedbackRequest((DefaultTableModel) feedbackTable.getModel()));

        // --- Requirements Tab ---
        JPanel requirementsPanel = new JPanel(new BorderLayout());
        JTextArea requirementArea = new JTextArea(5, 30);
        JButton submitRequirement = new JButton("Submit Requirement");
        JTable requirementsTable = new JTable(new DefaultTableModel(new Object[]{"Requirement", "Admin Response", "Submitted At"}, 0));
        requirementsPanel.add(new JScrollPane(requirementsTable), BorderLayout.CENTER);
        JPanel reqInputPanel = new JPanel(new BorderLayout());
        reqInputPanel.add(new JLabel("Enter your requirement:"), BorderLayout.NORTH);
        reqInputPanel.add(new JScrollPane(requirementArea), BorderLayout.CENTER);
        reqInputPanel.add(submitRequirement, BorderLayout.SOUTH);
        requirementsPanel.add(reqInputPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Requirements", requirementsPanel);
        loadClientRequirements((DefaultTableModel) requirementsTable.getModel());
        submitRequirement.addActionListener(e -> submitRequirement(requirementArea, (DefaultTableModel) requirementsTable.getModel()));

        // --- Payments Tab ---
        JPanel paymentsPanel = new JPanel(new BorderLayout());
        JTable paymentsTable = new JTable(new DefaultTableModel(new Object[]{"Amount", "Date", "Method", "Status"}, 0));
        paymentsPanel.add(new JScrollPane(paymentsTable), BorderLayout.CENTER);
        tabbedPane.addTab("Payments", paymentsPanel);
        loadClientPayments((DefaultTableModel) paymentsTable.getModel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Logout button action: return to login window
        logout.addActionListener(e -> {
            dispose();
            new LoginWindow().setVisible(true);
        });
    }

    // Load notifications (not used in current UI)
    private void loadNotifications() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT title, status FROM campaigns WHERE client_id = ?")) {
            stmt.setInt(1, 1);
            ResultSet rs = stmt.executeQuery();
            StringBuilder notifications = new StringBuilder("Notifications:\n");
            while (rs.next()) {
                notifications.append("- ").append(rs.getString("title")).append(": ").append(rs.getString("status")).append("\n");
            }
            notificationsArea.setText(notifications.toString());
        } catch (SQLException e) {
            notificationsArea.setText("Error loading notifications: " + e.getMessage());
        }
    }

    // Load client details from the database
    private void loadClientDetails() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name, email, phone, industry FROM clients WHERE client_id = ?")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
                phoneField.setText(rs.getString("phone"));
                industryField.setText(rs.getString("industry"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading client details: " + e.getMessage());
        }
    }

    // Save client details to the database
    private void saveClientDetails() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE clients SET name=?, email=?, phone=?, industry=? WHERE client_id=?")) {
            stmt.setString(1, nameField.getText().trim());
            stmt.setString(2, emailField.getText().trim());
            stmt.setString(3, phoneField.getText().trim());
            stmt.setString(4, industryField.getText().trim());
            stmt.setInt(5, clientId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Details updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating details: " + e.getMessage());
        }
    }

    // Fetch the client's name from the database
    private void fetchClientName() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM clients WHERE client_id = ?")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                clientName = rs.getString("name");
            }
        } catch (SQLException e) {
            clientName = "";
        }
    }

    // Submit feedback to the database and reload the table
    private void submitFeedbackRequest(DefaultTableModel model) {
        String message = feedbackArea.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter feedback.");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO feedback (client_id, client_name, message, submitted_at) VALUES (?, ?, ?, NOW())")) {
            stmt.setInt(1, clientId);
            stmt.setString(2, clientName);
            stmt.setString(3, message);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Feedback submitted!");
            feedbackArea.setText("");
            loadClientFeedback(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error submitting feedback: " + e.getMessage());
        }
    }

    // Load feedback for this client from the database
    private void loadClientFeedback(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT message, reply, submitted_at FROM feedback WHERE client_id = ? ORDER BY submitted_at DESC")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("message"), rs.getString("reply"), rs.getTimestamp("submitted_at")});
            }
        } catch (SQLException e) {
            // Optionally show error
        }
    }

    // Load requirements for this client from the database
    private void loadClientRequirements(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT requirement, admin_response, submitted_at FROM requirements WHERE client_id = ? ORDER BY submitted_at DESC")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("requirement"), rs.getString("admin_response"), rs.getTimestamp("submitted_at")});
            }
        } catch (SQLException e) {
            // Optionally show error
        }
    }

    // Submit a new requirement to the database and reload the table
    private void submitRequirement(JTextArea area, DefaultTableModel model) {
        String req = area.getText().trim();
        if (req.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a requirement.");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO requirements (client_id, requirement, submitted_at) VALUES (?, ?, NOW())")) {
            stmt.setInt(1, clientId);
            stmt.setString(2, req);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Requirement submitted!");
            area.setText("");
            loadClientRequirements(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error submitting requirement: " + e.getMessage());
        }
    }

    // Load all campaigns for this client and show in the campaigns table
    private void loadClientCampaigns(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name, status FROM campaigns WHERE client_id = ?")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            // Optionally show error
        }
    }

    // Load all payments for this client and show in the payments table
    private void loadClientPayments(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT amount, payment_date, method, status FROM payments WHERE client_id = ?")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getDouble("amount"),
                    rs.getDate("payment_date"),
                    rs.getString("method"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            // Optionally show error
        }
    }

    // Load the latest campaign's status for this client (no admin remarks)
    private void loadLatestCampaignStatus(JLabel statusLabel) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT status FROM campaigns WHERE client_id = ? ORDER BY end_date DESC LIMIT 1")) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                statusLabel.setText("Campaign Status: " + rs.getString("status"));
            } else {
                statusLabel.setText("Campaign Status: No campaigns found.");
            }
        } catch (SQLException e) {
            statusLabel.setText("Campaign Status: No campaigns found.");
        }
    }

    // Main method for testing: launches dashboard for client ID 1
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new ClientDashboard(1).setVisible(true));
    }
} 