import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Main login window for the SocialPixel Client Portal
public class LoginWindow extends JFrame {
    private JTextField usernameField;      // Field to enter username
    private JPasswordField passwordField;  // Field to enter password (masked)

    // Default constructor
    public LoginWindow() {
        initComponents(); // Initialize the GUI
    }

    // Constructor with predefined username and password (e.g., after registration)
    public LoginWindow(String username, String password) {
        initComponents();
        usernameField.setText(username);             // Set pre-filled username
        passwordField.setText(password);             // Set pre-filled password
    }

    // Method to create and arrange UI components
    private void initComponents() {
        setTitle("SocialPixel - Client Portal Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setLayout(new BorderLayout());

        // ===== Logo Panel =====
        JPanel logoPanel = new JPanel();
        JLabel logoLabel = new JLabel("SocialPixel", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 28));
        logoLabel.setForeground(new Color(0, 102, 204)); // Blue color
        logoPanel.add(logoLabel);
        add(logoPanel, BorderLayout.NORTH); // Add to top

        // ===== Form Panel =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username Label & Field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        // Password Label & Field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        add(formPanel, BorderLayout.CENTER); // Add to center

        // ===== Button Panel =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Button styling
        loginButton.setBackground(new Color(0, 102, 204));
        loginButton.setForeground(Color.WHITE);
        registerButton.setBackground(new Color(0, 102, 204));
        registerButton.setForeground(Color.WHITE);

        // ===== Login Button Action =====
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Admin shortcut login
            if ("admin".equals(username) && "123".equals(password)) {
                dispose(); // Close login window
                new AdminDashboard().setVisible(true); // Open admin dashboard
                return;
            }

            // Authenticate user (client)
            if (authenticate(username, password)) {
                dispose(); // Close login window
                int clientId = getClientId(username); // Get ID of logged-in client
                new ClientDashboard(clientId).setVisible(true); // Open client dashboard
            } else {
                // Show error if login fails
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ===== Register Button Action =====
        registerButton.addActionListener(e -> {
            dispose(); // Close login window
            new RegistrationWindow().setVisible(true); // Open registration window
        });

        // Add buttons to panel
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH); // Add to bottom
    }

    // Method to authenticate user against the database
    private boolean authenticate(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM clients WHERE username = ? AND password = ?")) {

            stmt.setString(1, username);
            stmt.setString(2, password); // In production, use hashed passwords!

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0; // Returns true if any record is found

        } catch (SQLException e) {
            // Show SQL error
            JOptionPane.showMessageDialog(this, "Error authenticating: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Method to fetch the client_id for a given username
    private int getClientId(String username) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT client_id FROM clients WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("client_id"); // Return client ID
            }

        } catch (SQLException e) {
            // Show SQL error
            JOptionPane.showMessageDialog(this, "Error fetching client ID: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return -1; // Return -1 if not found or error occurred
    }
}
