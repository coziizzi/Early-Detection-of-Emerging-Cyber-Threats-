
package cyberthreatdetection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ThreatManagementScreen extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    public ThreatManagementScreen() {

        setTitle("Threat Management");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(new Color(15, 23, 42));
        main.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("THREAT MANAGEMENT",
                SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Investigate and Manage Security Alerts",
                SwingConstants.CENTER);
        subtitle.setForeground(new Color(148, 163, 184));

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        main.add(header, BorderLayout.NORTH);

        String[] columns = {
            "ID", "MONTH", "COUNT", "PREVIOUS AVG", "MESSAGE", "STATUS"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(51, 65, 85));
        table.getTableHeader().setBackground(new Color(51, 65, 85));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 41, 59));

        main.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);

        JButton update = new JButton("Update Status");
        JButton refresh = new JButton("Refresh");
        JButton back = new JButton("Back to Home");

        update.addActionListener(e -> updateStatus());
        refresh.addActionListener(e -> loadAlerts());
        back.addActionListener(e -> dispose());

        bottom.add(update);
        bottom.add(refresh);
        bottom.add(back);

        main.add(bottom, BorderLayout.SOUTH);

        add(main);
        loadAlerts();
    }

    private void loadAlerts() {

        model.setRowCount(0);

        String sql = "SELECT * FROM early_warnings ORDER BY alert_month";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("alert_month"),
                    rs.getInt("vulnerability_count"),
                    String.format("%.2f",
                            rs.getDouble("previous_average")),
                    rs.getString("alert_message"),
                    rs.getString("status")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load alerts: " + e.getMessage());
        }
    }

    private void updateStatus() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an alert first!");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        String[] options = {
            "NEW", "INVESTIGATING", "RESOLVED"
        };

        String status = (String) JOptionPane.showInputDialog(
                this,
                "Choose alert status:",
                "Update Alert",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                "NEW");

        if (status == null) {
            return;
        }

        String sql =
                "UPDATE early_warnings SET status = ? WHERE id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, id);

            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Alert status updated successfully!");

            loadAlerts();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Update failed: " + e.getMessage());
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            ThreatManagementScreen screen = new ThreatManagementScreen();
            screen.setVisible(true);
        });
    }
}
