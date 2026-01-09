package ForecastFM;

public class WeatherDto {

    private final int weatherId;
    private final String main;
    private final String description;
    private final double temperature;

    public WeatherDto(int weatherId, String main, String description, double temperature) {
        this.weatherId = weatherId;
        this.main = main;
        this.description = description;
        this.temperature = temperature;
    }

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
