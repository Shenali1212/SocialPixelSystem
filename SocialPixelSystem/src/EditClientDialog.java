import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// Dialog for editing an existing client record
public class EditClientDialog extends JDialog {
    private JTextField nameField, contactField, nicField, industryField; // Input fields
    private int clientId; // Stores the ID of the client being edited

    // Constructor - initializes dialog with selected client's data
    public EditClientDialog(JFrame parent, DefaultTableModel tableModel, int rowIndex) {
        super(parent, "Edit Client", true); // Modal dialog
        setSize(400, 300);
        setLocationRelativeTo(parent); // Center on parent
        setLayout(new GridLayout(6, 2, 10, 10)); // 6 rows, 2 columns layout with spacing

        // ======= Retrieve existing data from table model =======
        clientId = (int) tableModel.getValueAt(rowIndex, 0); // Get client ID
        String name = tableModel.getValueAt(rowIndex, 1).toString();
        String contact = tableModel.getValueAt(rowIndex, 2).toString();
        String nic = tableModel.getValueAt(rowIndex, 3).toString();
        String industry = tableModel.getValueAt(rowIndex, 4).toString();

        // ======= Create input fields with existing values =======
        add(new JLabel("Full Name:"));
        nameField = new JTextField(name);
        add(nameField);

        add(new JLabel("Contact:"));
        contactField = new JTextField(contact);
        add(contactField);

        add(new JLabel("NIC:"));
        nicField = new JTextField(nic);
        add(nicField);

        add(new JLabel("Industry:"));
        industryField = new JTextField(industry);
        add(industryField);

        // ======= Save and Cancel buttons =======
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        add(saveBtn);
        add(cancelBtn);

        // ======= Save Button Action =======
        saveBtn.addActionListener(e -> {
            // Get values from fields
            String newName = nameField.getText();
            String newContact = contactField.getText();
            String newNic = nicField.getText();
            String newIndustry = industryField.getText();

            // Check for empty fields
            if (newName.isEmpty() || newContact.isEmpty() || newNic.isEmpty() || newIndustry.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            // ======= Update database =======
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE clients SET name=?, contact_info=?, nic=?, industry=? WHERE client_id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newName);
                stmt.setString(2, newContact);
                stmt.setString(3, newNic);
                stmt.setString(4, newIndustry);
                stmt.setInt(5, clientId);

                stmt.executeUpdate(); // Run update query

                // Update table model with new values (so UI refreshes immediately)
                tableModel.setValueAt(newName, rowIndex, 1);
                tableModel.setValueAt(newContact, rowIndex, 2);
                tableModel.setValueAt(newNic, rowIndex, 3);
                tableModel.setValueAt(newIndustry, rowIndex, 4);

                // Success message
                JOptionPane.showMessageDialog(this, "Client updated successfully!");
                dispose(); // Close dialog
            } catch (SQLException ex) {
                // Show error message on exception
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // ======= Cancel Button Action =======
        cancelBtn.addActionListener(e -> dispose()); // Close dialog without saving
    }
}
