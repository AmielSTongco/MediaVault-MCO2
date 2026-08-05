package application.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.dao.MediaDAO;
import application.model.Media;
import application.model.UserSession;
import application.view.MediaTableOwner;
import application.view.TableBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class AllMediasDisplayController extends BaseMediaPageController implements MediaTableOwner {
	
	/*
	 * Controls the scene which displays all songs,
	 * games, and shows belonging to the current user
	 */
	
	@FXML
	private Button homeButton;

	@FXML
	private TableView<Media> mediaTable;

	@FXML
	private TableColumn<Media, Number> numberColumn;

	@FXML
	private TableColumn<Media, Media> titleColumn;

	@FXML
	private TableColumn<Media, String> creatorColumn;

	@FXML
	private TableColumn<Media, String> yearColumn;

	@FXML
	private TableColumn<Media, String> statusColumn;

	@FXML
	private TableColumn<Media, String> ratingColumn;

	@FXML
	private TableColumn<Media, String> reviewColumn;

	@FXML
	private TableColumn<Media, String> infoColumn;

	private MediaDAO mediaDAO;

	/**
	 * Initializes shared page elements, navigation buttons, and media table.
	 */
	@FXML
	public void initialize() {
		initializeBase();
		
		rootStackPane.getStyleClass().removeAll("theme-songs", "theme-games", "theme-shows", "theme-media");
		rootStackPane.getStyleClass().add("theme-media");
		mediaLabel.setText("Media");

		makeNavigationButton(homeButton, "/resources/application/images/icons/home-icon-svgrepo-com.png", "Home", this::goHome);

		initializeNavigationBar();
		TableBuilder.createMediaTable(this);
	}
	
	/**
	 * Returns to the main menu while preserving the active
	 * database connection.
	 */
	private void goHome() {
		try {
			// Loads the menu scene
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/Menu.fxml"));
			Parent root = loader.load();

			// Passes the active database connection to the menu
			MenuController controller = loader.getController();
			controller.setConnection(conn);

			// Displays the loaded scene
			Stage stage = (Stage)rootPane.getScene().getWindow();
			stage.getScene().setRoot(root);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets database connection and initializes media data access.
	 *
	 * @param conn active database connection
	 */
	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		mediaDAO = new MediaDAO(conn, UserSession.getCurrentUserId());
		loadTableData();
	}

	/**
	 * Loads every song, game, and show owned by the current user.
	 */
	@Override
	protected void loadTableData() {
		if(mediaDAO != null)
		{
			try {
				List<Media> allMedia = new ArrayList<>();

				// Combines all media types into one list
				allMedia.addAll(mediaDAO.getSongsByUser());
				allMedia.addAll(mediaDAO.getGamesByUser());
				allMedia.addAll(mediaDAO.getShowsByUser());

				mediaTable.getItems().setAll(allMedia);
			}
			catch(SQLException e) {
				mediaTable.getItems().clear();
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns media table.
	 *
	 * @return media table
	 */
	@Override
	public TableView<Media> getMediaTable() {
		return mediaTable;
	}

	/**
	 * Returns media number column.
	 *
	 * @return media number column
	 */
	@Override
	public TableColumn<Media, Number> getNumberColumn() {
		return numberColumn;
	}

	/**
	 * Returns media title column.
	 *
	 * @return media title column
	 */
	@Override
	public TableColumn<Media, Media> getTitleColumn() {
		return titleColumn;
	}

	/**
	 * Returns media creator column.
	 *
	 * @return media creator column
	 */
	@Override
	public TableColumn<Media, String> getCreatorColumn() {
		return creatorColumn;
	}

	/**
	 * Returns media release year column.
	 *
	 * @return media release year column
	 */
	@Override
	public TableColumn<Media, String> getYearColumn() {
		return yearColumn;
	}

	/**
	 * Returns media status column.
	 *
	 * @return media status column
	 */
	@Override
	public TableColumn<Media, String> getStatusColumn() {
		return statusColumn;
	}

	/**
	 * Returns media rating column.
	 *
	 * @return media rating column
	 */
	@Override
	public TableColumn<Media, String> getRatingColumn() {
		return ratingColumn;
	}

	/**
	 * Returns media review column.
	 *
	 * @return media review column
	 */
	@Override
	public TableColumn<Media, String> getReviewColumn() {
		return reviewColumn;
	}

	/**
	 * Returns media information column.
	 *
	 * @return media information column
	 */
	@Override
	public TableColumn<Media, String> getInfoColumn() {
		return infoColumn;
	}
}