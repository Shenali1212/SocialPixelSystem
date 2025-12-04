import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// Main class for the admin dashboard window
public class AdminDashboard extends JFrame {
    // Sidebar panel for navigation
    private JPanel sidebar;
    // Table to display client data
    JTable clientTable;
    // Table model for clientTable
    private DefaultTableModel tableModel;
    // Flag to track sidebar state (expanded/collapsed)
    private boolean sidebarExpanded = true;

    // Main content panel and layout manager
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // Labels for dashboard statistics
    private JLabel clientsCardLabel;
    private JLabel campaignsCardLabel;
    private JLabel paymentsCardLabel;
    private JLabel pendingPaymentsCardLabel;

    // Constructor: sets up the admin dashboard window
    public AdminDashboard() {
        setTitle("Admin Dashboard - SocialPixel"); // Set window title
        setSize(1000, 600); // Set window size
        setLocationRelativeTo(null); // Center window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close app on exit
        setLayout(new BorderLayout()); // Use BorderLayout

        // ===== SIDEBAR =====
        sidebar = new JPanel();
        sidebar.setBackground(new Color(40, 40, 40)); // Dark background
        sidebar.setPreferredSize(new Dimension(200, getHeight())); // Sidebar width
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS)); // Vertical layout

        // Sidebar menu toggle button
        JButton toggleBtn = new JButton("Menu");
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBackground(new Color(60, 60, 60));
        toggleBtn.setForeground(Color.WHITE);
        toggleBtn.setMaximumSize(new Dimension(200, 40));
        toggleBtn.setHorizontalAlignment(SwingConstants.LEFT);
        toggleBtn.setBorderPainted(false);
        toggleBtn.addActionListener(e -> toggleSidebar()); // Collapse/expand sidebar

        sidebar.add(toggleBtn);

        // Sidebar navigation buttons
        String[] menuItems = {"Dashboard", "Campaigns", "Payments", "Client Feedback", "Requirements", "Logout"};
        for (String item : menuItems) {
            JButton btn = new JButton(item);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btn.setBackground(new Color(60, 60, 60));
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setMaximumSize(new Dimension(200, 40));

            // Handle sidebar navigation
            btn.addActionListener(e -> {
                String command = e.getActionCommand();
                switch (command) {
                    case "Dashboard" -> cardLayout.show(mainPanel, "Dashboard");
                    case "Campaigns" -> {
                        cardLayout.show(mainPanel, "Campaigns");
                        ((CampaignsPage) mainPanel.getComponent(2)).loadCampaignData();
                    }
                    case "Payments" -> {
                        cardLayout.show(mainPanel, "Payments");
                        ((PaymentsPage) mainPanel.getComponent(1)).loadPaymentData();
                    }
                    case "Client Feedback" -> {
                        cardLayout.show(mainPanel, "ClientFeedback");
                        ((ClientFeedbackPanel) mainPanel.getComponent(4)).loadFeedback();
                    }
                    case "Requirements" -> {
                        cardLayout.show(mainPanel, "Requirements");
                        ((RequirementsPanel) mainPanel.getComponent(5)).loadRequirements();
                    }
                    case "Logout" -> { dispose(); new LoginWindow().setVisible(true); }
                }
            });

            sidebar.add(btn);
        }

        add(sidebar, BorderLayout.WEST); // Add sidebar to the left

        // ===== Main Panel with CardLayout =====
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- Dashboard View ---
        JPanel dashboardView = new JPanel(new GridBagLayout());
        dashboardView.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dashboard heading
        JLabel heading = new JLabel("Admin Dashboard");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        dashboardView.add(heading, gbc);

        // Statistic cards for dashboard
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        clientsCardLabel = new JLabel();
        dashboardView.add(createCard("Clients", clientsCardLabel, new Color(102, 204, 255)), gbc);
        gbc.gridx = 1;
        campaignsCardLabel = new JLabel();
        dashboardView.add(createCard("Campaigns", campaignsCardLabel, new Color(255, 204, 102)), gbc);
        gbc.gridx = 2;
        paymentsCardLabel = new JLabel();
        dashboardView.add(createCard("Paid Payments", paymentsCardLabel, new Color(153, 255, 153)), gbc);
        gbc.gridx = 3;
        pendingPaymentsCardLabel = new JLabel();
        dashboardView.add(createCard("Pending Payments", pendingPaymentsCardLabel, new Color(255, 179, 102)), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.1;
        dashboardView.add(new JLabel(), gbc); // Spacer

        // Add Client Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JButton addBtn = new JButton("Add Client");
        addBtn.setBackground(new Color(59, 89, 182));
        addBtn.setForeground(Color.WHITE);
        addBtn.setPreferredSize(new Dimension(130, 32));
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.addActionListener(e -> new AddClientDialog(this, tableModel).setVisible(true));
        dashboardView.add(addBtn, gbc);

        // Table Label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        JLabel tableLabel = new JLabel("Client Data");
        tableLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dashboardView.add(tableLabel, gbc);

        // Table Setup
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Contact", "NIC", "Industry", "Edit", "Delete"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5 || col == 6;
            }
        };
        clientTable = new JTable(tableModel);
        loadClientData();

        clientTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        clientTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), "edit", this));

        clientTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        clientTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), "delete", this));

        JScrollPane scrollPane = new JScrollPane(clientTable);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        dashboardView.add(scrollPane, gbc);

        // --- Payments View ---
        PaymentsPage paymentsView = new PaymentsPage(this);

        // --- Campaigns View ---
        CampaignsPage campaignsView = new CampaignsPage(this);

        // --- Client Feedback View ---
        ClientFeedbackPanel clientFeedbackPanel = new ClientFeedbackPanel();

        // --- Requirements View ---
        RequirementsPanel requirementsPanel = new RequirementsPanel();

        mainPanel.add(dashboardView, "Dashboard");
        mainPanel.add(paymentsView, "Payments");
        mainPanel.add(campaignsView, "Campaigns");
        mainPanel.add(clientFeedbackPanel, "ClientFeedback");
        mainPanel.add(requirementsPanel, "Requirements");
        add(mainPanel, BorderLayout.CENTER);
        
        updateStats(); // Initial stats load
    }

    // Helper method to create a colored card for dashboard statistics
    private JPanel createCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.DARK_GRAY, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // Toggle the sidebar between expanded and collapsed
    private void toggleSidebar() {
        sidebar.setPreferredSize(new Dimension(sidebarExpanded ? 60 : 200, getHeight()));
        sidebarExpanded = !sidebarExpanded;
        sidebar.revalidate();
    }

    // Load all client data from the database into the table
    public void loadClientData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT client_id, name, contact_info, nic, industry FROM clients";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("client_id"),
                        rs.getString("name"),
                        rs.getString("contact_info"),
                        rs.getString("nic"),
                        rs.getString("industry"),
                        "Edit",
                        "Delete"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update the dashboard statistics cards
    public void updateStats() {
        clientsCardLabel.setText(String.valueOf(getClientCount()));
        campaignsCardLabel.setText(String.valueOf(getCampaignCount()));
        paymentsCardLabel.setText("Rs. " + String.format("%.2f", getTotalPaidPayments()));
        pendingPaymentsCardLabel.setText("Rs. " + String.format("%.2f", getTotalPendingPayments()));
    }

    // Get the total number of clients
    private int getClientCount() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM clients";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    // Get the total number of campaigns
    private int getCampaignCount() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM campaigns";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    // Get the total amount of paid payments
    private double getTotalPaidPayments() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT SUM(amount) FROM payments WHERE status = 'Paid'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            return 0.0;
        }
    }

    // Get the total amount of pending payments
    private double getTotalPendingPayments() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT SUM(amount) FROM payments WHERE status = 'Pending'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
        return 0.0;
        }
    }

    // Main method to launch the admin dashboard
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}

