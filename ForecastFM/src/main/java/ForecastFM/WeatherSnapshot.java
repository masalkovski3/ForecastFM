package ForecastFM;

public class WeatherSnapshot {
    private final int weatherId;

    public WeatherSnapshot(int weatherId) {
        this.weatherId = weatherId;
    }

    public int getWeatherId() { return weatherId; }

    public int getGroup() {
        return weatherId / 100; // 2,3,5,6,7,8
    }

    public boolean isClear() {
        return weatherId == 800;
    }
}

