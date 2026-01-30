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

    public List<String> getTracksFromMood(MoodProfile mood) throws IOException, InterruptedException {

        List<String> genreTracks = getTracksFromGenresCombined(mood,  10);

        List<String> moodTracks = new ArrayList<>();
        for (String track : genreTracks) {
            TrackDto trackDto = getTrackByName(track);
            if (trackDto == null) continue;

            if (moodMatches(mood, trackDto)) {
                moodTracks.add(track);
            }
        }

        if (moodTracks.isEmpty()) {
            return genreTracks.stream().distinct().limit(10).toList();
        }

        return moodTracks.stream().distinct().limit(10).toList();
    }

    public List<String> getTracksFromGenresCombined(MoodProfile mood, int limit)
            throws IOException, InterruptedException {

        String token = getAccessToken();
        String query = String.join(" ", mood.getSeedGenres()) + "music";
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = "https://api.spotify.com/v1/search"
                + "?q=" + q
                + "&type=track"
                + "&limit=" + limit
                + "&market=SE";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .header("User-Agent", "ForecastFM/1.0")
                .GET()
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new RuntimeException("Spotify search failed, HTTP " + res.statusCode() + ": " + res.body());
        }

        JSONObject root = new JSONObject(res.body());
        JSONArray items = root.getJSONObject("tracks").getJSONArray("items");

        List<String> out = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject t = items.getJSONObject(i);

            String trackName = t.optString("name", "Unknown track");

            JSONArray artistsArr = t.optJSONArray("artists");
            String artistName = "Unknown artist";
            if (artistsArr != null && artistsArr.length() > 0) {
                artistName = artistsArr.getJSONObject(0).optString("name", artistName);
            }

            out.add(trackName + " – " + artistName);
        }


        return out.stream().distinct().limit(limit).toList();
    }

    public TrackDto getTrackByName(String trackName) throws IOException, InterruptedException {
        String token = getAccessToken();

        String query = URLEncoder.encode("track:\"" + trackName + "\"", StandardCharsets.UTF_8);

        String url = "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=1&market=SE";


        HttpRequest searchRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(searchRequest, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());
        JSONArray items = json.getJSONObject("tracks").getJSONArray("items");

        if (items.isEmpty()) {
            return null;
        }

        JSONObject trackJson = items.getJSONObject(0);
        String name = trackJson.getString("name");
        String artist = trackJson.getJSONArray("artists").getJSONObject(0).getString("name");
        String urlTrack = trackJson.getJSONObject("external_urls").getString("spotify");
        String previewUrl = trackJson.optString("preview_url", null);
        String trackId = trackJson.getString("id");
        System.out.println(getTrackNameById(trackId));

        String featuresUrl = "https://api.spotify.com/v1/audio-features/" + trackId;
        HttpRequest featuresRequest = HttpRequest.newBuilder()
                .uri(URI.create(featuresUrl))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> featuresResponse = httpClient.send(featuresRequest, HttpResponse.BodyHandlers.ofString());
        JSONObject featuresJson = new JSONObject(featuresResponse.body());

        double valence;
        double danceability;
        double energy;
        if (featuresJson.has("valence")) {
            valence = featuresJson.getDouble("valence");
        } else {
            return null;
        }

        if (featuresJson.has("danceability")) {
            danceability = featuresJson.getDouble("danceability");
        } else {
            return null;
        }

        if (featuresJson.has("energy")) {
            energy = featuresJson.getDouble("energy");
        } else {
            return null;
        }

        return new TrackDto(name, artist, urlTrack, previewUrl, valence, danceability, energy);
    }

    public boolean moodMatches(MoodProfile mood, TrackDto trackDto) throws IOException, InterruptedException {
        double tolerance = 0.30;
        return Math.abs(trackDto.getValence() - mood.getValence()) <= tolerance &&
                Math.abs(trackDto.getDanceability() - mood.getDanceability()) <= tolerance &&
                Math.abs(trackDto.getEnergy() - mood.getEnergy()) <= tolerance;
    }

    /**
     * Hämtar låtnamnet för en given Spotify track ID.
     * Returnerar null om track inte finns.
     */
    public String getTrackNameById(String trackId) throws IOException, InterruptedException {
        String token = getAccessToken();

        String trackUrl = "https://api.spotify.com/v1/tracks/" + trackId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trackUrl))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        JSONObject trackJson = new JSONObject(response.body());
        return trackJson.optString("name", null);
    }

}
