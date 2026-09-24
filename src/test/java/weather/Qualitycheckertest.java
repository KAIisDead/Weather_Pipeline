package weather;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityCheckerTest {

    @Test
    void passesWhenDataIsClean() throws Exception {
        setUpCleanWeatherTable("35.0");  // a normal temperature

        String output = captureCheckerOutput();

        assertTrue(output.contains("PASS: no missing temperatures"));
        assertTrue(output.contains("PASS: no duplicate city and date"));
        assertTrue(output.contains("PASS: temperatures are realistic"));
    }

    @Test
    void failsWhenTemperatureIsImpossible() throws Exception {
        setUpCleanWeatherTable("500.0");  // clearly not a real temperature

        String output = captureCheckerOutput();

        assertTrue(output.contains("FAIL: temperatures are realistic"));
    }

    /** Creates a one-row clean_weather table so we control exactly what QualityChecker sees. */
    private void setUpCleanWeatherTable(String maxTemp) throws Exception {
        try (Connection connection = DriverManager.getConnection(Loader.DB_URL);
             Statement statement = connection.createStatement()) {

            statement.execute("DROP TABLE IF EXISTS clean_weather");
            statement.execute("""
                    CREATE TABLE clean_weather (
                        city       TEXT,
                        date       TEXT,
                        max_temp_c REAL,
                        min_temp_c REAL,
                        rain_mm    REAL
                    )
                    """);
            statement.execute(
                    "INSERT INTO clean_weather VALUES ('Cairo', '2026-09-20', " + maxTemp + ", 20.0, 0.0)");
        }
    }

    /** Runs the quality checks and returns everything they printed, as one string. */
    private String captureCheckerOutput() throws Exception {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(captured));

        try {
            new QualityChecker().runChecks();
        } finally {
            System.setOut(original);  // always put the real console back
        }

        return captured.toString();
    }
}