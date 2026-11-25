
package cyberthreatdetection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TemporalAnalysis {

    public static void analyze() {

        String sql = "SELECT strftime('%Y-%m', date_added) AS month, "
                + "COUNT(*) AS total "
                + "FROM kev_vulnerabilities "
                + "WHERE date_added IS NOT NULL "
                + "GROUP BY month "
                + "ORDER BY month";

        int totalVulnerabilities = 0;
        int months = 0;
        int highestCount = 0;
        String highestMonth = "N/A";

        System.out.println("\n=================================");
        System.out.println("       TEMPORAL ANALYSIS");
        System.out.println("=================================");

        System.out.printf("%-15s %-15s%n", "MONTH", "VULNERABILITIES");
        System.out.println("---------------------------------");

        try (Connection conn = Database.connect()) {

            if (conn == null) {
                System.out.println("Database connection unavailable!");
                return;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    String month = rs.getString("month");
                    int count = rs.getInt("total");

                    System.out.printf("%-15s %-15d%n", month, count);

                    totalVulnerabilities += count;
                    months++;

                    if (count > highestCount) {
                        highestCount = count;
                        highestMonth = month;
                    }
                }
            }

            System.out.println("---------------------------------");
            System.out.println("Total vulnerabilities: "
                    + totalVulnerabilities);

            System.out.println("Months analyzed: " + months);

            if (months > 0) {

                double average = (double) totalVulnerabilities / months;

                System.out.printf("Average per month: %.2f%n", average);

                System.out.println("Highest activity month: "
                        + highestMonth);

                System.out.println("Highest monthly additions: "
                        + highestCount);
            }

        } catch (SQLException e) {
            System.out.println("Temporal analysis failed: "
                    + e.getMessage());
        }
    }
}
