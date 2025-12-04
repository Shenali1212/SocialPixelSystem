import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// This class handles user registration for SocialPixel
public class RegistrationWindow extends JFrame {
    // Declare all form fields
    private JTextField nicField, nameField, emailField, phoneField, usernameField, industryField, requirementField;
    private JPasswordField passwordField;

    // Constructor
    public RegistrationWindow() {
        initComponents(); // Initialize all UI components
    }

    // Setup the registration form UI
    private void initComponents() {
        setTitle("SocialPixel - Client Registration");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close this window only
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === Header / Logo ===
        JLabel logoLabel = new JLabel("SocialPixel Registration", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 28));
        logoLabel.setForeground(new Color(0, 102, 204));
        add(logoLabel, BorderLayout.NORTH);

        // === Form Panel (Center) ===
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add form fields using GridBagLayout
        addField(formPanel, gbc, 0, "NIC:", nicField = new JTextField(15));
        addField(formPanel, gbc, 1, "Name:", nameField = new JTextField(15));
        addField(formPanel, gbc, 2, "Email:", emailField = new JTextField(15));
        addField(formPanel, gbc, 3, "Phone:", phoneField = new JTextField(15));
        addField(formPanel, gbc, 4, "Username:", usernameField = new JTextField(15));
        addField(formPanel, gbc, 5, "Password:", passwordField = new JPasswordField(15));
        addField(formPanel, gbc, 6, "Industry:", industryField = new JTextField(15));
        addField(formPanel, gbc, 7, "Requirement:", requirementField = new JTextField(15));

        add(formPanel, BorderLayout.CENTER);

        // === Buttons Panel (South) ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");

        // Button Styling
        registerButton.setBackground(new Color(0, 102, 204));
        registerButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(0, 102, 204));
        backButton.setForeground(Color.WHITE);

        // Add to button panel
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action listeners
        registerButton.addActionListener(e -> registerClient());
        backButton.addActionListener(e -> {
            dispose(); // Close registration window
            new LoginWindow().setVisible(true); // Go back to login
        });
    }

    // Helper method to add a label and field row
    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    // Handles the actual registration process
    private void registerClient() {
        // Collect field values
        String nic = nicField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String industry = industryField.getText().trim();
        String requirement = requirementField.getText().trim();

        // === Validations ===
        if (nic.isEmpty() || name.isEmpty() || username.isEmpty() || password.isEmpty() || industry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIC, Name, Username, Password, and Industry are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!email.isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (requirement.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Requirement is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // === Database Insertion ===
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if NIC already exists
            PreparedStatement checkNic = conn.prepareStatement("SELECT COUNT(*) FROM clients WHERE nic = ?");
            checkNic.setString(1, nic);
            ResultSet rsNic = checkNic.executeQuery();
            rsNic.next();
            if (rsNic.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "NIC already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if username is already taken
            PreparedStatement checkUsername = conn.prepareStatement("SELECT COUNT(*) FROM clients WHERE username = ?");
            checkUsername.setString(1, username);
            ResultSet rsUsername = checkUsername.executeQuery();
            rsUsername.next();
            if (rsUsername.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert new client into `clients` table
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO clients (nic, nic_encrypted, name, email, phone, contact_info, industry, username, password) VALUES (?, SHA2(?, 256), ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            stmt.setString(1, nic);
            stmt.setString(2, nic); // For SHA2 encryption
            stmt.setString(3, name);
            stmt.setString(4, email.isEmpty() ? null : email);
            stmt.setString(5, phone.isEmpty() ? null : phone);
            stmt.setString(6, phone.isEmpty() ? null : phone); // Storing phone as contact_info
            stmt.setString(7, industry);
            stmt.setString(8, username);
            stmt.setString(9, password); // NOTE: In production, hash passwords!

            stmt.executeUpdate();

            // Get auto-generated client_id
            int newClientId = -1;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newClientId = generatedKeys.getInt(1);
                }
            }

            // If registration was successful, insert the requirement
            if (newClientId != -1) {
                try (PreparedStatement reqStmt = conn.prepareStatement("INSERT INTO requirements (client_id, requirement, submitted_at) VALUES (?, ?, NOW())")) {
                    reqStmt.setInt(1, newClientId);
                    reqStmt.setString(2, requirement);
                    reqStmt.executeUpdate();
                }
            }

            // === Success Message ===
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Redirect to Login with filled credentials
            dispose();
            new LoginWindow(username, password).setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error registering client: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
