package weather;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * Step 2 of the pipeline: write the extracted rows into a SQLite table called raw_weather.
 */
public class Loader {

    /** Where the database file lives. SQLite creates it automatically if missing. */
    public static final String DB_URL = "jdbc:sqlite:data/weather.db";

    /**
     * Wipes the raw table and inserts the fresh rows.
     */
    public void load(List<WeatherRow> rows) throws Exception {
        try (Connection connection = java.sql.DriverManager.getConnection(DB_URL)) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS raw_weather");
                statement.execute("""
                        CREATE TABLE raw_weather (
                            city        TEXT,
                            date        TEXT,
                            max_temp_c  REAL,
                            min_temp_c  REAL,
                            rain_mm     REAL,
                            loaded_at   TEXT
                        )
                        """);
            }

            String insert = """
                    INSERT INTO raw_weather (city, date, max_temp_c, min_temp_c, rain_mm, loaded_at)
                    VALUES (?, ?, ?, ?, ?, datetime('now'))
                    """;

            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                for (WeatherRow row : rows) {
                    ps.setString(1, row.city());
                    ps.setString(2, row.date());
                    ps.setDouble(3, row.maxTempC());
                    ps.setDouble(4, row.minTempC());
                    ps.setDouble(5, row.rainMm());
                    ps.addBatch();   // queue it up instead of sending one at a time
                }
                ps.executeBatch();
            }
        }
        System.out.println("Loaded " + rows.size() + " rows into " + DB_URL);
    }
}