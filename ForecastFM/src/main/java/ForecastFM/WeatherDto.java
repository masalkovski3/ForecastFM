package ForecastFM;

public class WeatherDto {

    private final int weatherId;
    private final String main;
    private final String description;
    private final double temperature;
    private final String city;

    public WeatherDto(int weatherId, String main, String description, double temperature
        , String city) {
        this.weatherId = weatherId;
        this.main = main;
        this.description = description;
        this.temperature = temperature;
        this.city = city;
    }

    public String getCity() { return city; }

    public int getWeatherId() {
        return weatherId;
    }

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }

    public double getTemperature() {
        return temperature;
    }
}
