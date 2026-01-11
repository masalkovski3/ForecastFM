package ForecastFM;

import java.util.List;

public class MashupResponse {

    private final WeatherDto weather;
    private final MoodProfile mood;
    //private final List<TrackDto> tracks;
    private final List<String> songs;
    private final int limit;

    private MashupResponse(WeatherDto weather, MoodProfile mood, List<String> songs, int limit) {
        this.weather = weather;
        this.mood = mood;
        //this.tracks = tracks;
        this.songs = songs;
        this.limit = limit;
    }

    public WeatherDto getWeather() {
        return weather;
    }

    public MoodProfile getMood() {
        return mood;
    }

//    public List<TrackDto> getTracks() {
//        return tracks;
//    }

    public int getLimit() {
        return limit;
    }

    public static MashupResponse from(WeatherDto weather,
                                      MoodProfile mood,
                                      List<String> songs, int limit) {
        return new MashupResponse(weather, mood, songs, limit);
    }
}
