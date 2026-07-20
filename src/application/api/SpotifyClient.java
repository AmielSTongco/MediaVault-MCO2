package application.api;

import java.util.List;
import java.util.ArrayList;

import application.model.Song;
import application.model.Status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Client for interacting with the Spotify Web API.
 *
 * <p>This class authenticates using the Client Credentials Flow and
 * provides methods for searching tracks and converting the results
 * into Song objects.</p>
 */
public class SpotifyClient {

    private final String clientId;
    private final String clientSecret;
    private final HttpClient client;
    
    /**
     * Creates a SpotifyClient using the provided API credentials.
     *
     * @param clientId the Spotify application's client ID
     * @param clientSecret the Spotify application's client secret
     */
    public SpotifyClient(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.client = HttpClient.newHttpClient();
    }
    
    /**
     * Requests an OAuth access token from Spotify using the
     * Client Credentials Flow.
     *
     * @return a valid Spotify access token
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public String getAccessToken() throws IOException, InterruptedException {
    	
    	// Prepare the request body for Spotify authentication.
        String body = "grant_type=client_credentials"
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);
        
        // Build the HTTP POST request.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        
        // Send the authentication request.
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        String json = response.body();

        // Simple extraction without a JSON library
        int start = json.indexOf("\"access_token\":\"") + 16;
        int end = json.indexOf("\"", start);

        return json.substring(start, end);
    }
    
    /**
     * Searches Spotify for tracks matching the specified query.
     *
     * <p>The returned tracks are converted into Song objects with
     * default user-specific values such as status, rating, and review.</p>
     *
     * @param query the search term entered by the user
     * @return a list of matching songs
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public List<Song> searchTracks(String query) throws IOException, InterruptedException {
    	
        String accessToken = getAccessToken();
        
        // Encode the search query for use in the request URL.
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = "https://api.spotify.com/v1/search?q=" + encodedQuery + "&type=track&limit=10";
        
        // Build the authenticated GET request.
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Authorization", "Bearer " + accessToken).GET().build();
        
        // Send the search request.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String json = response.body();
        
        // Store the resulting Song objects.
        List<Song> songs = new ArrayList<>();
        
        // Parse the JSON response.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        
        // Return an empty list if Spotify returns no tracks.
        if (!root.has("tracks"))
        {
            System.out.println("Spotify did not return any tracks.");
            return songs;
        }

        JsonNode items = root.get("tracks").get("items");
        
        // Convert each track into a Song object.
        for (JsonNode item : items) {
            String title = item.get("name").asText();

            String artist = item.get("artists").get(0).get("name").asText();

            String album = item.get("album").get("name").asText();

            int yearReleased = Integer.parseInt(item.get("album").get("release_date").asText().substring(0, 4));

            int runtimeSeconds = item.get("duration_ms").asInt() / 1000;

            Status status = Status.PLANNED;
            double userRating = 0.0;
            String review = "";

            songs.add(new Song(title, status, userRating, album, artist, yearReleased, runtimeSeconds, review));
        }
        
        return songs;
    }
}