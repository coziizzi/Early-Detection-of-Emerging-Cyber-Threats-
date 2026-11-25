
package cyberthreatdetection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class MonthlyTrendChart extends JPanel {

    private ArrayList<String> months = new ArrayList<>();
    private ArrayList<Integer> counts = new ArrayList<>();

    public MonthlyTrendChart() {

        setBackground(new Color(30, 41, 59));
        setPreferredSize(new Dimension(800, 280));

        loadData();
    }

    public void loadData() {

        months.clear();
        counts.clear();

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

        } catch (Exception e) {
            System.out.println("Chart loading failed: "
                    + e.getMessage());
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 17));
        g2.drawString("MONTHLY VULNERABILITY TREND", 25, 28);

        if (counts.isEmpty()) {
            g2.drawString("No data available", 40, 100);
            return;
        }

        int left = 65;
        int right = getWidth() - 30;
        int top = 55;
        int bottom = getHeight() - 55;

        int max = 1;

        for (int value : counts) {
            max = Math.max(max, value);
        }

        max = ((max + 49) / 50) * 50;

        g2.setFont(new Font("Arial", Font.PLAIN, 11));

        for (int i = 0; i <= 5; i++) {

            int y = bottom - (i * (bottom - top) / 5);
            int value = i * max / 5;

            g2.setColor(new Color(71, 85, 105));
            g2.drawLine(left, y, right, y);

            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(value), 25, y + 5);
        }

        g2.setColor(Color.WHITE);
        g2.drawLine(left, top, left, bottom);
        g2.drawLine(left, bottom, right, bottom);

        int size = counts.size();

        int previousX = -1;
        int previousY = -1;

        for (int i = 0; i < size; i++) {

            int x = left + (size == 1 ? 0 :
                    i * (right - left) / (size - 1));

            int y = bottom -
                    (counts.get(i) * (bottom - top) / max);

            if (previousX != -1) {
                g2.setColor(new Color(56, 189, 248));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(previousX, previousY, x, y);
            }

            g2.setColor(new Color(34, 211, 238));
            g2.fillOval(x - 4, y - 4, 8, 8);

            previousX = x;
            previousY = y;
        }

        int interval = Math.max(1, (size + 7) / 8);

        g2.setColor(Color.WHITE);

        for (int i = 0; i < size; i += interval) {

            int x = left + (size == 1 ? 0 :
                    i * (right - left) / (size - 1));

            g2.drawString(months.get(i), x - 22, bottom + 20);
        }

        g2.drawString(
                "Months",
                (left + right) / 2 - 20,
                getHeight() - 10);
    }
}
