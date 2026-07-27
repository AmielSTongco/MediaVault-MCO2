package application.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application.model.Media;
import application.model.Song;
import application.model.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class MediaPlaylistsItemsController implements Initializable {

    @FXML 
    private TableView<Media> tableView;
    
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
    private BorderPane rootPane;
    
    @FXML
    private ImageView settingsIcon;
    
    // For Expandable Navigation Bar
 	private static final int deltaXNavButton1 = 10;
 	private static final int deltaXNavButton2 = -10;
  
 	@FXML
 	private HBox extendableNavigationPane;
    
    @FXML
	private Button navButton1;
 
	@FXML
	private Button navButton2;
 
	private Rectangle clipRect;
 
	private DropShadow dropShadowForSelectedPane;

	public void setupView(String title, String styleClass) {
		mediaLabel.setText(title);
		
		rootPane.getStyleClass().removeAll("theme-songs", "theme-games", "theme-shows");
		rootPane.getStyleClass().add(styleClass);

        tableView.getColumns().clear();
        
        setupTable(title);
        
        // sample data
        List<Media> sample = new ArrayList<>();
        
        sample.add(new Song("Midnight Drive",
                Status.COMPLETED,
                4.2,
                "Neon Nights",
                "Luna Waves",
                2021,
                215,
                "Energetic synthwave track"));

        sample.add(new Song("Echoes of Time",
                Status.COMPLETED,
                3.9,
                "Timeless",
                "Aurora Sky",
                2019,
                298,
                "Dreamy ballad with orchestral layers"));

        sample.add(new Song("Firestorm",
                Status.COMPLETED,
                4.7,
                "Inferno",
                "Blaze Horizon",
                2023,
                185,
                "Fast-paced rock anthem"));

        sample.add(new Song("Ocean Whispers",
                Status.COMPLETED,
                4.0,
                "Blue Horizon",
                "Coral Reef",
                2020,
                242,
                "Calm acoustic song with oceanic themes"));

        sample.add(new Song("Digital Heartbeat",
                Status.COMPLETED,
                4.5,
                "Circuit Dreams",
                "Pixel Pulse",
                2022,
                199,
                "Upbeat electronic dance track"));
        
        ObservableList<Media> data = FXCollections.observableArrayList(sample);
        
        tableView.setItems(data);
        
        Image logoImg = new Image(getClass().getResourceAsStream("/resources/application/images/logos/logo.png"));
        Image titleImg = new Image(getClass().getResourceAsStream("/resources/application/images/logos/title.png"));
        Image settingsImg = new Image(getClass().getResourceAsStream("/resources/application/images/icons/settings-gear-svgrepo-com.png"));
        Image profileImg = new Image(getClass().getResourceAsStream("/resources/application/images/default/default-profile.png"));
        
        // Assign images to ImageView nodes
        mediaVaultLogo.setImage(logoImg);
        mediaVaultTitle.setImage(titleImg);
        settingsIcon.setImage(settingsImg);
        profileAvatar.setImage(profileImg);
        
        clipRect = new Rectangle();
		clipRect.setWidth(extendableNavigationPane.getPrefWidth());
		setIcon(navButton1, "/resources/application/images/icons/plus-svgrepo-com.png");
		setIcon(navButton2, "/resources/application/images/icons/back-reply-svgrepo-com.png");
		hidePane();
 
		dropShadowForSelectedPane = new DropShadow(BlurType.THREE_PASS_BOX, Color.BLUE, 7, 0.2, 0, 1);
	}
	
	public void setupTable(String mediaType) {
		// Declare Columns
		TableColumn<Media, String> dragCol = new TableColumn<>("");
		TableColumn<Media, String> mediaArtCol = new TableColumn<>("Media Art");
		TableColumn<Media, String> title = new TableColumn<>("Title");
		TableColumn<Media, String> creator = new TableColumn<>("Creator");
		TableColumn<Media, Status> status = new TableColumn<>("Status");
		TableColumn<Media, String> userRating = new TableColumn<>("Rating");
		TableColumn<Media, String> review = new TableColumn<>("Reviewed");
        
		Image dragImage = new Image(getClass().getResourceAsStream("/resources/application/images/icons/drag-horizontal-svgrepo-com.png"));

		dragCol.setPrefWidth(25);
		dragCol.setCellFactory(col -> new TableCell<Media, String>() {
		    private final ImageView dragImageView = new ImageView(dragImage);
		    {
		        dragImageView.setFitWidth(12);
		        dragImageView.setFitHeight(12);
		        dragImageView.setPreserveRatio(true);
		        setAlignment(Pos.CENTER);
		    }

		    @Override
		    protected void updateItem(String item, boolean empty) {
		        super.updateItem(item, empty);
		        if (empty) {
		            setGraphic(null);
		        } else {
		            setGraphic(dragImageView);
		        }
		    }
		});
		
		mediaArtCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl")); 
        mediaArtCol.setPrefWidth(45);

        // Fetch URL from field
        mediaArtCol.setCellFactory(col -> new TableCell<Media, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(45);
                imageView.setFitHeight(45);
                
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(45, 45);
                clip.setArcWidth(10);
                clip.setArcHeight(10);
                imageView.setClip(clip);
            }

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);

                if (empty || url == null || url.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    Image webImage = new Image(url, 45, 45, true, true, true);
                    imageView.setImage(webImage);
                    setGraphic(imageView);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        creator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        userRating.setCellValueFactory(new PropertyValueFactory<>("userRatingString"));
        review.setCellValueFactory(new PropertyValueFactory<>("reviewedStatus"));

        // Add columns to Table View
        tableView.getColumns().clear();
        tableView.getColumns().addAll(dragCol, mediaArtCol, title, creator);
        
        switch (mediaType) {
	        case "Songs" -> {
	            TableColumn<Media, String> yearSong = new TableColumn<>("Year Released");
	            TableColumn<Media, String> album = new TableColumn<>("Album");
	            TableColumn<Media, String> runtime = new TableColumn<>("Runtime");
	
	            yearSong.setCellValueFactory(new PropertyValueFactory<>("yearReleased"));
	            album.setCellValueFactory(new PropertyValueFactory<>("album"));
	            runtime.setCellValueFactory(new PropertyValueFactory<>("runtimeSeconds"));
	
	            tableView.getColumns().addAll(yearSong, album, runtime);
	        }
	        case "Games" -> {
	            TableColumn<Media, String> genreGame = new TableColumn<>("Genre");
	            TableColumn<Media, String> yearGame = new TableColumn<>("Year Released");
	            TableColumn<Media, String> playtime = new TableColumn<>("Avg Playtime (Mins)");
	
	            genreGame.setCellValueFactory(new PropertyValueFactory<>("genre"));
	            yearGame.setCellValueFactory(new PropertyValueFactory<>("yearReleased"));
	            playtime.setCellValueFactory(new PropertyValueFactory<>("avgPlaytimeMins"));
	
	            tableView.getColumns().addAll(genreGame, yearGame, playtime);
	        }
	        case "Shows" -> {
	            TableColumn<Media, String> genreShow = new TableColumn<>("Genre");
	            TableColumn<Media, String> yearStartCol = new TableColumn<>("Year Start");
	            TableColumn<Media, String> yearEndCol = new TableColumn<>("Year End");
	            TableColumn<Media, Integer> numOfSeasonsCol = new TableColumn<>("Seasons");
	            TableColumn<Media, Integer> numOfEpisodesCol = new TableColumn<>("Episodes");
	            TableColumn<Media, String> avgMinsPerEpCol = new TableColumn<>("Avg Mins/Ep");
	            TableColumn<Media, Boolean> airingCol = new TableColumn<>("Airing");
	
	            genreShow.setCellValueFactory(new PropertyValueFactory<>("genre"));
	            yearStartCol.setCellValueFactory(new PropertyValueFactory<>("yearStart"));
	            yearEndCol.setCellValueFactory(new PropertyValueFactory<>("yearEnd"));
	            numOfSeasonsCol.setCellValueFactory(new PropertyValueFactory<>("numOfSeasons"));
	            numOfEpisodesCol.setCellValueFactory(new PropertyValueFactory<>("numOfEpisodes"));
	            avgMinsPerEpCol.setCellValueFactory(new PropertyValueFactory<>("avgMinsPerEp"));
	            airingCol.setCellValueFactory(new PropertyValueFactory<>("airing"));
	
	            tableView.getColumns().addAll(
	                genreShow, yearStartCol, yearEndCol, 
	                numOfSeasonsCol, numOfEpisodesCol, avgMinsPerEpCol, airingCol
	            );
	        }
	    }
        
        tableView.getColumns().addAll(status, userRating, review);
        
        // Drag and drop rows
        tableView.setRowFactory(tv -> {
            TableRow<Media> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(index.toString());
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    if (row.getIndex() != Integer.parseInt(db.getString())) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    Media draggedSong = tableView.getItems().remove(draggedIndex);

                    int dropIndex = row.isEmpty() ? tableView.getItems().size() : row.getIndex();
                    tableView.getItems().add(dropIndex, draggedSong);

                    event.setDropCompleted(true);
                    tableView.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });

            return row;
        });

        updateTablePage();
	}
	
	@Override
    public void initialize(URL arg0, ResourceBundle arg1) {        
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
		final KeyValue kvDwn3 = new KeyValue(extendableNavigationPane.translateYProperty(), 0);
		final KeyFrame kfDwn = new KeyFrame(Duration.millis(100), createBouncingEffect(extendableNavigationPane.getHeight()), kvDwn1, kvDwn2, kvDwn3);
 
		// Animation for moving button 1
		final KeyValue kvB1 = new KeyValue(navButton1.translateXProperty(), -deltaXNavButton1);
		final KeyFrame kfB1 = new KeyFrame(Duration.millis(200), kvB1);
 
		// Animation for moving button 2
		final KeyValue kvB2 = new KeyValue(navButton2.translateXProperty(), -deltaXNavButton2);
		final KeyFrame kfB2 = new KeyFrame(Duration.millis(200), kvB2);
 
		navButton1.setText("Add Playlist");
		navButton2.setText("Back");
		timelineDown.getKeyFrames().addAll(kfDwn, kfB1, kfB2);
		timelineDown.play();
	}
 
	@FXML
	private void hidePane() { 
		// Animation for hiding the pane..
		Timeline timelineUp = new Timeline();
 
		final KeyValue kvUp1 = new KeyValue(clipRect.heightProperty(), 55);
		final KeyValue kvUp2 = new KeyValue(extendableNavigationPane.translateYProperty(), 10);
		final KeyFrame kfUp = new KeyFrame(Duration.millis(200), kvUp1, kvUp2);
 
		// Animation for moving button 1
		final KeyValue kvB1 = new KeyValue(navButton1.translateXProperty(), deltaXNavButton1);
		final KeyFrame kfB1 = new KeyFrame(Duration.millis(200), kvB1);
 
		final KeyValue kvB2 = new KeyValue(navButton2.translateXProperty(), deltaXNavButton2);
		final KeyFrame kfB2 = new KeyFrame(Duration.millis(200), kvB2);
 
		navButton1.setText(null);
		navButton2.setText(null);
		timelineUp.getKeyFrames().addAll(kfUp, kfB1, kfB2);
		timelineUp.play();
	}
 
	@FXML
	private void selectPane1() {
		System.out.println("Selecting pane 1");
		deselectAllPanes();
		navButton1.setEffect(dropShadowForSelectedPane);
	}
 
	@FXML
	private void selectPane2() {
		System.out.println("Selecting pane 2");
		deselectAllPanes();
		navButton2.setEffect(dropShadowForSelectedPane);
	}
 
	private void deselectAllPanes() {
		navButton1.setEffect(null);
		navButton2.setEffect(null);
	}
 
	private EventHandler<ActionEvent> createBouncingEffect(double height) {
		final Timeline timelineBounce = new Timeline();
		timelineBounce.setCycleCount(2);
		timelineBounce.setAutoReverse(true);
		final KeyValue kv1 = new KeyValue(clipRect.heightProperty(), (height - 15));
		final KeyValue kv2 = new KeyValue(clipRect.translateYProperty(), 15);
		final KeyValue kv3 = new KeyValue(extendableNavigationPane.translateYProperty(), -15);
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