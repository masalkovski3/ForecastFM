package ForecastFM;

public class WeatherServiceTestManual {
    public static void main(String[] args) {
        try {
            WeatherService service = new WeatherService();

            double lat = 55.6059;
            double lon = 13.0007;

            String city = service.getCityFromLatLon(lat, lon);
            System.out.println("Stad: " + city);

            WeatherDto weather = service.getWeatherDto(lat, lon);
            if (weather != null) {
                System.out.println("Väder: " + weather.getMain() 
                    + ", " + weather.getDescription() 
                    + ", Temp: " + weather.getTemperature());
            } else {
                System.out.println("Kunde inte hämta väderdata");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
