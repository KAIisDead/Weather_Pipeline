package weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Step 1 of the pipeline: download daily weather for each city from the Open-Meteo API.
 * No API key is needed.
 */
public class Extractor {

    /** The cities we track, each with its latitude and longitude. */
    private static final Map<String, double[]> CITIES = new LinkedHashMap<>();

    static {
        CITIES.put("Johannesburg", new double[]{-26.20, 28.04});
        CITIES.put("Cape Town", new double[]{-33.92, 18.42});
        CITIES.put("Durban", new double[]{-29.86, 31.02});
        CITIES.put("Nairobi", new double[]{-1.29, 36.82});
        CITIES.put("Cairo", new double[]{30.04, 31.24});
    }

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Fetches every city and returns all the rows in one list.
     */
    public List<WeatherRow> extractAll() throws Exception {
        List<WeatherRow> rows = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : CITIES.entrySet()) {
            System.out.println("Fetching " + entry.getKey() + "...");
            rows.addAll(extractCity(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }
        System.out.println("Extracted " + rows.size() + " rows");
        return rows;
    }

    /**
     * Calls the API for one city and turns the JSON answer into WeatherRow objects.
     */
    private List<WeatherRow> extractCity(String city, double lat, double lon) throws Exception {
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum"
                + "&past_days=7&forecast_days=1&timezone=auto";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API call failed for " + city + ": " + response.statusCode());
        }

        // "daily" holds four parallel lists: dates, max temps, min temps, rainfall.
        JsonNode daily = mapper.readTree(response.body()).get("daily");
        JsonNode dates = daily.get("time");
        JsonNode maxTemps = daily.get("temperature_2m_max");
        JsonNode minTemps = daily.get("temperature_2m_min");
        JsonNode rainfall = daily.get("precipitation_sum");

        List<WeatherRow> rows = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            rows.add(new WeatherRow(
                    city,
                    dates.get(i).asText(),
                    maxTemps.get(i).asDouble(),
                    minTemps.get(i).asDouble(),
                    rainfall.get(i).asDouble()
            ));
        }
        return rows;
    }
}