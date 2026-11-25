
package cyberthreatdetection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DataManagementScreen extends JFrame {

    private DefaultTableModel model;
    private JLabel totalLabel;

    public DataManagementScreen() {

        setTitle("Data Management");
        setSize(1150, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(new Color(15, 23, 42));
        main.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("DATA MANAGEMENT",
                SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "CISA Known Exploited Vulnerabilities Dataset",
                SwingConstants.CENTER);
        subtitle.setForeground(new Color(148, 163, 184));

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        header.add(title);
        header.add(subtitle);

        main.add(header, BorderLayout.NORTH);

        String[] columns = {
            "CVE ID", "VENDOR", "PRODUCT",
            "VULNERABILITY", "DATE ADDED", "RANSOMWARE"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(51, 65, 85));
        table.getTableHeader().setBackground(new Color(51, 65, 85));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 41, 59));

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);

        totalLabel = new JLabel("Total records: 0");
        totalLabel.setForeground(new Color(56, 189, 248));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        center.add(totalLabel, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        main.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setOpaque(false);

        JButton importButton = new JButton("Import CISA Dataset");
        JButton refresh = new JButton("Refresh");
        JButton back = new JButton("Back to Home");

        importButton.addActionListener(e -> {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Import the CISA dataset again?",
                    "Confirm Import",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                CISADataImporter.importData();
                loadData();
                JOptionPane.showMessageDialog(this,
                        "Dataset import process completed. Check the Output window for details.");
            }
        });

        refresh.addActionListener(e -> loadData());
        back.addActionListener(e -> dispose());

        bottom.add(importButton);
        bottom.add(refresh);
        bottom.add(back);

        main.add(bottom, BorderLayout.SOUTH);

        add(main);
        loadData();
    }

    private void loadData() {

        model.setRowCount(0);

        String sql = "SELECT cve_id, vendor_project, product, "
                + "vulnerability_name, date_added, ransomware_use "
                + "FROM kev_vulnerabilities ORDER BY date_added DESC";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int count = 0;

            while (rs.next()) {

                model.addRow(new Object[]{
                    rs.getString("cve_id"),
                    rs.getString("vendor_project"),
                    rs.getString("product"),
                    rs.getString("vulnerability_name"),
                    rs.getString("date_added"),
                    rs.getString("ransomware_use")
                });

                count++;
            }

            totalLabel.setText("Total records: " + count);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Data loading failed: " + e.getMessage());
        }
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            DataManagementScreen screen = new DataManagementScreen();
            screen.setVisible(true);
        });
    }
}
