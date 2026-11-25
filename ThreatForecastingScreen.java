
package cyberthreatdetection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.YearMonth;
import java.util.ArrayList;

public class ThreatForecastingScreen extends JFrame {

    private JLabel movingMAE;
    private JLabel smoothingMAE;
    private JLabel betterModel;

    private DefaultTableModel model;

    private static final double ALPHA = 0.4;

    public ThreatForecastingScreen() {

        setTitle("Threat Forecasting");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(new Color(15, 23, 42));
        main.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel(
                "THREAT FORECASTING",
                SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Predicting Future Vulnerability Trends",
                SwingConstants.CENTER);
        subtitle.setForeground(new Color(148, 163, 184));

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        main.add(header, BorderLayout.NORTH);

        // MODEL ACCURACY CARDS
        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 15));
        cards.setOpaque(false);

        movingMAE = new JLabel("0.00");
        smoothingMAE = new JLabel("0.00");
        betterModel = new JLabel("N/A");

        cards.add(createCard("MOVING AVERAGE MAE", movingMAE));
        cards.add(createCard("EXPONENTIAL SMOOTHING MAE", smoothingMAE));
        cards.add(createCard("BETTER MODEL", betterModel));

        // FORECAST TABLE
        String[] columns = {
            "FUTURE MONTH",
            "MOVING AVERAGE FORECAST",
            "EXPONENTIAL SMOOTHING FORECAST"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(38);
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(51, 65, 85));

        table.getTableHeader().setBackground(new Color(51, 65, 85));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 41, 59));

        JPanel center = new JPanel(new BorderLayout(15, 20));
        center.setOpaque(false);
        center.add(cards, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        main.add(center, BorderLayout.CENTER);

        // BUTTONS
        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);

        JButton refresh = new JButton("Refresh Forecast");
        JButton back = new JButton("Back to Home");

        refresh.addActionListener(e -> loadForecast());
        back.addActionListener(e -> dispose());

        bottom.add(refresh);
        bottom.add(back);

        main.add(bottom, BorderLayout.SOUTH);

        add(main);

        loadForecast();
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
        value.setFont(new Font("Arial", Font.BOLD, 22));

        card.add(label);
        card.add(value);

        return card;
    }

    private void loadForecast() {

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

            int n = counts.size();

            if (n < 4) {
                JOptionPane.showMessageDialog(this,
                        "Not enough historical data for forecasting.");
                return;
            }

            double movingError = 0;
            double smoothingError = 0;
            int comparisons = 0;

            double level = counts.get(0);

            for (int i = 1; i < n; i++) {

                double smoothingPrediction = level;

                if (i >= 3) {

                    double movingPrediction =
                            (counts.get(i - 1)
                            + counts.get(i - 2)
                            + counts.get(i - 3)) / 3.0;

                    movingError += Math.abs(
                            counts.get(i) - movingPrediction);

                    smoothingError += Math.abs(
                            counts.get(i) - smoothingPrediction);

                    comparisons++;
                }

                level = ALPHA * counts.get(i)
                        + (1 - ALPHA) * level;
            }

            double ma = movingError / comparisons;
            double es = smoothingError / comparisons;

            movingMAE.setText(String.format("%.2f", ma));
            smoothingMAE.setText(String.format("%.2f", es));

            betterModel.setText(
                    ma < es ? "Moving Average"
                    : es < ma ? "Exponential Smoothing"
                    : "Equal");

            YearMonth lastMonth =
                    YearMonth.parse(months.get(n - 1));

            ArrayList<Double> values = new ArrayList<>();

            for (int count : counts) {
                values.add((double) count);
            }

            for (int i = 1; i <= 3; i++) {

                int size = values.size();

                double forecast =
                        (values.get(size - 1)
                        + values.get(size - 2)
                        + values.get(size - 3)) / 3.0;

                YearMonth future =
                        lastMonth.plusMonths(i);

                model.addRow(new Object[]{
                    future.toString(),
                    String.format("%.2f", forecast),
                    String.format("%.2f", level)
                });

                values.add(forecast);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Forecasting failed: " + e.getMessage());
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            ThreatForecastingScreen screen =
                    new ThreatForecastingScreen();
            screen.setVisible(true);
        });
    }
}

