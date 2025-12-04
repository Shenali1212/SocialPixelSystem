import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// Dialog window for adding a new campaign
public class AddCampaignDialog extends JDialog {
    // UI components for the form
    private JComboBox<String> clientCombo;
    private JTextField nameField;
    private JFormattedTextField startDateField;
    private JFormattedTextField endDateField;
    private JComboBox<String> statusCombo;
    // Reference to the main admin dashboard to update stats
    private AdminDashboard adminDashboard;
    // Table model to add the new campaign to
    private DefaultTableModel tableModel;

    // Constructor: sets up the dialog window
    public AddCampaignDialog(AdminDashboard parent, DefaultTableModel tableModel) {
        super(parent, "Add Campaign", true); // Create dialog with parent, title, and modality
        this.adminDashboard = parent; // Store reference to admin dashboard
        this.tableModel = tableModel; // Store reference to table model
        setSize(420, 400); // Set dialog size
        setLocationRelativeTo(parent); // Center dialog relative to parent
        setResizable(false); // Disable resizing
        JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout for flexible form layout
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18)); // Add padding
        GridBagConstraints gbc = new GridBagConstraints(); // Constraints for layout
        gbc.insets = new Insets(8, 8, 8, 8); // Spacing between components
        gbc.anchor = GridBagConstraints.EAST; // Align components to the right

        // Form fields
        // Campaign Name
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; nameField = new JTextField(15); panel.add(nameField, gbc);

        // Client Dropdown
        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Client:"), gbc);
        gbc.gridx = 1; clientCombo = new JComboBox<>(); loadClients(); panel.add(clientCombo, gbc);

        // Start Date
        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1; startDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        startDateField.setColumns(15); startDateField.setValue(new Date()); panel.add(startDateField, gbc);

        // End Date
        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 1; endDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        endDateField.setColumns(15); endDateField.setValue(new Date()); panel.add(endDateField, gbc);

        // Status Dropdown
        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; statusCombo = new JComboBox<>(new String[]{"Planning", "Active", "Completed", "Cancelled"});
        panel.add(statusCombo, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        setContentPane(panel); // Set the panel as the content of the dialog

        // Action listener for the Save button
        saveBtn.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                // Get client ID from the selected client name
                String clientName = (String) clientCombo.getSelectedItem();
                PreparedStatement clientStmt = conn.prepareStatement("SELECT client_id FROM clients WHERE name = ?");
                clientStmt.setString(1, clientName);
                ResultSet clientRs = clientStmt.executeQuery();
                int clientId = clientRs.next() ? clientRs.getInt("client_id") : -1;

                // SQL statement to insert a new campaign
                String sql = "INSERT INTO campaigns (name, client_id, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, nameField.getText());
                stmt.setInt(2, clientId);
                stmt.setDate(3, new java.sql.Date(((Date) startDateField.getValue()).getTime()));
                stmt.setDate(4, new java.sql.Date(((Date) endDateField.getValue()).getTime()));
                stmt.setString(5, (String) statusCombo.getSelectedItem());
                stmt.executeUpdate();

                // Get the newly generated campaign ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    // Retrieve the full campaign details to add to the table model
                    String fetchSql = "SELECT ca.campaign_id, ca.name, c.name as client_name, ca.start_date, ca.end_date, ca.status FROM campaigns ca JOIN clients c ON ca.client_id = c.client_id WHERE ca.campaign_id = ?";
                    PreparedStatement fetchStmt = conn.prepareStatement(fetchSql);
                    fetchStmt.setInt(1, newId);
                    ResultSet fetchRs = fetchStmt.executeQuery();
                    if (fetchRs.next()) {
                        // Add the new campaign as a row in the admin dashboard's table
                        tableModel.addRow(new Object[]{
                            fetchRs.getInt("campaign_id"),
                            fetchRs.getString("name"),
                            fetchRs.getString("client_name"),
                            fetchRs.getDate("start_date"),
                            fetchRs.getDate("end_date"),
                            fetchRs.getString("status"),
                            "Edit",
                            "Delete"
                        });
                    }
                }
                adminDashboard.updateStats(); // Update the statistic cards on the admin dashboard
                // Refresh campaigns table if possible (not the best way, but functional)
                if (adminDashboard != null) {
                    for (Component comp : adminDashboard.getContentPane().getComponents()) {
                        if (comp instanceof JPanel && comp.getClass().getSimpleName().equals("CampaignsPage")) {
                            ((CampaignsPage) comp).loadCampaignData();
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Campaign added successfully!");
                dispose(); // Close the dialog
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        // Action listener for the Cancel button
        cancelBtn.addActionListener(e -> dispose()); // Close the dialog without saving
    }

    // Load all client names from the database into the client dropdown
    private void loadClients() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM clients");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                clientCombo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print error to console if loading fails
        }
    }
} 