// ===== ButtonRenderer =====
// Custom cell renderer for table buttons (Edit, Delete, Reply)
class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (table.getColumnName(column).equals("Delete")) {
            setBackground(new Color(255, 102, 102)); // Red for Delete
            setForeground(Color.WHITE);
            setText("Delete");
        } else if (table.getColumnName(column).equals("Edit")) {
            setBackground(new Color(77, 166, 255)); // Blue for Edit
            setForeground(Color.WHITE);
            setText("Edit");
        } else if (table.getColumnName(column).equals("Reply")) {
            setBackground(new Color(77, 166, 255));
            setForeground(Color.WHITE);
            setText("Reply");
        } else {
            setBackground(UIManager.getColor("Button.background"));
            setForeground(Color.BLACK);
            setText(value == null ? "Reply" : value.toString());
        }
        return this;
    }
}

// ===== ButtonEditor =====
// Custom cell editor for table buttons (Edit, Delete, Reply)
class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String actionType;
    private Object parentPanel;
    private String replyText = "";
    public ButtonEditor(JCheckBox checkBox, String actionType, Object parentPanel) {
        super(checkBox);
        this.actionType = actionType;
        this.parentPanel = parentPanel;
        if ("reply".equals(actionType)) {
            button = new JButton("Reply");
            button.addActionListener(e -> {
                System.out.println("Reply button clicked!"); // DEBUG
                if (parentPanel instanceof JPanel) {
                    JScrollPane scrollPane = (JScrollPane) ((JPanel) parentPanel).getComponent(0);
                    JTable table = (JTable) scrollPane.getViewport().getView();
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        replyText = JOptionPane.showInputDialog((JPanel) parentPanel, "Enter reply:");
                        if (replyText != null && !replyText.trim().isEmpty()) {
                            int modelRow = table.convertRowIndexToModel(row);
                            DefaultTableModel model = (DefaultTableModel) table.getModel();
                            int id = (int) model.getValueAt(modelRow, 0); // Use hidden ID column
                            String tableName = parentPanel instanceof ClientChangesPanel ? "change_requests" : "feedback";
                            try (Connection conn = DBConnection.getConnection();
                                 PreparedStatement stmt = conn.prepareStatement(
                                    "UPDATE " + tableName + " SET reply=? WHERE id=?")) {
                                stmt.setString(1, replyText);
                                stmt.setInt(2, id);
                                stmt.executeUpdate();
                                JOptionPane.showMessageDialog((JPanel) parentPanel, "Reply saved!");
                                if (parentPanel instanceof ClientChangesPanel) ((ClientChangesPanel) parentPanel).loadChangeRequests();
                                if (parentPanel instanceof ClientFeedbackPanel) ((ClientFeedbackPanel) parentPanel).loadFeedback();
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog((JPanel) parentPanel, "Error saving reply: " + ex.getMessage());
                            }
                        }
                    }
                }
            });
        } else if ("reply_requirement".equals(actionType)) {
            button = new JButton("Reply");
            button.addActionListener(e -> {
                System.out.println("Reply to requirement clicked!");
                if (parentPanel instanceof JPanel) {
                    JScrollPane scrollPane = (JScrollPane) ((JPanel) parentPanel).getComponent(0);
                    JTable table = (JTable) scrollPane.getViewport().getView();
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String reply = JOptionPane.showInputDialog((JPanel) parentPanel, "Enter admin response:");
                        if (reply != null && !reply.trim().isEmpty()) {
                            int modelRow = table.convertRowIndexToModel(row);
                            DefaultTableModel model = (DefaultTableModel) table.getModel();
                            int id = (int) model.getValueAt(modelRow, 0);
                            try (Connection conn = DBConnection.getConnection();
                                 PreparedStatement stmt = conn.prepareStatement("UPDATE requirements SET admin_response=? WHERE id=?")) {
                                stmt.setString(1, reply);
                                stmt.setInt(2, id);
                                stmt.executeUpdate();
                                JOptionPane.showMessageDialog((JPanel) parentPanel, "Response saved!");
                                if (parentPanel instanceof RequirementsPanel) ((RequirementsPanel) parentPanel).loadRequirements();
                            } catch (SQLException ex) {
                                JOptionPane.showMessageDialog((JPanel) parentPanel, "Error saving response: " + ex.getMessage());
                            }
                        }
                    }
                }
            });
        } else {
            // For edit/delete in AdminDashboard
            button = new JButton(actionType.equals("edit") ? "Edit" : "Delete");
            if ("delete".equals(actionType)) {
                button.setBackground(new Color(255, 102, 102));
                button.setForeground(Color.WHITE);
            } else if ("edit".equals(actionType)) {
                button.setBackground(new Color(77, 166, 255));
        button.setForeground(Color.WHITE);
            }
       button.addActionListener(e -> {
                if (parentPanel instanceof AdminDashboard) {
                    JTable table = ((AdminDashboard) parentPanel).clientTable;
        int row = table.getSelectedRow();
                    if (row != -1) {
                        int modelRow = table.convertRowIndexToModel(row);
                        if ("edit".equals(actionType)) {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(button);
                            new EditClientDialog(parent, (DefaultTableModel) table.getModel(), modelRow).setVisible(true);
                        } else if ("delete".equals(actionType)) {
                            String clientId = table.getModel().getValueAt(modelRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(button,
                    "Are you sure you want to delete client ID " + clientId + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE client_id=?");
                    stmt.setInt(1, Integer.parseInt(clientId));
                    stmt.executeUpdate();
                                    ((DefaultTableModel) table.getModel()).removeRow(modelRow);
                                    ((AdminDashboard) parentPanel).updateStats();
                    JOptionPane.showMessageDialog(button, "Client deleted.");
                } catch (SQLException ex) {
                                    if (ex.getMessage().contains("a foreign key constraint fails")) {
                                        JOptionPane.showMessageDialog(button, "Cannot delete client: related campaigns or other records exist.", "Delete Error", JOptionPane.WARNING_MESSAGE);
                                    } else {
                    JOptionPane.showMessageDialog(button, "Error: " + ex.getMessage());
                                    }
                                }
                }
            }
        }
    }
});
        }
    }
    // Returns the button component for the table cell
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return button;
    }
    // Returns the value of the cell editor (used for reply text)
    public Object getCellEditorValue() {
        return replyText;
    }
}

