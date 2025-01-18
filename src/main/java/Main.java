import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) {

    }

    public void fetchFromAPI(){
        System.out.println("Fetching from Places API");
        String url = "https://places.googleapis.com/v1/places:searchText";

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpRequest request = HttpRequest.newBuilder().GET()
                    .header("maxResultCount","10")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "places.primaryType,places.priceLevel,")


                    .uri(URI.create(url)).build();


            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            String jsonData = response.body();
            System.out.println(jsonData);

        }
        catch (IOException | InterruptedException e) {
            System.out.println(e.getClass() + "Exception when sending request: " + e);
        }
    }
}