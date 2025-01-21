import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;

/*
To future readers:
I realize that the recommended way to access the google maps apis is through the google client libraries.
At this point however, the documentation is unfinished and lacking for my use cases, so I have decided to use
standard web calls for ease of development.
 */

public class Main {

    // Global variable for the API key
    private static final String API_KEY;

    static {
        // Load the API key from the .env file
        System.out.println("Loading API key...");
        Dotenv dotenv = Dotenv.load();
        API_KEY = dotenv.get("PLACES_API_KEY");

        if (API_KEY == null || API_KEY.isEmpty()) {
            System.out.println("Invalid API Key");
            throw new IllegalStateException("API key is not set in the .env file");
        }
    }

    public static void main(String[] args) {

        System.out.println("Received Request: Location: " + args[0] + " Radius: " + args[1]);
        double[] location = convertRequestToGeoCode(args[0]);
        callNearbySearch(location[0], location[1], Integer.parseInt(args[1]));
    }

    public static double[] convertRequestToGeoCode(String locationRequest) {

        // Encode the address to make it URL-safe
        String encodedAddress = URLEncoder.encode(locationRequest, StandardCharsets.UTF_8);

        // Construct the URL
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedAddress + "&key=" + API_KEY;

        // Create an HttpClient instance
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {

            // Build the GET request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            // Send the request and get the response
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: ");
            System.out.println(response.body());
            return extractLatitudeLongitudeFromJson(response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Error occurred while sending the API request: " + e.getMessage());
        }
        return new double[0];
    }


    public static double[] extractLatitudeLongitudeFromJson(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        if ("OK".equals(jsonObject.get("status").getAsString())) {
            JsonObject location = jsonObject
                    .getAsJsonArray("results")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("geometry")
                    .getAsJsonObject("location");

            double lat = location.get("lat").getAsDouble();
            double lng = location.get("lng").getAsDouble();

            return new double[]{lat, lng};
        } else {
            throw new IllegalStateException("Error: " + jsonObject.get("status").getAsString());
        }
    }


    public static void callNearbySearch(double latitude, double longitude, double radius) {


        System.out.println("Fetching from Places API");
        String url = "https://places.googleapis.com/v1/places:searchNearby";
        int maxResultCount = 10;

        JsonObject payload = new JsonObject();

        payload.addProperty("maxResultCount", maxResultCount);

        JsonObject locationRestriction = new JsonObject();
        payload.add("locationRestriction", locationRestriction);

        JsonObject circle = new JsonObject();
        locationRestriction.add("circle", circle);


        JsonObject center = new JsonObject();
        circle.add("center", center);

        center.addProperty("latitude", latitude);
        center.addProperty("longitude", longitude);

        circle.addProperty("radius", radius);


        String jsonPayload = new Gson().toJson(payload);

        System.out.println("Payload Sent: " + jsonPayload);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("maxResultCount","10")
                    .header("X-Goog-Api-Key", API_KEY)
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
            System.err.println("Error occurred while sending the API request: " + e.getMessage());
        }
    }
}