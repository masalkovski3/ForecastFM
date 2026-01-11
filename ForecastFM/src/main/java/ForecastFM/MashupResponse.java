package ForecastFM;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

@JsonInclude(ALWAYS)
public class MashupResponse {

    private final WeatherDto weather;
    private final MoodProfile mood;
    //private final List<TrackDto> tracks;
    private final List<String> tracks;
    private final int limit;

    private MashupResponse(WeatherDto weather, MoodProfile mood, List<String> tracks, int limit) {
        this.weather = weather;
        this.mood = mood;
        //this.tracks = tracks;
        this.tracks = tracks;
        this.limit = limit;
    }

    public WeatherDto getWeather() {
        return weather;
    }

    public MoodProfile getMood() {
        return mood;
    }

    public List<String> getTracks() {
        return tracks;
    }

    public int getLimit() {
        return limit;
    }

    public static MashupResponse from(WeatherDto weather,
                                      MoodProfile mood,
                                      List<String> tracks, int limit) {

        return new MashupResponse(weather, mood, tracks, limit);
    }
}
