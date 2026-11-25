
package cyberthreatdetection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;

public class ThreatForecasting {

    private static final double ALPHA = 0.4;

    public static void forecast() {

        String sql = "SELECT strftime('%Y-%m', date_added) AS month, "
                + "COUNT(*) AS total "
                + "FROM kev_vulnerabilities "
                + "WHERE date_added IS NOT NULL "
                + "GROUP BY month "
                + "ORDER BY month";

        ArrayList<String> months = new ArrayList<>();
        ArrayList<Integer> counts = new ArrayList<>();

        System.out.println("\n=================================");
        System.out.println("       THREAT FORECASTING");
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

            int n = counts.size();

            if (n < 3) {
                System.out.println("Insufficient historical data!");
                return;
            }

            // Calculate Moving Average and
            // Exponential Smoothing errors.

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

            double movingMAE = movingError / comparisons;
            double smoothingMAE = smoothingError / comparisons;

            System.out.println("\nMODEL COMPARISON");
            System.out.println("---------------------------------");

            System.out.printf("Moving Average MAE: %.2f%n", movingMAE);
            System.out.printf("Exponential Smoothing MAE: %.2f%n",
                    smoothingMAE);

            if (movingMAE < smoothingMAE) {
                System.out.println("Better model: Moving Average");
            } else if (smoothingMAE < movingMAE) {
                System.out.println("Better model: Exponential Smoothing");
            } else {
                System.out.println("Both models have equal MAE.");
            }

            // Forecast the next three months.

            System.out.println("\nNEXT THREE MONTHS FORECAST");
            System.out.println("---------------------------------");

            YearMonth lastMonth =
                    YearMonth.parse(months.get(n - 1));

            ArrayList<Double> movingValues = new ArrayList<>();

            for (int value : counts) {
                movingValues.add((double) value);
            }

            double smoothingForecast = level;

            for (int i = 1; i <= 3; i++) {

                double movingForecast = 0;

                int size = movingValues.size();

                for (int j = size - 3; j < size; j++) {
                    movingForecast += movingValues.get(j);
                }

                movingForecast /= 3.0;

                YearMonth futureMonth = lastMonth.plusMonths(i);

                System.out.printf("%-12s%n", futureMonth);

                System.out.printf(
                        "Moving Average Forecast: %.2f%n",
                        movingForecast);

                System.out.printf(
                        "Exponential Smoothing Forecast: %.2f%n",
                        smoothingForecast);

                movingValues.add(movingForecast);

                System.out.println();
            }

            System.out.println("---------------------------------");
            System.out.println("Forecasting completed successfully!");

        } catch (SQLException e) {
            System.out.println("Forecasting failed: " + e.getMessage());
        }
    }
}
