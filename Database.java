
package cyberthreatdetection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class Database {

    private static final String URL = "jdbc:sqlite:cyberthreats.db";

    public static Connection connect() {

        try {
            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection(URL);

            System.out.println("Database connected successfully!");

            return conn;

        } catch (Exception e) {

            System.out.println("Database connection failed!");
            e.printStackTrace();

            return null;
        }
    }

    public static void createTables() {

        String sql = "CREATE TABLE IF NOT EXISTS threats ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "threat_name TEXT NOT NULL,"
                + "category TEXT,"
                + "date_added TEXT,"
                + "severity TEXT,"
                + "description TEXT"
                + ");";

        try (Connection conn = connect()) {

            if (conn == null) {
                System.out.println("Cannot create table: database connection unavailable.");
                return;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("Threats table created successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
