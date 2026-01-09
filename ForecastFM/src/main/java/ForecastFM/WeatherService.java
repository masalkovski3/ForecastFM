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
        // API-nyckel på klassnivå
    private static final String WEATHER_API_KEY = System.getenv("OPENWEATHER_API_KEY");
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
        public static void main(String[]args) throws Exception {

        // Hårdkodad lat/lon (exempel Malmö)
        double lat = 55.6059;
        double lon = 13.0007;

        // Reverse geocoding för att få stad
        String city = getCityFromLatLon(lat, lon);
        System.out.println("Upptäckt stad: " + city);

        // Hämta väderdata med funktion
        JsonNode weatherData = getWeather(lat, lon);

        if (weatherData != null) {
            // Temperatur
            int temp = (int) weatherData.get("main").get("temp").asDouble();
            System.out.println("Temperatur: " + temp + "°C");

            // Väderbeskrivning
            String weather = weatherData.get("weather").get(0).get("description").asText();
            System.out.println("Väder: " + weather);

            // Vind
            int wind = (int) weatherData.get("wind").get("speed").asDouble();
            System.out.println("Vind: " + wind + " m/sekund");

            // Ikon
            String icon = weatherData.get("weather").get(0).get("icon").asText();
            System.out.println("Ikon: " + icon);
        }
    }

    // Funktion för reverse geocoding: lat/lon → stad
    public static String getCityFromLatLon(double lat, double lon) throws Exception {
        String reverseUrl = "https://api.openweathermap.org/geo/1.0/reverse"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&limit=1"
                + "&appid=" + WEATHER_API_KEY;

        HttpRequest geoRequest = HttpRequest.newBuilder()
                .uri(URI.create(reverseUrl))
                .build();

        HttpResponse<String> geoResponse = client.send(geoRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode geoRoot = mapper.readTree(geoResponse.body());

        if (geoRoot.isEmpty()) {
            return "Okänd plats";
        }

        return geoRoot.get(0).get("name").asText();
    }

    // Ny funktion: Hämta väderdata för lat/lon
    public static JsonNode getWeather(double lat, double lon) throws Exception {
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
}
