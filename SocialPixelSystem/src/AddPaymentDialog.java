import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Dialog window for adding a new payment
public class AddPaymentDialog extends JDialog {
    // UI components for the form
    private JComboBox<String> clientCombo;
    private JTextField amountField;
    private JFormattedTextField dateField;
    private JComboBox<String> methodCombo;
    private JComboBox<String> statusCombo;
    // Reference to the main admin dashboard to update stats
    private AdminDashboard adminDashboard;

    // Constructor: sets up the dialog window
    public AddPaymentDialog(AdminDashboard parent, DefaultTableModel tableModel) {
        super(parent, "Add Payment", true); // Create dialog with parent, title, and modality
        this.adminDashboard = parent; // Store reference to admin dashboard
        setSize(400, 320); // Set dialog size
        setLocationRelativeTo(parent); // Center dialog relative to parent
        setResizable(false); // Disable resizing
        JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout for flexible form layout
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18)); // Add padding
        GridBagConstraints gbc = new GridBagConstraints(); // Constraints for layout
        gbc.insets = new Insets(8, 8, 8, 8); // Spacing between components
        gbc.anchor = GridBagConstraints.EAST; // Align components to the right

        // Client dropdown
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Client:"), gbc);
        gbc.gridx = 1;
        clientCombo = new JComboBox<>();
        loadClients(); // Populate the dropdown with client names
        panel.add(clientCombo, gbc);

        // Amount
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField(15);
        panel.add(amountField, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        dateField.setColumns(15);
        dateField.setValue(new Date()); // Set current date as default
        panel.add(dateField, gbc);

        // Method Dropdown
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Method:"), gbc);
        gbc.gridx = 1;
        methodCombo = new JComboBox<>(new String[]{"Cash", "Card", "Bank Transfer"});
        panel.add(methodCombo, gbc);

        // Status Dropdown
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"Paid", "Pending"});
        panel.add(statusCombo, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        setContentPane(panel); // Set the panel as the content of the dialog

        // Action listener for the Save button
        saveBtn.addActionListener(e -> {
            // Get values from all form fields
            String clientName = (String) clientCombo.getSelectedItem();
            String amountStr = amountField.getText();
            Date dateValue = (Date) dateField.getValue();
            String method = (String) methodCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();

            // Validate that all fields are filled
            if (clientName == null || amountStr.isEmpty() || dateValue == null || method.isEmpty() || status.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields correctly.");
                return; // Stop if validation fails
            }

            // Database operation
            try (Connection conn = DBConnection.getConnection()) {
                // Get client_id from the selected client name
                PreparedStatement clientStmt = conn.prepareStatement("SELECT client_id FROM clients WHERE name = ?");
                clientStmt.setString(1, clientName);
                ResultSet clientRs = clientStmt.executeQuery();
                int clientId = -1;
                if (clientRs.next()) {
                    clientId = clientRs.getInt("client_id");
                }
                if (clientId == -1) {
                    JOptionPane.showMessageDialog(this, "Client not found.");
                    return; // Stop if client is not found
                }
                // SQL statement to insert a new payment
                String sql = "INSERT INTO payments (client_id, amount, payment_date, method, status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, clientId);
                stmt.setDouble(2, Double.parseDouble(amountStr));
                stmt.setDate(3, new java.sql.Date(dateValue.getTime()));
                stmt.setString(4, method);
                stmt.setString(5, status);
                stmt.executeUpdate();

                // Get the newly generated payment ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    // Add the new payment as a row in the admin dashboard's payments table
                    tableModel.addRow(new Object[]{newId, clientName, Double.parseDouble(amountStr), new SimpleDateFormat("yyyy-MM-dd").format(dateValue), method, status, "Edit", "Delete"});
                    adminDashboard.updateStats(); // Update the statistic cards
                    JOptionPane.showMessageDialog(this, "Payment added successfully!");
                    dispose(); // Close the dialog
                }
            } catch (SQLException ex) {
                // Show error message if database operation fails
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // Action listener for the Cancel button
        cancelBtn.addActionListener(e -> dispose()); // Close the dialog without saving
    }

    // Load all client names from the database into the client dropdown
    private void loadClients() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT name FROM clients";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                clientCombo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print error to console if loading fails
        }
    }
} 