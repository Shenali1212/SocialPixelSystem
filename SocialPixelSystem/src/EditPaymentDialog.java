import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Dialog for editing an existing payment record
public class EditPaymentDialog extends JDialog {
    private JComboBox<String> clientCombo;        // Dropdown for client names
    private JTextField amountField;               // Text field for payment amount
    private JFormattedTextField dateField;        // Date field formatted as yyyy-MM-dd
    private JComboBox<String> methodCombo;        // Dropdown for payment method
    private JComboBox<String> statusCombo;        // Dropdown for payment status
    private int paymentId;                        // Payment ID to update
    private AdminDashboard adminDashboard;        // Reference to AdminDashboard for updating stats

    // Constructor: Sets up UI and loads existing values from the table
    public EditPaymentDialog(AdminDashboard parent, DefaultTableModel tableModel, int rowIndex) {
        super(parent, "Edit Payment", true); // Modal dialog
        this.adminDashboard = parent;

        // Dialog setup
        setSize(400, 320);
        setLocationRelativeTo(parent); // Center the dialog
        setResizable(false);

        // Layout setup
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;

        // === Load data from the selected row ===
        paymentId = Integer.parseInt(tableModel.getValueAt(rowIndex, 0).toString());
        String clientName = tableModel.getValueAt(rowIndex, 1).toString();
        String amount = tableModel.getValueAt(rowIndex, 2).toString();
        String date = tableModel.getValueAt(rowIndex, 3).toString();
        String method = tableModel.getValueAt(rowIndex, 4).toString();
        String status = tableModel.getValueAt(rowIndex, 5).toString();

        // === Client Dropdown ===
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Client:"), gbc);
        gbc.gridx = 1;
        clientCombo = new JComboBox<>();
        loadClients(clientName); // Load client list and select current client
        panel.add(clientCombo, gbc);

        // === Amount Field ===
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField(amount, 15);
        panel.add(amountField, gbc);

        // === Date Field ===
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        try {
            // Set existing date value
            dateField.setValue(new SimpleDateFormat("yyyy-MM-dd").parse(date));
        } catch (ParseException e) {
            dateField.setValue(new Date()); // Fallback to today
        }
        dateField.setColumns(15);
        panel.add(dateField, gbc);

        // === Method Dropdown ===
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Method:"), gbc);
        gbc.gridx = 1;
        methodCombo = new JComboBox<>(new String[]{"Cash", "Card", "Bank Transfer"});
        methodCombo.setSelectedItem(method);
        panel.add(methodCombo, gbc);

        // === Status Dropdown ===
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"Paid", "Pending"});
        statusCombo.setSelectedItem(status);
        panel.add(statusCombo, gbc);

        // === Buttons ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        setContentPane(panel); // Add panel to the dialog

        // === Save Button Action ===
        saveBtn.addActionListener(e -> {
            // Retrieve form values
            String newClientName = (String) clientCombo.getSelectedItem();
            String newAmount = amountField.getText();
            Date newDate = (Date) dateField.getValue();
            String newMethod = (String) methodCombo.getSelectedItem();
            String newStatus = (String) statusCombo.getSelectedItem();

            // Validate fields
            if (newClientName == null || newAmount.isEmpty() || newDate == null || newMethod.isEmpty() || newStatus.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields correctly.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                // Get client_id from client name
                PreparedStatement clientStmt = conn.prepareStatement("SELECT client_id FROM clients WHERE name = ?");
                clientStmt.setString(1, newClientName);
                ResultSet clientRs = clientStmt.executeQuery();
                int clientId = -1;
                if (clientRs.next()) {
                    clientId = clientRs.getInt("client_id");
                }

                if (clientId == -1) {
                    JOptionPane.showMessageDialog(this, "Client not found.");
                    return;
                }

                // Prepare SQL update statement
                String sql = "UPDATE payments SET client_id=?, amount=?, payment_date=?, method=?, status=? WHERE payment_id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, clientId);
                stmt.setDouble(2, Double.parseDouble(newAmount));
                stmt.setDate(3, new java.sql.Date(newDate.getTime()));
                stmt.setString(4, newMethod);
                stmt.setString(5, newStatus);
                stmt.setInt(6, paymentId);

                stmt.executeUpdate(); // Execute update query

                // Update table row data immediately
                tableModel.setValueAt(newClientName, rowIndex, 1);
                tableModel.setValueAt(Double.parseDouble(newAmount), rowIndex, 2);
                tableModel.setValueAt(new SimpleDateFormat("yyyy-MM-dd").format(newDate), rowIndex, 3);
                tableModel.setValueAt(newMethod, rowIndex, 4);
                tableModel.setValueAt(newStatus, rowIndex, 5);

                adminDashboard.updateStats(); // Refresh stats on dashboard

                JOptionPane.showMessageDialog(this, "Payment updated successfully!");
                dispose(); // Close the dialog
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // === Cancel Button Action ===
        cancelBtn.addActionListener(e -> dispose()); // Close dialog without saving
    }

    // Load all client names into the dropdown and select the current one
    private void loadClients(String selectedClient) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT name FROM clients";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                clientCombo.addItem(name); // Add each name to combo box
                if (name.equals(selectedClient)) {
                    clientCombo.setSelectedItem(name); // Select current client
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
