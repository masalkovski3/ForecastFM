package ForecastFM;

import org.springframework.stereotype.Service;

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

        public MashupResponse createMashup(double lat, double lon, int limit, String units){
            WeatherDto weather = weatherService.getWeather(lat, lon, units);
            WeatherSnapshot snapshot = new WeatherSnapshot(weather.getWeatherId());
            MoodProfile mood = moodMapper.fromWeather(snapshot);
            SearchResult tracks = spotifyService.searchTracks(mood, limit);

            return MashupResponse.from(weather, mood, tracks, limit, units, lat, lon);
        }
}
