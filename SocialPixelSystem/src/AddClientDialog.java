import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

// Dialog window for adding a new client
public class AddClientDialog extends JDialog {
    // UI components for the form fields
    private JTextField nameField, contactField, nicField, industryField;

    // Constructor: sets up the dialog window
    public AddClientDialog(JFrame parent, DefaultTableModel tableModel) {
        super(parent, "Add New Client", true); // Create dialog with parent, title, and modality
        setSize(420, 260); // Set dialog size
        setLocationRelativeTo(parent); // Center dialog relative to parent
        setResizable(false); // Disable resizing
        JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout for flexible form layout
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18)); // Add padding
        GridBagConstraints gbc = new GridBagConstraints(); // Constraints for layout
        gbc.insets = new Insets(8, 8, 8, 8); // Spacing between components
        gbc.anchor = GridBagConstraints.EAST; // Align components to the right

        // Labels and fields
        // Full Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(18);
        panel.add(nameField, gbc);

        // Contact
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Contact:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contactField = new JTextField(18);
        panel.add(contactField, gbc);

        // NIC
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("NIC:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nicField = new JTextField(18);
        panel.add(nicField, gbc);

        // Industry
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Industry:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        industryField = new JTextField(18);
        panel.add(industryField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(buttonPanel, gbc);

        setContentPane(panel); // Set the panel as the content of the dialog

        // Action listener for the Save button
        saveBtn.addActionListener(e -> {
            // Get text from all fields
            String name = nameField.getText();
            String contact = contactField.getText();
            String nic = nicField.getText();
            String industry = industryField.getText();

            // Validate that all fields are filled
            if (name.isEmpty() || contact.isEmpty() || nic.isEmpty() || industry.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return; // Stop if validation fails
            }

            // Database operation
            try (Connection conn = DBConnection.getConnection()) {
                // SQL statement to insert a new client
                String sql = "INSERT INTO clients (name, contact_info, nic, industry, username, password, email, phone) VALUES (?, ?, ?, ?, NULL, NULL, NULL, NULL)";
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, name);
                stmt.setString(2, contact);
                stmt.setString(3, nic);
                stmt.setString(4, industry);
                stmt.executeUpdate();

                // Get the newly generated client ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    // Add the new client as a row in the admin dashboard's table
                    tableModel.addRow(new Object[]{newId, name, contact, nic, industry, "Edit", "Delete"});
                    // If the parent is the AdminDashboard, reload its data and update stats
                    if (parent instanceof AdminDashboard) {
                        ((AdminDashboard) parent).loadClientData();
                        ((AdminDashboard) parent).updateStats();
                    }
                    JOptionPane.showMessageDialog(this, "Client added successfully!");
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
}
