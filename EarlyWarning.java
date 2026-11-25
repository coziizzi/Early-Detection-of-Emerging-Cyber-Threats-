
package cyberthreatdetection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class EarlyWarning {

    public static void generateAlerts() {

        String createTable = "CREATE TABLE IF NOT EXISTS early_warnings ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "alert_month TEXT UNIQUE,"
                + "vulnerability_count INTEGER,"
                + "previous_average REAL,"
                + "alert_message TEXT,"
                + "status TEXT DEFAULT 'NEW'"
                + ")";

        String fetchData = "SELECT strftime('%Y-%m', date_added) AS month, "
                + "COUNT(*) AS total "
                + "FROM kev_vulnerabilities "
                + "WHERE date_added IS NOT NULL "
                + "GROUP BY month "
                + "ORDER BY month";

        String insertAlert = "INSERT OR IGNORE INTO early_warnings "
                + "(alert_month, vulnerability_count, previous_average, "
                + "alert_message, status) "
                + "VALUES (?, ?, ?, ?, 'NEW')";

        System.out.println("\n=================================");
        System.out.println("        EARLY WARNING SYSTEM");
        System.out.println("=================================");

        try (Connection conn = Database.connect()) {

            if (conn == null) {
                System.out.println("Database unavailable!");
                return;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTable);
            }

            ArrayList<String> months = new ArrayList<>();
            ArrayList<Integer> counts = new ArrayList<>();

            try (PreparedStatement pstmt = conn.prepareStatement(fetchData);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    months.add(rs.getString("month"));
                    counts.add(rs.getInt("total"));
                }
            }

            int alerts = 0;

            String checkSQL =
                    "SELECT COUNT(*) FROM early_warnings WHERE alert_month = ?";

            try (PreparedStatement insert =
                    conn.prepareStatement(insertAlert);
                 PreparedStatement check =
                    conn.prepareStatement(checkSQL)) {

                System.out.printf("%-12s %-12s %-15s %-15s%n",
                        "MONTH", "COUNT", "PREV. AVG", "WARNING");

                System.out.println("-----------------------------------------------");

                for (int i = 3; i < months.size(); i++) {

                    double previousAverage =
                            (counts.get(i - 1)
                            + counts.get(i - 2)
                            + counts.get(i - 3)) / 3.0;

                    if (counts.get(i) >= 10
                            && counts.get(i) >= 2 * previousAverage) {

                        check.setString(1, months.get(i));

                        boolean alreadyExists = false;

                        try (ResultSet rs = check.executeQuery()) {
                            if (rs.next()) {
                                alreadyExists = rs.getInt(1) > 0;
                            }
                        }

                        if (!alreadyExists) {

                            String message =
                                    "Unusual increase in vulnerability additions";

                            insert.setString(1, months.get(i));
                            insert.setInt(2, counts.get(i));
                            insert.setDouble(3, previousAverage);
                            insert.setString(4, message);

                            insert.executeUpdate();

                            alerts++;
                        }

                        System.out.printf("%-12s %-12d %-15.2f %-15s%n",
                                months.get(i), counts.get(i),
                                previousAverage, "HIGH ALERT");
                    }
                }
            }

            System.out.println("-----------------------------------------------");
            System.out.println("New alerts generated: " + alerts);
            System.out.println("Existing alert statuses preserved!");
            System.out.println("Alerts saved successfully!");

        } catch (Exception e) {
            System.out.println("Early warning failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void displayAlerts() {

        String sql = "SELECT * FROM early_warnings ORDER BY alert_month";

        System.out.println("\n=================================");
        System.out.println("       STORED WARNING ALERTS");
        System.out.println("=================================");

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.printf("%-5s %-12s %-10s %-15s%n",
                    "ID", "MONTH", "COUNT", "STATUS");

            System.out.println("-----------------------------------------------");

            while (rs.next()) {

                System.out.printf("%-5d %-12s %-10d %-15s%n",
                        rs.getInt("id"),
                        rs.getString("alert_month"),
                        rs.getInt("vulnerability_count"),
                        rs.getString("status"));
            }

        } catch (Exception e) {
            System.out.println("Unable to display alerts: " + e.getMessage());
        }
    }
}
