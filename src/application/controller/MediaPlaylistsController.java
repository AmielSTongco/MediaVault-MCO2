package application.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application.dao.impl.MediaPlaylistDAOImpl;
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
//import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
//import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
//import javafx.scene.input.ClipboardContent;
//import javafx.scene.input.Dragboard;
//import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ComboBox;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
//import javafx.scene.layout.Priority;
//import javafx.scene.layout.Region;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
//import java.io.File;

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
    private TableView<MediaPlaylist> mediaPlaylistTable;

    @FXML
    private TableColumn<MediaPlaylist, Void> dragColumn;

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
 
	private Rectangle clipRect;
 
	private DropShadow dropShadowForSelectedPane;
	
	private static final int navigationYOffset = 20;
	
	private MediaPlaylistDAOImpl mediaPlaylistDAO;
	private Connection conn;
	private Type mediaType;
	
	public void setConnection(Connection conn) {
		this.conn = conn;
	}
	
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
        
        mediaPlaylistTable.setPrefWidth(1360);
        mediaPlaylistTable.setPrefHeight(550);
        mediaPlaylistTable.setMinHeight(550);
        mediaPlaylistTable.setMaxHeight(550);
        
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
        
        mediaPlaylistTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
	}
	
	public void setupView(Type mediaType) {
		this.mediaType = mediaType;
		mediaLabel.setText(mediaType.getTitle());

		rootPane.getStyleClass().removeAll("theme-songs", "theme-games", "theme-shows");
		rootPane.getStyleClass().add(mediaType.getStyleClass());
		
		mediaPlaylistDAO = new MediaPlaylistDAOImpl(conn, UserSession.getCurrentUserId());
		
		setupColumnWidths();
        setupHeaders();
        setupTableCells();
        loadUserData();
        //loadTestData();
	}
	
	private void loadUserData() {
		ObservableList<MediaPlaylist> media = FXCollections.observableArrayList();
		List<MediaPlaylist> mediaItems = new ArrayList<MediaPlaylist>();

		try {
			mediaItems = mediaPlaylistDAO.getPlaylistsByUser(UserSession.getCurrentUserId(), mediaType);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		media.addAll(mediaItems);

		mediaPlaylistTable.setItems(media);
	}

	private void setupColumnWidths() {

		numberColumn.setPrefWidth(70);
		numberColumn.getStyleClass().add("number-column");
		titleColumn.setPrefWidth(310);
		totalColumn.setPrefWidth(230);
		completedColumn.setPrefWidth(130);
		inProgressColumn.setPrefWidth(220);
		plannedColumn.setPrefWidth(90);
		avgRatingColumn.setPrefWidth(220);

		numberColumn.setResizable(false);
		titleColumn.setResizable(false);
		totalColumn.setResizable(false);
		completedColumn.setResizable(false);
		inProgressColumn.setResizable(false);
		plannedColumn.setResizable(false);
		avgRatingColumn.setResizable(false);
		
		numberColumn.setReorderable(false);
		titleColumn.setReorderable(false);
		totalColumn.setReorderable(false);
		completedColumn.setReorderable(false);
		inProgressColumn.setReorderable(false);
		plannedColumn.setReorderable(false);
		avgRatingColumn.setReorderable(false);
	}
	
	/*
	private void loadTestData() {
		ObservableList<MediaPlaylist> media = FXCollections.observableArrayList();

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
	*/
	
	private void setupHeaders() {
		setupTextHeader(numberColumn, "#");
		setupSearchHeader(titleColumn, "Title");
		setupSearchHeader(totalColumn, "Total");
		setupTextHeader(completedColumn, "Completed");
		setupTextHeader(inProgressColumn, "In Progress");
		setupTextHeader(plannedColumn, "Planned");
		setupTextHeader(avgRatingColumn, "Average Rating");
	}
	
	private void setupTextHeader(TableColumn<MediaPlaylist, ?> column, String text) {
		Label label = new Label(text);
		label.getStyleClass().add("header-label");

		StackPane header = new StackPane(label);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(0, 30, 0, 10));

		column.setText(null);
		column.setGraphic(header);
	}
	
	private void setupSearchHeader(TableColumn<MediaPlaylist, ?> column, String prompt) {
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
	
	private void setupTableCells() {
		
		numberColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(mediaPlaylistTable.getItems().indexOf(cell.getValue()) + 1));

		numberColumn.setCellFactory(column -> new TableCell<MediaPlaylist, Number>() {
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

		titleColumn.setCellFactory(column -> new TableCell<MediaPlaylist, MediaPlaylist>() {
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
			protected void updateItem(MediaPlaylist media, boolean empty) {
				super.updateItem(media, empty);

				if(empty || media == null) {
					title.setText(null);
					cover.setImage(null);
					setText(null);
					setGraphic(null);
				}
				else {
					title.setText(media.getTitle());

					if (media.getImagePath() != null && !media.getImagePath().isBlank()) {
					    File imageFile = new File(media.getImagePath());

					    if (imageFile.exists()) {
					        cover.setImage(new Image(imageFile.toURI().toString()));
					    } else {
					        cover.setImage(null);
					    }
					} else {
					    cover.setImage(null);
					}

					setText(null);
					setGraphic(wrapper);
				}
			}
		});

		totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalCount"));

		completedColumn.setCellValueFactory(new PropertyValueFactory<>("completedCount"));

		inProgressColumn.setCellValueFactory(new PropertyValueFactory<>("inProgressCount"));

		plannedColumn.setCellValueFactory(new PropertyValueFactory<>("plannedCount"));

		avgRatingColumn.setCellValueFactory(new PropertyValueFactory<>("avgRatingCount"));
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

		    controller.setConnection(conn);
		    controller.setMediaType(mediaType);
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