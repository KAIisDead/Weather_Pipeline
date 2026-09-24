package weather;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherRowTest {

    @Test
    void storesAllFieldsCorrectly() {
        WeatherRow row = new WeatherRow("Cairo", "2026-09-20", 35.0, 22.0, 0.0);

        assertEquals("Cairo", row.city());
        assertEquals("2026-09-20", row.date());
        assertEquals(35.0, row.maxTempC());
        assertEquals(22.0, row.minTempC());
        assertEquals(0.0, row.rainMm());
    }
}