package weather;

/**
 * One day of weather for one city.
 * A record is a short way of writing a class that just holds data.
 */
public record WeatherRow(
        String city,
        String date,
        double maxTempC,
        double minTempC,
        double rainMm
) {
}