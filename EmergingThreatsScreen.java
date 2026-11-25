
package cyberthreatdetection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class EmergingThreatsScreen extends JFrame {

    private JLabel totalAlerts;
    private DefaultTableModel model;

    public EmergingThreatsScreen() {

        setTitle("Emerging Threat Identification");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(new Color(15, 23, 42));
        main.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel(
                "EMERGING THREAT IDENTIFICATION",
                SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtitle = new JLabel(
                "Detection of Unusual Vulnerability Increases",
                SwingConstants.CENTER);
        subtitle.setForeground(new Color(148, 163, 184));

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        main.add(header, BorderLayout.NORTH);

        // ALERT SUMMARY
        JPanel summary = new JPanel(new GridLayout(1, 1));
        summary.setBackground(new Color(30, 41, 59));
        summary.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        totalAlerts = new JLabel("0");
        totalAlerts.setForeground(new Color(248, 113, 113));
        totalAlerts.setFont(new Font("Arial", Font.BOLD, 30));

        JLabel summaryTitle = new JLabel("TOTAL SPIKES DETECTED");
        summaryTitle.setForeground(new Color(148, 163, 184));

        JPanel summaryContent = new JPanel(new GridLayout(2, 1, 5, 5));
        summaryContent.setOpaque(false);
        summaryContent.add(summaryTitle);
        summaryContent.add(totalAlerts);

        summary.add(summaryContent);

        // TABLE
        String[] columns = {
            "MONTH", "VULNERABILITIES",
            "PREVIOUS 3-MONTH AVG", "STATUS"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(51, 65, 85));
        table.setSelectionBackground(new Color(37, 99, 235));

        table.getTableHeader().setBackground(new Color(51, 65, 85));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 41, 59));

        JPanel center = new JPanel(new BorderLayout(10, 15));
        center.setOpaque(false);
        center.add(summary, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        main.add(center, BorderLayout.CENTER);

        // BUTTONS
        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);

        JButton refresh = new JButton("Refresh Analysis");
        JButton back = new JButton("Back to Home");

        refresh.addActionListener(e -> loadThreats());
        back.addActionListener(e -> dispose());

        bottom.add(refresh);
        bottom.add(back);

        main.add(bottom, BorderLayout.SOUTH);

        add(main);

        loadThreats();
    }

    private void loadThreats() {

        model.setRowCount(0);

        ArrayList<String> months = new ArrayList<>();
        ArrayList<Integer> counts = new ArrayList<>();

        String sql =
                "SELECT strftime('%Y-%m', date_added) AS month, "
                + "COUNT(*) AS total "
                + "FROM kev_vulnerabilities "
                + "WHERE date_added IS NOT NULL "
                + "GROUP BY month ORDER BY month";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                months.add(rs.getString("month"));
                counts.add(rs.getInt("total"));
            }

            int alerts = 0;

            for (int i = 3; i < counts.size(); i++) {

                double previousAverage =
                        (counts.get(i - 1)
                        + counts.get(i - 2)
                        + counts.get(i - 3)) / 3.0;

                if (counts.get(i) >= 10
                        && counts.get(i) >= 2 * previousAverage) {

                    model.addRow(new Object[]{
                        months.get(i),
                        counts.get(i),
                        String.format("%.2f", previousAverage),
                        "HIGH ALERT"
                    });

                    alerts++;
                }
            }

            totalAlerts.setText(String.valueOf(alerts));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Threat analysis failed: " + e.getMessage());
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            EmergingThreatsScreen screen =
                    new EmergingThreatsScreen();
            screen.setVisible(true);
        });
    }
}
