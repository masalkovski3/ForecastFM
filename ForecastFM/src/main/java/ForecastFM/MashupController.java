package ForecastFM;

import jdk.jfr.Registered;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mashup")
public class MashupController {

    private MashupService mashupService;

    public MashupController(MashupService mashupService){
        this.mashupService = mashupService;
    }

    @GetMapping
    public MashupResponse getWeatherMusic(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10") int limit) throws Exception {

        return mashupService.createMashup(lat, lon, limit);
    }
}
