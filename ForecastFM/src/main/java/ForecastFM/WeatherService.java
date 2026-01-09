package ForecastFM;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.net.URI;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class WeatherService{
        public static void main(String[]args) throws Exception {

        String WEATHER_API_KEY = System.getenv("OPENWEATHER_API_KEY"); // Hämtar API nyckeln från env filen
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Stad: ");
        String city = scanner.nextLine().trim();
        city = city.replace(" ", "%20");
        scanner.close();

        String baseUrl = "https://api.openweathermap.org/data/2.5/weather?q=";
        String units = "&units=metric";
        String apiParameters = "&appid=" + WEATHER_API_KEY; 
        String url = baseUrl + city + apiParameters + units;
        System.out.println(url);

        HttpClient client = HttpClient.newHttpClient(); // Skapar en HTTP klient

        HttpRequest request = HttpRequest.newBuilder() //Skapar en request = API anropet
            .uri(URI.create(url))
            .build();

        HttpResponse<String> response = 
            client.send(request, HttpResponse.BodyHandlers.ofString()); //här skickas requesten till Openweather 

        System.out.println("Statuskod: " + response.statusCode());
        
        String json = response.body(); //sparar svaret från anropet här
        System.out.println("Response: "+ json);

        ObjectMapper mapper = new ObjectMapper(); //Lägger till objectmapper som kan översätta jsondatan
        JsonNode root = mapper.readTree(json); //Bygger ett lättläsligt träd av datan 
        System.out.println(root); //root är alltså den fina nya json strukturen

        JsonNode mainNode = root.get("main"); //plocka ut det som ligger i main i trädet
        System.out.println(mainNode);

        //För att plocka ut temperaturen:
        int temp = (int) mainNode.get("temp").asDouble();
        System.out.println("Temperatur: " + temp + "°C");

        //För att plocka ut vädertyp:
        String weather = root.get("weather").get(0).get("description").asText(); //ändra från description till main om vi inte vill ha detaljerad info
        System.out.println("Väder: " + weather);

        //För att plocka ut vind:
        int wind = (int) root.get("wind").get("speed").asDouble();
        System.out.println("Vind: " + wind + " m/sekund");

        //För att plocka ut ikonen:
        String icon = root.get("weather").get(0).get("icon").asText();
        System.out.println("Ikon" + icon);


    }
}


