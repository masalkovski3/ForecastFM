package ForecastFM;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class WeatherService{
    private final String WEATHER_API_KEY = System.getenv("OPENWEATHER_API_KEY");
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final double DEFAULT_LAT = 55.6050;
    private static final double DEFAULT_LON = 13.0038;
    private static final String DEFAULT_CITY = "Malmö";
    
    
    public JsonNode getWeather(double lat, double lon) throws Exception {
        String weatherUrl = "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&appid=" + WEATHER_API_KEY
                + "&units=metric";

        HttpRequest weatherRequest = HttpRequest.newBuilder()
                .uri(URI.create(weatherUrl))
                .build();

        HttpResponse<String> response = client.send(weatherRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Fel vid hämtning av väder: " + response.statusCode());
            return null;
        }

        return mapper.readTree(response.body());
    }

    public WeatherDto getWeatherDto(double lat, double lon) throws Exception {
    JsonNode data = getWeather(lat, lon);

    if (data == null) {
        return null;
    }

    JsonNode weatherNode = data.get("weather").get(0);

    int weatherId = data.get("weather").get(0).get("id").asInt();
    String main = data.get("weather").get(0).get("main").asText();
    String description = data.get("weather").get(0).get("description").asText();
    String icon = weatherNode.get("icon").asText();
    double temperature = data.get("main").get("temp").asDouble();
    String city = (data.get("name") != null && !data.get("name").isNull()) ?
            data.get("name").asText() : "Unknown city";

    return new WeatherDto(weatherId, main, description, temperature, city, icon);
    }

    public WeatherDto getWeatherForMashup(double lat, double lon) throws Exception {
        JsonNode data = getWeather(lat, lon);
        if (data == null) {
            return null;
        }
        JsonNode weatherNode = data.get("weather").get(0);

        int weatherId = data.get("weather").get(0).get("id").asInt();
        String main = data.get("weather").get(0).get("main").asText();
        String description = data.get("weather").get(0).get("description").asText();
        double temperature = data.get("main").get("temp").asDouble();
        String city = (data.get("name") != null && !data.get("name").isNull()) ?
                data.get("name").asText() : "Unknown city";
        String icon = weatherNode.get("icon").asText();

        return new WeatherDto(weatherId, main, description, temperature, city, icon);
    }

        public WeatherDto getWeatherWithFallback(double lat, double lon) {
        try {
            WeatherDto weather = getWeatherForMashup(lat, lon);
            if (weather != null) {
                return weather;
            }
        } catch (Exception e) {
            System.out.println("Weather failed, falling back to Malmö");
        }

        try {
            WeatherDto fallback = getWeatherForMashup(DEFAULT_LAT, DEFAULT_LON);
            return new WeatherDto(
                    fallback.getWeatherId(),
                    fallback.getMain(),
                    fallback.getDescription(),
                    fallback.getTemperature(),
                    DEFAULT_CITY + " (fallback)",
                    fallback.getIcon()
            );
        } catch (Exception e) {
            return null; 
        }
    }
}



