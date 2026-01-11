package ForecastFM;

import java.util.List;

public class MashupResponse {

    private final WeatherDto weather;
    private final MoodProfile mood;
    private final List<TrackDto> tracks;
    private final int limit;

    private MashupResponse(WeatherDto weather, MoodProfile mood, List<TrackDto> tracks, int limit) {
        this.weather = weather;
        this.mood = mood;
        this.tracks = tracks;
        this.limit = limit;
    }

    public WeatherDto getWeather() {
        return weather;
    }

    public MoodProfile getMood() {
        return mood;
    }

    public List<TrackDto> getTracks() {
        return tracks;
    }

    public int getLimit() {
        return limit;
    }

    public static MashupResponse from(WeatherDto weather,
                                      MoodProfile mood,
                                      List<TrackDto> tracks, int limit) {
        return new MashupResponse(weather, mood, tracks, limit);
    }
}
