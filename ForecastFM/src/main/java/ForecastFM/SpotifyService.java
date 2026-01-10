package ForecastFM;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.net.URI;
import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class SpotifyService {
    private final String clientId = System.getenv("SPOTIFY_CLIENT_ID");
    private final String clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET");
    private HttpClient httpClient = HttpClient.newHttpClient();
    private String accessToken;
    private JSONObject json;

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
            System.out.println("Spotify token status: " + response.statusCode());
            System.out.println("Spotify token body: " + response.body());
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
     *
     * @param seedGenres
     * @param tempo
     * @param valence
     * @param limit
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String getRecommendations(List<String> seedGenres, double tempo, double valence, double danceability, int limit) throws IOException, InterruptedException {
        String token = getAccessToken();

        String seedGenresParam = URLEncoder.encode(String.join(",", seedGenres), StandardCharsets.UTF_8);

        String url = String.format(
                "https://api.spotify.com/v1/recommendations?seed_genres=%s&target_energy=%s&target_valence=%s&target_danceability=%s&limit=%d",
                seedGenresParam,
                tempo,
                valence,
                danceability,
                limit
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public SearchResult searchTracks(MoodProfile mood, int limit) throws IOException, InterruptedException {
        String response = getRecommendations(mood.getSeedGenres(), mood.getTempo(), mood.getValence(), mood.getDanceability(), limit);
        return new SearchResult(response);
    }
}
