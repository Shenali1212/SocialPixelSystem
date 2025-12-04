import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// This class represents the Payments tab in the AdminDashboard
public class PaymentsPage extends JPanel {
    private JTable paymentTable;              // Table to display payments
    private DefaultTableModel tableModel;     // Table model to manage payment data
    private AdminDashboard adminDashboard;    // Reference to the parent dashboard

    // Constructor
    public PaymentsPage(AdminDashboard adminDashboard) {
        this.adminDashboard = adminDashboard;

        setLayout(new BorderLayout()); // Main layout for this panel
        setBackground(Color.WHITE);    // Set background color

        // === Content Panel ===
        JPanel content = new JPanel(new GridBagLayout()); // Use GridBagLayout for flexible UI
        content.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints(); // Constraints for GridBag
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // === Heading Label ===
        JLabel heading = new JLabel("Payments");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        content.add(heading, gbc);

        // === Table Model Setup ===
        tableModel = new DefaultTableModel(new Object[]{
                "ID", "Client Name", "Amount", "Date", "Method", "Status", "Edit", "Delete"
        }, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 6 || col == 7; // Only "Edit" and "Delete" columns are editable (for button actions)
            }
        };

        // === Payment Table ===
        paymentTable = new JTable(tableModel);
        loadPaymentData(); // Load payment data from DB

        // Add button renderers and editors to the table
        paymentTable.getColumn("Edit").setCellRenderer(new PaymentButtonRenderer("Edit"));
        paymentTable.getColumn("Edit").setCellEditor(new PaymentButtonEditor(new JCheckBox(), "edit", this));

        paymentTable.getColumn("Delete").setCellRenderer(new PaymentButtonRenderer("Delete"));
        paymentTable.getColumn("Delete").setCellEditor(new PaymentButtonEditor(new JCheckBox(), "delete", this));

        // === Scroll Pane for Table ===
        JScrollPane scrollPane = new JScrollPane(paymentTable);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        content.add(scrollPane, gbc);

        // === Add Payment Button ===
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        JButton addBtn = new JButton("Add Payment");
        addBtn.setPreferredSize(new Dimension(150, 32));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setBackground(new Color(59, 89, 182)); // Blue color
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> new AddPaymentDialog(adminDashboard, tableModel).setVisible(true)); // Show AddPaymentDialog

        content.add(addBtn, gbc);

        // Add the content panel to the center of PaymentsPage
        add(content, BorderLayout.CENTER);
    }

    // === Load Payment Data from DB ===
    public void loadPaymentData() {
        tableModel.setRowCount(0); // Clear existing rows
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT p.payment_id, c.name, p.amount, p.payment_date, p.method, p.status
                FROM payments p
                JOIN clients c ON p.client_id = c.client_id
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Add each row to the table
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("payment_id"),
                        rs.getString("name"),
                        rs.getDouble("amount"),
                        rs.getDate("payment_date"),
                        rs.getString("method"),
                        rs.getString("status"),
                        "Edit",    // Button label
                        "Delete"   // Button label
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public AdminDashboard getAdminDashboard() {
        return adminDashboard;
    }
}

// === Renderer for Edit/Delete buttons ===
class PaymentButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    private String type;

    public PaymentButtonRenderer(String type) {
        this.type = type;
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setOpaque(true); // Ensure background is painted
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        setText(type);
        // Set background color based on button type
        setBackground(type.equals("Edit") ? new Color(77, 166, 255) : new Color(255, 102, 102));
        return this;
    }
}

// === Editor for Edit/Delete buttons ===
class PaymentButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private PaymentsPage paymentsPage;

    public PaymentButtonEditor(JCheckBox checkBox, String actionType, PaymentsPage paymentsPage) {
        super(checkBox);
        this.paymentsPage = paymentsPage;

        // Create the button with appropriate label
        button = new JButton(actionType.equals("edit") ? "Edit" : "Delete");
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBackground(actionType.equals("edit") ? new Color(77, 166, 255) : new Color(255, 102, 102));

        // Add ActionListener for button click
        button.addActionListener(e -> {
            JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, button);
            if (table != null) {
                int viewRow = table.getSelectedRow();
                if (viewRow < 0) return; // No row selected

                int modelRow = table.convertRowIndexToModel(viewRow);
                DefaultTableModel model = (DefaultTableModel) table.getModel();

                if (actionType.equals("edit")) {
                    // Open EditPaymentDialog
                    new EditPaymentDialog(paymentsPage.getAdminDashboard(), model, modelRow).setVisible(true);
                } else if (actionType.equals("delete")) {
                    // Delete payment confirmation
                    int paymentId = (int) model.getValueAt(modelRow, 0);
                    int confirm = JOptionPane.showConfirmDialog(button,
                            "Are you sure you want to delete payment ID " + paymentId + "?",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try (Connection conn = DBConnection.getConnection()) {
                            PreparedStatement stmt = conn.prepareStatement("DELETE FROM payments WHERE payment_id = ?");
                            stmt.setInt(1, paymentId);
                            stmt.executeUpdate();

                            model.removeRow(modelRow); // Remove from table
                            paymentsPage.getAdminDashboard().updateStats(); // Refresh dashboard stats
                            JOptionPane.showMessageDialog(button, "Payment deleted.");
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(button, "Error: " + ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        return button; // Return the custom button
    }

    @Override
    public Object getCellEditorValue() {
        return button.getText(); // Return button label
    }
}
