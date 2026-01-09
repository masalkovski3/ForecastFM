package ForecastFM;

public class SearchResult {

    private final String spotifyJson;

    public SearchResult(String spotifyJson) {
        this.spotifyJson = spotifyJson;
    }

    public String getSpotifyJson() {
        return spotifyJson;
    }
}
