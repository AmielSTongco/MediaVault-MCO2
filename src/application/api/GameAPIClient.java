package application.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import application.model.Game;
import application.model.Status;

public class GameAPIClient {
	
	/*
	 * The creation of this API Client was guided by the official documentation
	 * of the IGDB API (Source: https://api-docs.igdb.com/)
	 */
	
	private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
	private static final String GAMES_URL = "https://api.igdb.com/v4/games";
	private static final String TIME_TO_BEAT_URL = "https://api.igdb.com/v4/game_time_to_beats";			// For getting avg. playtime mins
	private static final String IMAGE_URL = "https://images.igdb.com/igdb/image/upload/t_cover_big/";		// To get image path string
	
	// Required attributes to connect to API
	private final String clientId;
	private final String clientSecret;
	private final HttpClient client;
	private final ObjectMapper mapper;
	
	// For authentication and authorization of API use
	private String accessToken;
	private long tokenExpiration;
	
	/**
	 * Creates a GameAPIClient using the provided Twitch credentials.
	 *
	 * @param clientId the Twitch application client ID
	 * @param clientSecret the Twitch application client secret
	 */
	public GameAPIClient(String clientId, String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.client = HttpClient.newHttpClient();
		this.mapper = new ObjectMapper();
	}
	
	/**
	 * Searches the IGDB API for games matching the specified query.
	 *
	 * <p>Returned games are converted into Game objects with default
	 * user-specific values such as status, rating, and review.</p>
	 *
	 * @param query the search term entered by the user
	 * @return a list of matching games
	 * @throws IOException if an I/O error occurs
	 * @throws InterruptedException if the request is interrupted
	 */
	public List<Game> searchGames(String query) throws IOException, InterruptedException {
		String token = getAccessToken();
		String safeQuery = query.replace("\\", "\\\\").replace("\"", "\\\"");
		
		// Full query used to extract at most 10 games
		String body = 	"search \"" + safeQuery + "\"; "
						+ "fields id, name, first_release_date, cover.image_id, genres.name, "
						+ "involved_companies.company.name, involved_companies.developer; "
						+ "where version_parent = null; "
						+ "limit 10;";

		HttpRequest request = createIGDBRequest(GAMES_URL, token, body);
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		// Checker for 200 OK Response
		if(response.statusCode() != 200)
			throw new IOException("IGDB request failed with status code " + response.statusCode() + ": " + response.body());
		
		
		List<Game> games = new ArrayList<>();
		JsonNode results = mapper.readTree(response.body());

		if(!results.isArray())
			return games;
		
		// Retrieves average playtimes for all returned games
		Map<Integer, Integer> playtimes = getAveragePlaytimes(results, token);
		
		// Extracting data from parsed JSON
		for(JsonNode item : results) {
			int gameId = getIntValue(item, "id");
			String title = getTextValue(item, "name");
			String creator = getDeveloper(item);
			String genre = getGenre(item);
			String imagePath = getImagePath(item);
			int yearReleased = getReleaseYear(item);
			int avgPlaytimeMins = playtimes.getOrDefault(gameId, 0);

			Status status = Status.PLANNED;
			double userRating = 0.0;
			String review = "";
			
			// Creates each of the found games
			Game game = new Game(title, creator, yearReleased, status, userRating, review, genre, avgPlaytimeMins, imagePath);

			game.setMediaId(gameId);
			games.add(game);
		}

		return games;
	}
	
	/**
	 * Retrieves the average completion times for the specified games.
	 *
	 * @param gameResults the JSON search results returned by IGDB
	 * @param token the current IGDB access token
	 * @return a map of game IDs to average playtimes in minutes
	 * @throws IOException if an I/O error occurs
	 * @throws InterruptedException if the request is interrupted
	 */
	private Map<Integer, Integer> getAveragePlaytimes(JsonNode gameResults, String token) throws IOException, InterruptedException {
		
		// Used to store each game ID and its avg. playtime in mins
		Map<Integer, Integer> playtimes = new HashMap<>();
		StringJoiner ids = new StringJoiner(",");
		
		// Collects all game IDs into a comma-separated list
		for(JsonNode item : gameResults) {
			int gameId = getIntValue(item, "id");

			if(gameId > 0)
				ids.add(String.valueOf(gameId));
		}
		
		// Sends a request only when at least one valid game ID was found
		if(ids.length() > 0) {
			
			// Query to retrieve normal completion times for the games
			String body = "fields game_id, normally; "
					+ "where game_id = (" + ids + "); "
					+ "limit 10;";

			HttpRequest request = createIGDBRequest(TIME_TO_BEAT_URL, token, body);
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if(response.statusCode() != 200)
				throw new IOException("IGDB time-to-beat request failed with status code " + response.statusCode() + ": " + response.body());
			
			// Converts the returned JSON text into a JsonNode tree
			JsonNode results = mapper.readTree(response.body());

			if(results.isArray()) {
				for(JsonNode item : results) {
					int gameId = getIntValue(item, "game_id");
					int normallySeconds = getIntValue(item, "normally");
					int normallyMinutes = normallySeconds / 60;

					playtimes.put(gameId, normallyMinutes);
				}
			}
		}

		return playtimes;
	}
	
