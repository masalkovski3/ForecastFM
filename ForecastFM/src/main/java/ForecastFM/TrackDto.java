package ForecastFM;

public class TrackDto {
    private final String name;
    private final String artist;
    private final String url;
    private final String previewUrl; // kan vara null

    public TrackDto(String name, String artist, String url, String previewUrl) {
        this.name = name;
        this.artist = artist;
        this.url = url;
        this.previewUrl = previewUrl;
    }

    public String getName() { return name; }
    public String getArtist() { return artist; }
    public String getUrl() { return url; }
    public String getPreviewUrl() { return previewUrl; }
}