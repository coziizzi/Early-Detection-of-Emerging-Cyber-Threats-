
package cyberthreatdetection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Dashboard extends JFrame {

    private JLabel totalVulnerabilities;
    private JLabel totalAlerts;
    private JLabel newAlerts;
    private JLabel monthsAnalyzed;

    private JTable alertTable;
    private DefaultTableModel tableModel;

    public Dashboard() {

        setTitle("Cyber Threat Detection System");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(new Color(15, 23, 42));

        // HEADER

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("CYBER THREAT DETECTION SYSTEM");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtitle = new JLabel(
                "EARLY DETECTION OF EMERGING CYBER THREATS");
        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel heading = new JPanel(new GridLayout(2, 1, 5, 5));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);

        header.add(heading, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // STATISTICS CARDS

        JPanel statistics = new JPanel(new GridLayout(1, 4, 15, 15));
        statistics.setOpaque(false);
        statistics.setBorder(BorderFactory.createEmptyBorder(
                15, 20, 10, 20));

        totalVulnerabilities = new JLabel("0");
        totalAlerts = new JLabel("0");
        newAlerts = new JLabel("0");
        monthsAnalyzed = new JLabel("0");

        statistics.add(createCard(
                "TOTAL VULNERABILITIES", totalVulnerabilities));

        statistics.add(createCard(
                "TOTAL ALERTS", totalAlerts));

        statistics.add(createCard(
                "NEW ALERTS", newAlerts));

        statistics.add(createCard(
                "MONTHS ANALYZED", monthsAnalyzed));

        // ALERT TABLE

        String[] columns = {
            "ID", "MONTH", "VULNERABILITIES",
            "PREVIOUS AVG", "MESSAGE", "STATUS"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        alertTable = new JTable(tableModel);
        alertTable.setRowHeight(30);
        alertTable.setFont(new Font("Arial", Font.PLAIN, 13));
        alertTable.setBackground(new Color(30, 41, 59));
        alertTable.setForeground(Color.WHITE);
        alertTable.setGridColor(new Color(51, 65, 85));
        alertTable.setSelectionBackground(new Color(37, 99, 235));
        alertTable.getTableHeader().setBackground(
                new Color(51, 65, 85));
        alertTable.getTableHeader().setForeground(Color.WHITE);
        alertTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(alertTable);
        scrollPane.getViewport().setBackground(new Color(30, 41, 59));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel center = new JPanel(new BorderLayout(10, 15));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(
                0, 20, 10, 20));

        JLabel alertTitle = new JLabel("THREAT ALERTS");
        alertTitle.setForeground(Color.WHITE);
        alertTitle.setFont(new Font("Arial", Font.BOLD, 18));

        center.add(alertTitle, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setOpaque(false);
        mainPanel.add(statistics, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        JPanel dashboardContent = new JPanel(new BorderLayout(10, 15));
        dashboardContent.setOpaque(false);
        dashboardContent.add(mainPanel, BorderLayout.CENTER);
        MonthlyTrendChart trendChart = new MonthlyTrendChart();
        dashboardContent.add(trendChart, BorderLayout.SOUTH);
        add(dashboardContent, BorderLayout.CENTER);


        // BOTTOM BUTTONS

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottom.setBackground(new Color(30, 41, 59));

        JButton refreshButton = new JButton("Refresh Dashboard");
        JButton updateButton = new JButton("Update Alert Status");

        refreshButton.addActionListener(e -> loadDashboard());

        updateButton.addActionListener(e -> updateAlertStatus());

        bottom.add(refreshButton);
        bottom.add(updateButton);

        add(bottom, BorderLayout.SOUTH);

        loadDashboard();

    }

    private JPanel createCard(String heading, JLabel value) {

        JPanel card = new JPanel(new GridLayout(2, 1, 5, 5));

        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createEmptyBorder(
                15, 15, 15, 15));

        JLabel label = new JLabel(heading);
        label.setForeground(new Color(148, 163, 184));
        label.setFont(new Font("Arial", Font.BOLD, 11));

        value.setForeground(new Color(56, 189, 248));
        value.setFont(new Font("Arial", Font.BOLD, 28));

        card.add(label);
        card.add(value);

        return card;
    }

    private void loadDashboard() {

        String countSQL =
                "SELECT COUNT(*) FROM kev_vulnerabilities";

        String monthSQL =
                "SELECT COUNT(DISTINCT strftime('%Y-%m', date_added)) "
                + "FROM kev_vulnerabilities";

        String alertSQL =
                "SELECT COUNT(*) FROM early_warnings";

        String newSQL =
                "SELECT COUNT(*) FROM early_warnings WHERE status = 'NEW'";

        try (Connection conn = Database.connect()) {

            if (conn == null) {
                JOptionPane.showMessageDialog(this,
                        "Database connection failed!");
                return;
            }

            totalVulnerabilities.setText(
                    getCount(conn, countSQL));

            monthsAnalyzed.setText(
                    getCount(conn, monthSQL));

            totalAlerts.setText(
                    getCount(conn, alertSQL));

            newAlerts.setText(
                    getCount(conn, newSQL));

            tableModel.setRowCount(0);

            String sql = "SELECT * FROM early_warnings "
                    + "ORDER BY alert_month";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("alert_month"),
                        rs.getInt("vulnerability_count"),
                        String.format("%.2f",
                                rs.getDouble("previous_average")),
                        rs.getString("alert_message"),
                        rs.getString("status")
                    };

                    tableModel.addRow(row);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Dashboard loading failed: " + e.getMessage());
        }
    }

    private String getCount(Connection conn, String sql)
            throws Exception {

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        }

        return "0";
    }

    private void updateAlertStatus() {

        int selectedRow = alertTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an alert first!");
            return;
        }

        int alertID = (int) tableModel.getValueAt(selectedRow, 0);

        String[] options = {
            "NEW", "INVESTIGATING", "RESOLVED"
        };

        String status = (String) JOptionPane.showInputDialog(
                this,
                "Select the new alert status:",
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
            pstmt.setInt(2, alertID);

            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this,
                        "Alert status updated successfully!");

                loadDashboard();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Update failed: " + e.getMessage());
        }
    }

    public static void launch() {

        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard();
            dashboard.setVisible(true);
        });
    }
}