	/**
	 * Creates an authenticated POST request for the IGDB API.
	 *
	 * @param url the target IGDB endpoint
	 * @param token the current access token
	 * @param body the request body
	 * @return the configured HTTP request
	 */
	private HttpRequest createIGDBRequest(String url, String token, String body) {
		
		// Creates an authenticated POST request for the IGDB API
		return HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Client-ID", clientId)
				.header("Authorization", "Bearer " + token)
				.header("Accept", "application/json")
				.header("Content-Type", "text/plain")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
	}
	
	/**
	 * Retrieves a valid Twitch OAuth access token.
	 *
	 * <p>The token is cached and automatically refreshed before it expires.</p>
	 *
	 * @return a valid access token
	 * @throws IOException if an I/O error occurs
	 * @throws InterruptedException if the request is interrupted
	 */
	private String getAccessToken() throws IOException, InterruptedException {
		long currentTime = System.currentTimeMillis();
		
		// Requests a new token if one does not exist or has already expired
		if(accessToken == null || currentTime >= tokenExpiration) {
			
			// Builds the Twitch OAuth token request URL
			String url = TOKEN_URL
					+ "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
					+ "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
					+ "&grant_type=client_credentials";
			
			// Sends auth req to Twitch
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.POST(HttpRequest.BodyPublishers.noBody())
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if(response.statusCode() != 200)
				throw new IOException("Twitch authentication failed with status code " + response.statusCode() + ": " + response.body());

			JsonNode root = mapper.readTree(response.body());
			
			// Extracts the access token from the response
			accessToken = getTextValue(root, "access_token");

			long expiresIn = root.has("expires_in")
					? root.get("expires_in").asLong()
					: 3600;
			
			// Refreshes token one minute before expiration
			tokenExpiration = currentTime + Math.max(0, expiresIn - 60) * 1000;
		}

		return accessToken;
	}
	
	/**
	 * Retrieves the game's developer from the returned JSON.
	 *
	 * @param item the JSON object representing a game
	 * @return the developer name, or an empty string if unavailable
	 */
	private String getDeveloper(JsonNode item) {
		
		// Retrieves the list of companies involved in the game
		JsonNode companies = item.get("involved_companies");

		if(companies != null && companies.isArray()) {
			for(JsonNode involvedCompany : companies) {
				
				// Checks if the involved company is marked as a developer
				boolean developer = involvedCompany.has("developer") && involvedCompany.get("developer").asBoolean();

				if(developer) {
					JsonNode company = involvedCompany.get("company");

					if(company != null && company.has("name"))
						return company.get("name").asText();
				}
			}
		}

		return "";
	}
	
	/**
	 * Retrieves the first listed genre from the returned JSON.
	 *
	 * @param item the JSON object representing a game
	 * @return the first genre, or an empty string if unavailable
	 */
	private String getGenre(JsonNode item) {
		
		// Retrieves list of genres assigned to game
		JsonNode genres = item.get("genres");

		if(genres != null && genres.isArray() && !genres.isEmpty()) {
			
			// Uses the first available genre
			JsonNode firstGenre = genres.get(0);

			if(firstGenre.has("name"))
				return firstGenre.get("name").asText();
		}

		return "";
	}
	
	/**
	 * Builds the game's cover image URL.
	 *
	 * @param item the JSON object representing a game
	 * @return the complete image URL, or an empty string if unavailable
	 */
	private String getImagePath(JsonNode item) {
		
		// Retrieves the game's cover information
		JsonNode cover = item.get("cover");

		if(cover != null && cover.has("image_id")) {
			String imageId = cover.get("image_id").asText();
			
			// Builds the complete cover image URL
			if(!imageId.isBlank())
				return IMAGE_URL + imageId + ".jpg";
		}

		return "";
	}
	
	/**
	 * Retrieves the game's release year.
	 *
	 * @param item the JSON object representing a game
	 * @return the release year, or 0 if unavailable
	 */
	private int getReleaseYear(JsonNode item) {
		
		// Retrieves the game's Unix release timestamp
		JsonNode releaseDate = item.get("first_release_date");

		if(releaseDate == null || releaseDate.isNull())
			return 0;
		
		// Converts the Unix timestamp into a calendar year
		return Instant.ofEpochSecond(releaseDate.asLong())
				.atZone(ZoneId.systemDefault())
				.getYear();
	}
	
	/**
	 * Safely retrieves a text value from a JSON field.
	 *
	 * @param node the parent JSON node
	 * @param fieldName the field to retrieve
	 * @return the field value, or an empty string if unavailable
	 */
	private String getTextValue(JsonNode node, String fieldName) {
		
		// Safely retrieves a text value from a JSON field
		JsonNode value = node.get(fieldName);

		if(value == null || value.isNull())
			return "";

		return value.asText();
	}
	
	/**
	 * Safely retrieves an integer value from a JSON field.
	 *
	 * @param node the parent JSON node
	 * @param fieldName the field to retrieve
	 * @return the field value, or 0 if unavailable
	 */
	private int getIntValue(JsonNode node, String fieldName) {
		
		// Safely retrieves an integer value from a JSON field
		JsonNode value = node.get(fieldName);

		if(value == null || value.isNull())
			return 0;

		return value.asInt();
	}
}