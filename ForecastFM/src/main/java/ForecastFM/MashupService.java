package ForecastFM;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MashupService {
        private WeatherService weatherService;
        private SpotifyService spotifyService;
        private MoodMapper moodMapper;

        public MashupService(WeatherService weatherService, SpotifyService spotifyService,
                             MoodMapper moodMapper) {
            this.weatherService = weatherService;
            this.spotifyService = spotifyService;
            this.moodMapper = moodMapper;
        }

        public MashupResponse createMashup(double lat, double lon, int limit) throws Exception {
            spotifyService.testSearch();
            WeatherDto weather = weatherService.getWeatherForMashup(lat, lon);
            WeatherSnapshot snapshot = new WeatherSnapshot(weather.getWeatherId());
            MoodProfile mood = moodMapper.fromWeather(snapshot);
            List<TrackDto> tracks = spotifyService.searchTracks(mood, limit);

            return MashupResponse.from(weather, mood, tracks, limit);
        }
}
