package ForecastFM;

import java.util.List;

public class MoodProfile {
    private final String id;
    private final List<String> keywords;
    private final List<String> seedGenres;
    private final double valence;
    private final double danceability;
    private final double tempo;

    public MoodProfile(String id, List<String> keywords, List<String> seedGenres, double valence, double danceability, double tempo) {
        this.id = id;
        this.keywords = keywords;
        this.seedGenres = seedGenres;
        this.valence = valence;
        this.danceability = danceability;
        this.tempo = tempo;
    }

    public String getId() { return id; }
    public List<String> getKeywords() { return keywords; }
    public List<String> getSeedGenres() { return seedGenres; }
    public double getValence() { return valence; }
    public double getDanceability() { return danceability; }
    public double getTempo() { return tempo; }
}