// Panel for displaying and replying to client change requests
class ClientChangesPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    public ClientChangesPanel() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new Object[]{"ID", "Client ID", "Client Name", "Message", "Submitted At", "Reply"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5;
            }
        };
        table = new JTable(model);
        // Hide the ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumn("Reply").setCellRenderer(new ButtonRenderer());
        table.getColumn("Reply").setCellEditor(new ButtonEditor(new JCheckBox(), "reply", this));
        // Add MouseListener to trigger Reply button on single click
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                JTable t = (JTable) e.getSource();
                Point p = e.getPoint();
                int row = t.rowAtPoint(p);
                int column = t.columnAtPoint(p);
                if (column == t.getColumn("Reply").getModelIndex()) {
                    t.editCellAt(row, column);
                    Component editor = t.getEditorComponent();
                    if (editor != null) {
                        editor.requestFocus();
                    }
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadChangeRequests();
    }
    // Load all change requests from the database
    public void loadChangeRequests() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, client_id, client_name, message, submitted_at, reply FROM change_requests ORDER BY submitted_at DESC")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("client_id"),
                    rs.getString("client_name"),
                    rs.getString("message"),
                    rs.getTimestamp("submitted_at"),
                    rs.getString("reply") == null ? "Reply" : rs.getString("reply")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading change requests: " + e.getMessage());
        }
    }
}

