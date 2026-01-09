package ForecastFM;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.net.URI;
import org.json.JSONObject;

public class SpotifyService {
    private String clientId;
    private String clientSecret;
    private HttpClient httpClient;
    private String accessToken;
    private JSONObject json;

    public SpotifyService(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String getAccessToken() throws IOException, InterruptedException {
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

    /**
     * Hämtar och returnerar alla genrer i API-et
     * @return lista med genrer.
     * @throws IOException
     * @throws InterruptedException
     */
    public String getGenres() throws IOException, InterruptedException {
        String token = getAccessToken();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/recommendations/available-genre-seeds"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * Hämtar en artist baserat på namn.
     * @param name
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getArtists(String name) throws IOException, InterruptedException {
        String token = getAccessToken();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/search?q=" + name + "&type=artist&limit=10"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * Hämtar recommendationer baserat på tempo och valence.
     * @param tempo
     * @param valence
     * @param limit
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getRecommendations(double tempo, double valence, double danceability, int limit) throws IOException, InterruptedException {
        String token = getAccessToken();

        String url = String.format(
                "https://api.spotify.com/v1/recommendations?seed_genres=%s&target_energy=%s&target_valence=%s&limit=%d",
                tempo,
                valence,
                danceability,
                limit
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
