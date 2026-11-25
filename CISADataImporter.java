
package cyberthreatdetection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CISADataImporter {

    public static void importData() {

        String filePath = "Dataset/known_exploited_vulnerabilities.json";

        String sql = "CREATE TABLE IF NOT EXISTS kev_vulnerabilities ("
                + "cve_id TEXT PRIMARY KEY,"
                + "vendor_project TEXT,"
                + "product TEXT,"
                + "vulnerability_name TEXT,"
                + "date_added TEXT,"
                + "description TEXT,"
                + "required_action TEXT,"
                + "due_date TEXT,"
                + "ransomware_use TEXT"
                + ")";

        try (Connection conn = Database.connect()) {

            if (conn == null) {
                System.out.println("Database unavailable!");
                return;
            }

            try (PreparedStatement create =
                    conn.prepareStatement(sql)) {
                create.execute();
            }

            String insertSQL = "INSERT OR REPLACE INTO kev_vulnerabilities "
                    + "(cve_id, vendor_project, product, "
                    + "vulnerability_name, date_added, description, "
                    + "required_action, due_date, ransomware_use) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (FileReader reader = new FileReader(filePath);
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                JsonArray vulnerabilities =
                        root.getAsJsonArray("vulnerabilities");

                int count = 0;

                for (int i = 0; i < vulnerabilities.size(); i++) {

                    JsonObject threat = vulnerabilities.get(i).getAsJsonObject();

                    pstmt.setString(1, threat.get("cveID").getAsString());
                    pstmt.setString(2, threat.get("vendorProject").getAsString());
                    pstmt.setString(3, threat.get("product").getAsString());
                    pstmt.setString(4, threat.get("vulnerabilityName").getAsString());
                    pstmt.setString(5, threat.get("dateAdded").getAsString());
                    pstmt.setString(6, threat.get("shortDescription").getAsString());
                    pstmt.setString(7, threat.get("requiredAction").getAsString());
                    pstmt.setString(8, threat.get("dueDate").getAsString());

                    String ransomware = threat.has("knownRansomwareCampaignUse")
                            ? threat.get("knownRansomwareCampaignUse").getAsString()
                            : "Unknown";

                    pstmt.setString(9, ransomware);

                    pstmt.executeUpdate();
                    count++;
                }

                System.out.println("Import completed successfully!");
                System.out.println("Vulnerabilities imported: " + count);
            }

        } catch (Exception e) {
            System.out.println("Import failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