// Panel for displaying and replying to client feedback
class ClientFeedbackPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    public ClientFeedbackPanel() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new Object[]{"ID", "Client ID", "Client Name", "Message", "Submitted At", "Reply"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5;
            }
        };
        table = new JTable(model);
        // Hide the ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumn("Reply").setCellRenderer(new ButtonRenderer());
        table.getColumn("Reply").setCellEditor(new ButtonEditor(new JCheckBox(), "reply", this));
        // Add MouseListener to trigger Reply button on single click
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                JTable t = (JTable) e.getSource();
                Point p = e.getPoint();
                int row = t.rowAtPoint(p);
                int column = t.columnAtPoint(p);
                if (column == t.getColumn("Reply").getModelIndex()) {
                    t.editCellAt(row, column);
                    Component editor = t.getEditorComponent();
                    if (editor != null) {
                        editor.requestFocus();
                    }
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadFeedback();
    }
    // Load all feedback from the database
    public void loadFeedback() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, client_id, client_name, message, submitted_at, reply FROM feedback ORDER BY submitted_at DESC")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("client_id"),
                    rs.getString("client_name"),
                    rs.getString("message"),
                    rs.getTimestamp("submitted_at"),
                    rs.getString("reply") == null ? "Reply" : rs.getString("reply")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading feedback: " + e.getMessage());
        }
    }
}

// Panel for displaying and replying to client requirements
class RequirementsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    public RequirementsPanel() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new Object[]{"ID", "Client Name", "Requirement", "Admin Response", "Submitted At", "Reply"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5;
            }
        };
        table = new JTable(model);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumn("Reply").setCellRenderer(new ButtonRenderer());
        table.getColumn("Reply").setCellEditor(new ButtonEditor(new JCheckBox(), "reply_requirement", this));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                JTable t = (JTable) e.getSource();
                Point p = e.getPoint();
                int row = t.rowAtPoint(p);
                int column = t.columnAtPoint(p);
                if (column == t.getColumn("Reply").getModelIndex()) {
                    t.editCellAt(row, column);
                    Component editor = t.getEditorComponent();
                    if (editor != null) {
                        editor.requestFocus();
                    }
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
        loadRequirements();
    }
    // Load all requirements from the database
    public void loadRequirements() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, client_id, requirement, admin_response, submitted_at FROM requirements ORDER BY submitted_at DESC")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String clientName = "";
                try (PreparedStatement nameStmt = conn.prepareStatement("SELECT name FROM clients WHERE client_id = ?")) {
                    nameStmt.setInt(1, rs.getInt("client_id"));
                    ResultSet nameRs = nameStmt.executeQuery();
                    if (nameRs.next()) clientName = nameRs.getString("name");
                }
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    clientName,
                    rs.getString("requirement"),
                    rs.getString("admin_response"),
                    rs.getTimestamp("submitted_at"),
                    "Reply"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading requirements: " + e.getMessage());
        }
    }
}
