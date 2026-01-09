package ForecastFM;

import java.util.List;

public class MoodProfile {
    private final String id;
    private final List<String> keywords;
    private final List<String> seedGenres;

    public MoodProfile(String id, List<String> keywords, List<String> seedGenres) {
        this.id = id;
        this.keywords = keywords;
        this.seedGenres = seedGenres;
    }

    public String getId() { return id; }
    public List<String> getKeywords() { return keywords; }
    public List<String> getSeedGenres() { return seedGenres; }
}
