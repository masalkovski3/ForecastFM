package ForecastFM;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.net.URI;
import java.util.List;

import org.json.JSONArray;
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

        String seedGenresParam = String.join(",", seedGenres);
        if (seedGenresParam.isBlank()) {
            throw new RuntimeException("No seed genres provided (seedGenres was empty)");
        }

        String url = "https://api.spotify.com/v1/recommendations"
                + "?seed_genres=" + seedGenresParam
                + "&target_tempo=" + tempo
                + "&target_valence=" + valence
                + "&target_danceability=" + danceability
                + "&limit=" + limit
                + "&market=SE";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public List<TrackDto> searchTracks(MoodProfile mood, int limit) throws IOException, InterruptedException {
        String response = getRecommendations(
                mood.getSeedGenres(),
                mood.getTempo(),
                mood.getValence(),
                mood.getDanceability(),
                limit
        );
        String trimmed = response == null ? "" : response.trim();
        if (!trimmed.startsWith("{")) {
            throw new RuntimeException("Expected JSON object from Spotify, got: " +
                    trimmed.substring(0, Math.min(300, trimmed.length())));
        }

        JSONObject root = new JSONObject(trimmed);

        //JSONObject root = new JSONObject(response);
        JSONArray tracks = root.optJSONArray("tracks");
        List<TrackDto> result = new ArrayList<>();

        if (tracks == null) {
            return result;
        }

        for (int i = 0; i < tracks.length(); i++) {
            JSONObject t = tracks.getJSONObject(i);

            String name = t.optString("name", "Unknown track");

            // artists: [{name: ...}, ...]
            JSONArray artistsArr = t.optJSONArray("artists");
            String artist = "Unknown artist";
            if (artistsArr != null && artistsArr.length() > 0) {
                List<String> names = new ArrayList<>();
                for (int a = 0; a < artistsArr.length(); a++) {
                    String an = artistsArr.getJSONObject(a).optString("name", "");
                    if (!an.isBlank()) names.add(an);
                }
                if (!names.isEmpty()) {
                    artist = String.join(", ", names);
                }
            }

            String url = "#";
            JSONObject externalUrls = t.optJSONObject("external_urls");
            if (externalUrls != null) {
                url = externalUrls.optString("spotify", "#");
            }

            // preview_url kan vara null
            String previewUrl = t.isNull("preview_url") ? null : t.optString("preview_url", null);

            result.add(new TrackDto(name, artist, url, previewUrl));
        }

        return result;
    }

    public String testSearch() throws IOException, InterruptedException {
        String token = getAccessToken();

        String url = "https://api.spotify.com/v1/search?q=abba&type=track&limit=1&market=SE";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .header("User-Agent", "ForecastFM/1.0")   // viktig i vissa edge-miljöer
                .GET()
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        System.out.println("Spotify search URI: " + res.uri());
        System.out.println("Spotify search status: " + res.statusCode());
        System.out.println("Spotify search headers: " + res.headers().map());
        System.out.println("Spotify search body length: " + (res.body() == null ? "null" : res.body().length()));

        return res.body();
    }

}
