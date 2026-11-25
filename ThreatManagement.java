
package cyberthreatdetection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ThreatManagement {

    public static void manage() {

        BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));

        try {

            int choice;

            do {

                System.out.println("\n=================================");
                System.out.println("       THREAT MANAGEMENT");
                System.out.println("=================================");
                System.out.println("1. View All Alerts");
                System.out.println("2. Search Alert by Month");
                System.out.println("3. Update Alert Status");
                System.out.println("4. Exit");
                System.out.println("---------------------------------");
                System.out.print("Enter your choice: ");

                choice = Integer.parseInt(br.readLine());

                switch (choice) {

                    case 1:
                        viewAlerts();
                        break;

                    case 2:
                        System.out.print("Enter month (YYYY-MM): ");
                        String month = br.readLine();
                        searchAlert(month);
                        break;

                    case 3:
                        System.out.print("Enter Alert ID: ");
                        int id = Integer.parseInt(br.readLine());

                        System.out.println("1. NEW");
                        System.out.println("2. INVESTIGATING");
                        System.out.println("3. RESOLVED");
                        System.out.print("Choose new status: ");

                        int statusChoice =
                                Integer.parseInt(br.readLine());

                        String status = null;

                        switch (statusChoice) {
                            case 1:
                                status = "NEW";
                                break;

                            case 2:
                                status = "INVESTIGATING";
                                break;

                            case 3:
                                status = "RESOLVED";
                                break;

                            default:
                                System.out.println("Invalid status choice!");
                                break;
                        }

                        if (statusChoice >= 1 && statusChoice <= 3) {
                            updateStatus(id, status);
                        }

                        break;

                    case 4:
                        System.out.println("Exiting Threat Management...");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 4);

        } catch (Exception e) {
            System.out.println("Input error: " + e.getMessage());
        }
    }

    public static void viewAlerts() {

        String sql = "SELECT * FROM early_warnings ORDER BY alert_month";

        System.out.println("\n========== ALL THREAT ALERTS ==========");

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
            System.out.println("Unable to view alerts: " + e.getMessage());
        }
    }

    public static void searchAlert(String month) {

        String sql = "SELECT * FROM early_warnings "
                + "WHERE alert_month LIKE ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + month + "%");

            try (ResultSet rs = pstmt.executeQuery()) {

                boolean found = false;

                System.out.println("\n========== SEARCH RESULTS ==========");

                while (rs.next()) {

                    found = true;

                    System.out.println("Alert ID: " + rs.getInt("id"));
                    System.out.println("Month: " + rs.getString("alert_month"));
                    System.out.println("Vulnerabilities: "
                            + rs.getInt("vulnerability_count"));
                    System.out.println("Message: "
                            + rs.getString("alert_message"));
                    System.out.println("Status: "
                            + rs.getString("status"));

                    System.out.println("---------------------------------");
                }

                if (!found) {
                    System.out.println("No matching alerts found!");
                }
            }

        } catch (Exception e) {
            System.out.println("Search failed: " + e.getMessage());
        }
    }

    public static void updateStatus(int id, String status) {

        String sql = "UPDATE early_warnings SET status = ? WHERE id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, id);

            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                System.out.println("Alert status updated successfully!");
                System.out.println("New status: " + status);
            } else {
                System.out.println("Alert ID not found!");
            }

        } catch (Exception e) {
            System.out.println("Status update failed: " + e.getMessage());
        }
    }
}

