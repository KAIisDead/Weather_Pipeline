package weather;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class Pipeline {

    public static void main(String[] args) throws Exception {
        List<WeatherRow> rows = new Extractor().extractAll();
        new Loader().load(rows);
        new Transformer().transform();
        new QualityChecker().runChecks();
        printSummary();
        System.out.println("Pipeline finished.");
    }

    /** Prints the city_summary table as a neat console table. */
    private static void printSummary() throws Exception {
        System.out.printf("%n%-15s %10s %10s %10s%n", "CITY", "AVG MAX", "AVG MIN", "RAIN mm");
        try (Connection connection = DriverManager.getConnection(Loader.DB_URL);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM city_summary")) {

            while (rs.next()) {
                System.out.printf("%-15s %10.1f %10.1f %10.1f%n",
                        rs.getString("city"),
                        rs.getDouble("avg_max_temp_c"),
                        rs.getDouble("avg_min_temp_c"),
                        rs.getDouble("total_rain_mm"));
            }
        }
    }
}