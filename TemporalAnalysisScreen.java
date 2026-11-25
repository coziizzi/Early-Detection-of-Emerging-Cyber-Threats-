
package cyberthreatdetection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TemporalAnalysisScreen extends JFrame {

    private JLabel totalLabel;
    private JLabel monthsLabel;
    private JLabel averageLabel;
    private JLabel highestLabel;

    public TemporalAnalysisScreen() {

        setTitle("Temporal Analysis");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(new Color(15, 23, 42));
        main.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel(
                "TEMPORAL ANALYSIS", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Historical Vulnerability Activity",
                SwingConstants.CENTER);
        subtitle.setForeground(new Color(148, 163, 184));

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        main.add(header, BorderLayout.NORTH);

        // STATISTICS
        JPanel stats = new JPanel(new GridLayout(1, 4, 15, 15));
        stats.setOpaque(false);

        totalLabel = new JLabel("0");
        monthsLabel = new JLabel("0");
        averageLabel = new JLabel("0");
        highestLabel = new JLabel("N/A");

        stats.add(createCard("TOTAL VULNERABILITIES", totalLabel));
        stats.add(createCard("MONTHS ANALYZED", monthsLabel));
        stats.add(createCard("MONTHLY AVERAGE", averageLabel));
        stats.add(createCard("HIGHEST ACTIVITY", highestLabel));

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setOpaque(false);
        content.add(stats, BorderLayout.NORTH);

        MonthlyTrendChart chart = new MonthlyTrendChart();
        content.add(chart, BorderLayout.CENTER);

        main.add(content, BorderLayout.CENTER);

        // BOTTOM BUTTONS
        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);

        JButton refresh = new JButton("Refresh Analysis");
        JButton back = new JButton("Back to Home");

        refresh.addActionListener(e -> {
            loadStatistics();
            chart.loadData();
        });

        back.addActionListener(e -> dispose());

        bottom.add(refresh);
        bottom.add(back);

        main.add(bottom, BorderLayout.SOUTH);

        add(main);

        loadStatistics();
    }

    private JPanel createCard(String heading, JLabel value) {

        JPanel card = new JPanel(new GridLayout(2, 1, 5, 5));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createEmptyBorder(
                15, 12, 15, 12));

        JLabel label = new JLabel(heading);
        label.setForeground(new Color(148, 163, 184));
        label.setFont(new Font("Arial", Font.BOLD, 11));

        value.setForeground(new Color(56, 189, 248));
        value.setFont(new Font("Arial", Font.BOLD, 23));

        card.add(label);
        card.add(value);

        return card;
    }

    private void loadStatistics() {

        String sql =
                "SELECT COUNT(*) AS total, "
                + "COUNT(DISTINCT strftime('%Y-%m', date_added)) AS months, "
                + "MAX(month_count) AS highest "
                + "FROM ("
                + "SELECT date_added, COUNT(*) AS month_count "
                + "FROM kev_vulnerabilities "
                + "WHERE date_added IS NOT NULL "
                + "GROUP BY strftime('%Y-%m', date_added)"
                + ")";

        String averageSQL =
                "SELECT AVG(month_count) FROM ("
                + "SELECT COUNT(*) AS month_count "
                + "FROM kev_vulnerabilities "
                + "WHERE date_added IS NOT NULL "
                + "GROUP BY strftime('%Y-%m', date_added)"
                + ")";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                totalLabel.setText(
                        String.valueOf(rs.getInt("total")));

                monthsLabel.setText(
                        String.valueOf(rs.getInt("months")));

                highestLabel.setText(
                        String.valueOf(rs.getInt("highest")));
            }

            try (PreparedStatement avg =
                    conn.prepareStatement(averageSQL);
                 ResultSet averageResult = avg.executeQuery()) {

                if (averageResult.next()) {
                    averageLabel.setText(String.format("%.2f",
                            averageResult.getDouble(1)));
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load analysis: " + e.getMessage());
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            TemporalAnalysisScreen screen =
                    new TemporalAnalysisScreen();
            screen.setVisible(true);
        });
    }
}
