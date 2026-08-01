package application.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import application.dao.impl.MediaPlaylistDAOImpl;
import application.model.MediaPlaylist;
import application.model.Type;
import application.model.UserSession;
import application.view.PlaylistTableOwner;
import application.view.TableBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

public class MediaPlaylistsController extends BaseMediaPageController implements PlaylistTableOwner {

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

	private MediaPlaylistDAOImpl mediaPlaylistDAO;

	@FXML
	public void initialize() {
		initializeBase();

		makeNavigationButton(
				addButton,
				"/resources/application/images/icons/plus-svgrepo-com.png",
				"Add Playlist",
				this::addPlaylist
			);

		makeNavigationButton(
			homeButton,
			"/resources/application/images/icons/home-icon-svgrepo-com.png",
			"Home",
			() -> switchScene("/resources/application/fxml/Menu.fxml")
		);
		
		initializeNavigationBar();

		TableBuilder.createPlaylistTable(this);
	}

	@Override
	public void setConnection(java.sql.Connection conn) {
		super.setConnection(conn);
		mediaPlaylistDAO = new MediaPlaylistDAOImpl(conn, UserSession.getCurrentUserId());
		loadTableData();
	}

	@Override
	public void setupView(Type mediaType) {
		super.setupView(mediaType);
		loadTableData();
	}

	@Override
	protected void loadTableData() {
		if(conn != null && mediaType != null) {
			if(mediaPlaylistDAO == null)
				mediaPlaylistDAO = new MediaPlaylistDAOImpl(conn, UserSession.getCurrentUserId());

			try {
				List<MediaPlaylist> playlists = mediaPlaylistDAO.getPlaylistsByUser(UserSession.getCurrentUserId(), mediaType);
				mediaPlaylistTable.getItems().setAll(playlists);
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void addPlaylist() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/AddPlaylist.fxml"));
			StackPane popup = loader.load();

			AddPlaylistController controller = loader.getController();
			controller.setConnection(conn);
			controller.setMediaType(mediaType);
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

	@Override
	public TableView<MediaPlaylist> getMediaPlaylistTable() {
		return mediaPlaylistTable;
	}

	@Override
	public TableColumn<MediaPlaylist, Number> getNumberColumn() {
		return numberColumn;
	}

	@Override
	public TableColumn<MediaPlaylist, MediaPlaylist> getTitleColumn() {
		return titleColumn;
	}

	@Override
	public TableColumn<MediaPlaylist, String> getTotalColumn() {
		return totalColumn;
	}

	@Override
	public TableColumn<MediaPlaylist, String> getCompletedColumn() {
		return completedColumn;
	}

	@Override
	public TableColumn<MediaPlaylist, String> getInProgressColumn() {
		return inProgressColumn;
	}

	@Override
	public TableColumn<MediaPlaylist, String> getPlannedColumn() {
		return plannedColumn;
	}

	@Override
	public TableColumn<MediaPlaylist, String> getAvgRatingColumn() {
		return avgRatingColumn;
	}
}