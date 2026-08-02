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
import application.model.Episode;

public class ShowAPIClient {

	private static final String BASE_URL = "https://api.themoviedb.org/3";
	private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w500";

	private final String accessToken;
	private final HttpClient client;
	private final ObjectMapper mapper;

	public ShowAPIClient(String accessToken) {
		this.accessToken = accessToken;
		this.client = HttpClient.newHttpClient();
		this.mapper = new ObjectMapper();
	}

	public List<Show> searchShows(String query) throws IOException, InterruptedException {
		String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

		String url = BASE_URL + "/search/tv?query=" + encodedQuery
				   + "&include_adult=false"
				   + "&language=en-US"
				   + "&page=1";

		JsonNode root = sendRequest(url);
		List<Show> shows = new ArrayList<>();

		JsonNode results = root.get("results");

		if(results == null || !results.isArray())
			return shows;

		int resultCount = Math.min(results.size(), 10);

		for(int i = 0; i < resultCount; i++) {
			JsonNode item = results.get(i);
			int apiId = getIntValue(item, "id");
			JsonNode showDetails = getShowDetails(apiId);

			String title = getTextValue(item, "name");
			String creator = getCreator(showDetails);
			int yearStart = getReleaseYear(showDetails, "first_air_date");
			int yearEnd = getReleaseYear(showDetails, "last_air_date");
			String genre = getGenre(showDetails);
			int numOfSeasons = getIntValue(showDetails, "number_of_seasons");
			boolean airing = getBooleanValue(showDetails, "in_production");
			String imagePath = buildImagePath(getTextValue(item, "poster_path"));

			List<String> seasonImagePaths = getSeasonImagePaths(showDetails);

			Show show = new Show(
				title,
				creator,
				yearStart,
				yearEnd,
				Status.PLANNED,
				0.0,
				"",
				genre,
				numOfSeasons,
				airing,
				imagePath
			);

			show.setApiId(apiId);
			show.setSeasonImagePaths(seasonImagePaths);
			shows.add(show);
		}

		return shows;
	}

	private JsonNode getShowDetails(int showId) throws IOException, InterruptedException {
		String url = BASE_URL + "/tv/" + showId + "?language=en-US";
		return sendRequest(url);
	}

	private JsonNode getSeasonDetails(int showId, int seasonNumber) throws IOException, InterruptedException {
		String url = BASE_URL + "/tv/" + showId + "/season/" + seasonNumber + "?language=en-US";
		return sendRequest(url);
	}

	private JsonNode sendRequest(String url) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Authorization", "Bearer " + accessToken)
				.header("accept", "application/json")
				.GET()
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if(response.statusCode() != 200)
			throw new IOException("TMDB request failed with status code " + response.statusCode() + ": " + response.body());

		return mapper.readTree(response.body());
	}
	
	public List<Episode> getEpisodes(int showApiId, int seasonNumber) throws IOException, InterruptedException {
		JsonNode seasonDetails = getSeasonDetails(showApiId, seasonNumber);
		List<Episode> episodes = new ArrayList<>();
		JsonNode results = seasonDetails.get("episodes");

		if(results != null && results.isArray()) {
			for(JsonNode item : results) {
				int episodeNumber = getIntValue(item, "episode_number");
				String title = getTextValue(item, "name");
				String writer = getEpisodeWriters(item);
				int yearReleased = getReleaseYear(item, "air_date");
				String imagePath = buildImagePath(getTextValue(item, "still_path"));

				Episode episode = new Episode(0, 0, episodeNumber, title, writer, yearReleased, Status.PLANNED, 0.0, "", imagePath);
				episodes.add(episode);
			}
		}

		return episodes;
	}
	
	private String getEpisodeWriters(JsonNode episode) {
		JsonNode crew = episode.get("crew");

		if(crew == null || !crew.isArray())
			return "/--/";

		String writers = "";

		for(JsonNode crewMember : crew) {
			String job = getTextValue(crewMember, "job");

			if(job.equalsIgnoreCase("Writer") || job.equalsIgnoreCase("Screenplay") || job.equalsIgnoreCase("Teleplay") || job.equalsIgnoreCase("Story")) {
				String name = getTextValue(crewMember, "name");

				if(!name.isBlank() && !writers.contains(name)) {
					if(!writers.isBlank())
						writers += ", ";

					writers += name;
				}
			}
		}

		if(writers.isBlank())
			return "/--/";

		return writers;
	}
	
	private List<String> getSeasonImagePaths(JsonNode showDetails) {
		List<String> seasonImagePaths = new ArrayList<>();
		JsonNode seasons = showDetails.get("seasons");

		if(seasons != null && seasons.isArray()) {
			for(JsonNode season : seasons) {
				int seasonNumber = getIntValue(season, "season_number");

				if(seasonNumber > 0) {
					while(seasonImagePaths.size() < seasonNumber)
						seasonImagePaths.add("");

					String posterPath = getTextValue(season, "poster_path");
					seasonImagePaths.set(seasonNumber - 1, buildImagePath(posterPath));
				}
			}
		}

		return seasonImagePaths;
	}

	private String buildImagePath(String filePath) {
		if(filePath == null || filePath.isBlank())
			return "";

		return IMAGE_URL + filePath;
	}

	private String getCreator(JsonNode node) {
		JsonNode creators = node.get("created_by");

		if(creators == null || !creators.isArray() || creators.isEmpty())
			return "";

		String creator = "";

		for(int i = 0; i < creators.size(); i++) {
			if(i > 0)
				creator += ", ";

			creator += getTextValue(creators.get(i), "name");
		}

		return creator;
	}

	private String getGenre(JsonNode node) {
		JsonNode genres = node.get("genres");

		if(genres == null || !genres.isArray() || genres.isEmpty())
			return "";

		String genre = "";

		for(int i = 0; i < genres.size(); i++) {
			if(i > 0)
				genre += ", ";

			genre += getTextValue(genres.get(i), "name");
		}

		return genre;
	}

	private int getReleaseYear(JsonNode node, String fieldName) {
		String date = getTextValue(node, fieldName);

		if(date.length() < 4)
			return 0;

		try {
			return Integer.parseInt(date.substring(0, 4));
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}

	private String getTextValue(JsonNode node, String fieldName) {
		if(node == null)
			return "";

		JsonNode value = node.get(fieldName);

		if(value == null || value.isNull())
			return "";

		return value.asText();
	}

	private int getIntValue(JsonNode node, String fieldName) {
		if(node == null)
			return 0;

		JsonNode value = node.get(fieldName);

		if(value == null || value.isNull())
			return 0;

		return value.asInt();
	}

	private boolean getBooleanValue(JsonNode node, String fieldName) {
		if(node == null)
			return false;

		JsonNode value = node.get(fieldName);

		if(value == null || value.isNull())
			return false;

		return value.asBoolean();
	}
}