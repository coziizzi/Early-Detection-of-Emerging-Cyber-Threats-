
package cyberthreatdetection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EarlyWarningsScreen extends JFrame {

    private DefaultTableModel model;
    private JLabel totalAlerts;

    public EarlyWarningsScreen() {

        setTitle("Early Warning System");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(new Color(15, 23, 42));
        main.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("EARLY WARNING SYSTEM",
                SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Generated Cyber Threat Alerts",
                SwingConstants.CENTER);
        subtitle.setForeground(new Color(148, 163, 184));

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        main.add(header, BorderLayout.NORTH);

        JPanel summary = new JPanel(new GridLayout(2, 1, 5, 5));
        summary.setBackground(new Color(30, 41, 59));
        summary.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel label = new JLabel("TOTAL STORED ALERTS");
        label.setForeground(new Color(148, 163, 184));

        totalAlerts = new JLabel("0");
        totalAlerts.setForeground(new Color(248, 113, 113));
        totalAlerts.setFont(new Font("Arial", Font.BOLD, 30));

        summary.add(label);
        summary.add(totalAlerts);

        String[] columns = {
            "ID", "MONTH", "COUNT", "PREVIOUS AVG", "MESSAGE", "STATUS"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(51, 65, 85));
        table.getTableHeader().setBackground(new Color(51, 65, 85));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 41, 59));

        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setOpaque(false);
        center.add(summary, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        main.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);

        JButton refresh = new JButton("Refresh Alerts");
        JButton back = new JButton("Back to Home");

        refresh.addActionListener(e -> loadAlerts());
        back.addActionListener(e -> dispose());

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

            int count = 0;

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

                count++;
            }

            totalAlerts.setText(String.valueOf(count));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load alerts: " + e.getMessage());
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            EarlyWarningsScreen screen = new EarlyWarningsScreen();
            screen.setVisible(true);
        });
    }
}
