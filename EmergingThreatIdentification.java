
package cyberthreatdetection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class EmergingThreatIdentification {

    public static void identify() {

        String sql = "SELECT strftime('%Y-%m', date_added) AS month, "
                + "COUNT(*) AS total "
                + "FROM kev_vulnerabilities "
                + "WHERE date_added IS NOT NULL "
                + "GROUP BY month "
                + "ORDER BY month";

        ArrayList<String> months = new ArrayList<>();
        ArrayList<Integer> counts = new ArrayList<>();

        System.out.println("\n=================================");
        System.out.println("   EMERGING THREAT IDENTIFICATION");
        System.out.println("=================================");

        try (Connection conn = Database.connect()) {

            if (conn == null) {
                System.out.println("Database unavailable!");
                return;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    months.add(rs.getString("month"));
                    counts.add(rs.getInt("total"));
                }
            }

            int alerts = 0;

            System.out.printf("%-12s %-12s %-15s %-15s%n",
                    "MONTH", "ADDITIONS", "PREV. AVERAGE", "STATUS");

            System.out.println("-----------------------------------------------");

            for (int i = 0; i < months.size(); i++) {

                if (i < 3) {
                    System.out.printf("%-12s %-12d %-15s %-15s%n",
                            months.get(i), counts.get(i), "N/A", "BASELINE");
                    continue;
                }

                double previousAverage =
                        (counts.get(i - 1)
                        + counts.get(i - 2)
                        + counts.get(i - 3)) / 3.0;

                boolean unusualIncrease =
                        counts.get(i) >= 10
                        && counts.get(i) >= 2 * previousAverage;

                if (unusualIncrease) {

                    System.out.printf("%-12s %-12d %-15.2f %-15s%n",
                            months.get(i), counts.get(i),
                            previousAverage, "SPIKE DETECTED");

                    alerts++;

                } else {

                    System.out.printf("%-12s %-12d %-15.2f %-15s%n",
                            months.get(i), counts.get(i),
                            previousAverage, "NORMAL");
                }
            }

            System.out.println("-----------------------------------------------");
            System.out.println("Total unusual increases detected: " + alerts);

        } catch (SQLException e) {
            System.out.println("Identification failed: " + e.getMessage());
        }
    }
}
