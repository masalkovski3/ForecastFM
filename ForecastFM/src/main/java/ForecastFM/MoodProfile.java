package ForecastFM;

import java.util.List;

public class MoodProfile {
    private final String id;
    private final List<String> keywords;
    private final List<String> seedGenres;
    private final double valence;
    private final double danceability;
    private final double energy;

    public MoodProfile(String id, List<String> keywords, List<String> seedGenres, double valence, double danceability, double energy) {
        this.id = id;
        this.keywords = keywords;
        this.seedGenres = seedGenres;
        this.valence = valence;
        this.danceability = danceability;
        this.energy = energy;
    }

    public String getId() { return id; }
    public List<String> getKeywords() { return keywords; }
    public List<String> getSeedGenres() { return seedGenres; }
    public double getValence() { return valence; }
    public double getDanceability() { return danceability; }
    public double getEnergy() { return energy; }
}
