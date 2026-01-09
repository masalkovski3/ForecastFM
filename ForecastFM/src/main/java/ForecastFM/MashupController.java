package ForecastFM;

import jdk.jfr.Registered;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mashups")
public class MashupController {

    private MashupService mashupService;

    public MashupController(MashupService mashupService){
        this.mashupService = mashupService;
    }

    @GetMapping("/weather-music")
    public MashupResponse getWeatherMusic(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "metric") String units){

        return mashupService.createMashup(lat, lon, limit, units);
    }
}
