package weather;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class QualityChecker {

    public void runChecks() throws Exception {
        try (Connection connection = DriverManager.getConnection(Loader.DB_URL);
             Statement statement = connection.createStatement()) {

            check(statement, "no missing temperatures",
                    "SELECT COUNT(*) FROM clean_weather WHERE max_temp_c IS NULL");

            check(statement, "no duplicate city and date",
                    """
                    SELECT COUNT(*) FROM (
                        SELECT city, date FROM clean_weather
                        GROUP BY city, date HAVING COUNT(*) > 1
                    )
                    """);

            check(statement, "temperatures are realistic",
                    "SELECT COUNT(*) FROM clean_weather WHERE max_temp_c > 60 OR max_temp_c < -40");
        }
    }

    /**
     * Runs a query that counts bad rows. Zero means the check passed.
     */
    private void check(Statement statement, String name, String sql) throws Exception {
        try (ResultSet rs = statement.executeQuery(sql)) {
            int badRows = rs.getInt(1);
            System.out.println((badRows == 0 ? "PASS: " : "FAIL: ") + name
                    + (badRows == 0 ? "" : " (" + badRows + " bad rows)"));
        }
    }
}