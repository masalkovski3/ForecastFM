package ForecastFM;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.net.URI;
import org.json.JSONObject;

public class SpotifyApiEndpoints {
    private String clientId;
    private String clientSecret;
    private HttpClient httpClient;
    private String accessToken;
    private JSONObject json;

    public SpotifyApiEndpoints(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String authenticate() throws IOException, InterruptedException {
        String a = clientId + ":" + clientSecret;
        String encodedA = Base64.getEncoder().encodeToString(a.getBytes(StandardCharsets.UTF_8));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + encodedA)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            json = new JSONObject(response.body());
            this.accessToken = json.getString("access_token");
            return json.getString("access_token");
        } else {
            throw new RuntimeException("Spotify API returned HTTP status code " + response.statusCode());
        }
    }
}
