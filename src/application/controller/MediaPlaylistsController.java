package application.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application.model.Media;
import application.model.Song;
import application.model.MediaPlaylist;
import application.model.Type;
import application.model.Status;
import application.model.UserSession;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ComboBox;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import java.io.File;

public class MediaPlaylistsController implements Initializable {

    @FXML 
    private TableView<MediaPlaylist> tableView;
    
    @FXML 
    private TextField songName;
    
    @FXML 
    private Label pageLabel;
    
    @FXML
    private ImageView profileAvatar;
    
    @FXML
    private Label mediaLabel;
    
    @FXML 
    private ImageView mediaVaultLogo;
    
    @FXML
    private ImageView mediaVaultTitle;
    
    @FXML
    private StackPane rootPane;
    
    @FXML
    private ImageView settingsIcon;
    
    // For Expandable Navigation Bar
 	private static final int deltaXNavButton1 = 10;
 	private static final int deltaXNavButton2 = -10;
  
 	@FXML
 	private HBox extendableNavigationPane;
    
    @FXML
	private Button addButton;
 
	@FXML
	private Button homeButton;
	
    @FXML
    private Label userName;
    
    @FXML
    private ImageView mediaLogo;
    
    @FXML
    private TableView<Media> mediaTable;

    @FXML
    private TableColumn<Media, Void> dragColumn;

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
 
	private Rectangle clipRect;
 
	private DropShadow dropShadowForSelectedPane;
	
	private static final int navigationYOffset = 20;
	
	private Connection conn;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		String username = UserSession.getCurrentUsername();
		
		Image logoImg = new Image(getClass().getResourceAsStream("/resources/application/images/logos/logo.png"));
        Image titleImg = new Image(getClass().getResourceAsStream("/resources/application/images/logos/title.png"));
        Image settingsImg = new Image(getClass().getResourceAsStream("/resources/application/images/icons/settings-gear-svgrepo-com.png"));
        Image profileImg = new Image(getClass().getResourceAsStream("/resources/application/images/default/default-profile.png"));
        Image mediaIcon = new Image(getClass().getResourceAsStream("/resources/application/images/icons/songs-icon.png"));
        
        // Assign images to ImageView nodes
        mediaVaultLogo.setImage(logoImg);
        mediaVaultTitle.setImage(titleImg);
        settingsIcon.setImage(settingsImg);
        profileAvatar.setImage(profileImg);
        mediaLogo.setImage(mediaIcon);
        
        mediaTable.setPrefWidth(1360);
        mediaTable.setPrefHeight(550);
        mediaTable.setMinHeight(550);
        mediaTable.setMaxHeight(550);
        
        userName.setText(username);
        
        clipRect = new Rectangle();
		clipRect.setWidth(extendableNavigationPane.getPrefWidth());
		setIcon(addButton, "/resources/application/images/icons/plus-svgrepo-com.png");
		setIcon(homeButton, "/resources/application/images/icons/home-icon-svgrepo-com.png");
		hidePane();
 
		dropShadowForSelectedPane = new DropShadow(BlurType.THREE_PASS_BOX, Color.BLUE, 7, 0.2, 0, 1);
		
		DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setOffsetY(5);
        shadow.setColor(Color.color(0, 0, 0, 0.4));
        
        Light.Distant light = new Light.Distant();
        light.setAzimuth(-135);
        
        Lighting lighting = new Lighting();
        lighting.setLight(light);
        lighting.setDiffuseConstant(1.45);
        lighting.setSurfaceScale(1);
        
        shadow.setInput(lighting);
        
        mediaLabel.setEffect(shadow);
        
        mediaTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        setupColumnWidths();
        setupHeaders();
        setupTableCells();
        loadTestData();
	}
	
	private void setupColumnWidths() {

		numberColumn.setPrefWidth(70);
		numberColumn.getStyleClass().add("number-column");
		titleColumn.setPrefWidth(310);
		creatorColumn.setPrefWidth(230);
		yearColumn.setPrefWidth(130);
		statusColumn.setPrefWidth(220);
		ratingColumn.setPrefWidth(90);
		reviewColumn.setPrefWidth(220);
		infoColumn.setPrefWidth(300);

		numberColumn.setResizable(false);
		titleColumn.setResizable(false);
		creatorColumn.setResizable(false);
		yearColumn.setResizable(false);
		statusColumn.setResizable(false);
		ratingColumn.setResizable(false);
		reviewColumn.setResizable(false);
		infoColumn.setResizable(false);
		
		numberColumn.setReorderable(false);
		titleColumn.setReorderable(false);
		creatorColumn.setReorderable(false);
		yearColumn.setReorderable(false);
		statusColumn.setReorderable(false);
		ratingColumn.setReorderable(false);
		reviewColumn.setReorderable(false);
		infoColumn.setReorderable(false);
	}
	
	private void loadTestData() {
		ObservableList<Media> media = FXCollections.observableArrayList();

		media.add(new Song(
				"Baby Powder",
				Status.COMPLETED,
				9.0,
				"Division",
				"Jenevieve",
				2013,
				177,
				"Great song.",
				"/resources/application/images/profiles/baby-powder.png"
			));
		
		for(int i=0; i < 12; i++)
		{
			String x = "A" + i;
			
			media.add(new Song(
					x,
					Status.COMPLETED,
					9.0,
					"Division",
					"Jenevieve",
					2013,
					177,
					"Great song.",
					"/resources/application/images/profiles/baby-powder.png"
				));
		}

		mediaTable.setItems(media);
	}
	
	private void setupHeaders() {
		setupSearchHeader(titleColumn, "Title");
		setupSearchHeader(creatorColumn, "Creator");
		
		List<String> statuses = new ArrayList<>();

		statuses.add("All");
		statuses.add("Planned");
		statuses.add("In Progress");
		statuses.add("Completed");
		
		List<String> reviews = new ArrayList<>();

		reviews.add("All");
		reviews.add("Unreviewed");
		reviews.add("Reviewed");
		
		setupDropdownHeader(statusColumn, "Status", statuses);
		setupDropdownHeader(reviewColumn, "Reviewed", reviews);
		
		setupTextHeader(numberColumn, "#");
		setupTextHeader(yearColumn, "Year");
		setupTextHeader(ratingColumn, "Rating");
		setupTextHeader(infoColumn, "Details");
	}
	
	private void setupTextHeader(TableColumn<Media, ?> column, String text) {
		Label label = new Label(text);
		label.getStyleClass().add("header-label");

		StackPane header = new StackPane(label);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(0, 30, 0, 10));

		column.setText(null);
		column.setGraphic(header);
	}
	
	private void setupSearchHeader(TableColumn<Media, ?> column, String prompt) {
		TextField searchField = new TextField();
		searchField.setPromptText(prompt);
		searchField.getStyleClass().add("header-search");
		searchField.setPrefHeight(32);
		searchField.setMinHeight(32);
		searchField.setMaxHeight(32);

		ImageView searchIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/application/images/icons/search-icon.png")));
		searchIcon.setFitWidth(11);
		searchIcon.setFitHeight(12);
		searchIcon.setPreserveRatio(true);
		searchIcon.setMouseTransparent(true);

		StackPane header = new StackPane(searchField, searchIcon);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(0, 30, 0, 6));

		searchField.prefWidthProperty().bind(header.widthProperty().subtract(36));

		StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
		StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 10));

		column.setText(null);
		column.setGraphic(header);
	}

	private void setupDropdownHeader(TableColumn<Media, ?> column, String prompt, List<String> items) {
		ComboBox<String> comboBox = new ComboBox<>();
		comboBox.getItems().addAll(items);
		comboBox.setPromptText(prompt);
		comboBox.getStyleClass().add("header-combo");
		comboBox.setPrefHeight(32);
		comboBox.setMinHeight(32);
		comboBox.setMaxHeight(32);

		ImageView dropdownIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/application/images/icons/dropdown-icon.png")));
		dropdownIcon.setFitWidth(12);
		dropdownIcon.setFitHeight(4);
		dropdownIcon.setPreserveRatio(true);
		dropdownIcon.setMouseTransparent(true);

		StackPane header = new StackPane(comboBox, dropdownIcon);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(0, 30, 0, 6));

		comboBox.prefWidthProperty().bind(header.widthProperty().subtract(36));

		StackPane.setAlignment(dropdownIcon, Pos.CENTER_LEFT);
		StackPane.setMargin(dropdownIcon, new Insets(0, 0, 0, 10));

		column.setText(null);
		column.setGraphic(header);
	}
	
	private void setupTableCells() {
		
		numberColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(mediaTable.getItems().indexOf(cell.getValue()) + 1));

		numberColumn.setCellFactory(column -> new TableCell<Media, Number>() {
			private final Label label = new Label();
			private final StackPane wrapper = new StackPane(label);

			{
				label.getStyleClass().add("media-cell");
				wrapper.setAlignment(Pos.CENTER_LEFT);
				wrapper.setPadding(new Insets(0, 0, 0, 10));
			}

			@Override
			protected void updateItem(Number value, boolean empty) {
				super.updateItem(value, empty);

				if(empty || value == null) {
					label.setText(null);
					setText(null);
					setGraphic(null);
				}
				else {
					label.setText(String.valueOf(value.intValue()));
					setText(null);
					setGraphic(wrapper);
				}
			}
		});
		
		titleColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));

		titleColumn.setCellFactory(column -> new TableCell<Media, Media>() {
			private final ImageView cover = new ImageView();
			private final Label title = new Label();
			private final StackPane imagePane = new StackPane(cover);
			private final HBox content = new HBox(imagePane, title);
			private final StackPane wrapper = new StackPane(content);

			{
				cover.setFitWidth(48);
				cover.setFitHeight(48);
				cover.setPreserveRatio(true);

				Rectangle clip = new Rectangle(48, 48);
				clip.setArcWidth(12);
				clip.setArcHeight(12);
				cover.setClip(clip);

				imagePane.setAlignment(Pos.CENTER);
				imagePane.setPadding(new Insets(6));

				title.getStyleClass().add("media-title-cell");

				content.setSpacing(12);
				content.setAlignment(Pos.CENTER_LEFT);

				wrapper.setAlignment(Pos.CENTER_LEFT);
				wrapper.setPadding(new Insets(0, 0, 0, 6));
			}

			@Override
			protected void updateItem(Media media, boolean empty) {
				super.updateItem(media, empty);

				if(empty || media == null) {
					title.setText(null);
					cover.setImage(null);
					setText(null);
					setGraphic(null);
				}
				else {
					title.setText(media.getTitle());

					if(media.getImagePath() != null && !media.getImagePath().isBlank()) {
						URL imageUrl = getClass().getResource(media.getImagePath());

						if(imageUrl != null)
							cover.setImage(new Image(imageUrl.toExternalForm()));
						else
							cover.setImage(null);
					}
					else
						cover.setImage(null);

					setText(null);
					setGraphic(wrapper);
				}
			}
		});

		creatorColumn.setCellValueFactory(new PropertyValueFactory<>("creator"));

		yearColumn.setCellValueFactory(new PropertyValueFactory<>("yearString"));

		statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

		ratingColumn.setCellValueFactory(new PropertyValueFactory<>("userRatingString"));

		reviewColumn.setCellValueFactory(new PropertyValueFactory<>("reviewedStatus"));

		infoColumn.setCellValueFactory(new PropertyValueFactory<>("mediaInfo"));
	}
	
	public void setupView(Type mediaType) {
		mediaLabel.setText(mediaType.getTitle());

		rootPane.getStyleClass().removeAll("theme-songs", "theme-games", "theme-shows");
		rootPane.getStyleClass().add(mediaType.getStyleClass());
	}
    
    private void setIcon(Button button, String name) {
		Image image = new Image(getClass().getResourceAsStream(name));
		ImageView imageView = new ImageView(image);
        
        imageView.setFitWidth(72);
        imageView.setFitHeight(72); 
        imageView.setPreserveRatio(true);
		
		button.setGraphic(imageView);
		button.setContentDisplay(ContentDisplay.TOP);
	}
	
	@FXML
	private void showPane() { 
		// Animation for showing the pane completely
		Timeline timelineDown = new Timeline();
 
		final KeyValue kvDwn1 = new KeyValue(clipRect.heightProperty(), extendableNavigationPane.getHeight());
		final KeyValue kvDwn2 = new KeyValue(clipRect.translateYProperty(), 0);
		final KeyValue kvDwn3 = new KeyValue(extendableNavigationPane.translateYProperty(), navigationYOffset);
		final KeyFrame kfDwn = new KeyFrame(Duration.millis(100), createBouncingEffect(extendableNavigationPane.getHeight()), kvDwn1, kvDwn2, kvDwn3);
 
		// Animation for moving button 1
		final KeyValue kvB1 = new KeyValue(addButton.translateXProperty(), -deltaXNavButton1);
		final KeyFrame kfB1 = new KeyFrame(Duration.millis(200), kvB1);
 
		// Animation for moving button 2
		final KeyValue kvB2 = new KeyValue(homeButton.translateXProperty(), -deltaXNavButton2);
		final KeyFrame kfB2 = new KeyFrame(Duration.millis(200), kvB2);
 
		addButton.setText("Add Playlist");
		homeButton.setText("Home");
		timelineDown.getKeyFrames().addAll(kfDwn, kfB1, kfB2);
		timelineDown.play();
	}
 
	@FXML
	private void hidePane() { 
		// Animation for hiding the pane..
		Timeline timelineUp = new Timeline();
 
		final KeyValue kvUp1 = new KeyValue(clipRect.heightProperty(), 55);
		final KeyValue kvUp2 = new KeyValue(extendableNavigationPane.translateYProperty(), navigationYOffset + 10);
		final KeyFrame kfUp = new KeyFrame(Duration.millis(200), kvUp1, kvUp2);
 
		// Animation for moving button 1
		final KeyValue kvB1 = new KeyValue(addButton.translateXProperty(), deltaXNavButton1);
		final KeyFrame kfB1 = new KeyFrame(Duration.millis(200), kvB1);
 
		final KeyValue kvB2 = new KeyValue(homeButton.translateXProperty(), deltaXNavButton2);
		final KeyFrame kfB2 = new KeyFrame(Duration.millis(200), kvB2);
 
		addButton.setText(null);
		homeButton.setText(null);
		timelineUp.getKeyFrames().addAll(kfUp, kfB1, kfB2);
		timelineUp.play();
	}
 
	@FXML
	private void addPlaylist() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/AddPlaylist.fxml"));
		    StackPane popup = loader.load();
		    
		    AddPlaylistController controller = loader.getController();

		    controller.setCloseAction(() -> rootPane.getChildren().remove(popup));

		    rootPane.getChildren().add(popup);

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		deselectAllPanes();
		addButton.setEffect(dropShadowForSelectedPane);
	}
 
	@FXML
	private void goToHome(ActionEvent event) {
		deselectAllPanes();
		homeButton.setEffect(dropShadowForSelectedPane);
		
		try {
	    		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/Menu.fxml"));
	    		Parent root = loader.load();
	        
	        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	        stage.getScene().setRoot(root);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
 
	private void deselectAllPanes() {
		addButton.setEffect(null);
		homeButton.setEffect(null);
	}
 
	private EventHandler<ActionEvent> createBouncingEffect(double height) {
		final Timeline timelineBounce = new Timeline();
		timelineBounce.setCycleCount(2);
		timelineBounce.setAutoReverse(true);
		final KeyValue kv1 = new KeyValue(clipRect.heightProperty(), (height - 15));
		final KeyValue kv2 = new KeyValue(clipRect.translateYProperty(), 15);
		final KeyValue kv3 = new KeyValue(extendableNavigationPane.translateYProperty(), navigationYOffset-15);
		final KeyFrame kf1 = new KeyFrame(Duration.millis(100), kv1, kv2, kv3);
		timelineBounce.getKeyFrames().add(kf1);
 
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				timelineBounce.play();
			}
		};
		return handler;
	}

    private void updateTablePage() {
        // Table page rendering logic
    }
}