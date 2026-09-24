package weather;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Transformer {

    private static final String CLEAN_SQL = """
            DROP TABLE IF EXISTS clean_weather;
            CREATE TABLE clean_weather AS
            SELECT city, date,
                   MAX(max_temp_c) AS max_temp_c,
                   MIN(min_temp_c) AS min_temp_c,
                   MAX(rain_mm)    AS rain_mm
            FROM raw_weather
            GROUP BY city, date;
            """;

    private static final String SUMMARY_SQL = """
            DROP TABLE IF EXISTS city_summary;
            CREATE TABLE city_summary AS
            SELECT city,
                   ROUND(AVG(max_temp_c), 1) AS avg_max_temp_c,
                   ROUND(AVG(min_temp_c), 1) AS avg_min_temp_c,
                   ROUND(SUM(rain_mm), 1)    AS total_rain_mm,
                   COUNT(*)                  AS days_recorded
            FROM clean_weather
            GROUP BY city
            ORDER BY avg_max_temp_c DESC;
            """;

    public void transform() throws Exception {
        try (Connection connection = DriverManager.getConnection(Loader.DB_URL);
             Statement statement = connection.createStatement()) {

            // SQLite runs one statement at a time, so split on the semicolons.
            for (String sql : (CLEAN_SQL + SUMMARY_SQL).split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
        System.out.println("Built clean_weather and city_summary");
    }
}