package ForecastFM;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MoodMapper {

    public MoodProfile fromWeather(WeatherSnapshot w) {

        if (w.isClear()) {
            return new MoodProfile(
                    "clear_uplift",
                    List.of("happy", "feel good", "sunny"),
                    List.of("pop", "dance"),
                    0.8,
                    0.8,
                    0.5
            );
        }

        switch (w.getGroup()) {
            case 2: // thunderstorm
                return new MoodProfile(
                        "storm_intense",
                        List.of("intense", "power", "thunder"),
                        List.of("rock", "metal"),
                        0.3,
                        0.5,
                        0.8

                );

            case 3: // drizzle
            case 5: // rain
                return new MoodProfile(
                        "rain_chill",
                        List.of("rainy day", "chill", "acoustic"),
                        List.of("acoustic", "ambient"),
                        0.4,
                        0.2,
                        0.4
                );

            case 6: // snow
                return new MoodProfile(
                        "snow_calm",
                        List.of("winter", "calm", "cozy"),
                        List.of("indie", "ambient"),
                        0.6,
                        0.4,
                        0.5
                );

            case 7: // atmosphere (fog/mist)
                return new MoodProfile(
                        "fog_ambient",
                        List.of("ambient", "downtempo", "focus"),
                        List.of("ambient", "electronic"),
                        0.3,
                        0.3,
                        0.4
                );

            case 8: // clouds (801-804)
                return new MoodProfile(
                        "clouds_easy",
                        List.of("chill", "easy", "laid back"),
                        List.of("indie", "pop"),
                        0.7,
                        0.6,
                        0.5
                );

            default:
                // fallback om OpenWeather ger något oväntat
                return new MoodProfile(
                        "default_mix",
                        List.of("chill", "popular"),
                        List.of("pop"),
                        0.5,
                        0.5,
                        0.5
                );
        }
    }
}

