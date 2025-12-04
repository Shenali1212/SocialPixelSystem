import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Dialog for editing an existing campaign
public class EditCampaignDialog extends JDialog {
    private JComboBox<String> clientCombo;           // Dropdown for selecting client name
    private JTextField nameField;                    // Field for campaign name
    private JFormattedTextField startDateField;      // Field for campaign start date
    private JFormattedTextField endDateField;        // Field for campaign end date
    private JComboBox<String> statusCombo;           // Dropdown for campaign status
    private int campaignId;                          // ID of the campaign being edited
    private AdminDashboard adminDashboard;           // Reference to the main dashboard

    // Constructor - initializes the form with values from selected row in table
    public EditCampaignDialog(AdminDashboard parent, DefaultTableModel tableModel, int rowIndex) {
        super(parent, "Edit Campaign", true);
        this.adminDashboard = parent;
        this.campaignId = (int) tableModel.getValueAt(rowIndex, 0); // Get campaign ID from table

        // Dialog properties
        setSize(420, 400);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Main panel using GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;

        // ======= Form Fields =======

        // Campaign name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(tableModel.getValueAt(rowIndex, 1).toString(), 15);
        panel.add(nameField, gbc);

        // Client dropdown
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Client:"), gbc);
        gbc.gridx = 1;
        clientCombo = new JComboBox<>();
        loadClients(tableModel.getValueAt(rowIndex, 2).toString()); // Load and select current client
        panel.add(clientCombo, gbc);

        // Start date
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        startDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        startDateField.setColumns(15);
        startDateField.setValue(tableModel.getValueAt(rowIndex, 3)); // Set existing date
        panel.add(startDateField, gbc);

        // End date
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 1;
        endDateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        endDateField.setColumns(15);
        endDateField.setValue(tableModel.getValueAt(rowIndex, 4)); // Set existing date
        panel.add(endDateField, gbc);

        // Status dropdown
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"Planning", "Active", "Completed", "Cancelled"});
        statusCombo.setSelectedItem(tableModel.getValueAt(rowIndex, 5).toString());
        panel.add(statusCombo, gbc);

        // ======= Buttons =======

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        setContentPane(panel);

        // ======= Save Button Action =======
        saveBtn.addActionListener(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                // Get selected client's ID from DB
                String clientName = (String) clientCombo.getSelectedItem();
                PreparedStatement clientStmt = conn.prepareStatement("SELECT client_id FROM clients WHERE name = ?");
                clientStmt.setString(1, clientName);
                ResultSet clientRs = clientStmt.executeQuery();
                int clientId = clientRs.next() ? clientRs.getInt("client_id") : -1;

                // Prepare update SQL
                String sql = "UPDATE campaigns SET name=?, client_id=?, start_date=?, end_date=?, status=? WHERE campaign_id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setString(1, nameField.getText());
                stmt.setInt(2, clientId);
                stmt.setDate(3, new java.sql.Date(((Date) startDateField.getValue()).getTime()));
                stmt.setDate(4, new java.sql.Date(((Date) endDateField.getValue()).getTime()));
                stmt.setString(5, (String) statusCombo.getSelectedItem());
                stmt.setInt(6, campaignId);

                // Execute update
                stmt.executeUpdate();

                // Refresh dashboard stats and notify success
                adminDashboard.updateStats();
                JOptionPane.showMessageDialog(this, "Campaign updated successfully!");
                dispose(); // Close dialog
            } catch (SQLException ex) {
                // Show error if any DB issue occurs
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // ======= Cancel Button Action =======
        cancelBtn.addActionListener(e -> dispose()); // Just close the dialog
    }

    // Load client names from DB into combo box and select the current one
    private void loadClients(String selectedClient) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM clients");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                clientCombo.addItem(rs.getString("name"));
            }
            clientCombo.setSelectedItem(selectedClient); // Set the selected client
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
