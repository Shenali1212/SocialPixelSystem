import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Main panel that displays the Campaigns Page UI
public class CampaignsPage extends JPanel {
    private JTable campaignTable;
    private DefaultTableModel tableModel;
    private AdminDashboard adminDashboard;

    // Constructor accepts AdminDashboard instance for later use
    public CampaignsPage(AdminDashboard adminDashboard) {
        this.adminDashboard = adminDashboard;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Panel to hold all UI components using GridBagLayout
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Heading label
        JLabel heading = new JLabel("Campaigns");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        content.add(heading, gbc);

        // Create table model with column names
        tableModel = new DefaultTableModel(new Object[]{
                "ID", "Name", "Client", "Start Date", "End Date", "Status", "Edit", "Delete"
        }, 0) {
            // Only Edit and Delete columns are editable
            public boolean isCellEditable(int row, int col) {
                return col >= 6;
            }
        };

        // JTable initialization
        campaignTable = new JTable(tableModel);
        loadCampaignData(); // Load data from DB

        // Set custom renderer and editor for Edit button
        campaignTable.getColumn("Edit").setCellRenderer(new CampaignButtonRenderer("Edit"));
        campaignTable.getColumn("Edit").setCellEditor(new CampaignButtonEditor(new JCheckBox(), "edit", this));

        // Set custom renderer and editor for Delete button
        campaignTable.getColumn("Delete").setCellRenderer(new CampaignButtonRenderer("Delete"));
        campaignTable.getColumn("Delete").setCellEditor(new CampaignButtonEditor(new JCheckBox(), "delete", this));

        // Add table inside scroll pane
        JScrollPane scrollPane = new JScrollPane(campaignTable);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        content.add(scrollPane, gbc);

        // Add Campaign button
        JButton addBtn = new JButton("Add Campaign");
        addBtn.setPreferredSize(new Dimension(160, 32));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setBackground(new Color(59, 89, 182));
        addBtn.setForeground(Color.WHITE);

        // Open AddCampaignDialog when clicked
        addBtn.addActionListener(e -> new AddCampaignDialog(adminDashboard, tableModel).setVisible(true));

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        content.add(addBtn, gbc);

        // Add everything to main panel
        add(content, BorderLayout.CENTER);
    }

    // Load campaign data from database
    public void loadCampaignData() {
        tableModel.setRowCount(0); // Clear table

        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT ca.campaign_id, ca.name, c.name as client_name, ca.start_date, ca.end_date, ca.status
                FROM campaigns ca
                JOIN clients c ON ca.client_id = c.client_id
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Add each row from the result set into the table
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("campaign_id"),
                        rs.getString("name"),
                        rs.getString("client_name"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getString("status"),
                        "Edit",
                        "Delete"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Getter for AdminDashboard instance
    public AdminDashboard getAdminDashboard() {
        return adminDashboard;
    }
}

// Button renderer for Edit/Delete buttons
class CampaignButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    private String type;

    public CampaignButtonRenderer(String type) {
        this.type = type;
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setOpaque(true);
    }

    // Renders Edit/Delete button in each table cell
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        setText(type);
        setBackground(type.equals("Edit") ? new Color(77, 166, 255) : new Color(255, 102, 102)); // Different color for each
        return this;
    }
}

// Editor class to handle Edit/Delete button actions
class CampaignButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private CampaignsPage campaignsPage;

    // Constructor to setup button and action
    public CampaignButtonEditor(JCheckBox checkBox, String actionType, CampaignsPage campaignsPage) {
        super(checkBox);
        this.campaignsPage = campaignsPage;

        // Configure button appearance
        button = new JButton(actionType.equals("edit") ? "Edit" : "Delete");
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBackground(actionType.equals("edit") ? new Color(77, 166, 255) : new Color(255, 102, 102));

        // Define button click behavior
        button.addActionListener(e -> {
            JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, button);
            if (table != null) {
                int viewRow = table.getSelectedRow();
                if (viewRow < 0) return;

                int modelRow = table.convertRowIndexToModel(viewRow);
                DefaultTableModel model = (DefaultTableModel) table.getModel();

                // Edit button clicked
                if (actionType.equals("edit")) {
                    new EditCampaignDialog(campaignsPage.getAdminDashboard(), model, modelRow).setVisible(true);
                    campaignsPage.loadCampaignData(); // Reload data after editing

                // Delete button clicked
                } else if (actionType.equals("delete")) {
                    int campaignId = (int) model.getValueAt(modelRow, 0); // Get ID of selected campaign
                    int confirm = JOptionPane.showConfirmDialog(button,
                            "Are you sure you want to delete campaign ID " + campaignId + "?",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try (Connection conn = DBConnection.getConnection()) {
                            PreparedStatement stmt = conn.prepareStatement("DELETE FROM campaigns WHERE campaign_id = ?");
                            stmt.setInt(1, campaignId);
                            stmt.executeUpdate();

                            model.removeRow(modelRow); // Remove row from table
                            campaignsPage.getAdminDashboard().updateStats(); // Update dashboard stats
                            campaignsPage.loadCampaignData(); // Reload table
                            JOptionPane.showMessageDialog(button, "Campaign deleted.");
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(button, "Error: " + ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    // Component that gets displayed when editing cell
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        return button;
    }

    // Return the button text as cell editor value
    public Object getCellEditorValue() {
        return button.getText();
    }
}
