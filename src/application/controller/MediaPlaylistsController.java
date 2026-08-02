package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import application.dao.MediaPlaylistDAO;
import application.model.MediaPlaylist;
import application.model.Type;
import application.model.UserSession;
import application.view.PlaylistTableOwner;
import application.view.TableBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MediaPlaylistsController extends BaseMediaPageController implements PlaylistTableOwner<MediaPlaylist> {
	
	/*
	 * Controls the scene which displays and manages
	 * media playlists of the selected media type
	 */

	@FXML
	private Button addButton;

	@FXML
	private Button homeButton;

	@FXML
	private TableView<MediaPlaylist> mediaPlaylistTable;

	@FXML
	private TableColumn<MediaPlaylist, Number> numberColumn;

	@FXML
	private TableColumn<MediaPlaylist, MediaPlaylist> titleColumn;

	@FXML
	private TableColumn<MediaPlaylist, String> totalColumn;

	@FXML
	private TableColumn<MediaPlaylist, String> completedColumn;

	@FXML
	private TableColumn<MediaPlaylist, String> inProgressColumn;

	@FXML
	private TableColumn<MediaPlaylist, String> plannedColumn;

	@FXML
	private TableColumn<MediaPlaylist, String> avgRatingColumn;

	private MediaPlaylistDAO mediaPlaylistDAO;
	
	/**
	 * Initializes shared page elements, navigation buttons, playlist table, and row reordering.
	 */
	@FXML
	public void initialize() {
		initializeBase();
		
		// Creates navigation buttons
		makeNavigationButton(addButton, "/resources/application/images/icons/plus-svgrepo-com.png", "Add Playlist", this::addPlaylist);
		makeNavigationButton(homeButton, "/resources/application/images/icons/home-icon-svgrepo-com.png", "Home", () -> switchScene("/resources/application/fxml/Menu.fxml"));
		
		initializeNavigationBar();

		TableBuilder.createPlaylistTable(this);
		
		// Enables reordering for custom playlists
		TableBuilder.enableRowReordering(mediaPlaylistTable, playlist -> !isDefaultPlaylist(playlist), this::savePlaylistOrder);
		
		handleDoubleClick(mediaPlaylistTable, this::openPlaylist);
	}
	
	/**
	 * Sets database connection and initializes playlist data access.
	 *
	 * @param conn active database connection
	 */
	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		mediaPlaylistDAO = new MediaPlaylistDAO(conn, UserSession.getCurrentUserId());
		loadTableData();
	}
	
	/**
	 * Applies selected media type and reloads playlist data.
	 *
	 * @param mediaType selected media type
	 */
	@Override
	public void setupView(Type mediaType) {
		super.setupView(mediaType);
		loadTableData();
	}
	
	/**
	 * Loads playlists belonging to the current user and media type.
	 */
	@Override
	protected void loadTableData() {
		if(conn != null && mediaType != null)
		{
			if(mediaPlaylistDAO == null)
				mediaPlaylistDAO = new MediaPlaylistDAO(conn, UserSession.getCurrentUserId());

			try {
				List<MediaPlaylist> playlists = mediaPlaylistDAO.getPlaylistsByUser(UserSession.getCurrentUserId(), mediaType);
				mediaPlaylistTable.getItems().setAll(playlists);
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Opens selected playlist and displays its media items.
	 *
	 * @param playlist selected media playlist
	 */
	private void openPlaylist(MediaPlaylist playlist) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/MediaPlaylistsItemsScene.fxml"));
			Parent root = loader.load();

			MediaPlaylistsItemsController controller = loader.getController();
			controller.setConnection(conn);
			controller.setPlaylist(playlist);
			controller.setupView(mediaType);

			Stage stage = (Stage)rootPane.getScene().getWindow();
			stage.getScene().setRoot(root);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens popup for creating a new playlist.
	 */
	private void addPlaylist() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/AddPlaylist.fxml"));
			StackPane popup = loader.load();

			AddPlaylistController controller = loader.getController();
			controller.setConnection(conn);
			controller.setMediaType(mediaType);
			
			// Removes popup and refreshes playlist table
			controller.setCloseAction(() -> {
				rootStackPane.getChildren().remove(popup);
				loadTableData();
			});

			rootStackPane.getChildren().add(popup);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks whether a playlist is the default playlist for the current media type.
	 *
	 * @param playlist playlist to check
	 * @return true if playlist is default, otherwise false
	 */
	private boolean isDefaultPlaylist(MediaPlaylist playlist) {
		String defaultTitle = "all_" + mediaType.getTitle().toLowerCase();
		return playlist.getTitle().equals(defaultTitle);
	}
	
	/**
	 * Saves current playlist display order.
	 */
	private void savePlaylistOrder() {
		try {
			mediaPlaylistDAO.updatePlaylistOrder(mediaPlaylistTable.getItems(), mediaType);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns media playlist table.
	 *
	 * @return media playlist table
	 */
	@Override
	public TableView<MediaPlaylist> getMediaPlaylistTable() {
		return mediaPlaylistTable;
	}
	
	/**
	 * Returns playlist number column.
	 *
	 * @return playlist number column
	 */
	@Override
	public TableColumn<MediaPlaylist, Number> getNumberColumn() {
		return numberColumn;
	}
	
	/**
	 * Returns playlist title column.
	 *
	 * @return playlist title column
	 */
	@Override
	public TableColumn<MediaPlaylist, MediaPlaylist> getTitleColumn() {
		return titleColumn;
	}
	
	/**
	 * Returns total media column.
	 *
	 * @return total media column
	 */
	@Override
	public TableColumn<MediaPlaylist, String> getTotalColumn() {
		return totalColumn;
	}
	
	/**
	 * Returns completed media column.
	 *
	 * @return completed media column
	 */
	@Override
	public TableColumn<MediaPlaylist, String> getCompletedColumn() {
		return completedColumn;
	}
	
	/**
	 * Returns in-progress media column.
	 *
	 * @return in-progress media column
	 */
	@Override
	public TableColumn<MediaPlaylist, String> getInProgressColumn() {
		return inProgressColumn;
	}
	
	/**
	 * Returns planned media column.
	 *
	 * @return planned media column
	 */
	@Override
	public TableColumn<MediaPlaylist, String> getPlannedColumn() {
		return plannedColumn;
	}
	
	/**
	 * Returns average rating column.
	 *
	 * @return average rating column
	 */
	@Override
	public TableColumn<MediaPlaylist, String> getAvgRatingColumn() {
		return avgRatingColumn;
	}
}