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
import java.util.SortedMap;

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
                mood.getEnergy(),
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

            result.add(new TrackDto(name, artist, url, previewUrl, 0, 0, 0));
        }

        return result;
    }

    public String testSearch(MoodProfile mood) throws IOException, InterruptedException {
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

    public List<String> getTracksFromMood(MoodProfile mood) throws IOException, InterruptedException {
        List<String> genreTracks = new ArrayList<>();
        for (String genre : mood.getSeedGenres()) {
            List<String> result = getTrackNames(genre);

            for (String r : result) {
                genreTracks.add(r);
            }
        }

        List<String> moodTracks = new ArrayList<>();
        for (String track : genreTracks) {
            TrackDto trackDto = getTrackByName(track);

            if (trackDto == null) {
                continue;
            }

            if (moodMatches(mood, trackDto)) {
                moodTracks.add(track);
            }
        }

        return moodTracks;
    }

    public String getTracksFromGenre(String genre) throws IOException, InterruptedException {
        String token = getAccessToken();
        String url = "https://api.spotify.com/v1/search"
                + "?q=" + URLEncoder.encode("genre:" + genre, StandardCharsets.UTF_8)
                + "&type=track"
                + "&limit=10"
                + "&market=SE";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .header("User-Agent", "ForecastFM/1.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Spotify search failed: " + response.statusCode());
        }

        return response.body();
    }

    public List<String> getTrackNames(String genre) throws IOException, InterruptedException {
        String songs = getTracksFromGenre(genre);
        List<String> names = new ArrayList<>();

        JSONObject obj = new JSONObject(songs);
        JSONObject tracks = obj.getJSONObject("tracks");
        JSONArray items = tracks.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject track = items.getJSONObject(i);
            String name = track.getString("name");
            names.add(name);
        }

        return names;
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
        double tolerance = 0.15;
        return Math.abs(trackDto.getValence() - mood.getValence()) <= tolerance &&
                Math.abs(trackDto.getDanceability() - mood.getDanceability()) <= tolerance &&
                Math.abs(trackDto.getEnergy() - mood.getEnergy()) <= tolerance;
    }

    /**
     * Hämtar en TrackDto baserat på Spotify track ID.
     * Returnerar null om track inte finns eller audio-features saknas.
     */
    public TrackDto getTrackById(String trackId) throws IOException, InterruptedException {
        String token = getAccessToken();

        // 1. Hämta track-info
        String trackUrl = "https://api.spotify.com/v1/tracks/" + trackId;

        HttpRequest trackRequest = HttpRequest.newBuilder()
                .uri(URI.create(trackUrl))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> trackResponse = httpClient.send(trackRequest, HttpResponse.BodyHandlers.ofString());

        if (trackResponse.statusCode() != 200) {
            return null; // Track finns inte
        }

        JSONObject trackJson = new JSONObject(trackResponse.body());

        String name = trackJson.optString("name", "Unknown track");

        JSONArray artistsArr = trackJson.optJSONArray("artists");
        String artist = "Unknown artist";
        if (artistsArr != null && artistsArr.length() > 0) {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < artistsArr.length(); i++) {
                String an = artistsArr.getJSONObject(i).optString("name", "");
                if (!an.isBlank()) names.add(an);
            }
            if (!names.isEmpty()) {
                artist = String.join(", ", names);
            }
        }

        String urlTrack = trackJson.optJSONObject("external_urls") != null ?
                trackJson.getJSONObject("external_urls").optString("spotify", "#") : "#";
        String previewUrl = trackJson.optString("preview_url", null);

        // 2. Hämta audio-features
        String featuresUrl = "https://api.spotify.com/v1/audio-features/" + trackId;

        HttpRequest featuresRequest = HttpRequest.newBuilder()
                .uri(URI.create(featuresUrl))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> featuresResponse = httpClient.send(featuresRequest, HttpResponse.BodyHandlers.ofString());

        if (featuresResponse.statusCode() != 200) {
            return null; // Audio-features finns inte
        }

        JSONObject featuresJson = new JSONObject(featuresResponse.body());

        // Säker hämtning av valence, danceability, energy
        double valence = featuresJson.optDouble("valence", -1);
        double danceability = featuresJson.optDouble("danceability", -1);
        double energy = featuresJson.optDouble("energy", -1);

        if (valence < 0 || danceability < 0 || energy < 0) {
            return null; // Saknade värden
        }

        return new TrackDto(name, artist, urlTrack, previewUrl, valence, danceability, energy);
    }

    /**
     * Hämtar låtnamnet för en given Spotify track ID.
     * Returnerar null om track inte finns.
     */
    public String getTrackNameById(String trackId) throws IOException, InterruptedException {
        String token = getAccessToken(); // Din metod för access token

        String trackUrl = "https://api.spotify.com/v1/tracks/" + trackId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trackUrl))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null; // Track finns inte
        }

        JSONObject trackJson = new JSONObject(response.body());
        return trackJson.optString("name", null); // Returnerar låtnamn eller null
    }

}
