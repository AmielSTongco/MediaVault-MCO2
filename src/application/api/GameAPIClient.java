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
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import application.model.Game;
import application.model.Status;

public class GameAPIClient {

	private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
	private static final String GAMES_URL = "https://api.igdb.com/v4/games";
	private static final String IMAGE_URL = "https://images.igdb.com/igdb/image/upload/t_cover_big/";

	private final String clientId;
	private final String clientSecret;
	private final HttpClient client;
	private final ObjectMapper mapper;

	private String accessToken;
	private long tokenExpiration;

	public GameAPIClient(String clientId, String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.client = HttpClient.newHttpClient();
		this.mapper = new ObjectMapper();
	}

	public List<Game> searchGames(String query) throws IOException, InterruptedException {
		String token = getAccessToken();
		String safeQuery = query.replace("\\", "\\\\").replace("\"", "\\\"");

		String body = "search \"" + safeQuery + "\"; "
					+ "fields name, first_release_date, cover.image_id, genres.name, "
					+ "involved_companies.company.name, involved_companies.developer; "
					+ "where version_parent = null; "
					+ "limit 10;";

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(GAMES_URL))
				.header("Client-ID", clientId)
				.header("Authorization", "Bearer " + token)
				.header("Content-Type", "text/plain")
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if(response.statusCode() != 200)
			throw new IOException("IGDB request failed with status code " + response.statusCode() + ": " + response.body());

		List<Game> games = new ArrayList<>();
		JsonNode results = mapper.readTree(response.body());

		if(!results.isArray())
			return games;

		for(JsonNode item : results) {
			String title = getTextValue(item, "name");
			String creator = getDeveloper(item);
			String genre = getGenre(item);
			String imagePath = getImagePath(item);
			int yearReleased = getReleaseYear(item);
			int avgPlaytimeMins = 0;

			Status status = Status.PLANNED;
			double userRating = 0.0;
			String review = "";

			Game game = new Game(
				title,
				creator,
				yearReleased,
				status,
				userRating,
				review,
				genre,
				avgPlaytimeMins,
				imagePath
			);

			game.setMediaId(getIntValue(item, "id"));
			games.add(game);
		}

		return games;
	}

	private String getAccessToken() throws IOException, InterruptedException {
		long currentTime = System.currentTimeMillis();

		if(accessToken == null || currentTime >= tokenExpiration) {
			String url = TOKEN_URL
					   + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
					   + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
					   + "&grant_type=client_credentials";

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.POST(HttpRequest.BodyPublishers.noBody())
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if(response.statusCode() != 200)
				throw new IOException("Twitch authentication failed with status code " + response.statusCode() + ": " + response.body());

			JsonNode root = mapper.readTree(response.body());

			accessToken = getTextValue(root, "access_token");

			long expiresIn = root.has("expires_in")
					? root.get("expires_in").asLong()
					: 3600;

			tokenExpiration = currentTime + Math.max(0, expiresIn - 60) * 1000;
		}

		return accessToken;
	}

	private String getDeveloper(JsonNode item) {
		JsonNode companies = item.get("involved_companies");

		if(companies != null && companies.isArray()) {
			for(JsonNode involvedCompany : companies) {
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

	private String getGenre(JsonNode item) {
		JsonNode genres = item.get("genres");

		if(genres != null && genres.isArray() && !genres.isEmpty()) {
			JsonNode firstGenre = genres.get(0);

			if(firstGenre.has("name"))
				return firstGenre.get("name").asText();
		}

		return "";
	}

	private String getImagePath(JsonNode item) {
		JsonNode cover = item.get("cover");

		if(cover != null && cover.has("image_id")) {
			String imageId = cover.get("image_id").asText();

			if(!imageId.isBlank())
				return IMAGE_URL + imageId + ".jpg";
		}

		return "";
	}

	private int getReleaseYear(JsonNode item) {
		JsonNode releaseDate = item.get("first_release_date");

		if(releaseDate == null || releaseDate.isNull())
			return 0;

		return Instant.ofEpochSecond(releaseDate.asLong())
				.atZone(ZoneId.systemDefault())
				.getYear();
	}

	private String getTextValue(JsonNode node, String fieldName) {
		JsonNode value = node.get(fieldName);

		if(value == null || value.isNull())
			return "";

		return value.asText();
	}

	private int getIntValue(JsonNode node, String fieldName) {
		JsonNode value = node.get(fieldName);

		if(value == null || value.isNull())
			return 0;

		return value.asInt();
	}
}