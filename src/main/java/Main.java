import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;


public class Main {
    public static void main(String[] args) {
        fetchFromAPI(args[0]);
    }

    public static void fetchFromAPI(String LocationRequest) {
        Dotenv dotenv = Dotenv.load(); // Automatically reads the `.env` file
        String apiKey = dotenv.get("PLACES_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API key is not set in the .env file");
        }

        System.out.println("Fetching from Places API");
        String url = "https://places.googleapis.com/v1/places:searchText";
        int maxResultCount = 10;

        JsonObject payload = new JsonObject();

        payload.addProperty("maxResultCount", maxResultCount);
        payload.addProperty("textQuery", LocationRequest);

        String jsonPayload = new Gson().toJson(payload);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("maxResultCount","10")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "places.primaryType,places.priceLevel,places.rating,places.userRatingCount")
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();



            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            String jsonData = response.body();
            System.out.println(jsonData);
            System.out.println("Response status code: " + response.statusCode());

        }
        catch (IOException | InterruptedException e) {
            System.out.println(e.getClass() + "Exception when sending request: " + e);
        }
    }
}