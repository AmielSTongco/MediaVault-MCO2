package application.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import application.model.Game;
import application.model.Status;

/**
 * Client for interacting with the GameBrain API.
 *
 * <p>This class authenticates using a GameBrain API key and provides
 * methods for searching games and converting the results into Game
 * objects.</p>
 */
public class GameAPIClient {

    private static final String BASE_URL = "https://api.gamebrain.co/v1/games";

    private final String apiKey;
    private final HttpClient client;
    private final ObjectMapper mapper;

    /**
     * Creates a GameAPIClient using the provided API key.
     *
     * @param apiKey the GameBrain API key
     */
    public GameAPIClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * Searches GameBrain for games matching the specified query.
     *
     * <p>The returned games are converted into Game objects with
     * default user-specific values such as status, rating, and
     * review.</p>
     *
     * @param query the search term entered by the user
     * @return a list of matching games
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public List<Game> searchGames(String query) throws IOException, InterruptedException {
    	
    	// Encode the search query for use in the request URL.
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = BASE_URL + "?query=" + encodedQuery
                + "&limit=10"
                + "&sort=computed_rating"
                + "&sort-order=desc"
                + "&generate-filter-options=false";
        
        // Build the authenticated GET request.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-api-key", apiKey)
                .GET()
                .build();
        
        // Send the search request.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("GameBrain request failed with status code "
                    + response.statusCode() + ": " + response.body());
        }

        List<Game> games = new ArrayList<>();

        JsonNode root = mapper.readTree(response.body());

        if (!root.has("results")) {
            System.out.println("GameBrain did not return any games.");
            return games;
        }

        JsonNode results = root.get("results");

        int resultCount = Math.min(results.size(), 10);

        for (int i = 0; i < resultCount; i++) {
            JsonNode item = results.get(i);

            int gameId = item.get("id").asInt();

            JsonNode gameDetails = getGameDetails(gameId);

            String title = getTextValue(item, "name");
            String genre = getTextValue(item, "genre");

            String creator = getCreator(gameDetails);
            int yearReleased = getIntValue(item, "year");
            int avgPlaytimeMins = 0;
            
        	String imagePath = getTextValue(item, "image");

        	if(imagePath.isBlank())
        		imagePath = getTextValue(gameDetails, "image");

            Status status = Status.PLANNED;
            double userRating = 0.0;
            String review = "";

            games.add(new Game(title, creator, yearReleased, status, userRating, review, genre, avgPlaytimeMins, imagePath));
        }

        return games;
    }

    /**
     * Retrieves the complete information for a specified game.
     *
     * @param gameId the GameBrain ID of the game
     * @return the complete game information
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    private JsonNode getGameDetails(int gameId) throws IOException, InterruptedException {
    	
    	String url = BASE_URL + "/" + gameId;
    	
    	// Build the authenticated GET request.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-api-key", apiKey)
                .GET()
                .build();
        
        // Send the game details request.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("GameBrain details request failed with status code "
                    + response.statusCode() + ": " + response.body());
        }

        return mapper.readTree(response.body());
    }

    /**
     * Retrieves the developer or creator from a game's information.
     *
     * @param node the JSON object containing the game information
     * @return the developer name, or an empty string if it is missing
     */
    private String getCreator(JsonNode node) {
        JsonNode developer = node.get("developer");

        if (developer != null && !developer.isNull()) {
            if (developer.isTextual()) {
                return developer.asText();
            }

            if (developer.isObject() && developer.has("name")) {
                return developer.get("name").asText();
            }
        }

        JsonNode developers = node.get("developers");

        if (developers != null && developers.isArray() && developers.size() > 0) {
            JsonNode firstDeveloper = developers.get(0);

            if (firstDeveloper.isTextual()) {
                return firstDeveloper.asText();
            }

            if (firstDeveloper.isObject() && firstDeveloper.has("name")) {
                return firstDeveloper.get("name").asText();
            }
        }

        JsonNode creator = node.get("creator");

        if (creator != null && !creator.isNull()) {
            if (creator.isTextual()) {
                return creator.asText();
            }

            if (creator.isObject() && creator.has("name")) {
                return creator.get("name").asText();
            }
        }

        return "";
    }

    /**
     * Safely reads a text field from a JSON object.
     *
     * @param node the JSON object
     * @param fieldName the field to retrieve
     * @return the field value, or an empty string if it is missing
     */
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);

        if (value == null || value.isNull()) {
            return "";
        }

        return value.asText();
    }

    /**
     * Safely reads an integer field from a JSON object.
     *
     * @param node the JSON object
     * @param fieldName the field to retrieve
     * @return the field value, or zero if it is missing
     */
    private int getIntValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);

        if (value == null || value.isNull()) {
            return 0;
        }

        return value.asInt();
    }
}