package application.controller;

import java.io.IOException;
import java.sql.Connection;

import application.dao.impl.SeasonDAOImpl;
import application.model.MediaPlaylist;
import application.model.Season;
import application.model.Show;
import application.model.Type;
import application.model.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SeasonDetailsController extends BaseMediaPageController {

	@FXML
	private Button backButton;

	@FXML
	private Button viewEpisodesButton;

	@FXML
	private Button deleteSeasonButton;

	@FXML
	private Button homeButton;

	@FXML
	private Text pageLabel;

	@FXML
	private ImageView mediaLogo;

	@FXML
	private Label showTitleLabel;

	@FXML
	private Label seasonNumberLabel;

	private SeasonDAOImpl seasonDAO;
	private MediaPlaylist playlist;
	private Show show;
	private Season season;
	private boolean pictureLoaded;

	@FXML
	public void initialize() {
		initializeBase();
		setupView(Type.SHOW);

		initializePicture();
		initializeButtons();
		initializeNavigationBar();
	}

	private void initializePicture() {
		if(mediaLogo != null) {
			Rectangle clip = new Rectangle();
			clip.setWidth(410);
			clip.setHeight(410);
			clip.setArcWidth(45);
			clip.setArcHeight(45);

			mediaLogo.setClip(clip);
			mediaLogo.setFitWidth(410);
			mediaLogo.setFitHeight(410);
			mediaLogo.setPreserveRatio(false);
			mediaLogo.setSmooth(true);
		}
	}

	private void initializeButtons() {
		makeNavigationButton(
			backButton,
			"/resources/application/images/icons/back-reply-svgrepo-com.png",
			"Back",
			this::goBack
		);

		makeNavigationButton(
			viewEpisodesButton,
			"/resources/application/images/icons/view-icon.png",
			"View Episodes",
			this::viewEpisodes
		);

		makeNavigationButton(
			homeButton,
			"/resources/application/images/icons/home-icon-svgrepo-com.png",
			"Home",
			() -> switchScene("/resources/application/fxml/Menu.fxml")
		);

		deleteSeasonButton.setVisible(false);
		deleteSeasonButton.setManaged(false);

		backButton.setTranslateY(-15);
		viewEpisodesButton.setTranslateY(-15);
		homeButton.setTranslateY(-15);
	}

	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		seasonDAO = new SeasonDAOImpl(conn, UserSession.getCurrentUserId());
	}

	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;
	}

	public void setShow(Show show) {
		this.show = show;
		updateSeasonDetails();
	}

	public void setSeason(Season season) {
		this.season = season;
		pictureLoaded = false;
		updateSeasonDetails();
	}

	private void updateSeasonDetails() {
		if(show != null)
			showTitleLabel.setText("Show: " + show.getTitle());

		if(season != null) {
			pageLabel.setText(season.getTitle());
			seasonNumberLabel.setText("Season Number: " + season.getSeasonNumber());

			if(!pictureLoaded)
				loadSeasonPicture();
		}
	}

	private void loadSeasonPicture() {
		if(mediaLogo != null && season != null) {
			Image image = null;
			boolean defaultImage = false;
			String imagePath = season.getImagePath();

			if(imagePath != null && !imagePath.isBlank())
				image = loadImage(imagePath);

			if(image == null && show != null) {
				String showImagePath = show.getImagePath();

				if(showImagePath != null && !showImagePath.isBlank())
					image = loadImage(showImagePath);
			}

			if(image == null) {
				defaultImage = true;
				image = loadImage("/resources/application/images/icons/default-show-icon.png");
			}

			if(image != null) {
				if(mediaLogo.getParent() instanceof StackPane) {
					StackPane container = (StackPane)mediaLogo.getParent();

					if(defaultImage)
						container.getStyleClass().remove("media-art-border");
					else if(!container.getStyleClass().contains("media-art-border"))
						container.getStyleClass().add("media-art-border");
				}

				mediaLogo.setViewport(null);
				mediaLogo.setImage(image);
				fillImage(mediaLogo);
				pictureLoaded = true;
			}
		}
	}

	private void fillImage(ImageView imageView) {
		Image image = imageView.getImage();

		if(image != null && image.getWidth() > 0 && image.getHeight() > 0) {
			double imageRatio = image.getWidth()/image.getHeight();
			double viewRatio = imageView.getFitWidth()/imageView.getFitHeight();

			if(imageRatio > viewRatio) {
				double width = viewRatio/imageRatio;

				imageView.setViewport(new Rectangle2D(
					(image.getWidth() - image.getWidth()*width)/2,
					0,
					image.getWidth()*width,
					image.getHeight()
				));
			}
			else {
				double height = imageRatio/viewRatio;

				imageView.setViewport(new Rectangle2D(
					0,
					(image.getHeight() - image.getHeight()*height)/2,
					image.getWidth(),
					image.getHeight()*height
				));
			}
		}
	}

	private void viewEpisodes() {
		if(show != null && season != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/EpisodesTableScene.fxml"));
				Parent root = loader.load();

				EpisodesTableController controller = loader.getController();
				controller.setConnection(conn);
				controller.setPlaylist(playlist);
				controller.setShow(show);
				controller.setSeason(season);
				controller.setupView(Type.SHOW);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void goBack() {
		if(show != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/SeasonsScene.fxml"));
				Parent root = loader.load();

				SeasonsTableController controller = loader.getController();
				controller.setConnection(conn);
				controller.setPlaylist(playlist);
				controller.setShow(show);
				controller.setupView(Type.SHOW);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void loadTableData() {
	}
}