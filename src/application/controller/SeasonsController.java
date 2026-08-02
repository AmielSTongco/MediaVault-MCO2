package application.controller;

import java.sql.Connection;
import java.sql.SQLException;

import application.dao.impl.SeasonDAOImpl;
import application.model.Season;
import application.model.Show;
import application.view.PlaylistTableOwner;
import application.view.TableBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import application.model.UserSession;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class SeasonsController extends BaseMediaPageController implements PlaylistTableOwner<Season> {
	
	@FXML
	private Button backButton;

	@FXML
	private Button addButton;

	@FXML
	private Button viewShowButton;

	@FXML
	private Button deleteShowButton;

	@FXML
	private Button homeButton;
	
	@FXML
	private Text pageLabel;
	
	@FXML
	private TableView<Season> mediaPlaylistTable;

	@FXML
	private TableColumn<Season, Number> numberColumn;

	@FXML
	private TableColumn<Season, Season> titleColumn;

	@FXML
	private TableColumn<Season, String> totalColumn;

	@FXML
	private TableColumn<Season, String> completedColumn;

	@FXML
	private TableColumn<Season, String> inProgressColumn;

	@FXML
	private TableColumn<Season, String> plannedColumn;

	@FXML
	private TableColumn<Season, String> avgRatingColumn;

	private final ObservableList<Season> seasons = FXCollections.observableArrayList();

	private SeasonDAOImpl seasonDAO;
	private Show show;

	@FXML
	public void initialize() {
		initializeBase();
		
		makeNavigationButton(
				backButton,
				"/resources/application/images/icons/back-reply-svgrepo-com.png",
				"Back",
				this::goBack
			);

			makeNavigationButton(
				addButton,
				"/resources/application/images/icons/plus-svgrepo-com.png",
				"Add Season Manually",
				this::addSeasonManually
			);

			makeNavigationButton(
				viewShowButton,
				"/resources/application/images/icons/view-details-icon.png",
				"View Show Details",
				this::viewShowDetails
			);

			makeNavigationButton(
				deleteShowButton,
				"/resources/application/images/icons/delete-icon.png",
				"Delete Show",
				this::deleteShow
			);

			makeNavigationButton(
				homeButton,
				"/resources/application/images/icons/home-icon-svgrepo-com.png",
				"Home",
				() -> switchScene("/resources/application/fxml/Menu.fxml")
			);
			
			initializeNavigationBar();
		
		TableBuilder.createPlaylistTable(this);
		mediaPlaylistTable.setItems(seasons);
		handleDoubleClick(mediaPlaylistTable, this::openSeason);
	}
	
	private void addSeasonManually() {
		// Open the manual season form.
	}

	private void viewShowDetails() {
		// Open the selected show's details.
	}

	private void deleteShow() {
		// Confirm and delete the current show.
	}

	private void goBack() {
		// Return to the show's playlist.
	}

	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		seasonDAO = new SeasonDAOImpl(conn, UserSession.getCurrentUserId());
	}

	public void setShow(Show show) {
		this.show = show;
		loadTableData();
	}

	@Override
	protected void loadTableData() {
		if(show != null && seasonDAO != null) {
			try {
				seasons.setAll(seasonDAO.getSeasonsByShowId(show.getMediaId()));
			}
			catch(SQLException e) {
				seasons.clear();
				e.printStackTrace();
			}
		}
	}

	private void openSeason(Season season) {
		if(season != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/EpisodesScene.fxml"));
				Parent root = loader.load();

				EpisodesController controller = loader.getController();
				controller.setConnection(conn);
				controller.setShow(show);
				controller.setSeason(season);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public TableView<Season> getMediaPlaylistTable() {
		return mediaPlaylistTable;
	}

	@Override
	public TableColumn<Season, Number> getNumberColumn() {
		return numberColumn;
	}

	@Override
	public TableColumn<Season, Season> getTitleColumn() {
		return titleColumn;
	}

	@Override
	public TableColumn<Season, String> getTotalColumn() {
		return totalColumn;
	}

	@Override
	public TableColumn<Season, String> getCompletedColumn() {
		return completedColumn;
	}

	@Override
	public TableColumn<Season, String> getInProgressColumn() {
		return inProgressColumn;
	}

	@Override
	public TableColumn<Season, String> getPlannedColumn() {
		return plannedColumn;
	}

	@Override
	public TableColumn<Season, String> getAvgRatingColumn() {
		return avgRatingColumn;
	}
}