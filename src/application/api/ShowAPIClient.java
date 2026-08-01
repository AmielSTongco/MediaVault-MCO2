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

import application.model.Show;
import application.model.Status;

/**
 * Client for interacting with the TMDB API.
 *
 * <p>This class authenticates using a TMDB access token and provides
 * methods for searching shows and converting the results into Show
 * objects.</p>
 */
public class ShowAPIClient {

    private static final String BASE_URL = "https://api.themoviedb.org/3";

    private final String accessToken;
    private final HttpClient client;
    private final ObjectMapper mapper;

    /**
     * Creates a ShowAPIClient using the provided TMDB access token.
     *
     * @param accessToken the TMDB API read access token
     */
    public ShowAPIClient(String accessToken) {
        this.accessToken = accessToken;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * Searches TMDB for shows matching the specified query.
     *
     * <p>The returned shows are converted into Show objects with
     * default user-specific values such as status, rating, and
     * review.</p>
     *
     * @param query the search term entered by the user
     * @return a list of matching shows
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public List<Show> searchShows(String query) throws IOException, InterruptedException {
    	
    	// Encode the search query for use in the request URL.
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = BASE_URL + "/search/tv?query=" + encodedQuery
                + "&include_adult=false"
                + "&language=en-US"
                + "&page=1";
        
        // Build the authenticated GET request.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("accept", "application/json")
                .GET()
                .build();
        
        // Send the search request.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("TMDB request failed with status code "
                    + response.statusCode() + ": " + response.body());
        }

        List<Show> shows = new ArrayList<>();

        JsonNode root = mapper.readTree(response.body());

        if (!root.has("results")) {
            System.out.println("TMDB did not return any shows.");
            return shows;
        }

        JsonNode results = root.get("results");

        int resultCount = Math.min(results.size(), 10);

        for (int i = 0; i < resultCount; i++) {
            JsonNode item = results.get(i);

            int showId = item.get("id").asInt();

            JsonNode showDetails = getShowDetails(showId);

            String title = getTextValue(item, "name");
            String creator = getCreator(showDetails);
            int yearStart = getReleaseYear(showDetails, "first_air_date");
            int yearEnd = getReleaseYear(showDetails, "last_air_date");
            String genre = getGenre(showDetails);
            int numOfSeasons = getIntValue(showDetails, "number_of_seasons");
            boolean airing = getBooleanValue(showDetails, "in_production");
            
        	String imagePath = "";

        	String posterPath = getTextValue(item, "poster_path");

        	if(!posterPath.isBlank())
        		imagePath = "https://image.tmdb.org/t/p/w500" + posterPath;

            Status status = Status.PLANNED;
            double userRating = 0.0;
            String review = "";

            shows.add(new Show(title, creator, yearStart, yearEnd, status, userRating, review, genre, numOfSeasons, airing, imagePath));
        }

        return shows;
    }

    /**
     * Retrieves the complete information for a specified show.
     *
     * @param showId the TMDB ID of the show
     * @return the complete show information
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    private JsonNode getShowDetails(int showId) throws IOException, InterruptedException {
    	
    	String url = BASE_URL + "/tv/" + showId + "?language=en-US";
    	
    	// Build the authenticated GET request.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("accept", "application/json")
                .GET()
                .build();
        
        // Send the show details request.
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("TMDB details request failed with status code "
                    + response.statusCode() + ": " + response.body());
        }

        return mapper.readTree(response.body());
    }

    /**
     * Retrieves the creator from a show's information.
     *
     * @param node the JSON object containing the show information
     * @return the creator name, or an empty string if it is missing
     */
    private String getCreator(JsonNode node) {
        JsonNode creators = node.get("created_by");

        if (creators == null || !creators.isArray() || creators.size() == 0) {
            return "";
        }

        String creator = "";

        for (int i = 0; i < creators.size(); i++) {
            JsonNode creatorNode = creators.get(i);

            if (i > 0) {
                creator += ", ";
            }

            creator += getTextValue(creatorNode, "name");
        }

        return creator;
    }

    /**
     * Retrieves the genres from a show's information.
     *
     * @param node the JSON object containing the show information
     * @return the genres, or an empty string if they are missing
     */
    private String getGenre(JsonNode node) {
        JsonNode genres = node.get("genres");

        if (genres == null || !genres.isArray() || genres.size() == 0) {
            return "";
        }

        String genre = "";

        for (int i = 0; i < genres.size(); i++) {
            JsonNode genreNode = genres.get(i);

            if (i > 0) {
                genre += ", ";
            }

            genre += getTextValue(genreNode, "name");
        }

        return genre;
    }

    /**
     * Retrieves the year from a specified date field.
     *
     * @param node the JSON object containing the show information
     * @param fieldName the date field to retrieve
     * @return the year, or zero if it is missing
     */
    private int getReleaseYear(JsonNode node, String fieldName) {
        String date = getTextValue(node, fieldName);

        if (date.length() < 4) {
            return 0;
        }

        try {
            return Integer.parseInt(date.substring(0, 4));
        } catch (NumberFormatException e) {
            return 0;
        }
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
    
    /**
     * Safely reads a boolean field from a JSON object.
     *
     * @param node the JSON object
     * @param fieldName the field to retrieve
     * @return the field value, or false if it is missing
     */
    private boolean getBooleanValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);

        if (value == null || value.isNull()) {
            return false;
        }

        return value.asBoolean();
    }
}