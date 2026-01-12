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
    private final String WEATHER_API_KEY = System.getenv("OPENWEATHER_API_KEY");
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    
    // Funktion för reverse geocoding: lat/lon → stad
    public String getCityFromLatLon(double lat, double lon) throws Exception {
        String reverseUrl = "https://api.openweathermap.org/geo/1.0/reverse"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&limit=1"
                + "&appid=" + WEATHER_API_KEY;

        HttpRequest geoRequest = HttpRequest.newBuilder()
                .uri(URI.create(reverseUrl))
                .build();

        HttpResponse<String> geoResponse = client.send(geoRequest, HttpResponse.BodyHandlers.ofString());

        if (geoResponse.statusCode() != 200) {
            return "unknown location";
        }
        JsonNode geoRoot = mapper.readTree(geoResponse.body());

        if(!geoRoot.isArray()){
            return "unknown location";
        }

        if (geoRoot.isEmpty()) {
            return "Okänd plats";
        }

        JsonNode first = geoRoot.get(0);
        JsonNode nameNode = first.get("name");

        if (nameNode == null || nameNode.isNull()) {
            return "Okänd plats";
        }

        return nameNode.asText();
        //return geoRoot.get(0).get("name").asText();
    }

    // Ny funktion: Hämta väderdata för lat/lon
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
    // Hämta väderdata som JsonNode
    JsonNode data = getWeather(lat, lon);

    if (data == null) {
        return null; // eller kasta exception beroende på hur du vill hantera fel
    }

    JsonNode weatherNode = data.get("weather").get(0);

    // Extrahera relevant info
    int weatherId = data.get("weather").get(0).get("id").asInt();
    String main = data.get("weather").get(0).get("main").asText();
    String description = data.get("weather").get(0).get("description").asText();
    String icon = weatherNode.get("icon").asText();
    double temperature = data.get("main").get("temp").asDouble();
    String city = (data.get("name") != null && !data.get("name").isNull()) ?
            data.get("name").asText() : "Unknown city";


    // Returnera ett WeatherDto-objekt
    return new WeatherDto(weatherId, main, description, temperature, city, icon);
    }

    public WeatherDto getWeatherForMashup(double lat, double lon) throws Exception {
        // Hämta JsonNode
        JsonNode data = getWeather(lat, lon);
        if (data == null) {
            return null;
        }
        JsonNode weatherNode = data.get("weather").get(0);

        // Mappa till WeatherDto
        int weatherId = data.get("weather").get(0).get("id").asInt();
        String main = data.get("weather").get(0).get("main").asText();
        String description = data.get("weather").get(0).get("description").asText();
        double temperature = data.get("main").get("temp").asDouble();
        String city = (data.get("name") != null && !data.get("name").isNull()) ?
                data.get("name").asText() : "Unknown city";
        String icon = weatherNode.get("icon").asText();

        return new WeatherDto(weatherId, main, description, temperature, city, icon);
    }
}



