package ForecastFM;

public class TrackDto {
    private final String name;
    private final String artist;
    private final String url;
    private final String previewUrl; // kan vara null
    private final double valence;
    private final double danceability;
    private final double energy;

    public TrackDto(String name, String artist, String url, String previewUrl, double valence, double danceability, double energy) {
        this.name = name;
        this.artist = artist;
        this.url = url;
        this.previewUrl = previewUrl;
        this.valence = valence;
        this.danceability = danceability;
        this.energy = energy;
    }

    public String getName() { return name; }
    public String getArtist() { return artist; }
    public String getUrl() { return url; }
    public String getPreviewUrl() { return previewUrl; }
    public double getValence() { return valence; }
    public double getDanceability() { return danceability; }
    public double getEnergy() { return energy; }
}