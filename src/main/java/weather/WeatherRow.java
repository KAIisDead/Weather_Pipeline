package weather;

public record WeatherRow(
        String city,
        String date,
        double maxTempC,
        double minTempC,
        double rainMm
) {
}