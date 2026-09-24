package weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransformerTest {

    @BeforeEach
    void setUpRawWeatherTable() throws Exception {
        try (Connection connection = DriverManager.getConnection(Loader.DB_URL);
             Statement statement = connection.createStatement()) {

            statement.execute("DROP TABLE IF EXISTS raw_weather");
            statement.execute("""
                    CREATE TABLE raw_weather (
                        city       TEXT,
                        date       TEXT,
                        max_temp_c REAL,
                        min_temp_c REAL,
                        rain_mm    REAL,
                        loaded_at  TEXT
                    )
                    """);

            // Two real days for Cairo, plus one accidental duplicate row.
            statement.execute("""
                    INSERT INTO raw_weather VALUES
                        ('Cairo', '2026-09-20', 35.0, 22.0, 0.0, '2026-09-21'),
                        ('Cairo', '2026-09-20', 35.0, 22.0, 0.0, '2026-09-21'),
                        ('Cairo', '2026-09-21', 33.0, 20.0, 1.5, '2026-09-21')
                    """);
        }
    }

    @Test
    void removesDuplicateRows() throws Exception {
        new Transformer().transform();

        try (Connection connection = DriverManager.getConnection(Loader.DB_URL);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT COUNT(*) FROM clean_weather WHERE city = 'Cairo'")) {

            rs.next();
            // Should be 2 days, not 3 rows, because the duplicate was removed.
            assertEquals(2, rs.getInt(1));
        }
    }

    @Test
    void calculatesCorrectAverages() throws Exception {
        new Transformer().transform();

        try (Connection connection = DriverManager.getConnection(Loader.DB_URL);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT avg_max_temp_c, total_rain_mm FROM city_summary WHERE city = 'Cairo'")) {

            rs.next();
            // (35.0 + 33.0) / 2 = 34.0
            assertEquals(34.0, rs.getDouble("avg_max_temp_c"), 0.01);
            // 0.0 + 1.5 = 1.5
            assertEquals(1.5, rs.getDouble("total_rain_mm"), 0.01);
        }
    }
}