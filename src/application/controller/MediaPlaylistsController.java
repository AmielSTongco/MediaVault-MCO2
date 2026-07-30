package application.controller;

/*
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application.model.MediaPlaylist;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
*/
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import application.model.Type;
import application.model.UserSession;
import javafx.scene.shape.Rectangle;
/*
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
*/
import javafx.scene.control.ComboBox;

public class MediaPlaylistsController{
	
    @FXML
    private StackPane rootStackPane;
	
    @FXML
    private BorderPane rootBorderPane;
    
    @FXML 
    private ImageView mediaVaultLogo;
    
    @FXML
    private ImageView mediaVaultTitle;
    
    @FXML
    private ImageView profileAvatar;
    
    @FXML
    private Label userName;
    
    @FXML
    private ImageView settingsIcon;
    
    @FXML 
    private ImageView mediaLogo;
    
    @FXML
    private ComboBox<String> titleComboBox;

    @FXML
    private ComboBox<String> creatorComboBox;

    @FXML
    private ComboBox<String> yearComboBox;

    @FXML
    private ComboBox<String> albumComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private ComboBox<String> reviewedComboBox;
    
    @FXML
    private VBox resultsVBox;
    
    @FXML 
    private TextField songName;
    
    @FXML 
    private Label pageLabel;
    
    @FXML
    private Label mediaLabel;
    
    @FXML
    private BorderPane rootPane;
    
    // For Expandable Navigation Bar
 	//private static final int deltaXNavButton1 = 10;
 	//private static final int deltaXNavButton2 = -10;
  
 	@FXML
 	private HBox extendableNavigationPane;
    
    @FXML
	private Button addButton;
 
	@FXML
	private Button homeButton;
 
	//private Rectangle clipRect;
 
	//private DropShadow dropShadowForSelectedPane;
	
	@FXML
	private StackPane contentPane;

	@FXML
	private Rectangle backgroundRectangle;
	
	@FXML
	private VBox contentVBox;
	
	private Type mediaType;
	
	/*
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
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
        
        // Drag and drop rows logic
        // Adapted from: https://stackoverflow.com/a/28606524
        tableView.setRowFactory(tv -> {
            TableRow<MediaPlaylist> row = new TableRow<>();

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
                    MediaPlaylist draggedSong = tableView.getItems().remove(draggedIndex);

                    int dropIndex = row.isEmpty() ? tableView.getItems().size() : row.getIndex();
                    tableView.getItems().add(dropIndex, draggedSong);

                    event.setDropCompleted(true);
                    tableView.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });

            return row;
        });
	}
	
	public void setupView(Type mediaType) {
		mediaLabel.setText(mediaType.getTitle());
		
		rootPane.getStyleClass().removeAll("theme-songs", "theme-games", "theme-shows");
		rootPane.getStyleClass().add(mediaType.getStyleClass());

        tableView.getColumns().clear();
        
        setupTable();
        
        // TODO: sample data, input DAO logic here
        List<MediaPlaylist> samplePlaylists = new ArrayList<>();

        samplePlaylists.add(new MediaPlaylist(
            1, "Krazy", 24, 18, 4, 2, 4.8
        ));

        samplePlaylists.add(new MediaPlaylist(
            2, "WOAH", 15, 9, 3, 3, 4.9
        ));

        samplePlaylists.add(new MediaPlaylist(
            3, "Ohmy Gulay", 30, 25, 2, 3, 4.5
        ));

        samplePlaylists.add(new MediaPlaylist(
            4, "Whooooshh", 40, 40, 0, 0, 4.2
        ));

        samplePlaylists.add(new MediaPlaylist(
            5, "-aack", 12, 1, 5, 6, 3.8
        ));

        ObservableList<MediaPlaylist> sample = FXCollections.observableArrayList(samplePlaylists);

        tableView.setItems(sample);
	}
	
	public void setupTable() {
		// Declare Columns
		TableColumn<MediaPlaylist, String> dragCol = new TableColumn<>("");
		TableColumn<MediaPlaylist, String> titleCol = new TableColumn<>("Title");
		TableColumn<MediaPlaylist, Integer> totalCol = new TableColumn<>("Total Items");
		TableColumn<MediaPlaylist, Long> completedCol = new TableColumn<>("Completed");
		TableColumn<MediaPlaylist, Long> inProgressCol = new TableColumn<>("In Progress");
		TableColumn<MediaPlaylist, Long> plannedCol = new TableColumn<>("Planned");
		TableColumn<MediaPlaylist, String> ratingCol = new TableColumn<>("Avg Rating");
        
		Image dragImage = new Image(getClass().getResourceAsStream("/resources/application/images/icons/drag-horizontal-svgrepo-com.png"));

		dragCol.setMaxWidth(25);
		dragCol.setPrefWidth(25);
		dragCol.setCellFactory(col -> new TableCell<MediaPlaylist, String>() {
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
        
		titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
		totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCount"));
		completedCol.setCellValueFactory(new PropertyValueFactory<>("completedCount"));
		inProgressCol.setCellValueFactory(new PropertyValueFactory<>("inProgressCount"));
		plannedCol.setCellValueFactory(new PropertyValueFactory<>("plannedCount"));
		ratingCol.setCellValueFactory(new PropertyValueFactory<>("avgRating"));

        // Add columns to Table View
        tableView.getColumns().clear();
        tableView.getColumns().addAll(dragCol, titleCol, completedCol, inProgressCol, plannedCol, ratingCol);

        updateTablePage();
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
		final KeyValue kvUp2 = new KeyValue(extendableNavigationPane.translateYProperty(), 10);
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
		// TODO: Add logic here
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
    */
	
	@FXML
	public void initialize() {
		
		String username = UserSession.getCurrentUsername();
		
		mediaVaultLogo.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/logos/logo.png")));
		mediaVaultTitle.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/logos/title.png")));
		settingsIcon.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/icons/settings-gear-svgrepo-com.png")));
		mediaLogo.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/icons/songs-icon.png")));
		
		userName.setText(username);
		
		backgroundRectangle.widthProperty().bind(contentPane.widthProperty().multiply(0.97));
		backgroundRectangle.heightProperty().bind(contentPane.heightProperty());
		
		contentVBox.maxWidthProperty().bind(backgroundRectangle.widthProperty().multiply(0.97));
		contentVBox.prefWidthProperty().bind(backgroundRectangle.widthProperty());
		
		statusComboBox.getItems().addAll("All", "Completed", "In Progress", "Planned");
		reviewedComboBox.getItems().addAll("All", "Reviewed", "Not Reviewed");

		statusComboBox.setValue("All");
		reviewedComboBox.setValue("All");

		setupFilters();
	}

	public void setupView(Type mediaType) {
		this.mediaType = mediaType;

		if(mediaType == Type.SONG)
			setupSongView();
		else if(mediaType == Type.GAME)
			setupGameView();
		else if(mediaType == Type.SHOW)
			setupShowView();

		loadResults();
	}

	private void setupSongView() {
		titleComboBox.setPromptText("Title");
		creatorComboBox.setPromptText("Artist");
		yearComboBox.setPromptText("Year");
		albumComboBox.setPromptText("Album");

		albumComboBox.setVisible(true);
		albumComboBox.setManaged(true);
	}

	private void setupGameView() {
		titleComboBox.setPromptText("Title");
		creatorComboBox.setPromptText("Developer");
		yearComboBox.setPromptText("Year");

		albumComboBox.setVisible(false);
		albumComboBox.setManaged(false);
	}

	private void setupShowView() {
		titleComboBox.setPromptText("Title");
		creatorComboBox.setPromptText("Creator");
		yearComboBox.setPromptText("Year");

		albumComboBox.setVisible(false);
		albumComboBox.setManaged(false);
	}

	private void setupFilters() {
		titleComboBox.setOnAction(event -> loadResults());
		creatorComboBox.setOnAction(event -> loadResults());
		yearComboBox.setOnAction(event -> loadResults());
		albumComboBox.setOnAction(event -> loadResults());
		statusComboBox.setOnAction(event -> loadResults());
		reviewedComboBox.setOnAction(event -> loadResults());
	}

	private void loadResults() {
		resultsVBox.getChildren().clear();

		if(mediaType != null) {
			if(mediaType == Type.SONG)
				addTemporarySongResult();
			else if(mediaType == Type.GAME)
				addTemporaryGameResult();
			else if(mediaType == Type.SHOW)
				addTemporaryShowResult();
		}
	}

	private void addTemporarySongResult() {
		HBox row = new HBox();
		row.setSpacing(15.0);

		Label buttonPlaceholder = new Label(">");
		buttonPlaceholder.setMinWidth(40.0);

		Label numberLabel = new Label("1");
		numberLabel.setMinWidth(40.0);

		Label titleLabel = new Label("Sample Song");
		titleLabel.setMinWidth(150.0);

		Label creatorLabel = new Label("Sample Artist");
		creatorLabel.setMinWidth(150.0);

		Label yearLabel = new Label("2026");
		yearLabel.setMinWidth(100.0);

		Label albumLabel = new Label("Sample Album");
		albumLabel.setMinWidth(150.0);

		Label runtimeLabel = new Label("3:25");
		runtimeLabel.setMinWidth(100.0);

		Label statusLabel = new Label("Completed");
		statusLabel.setMinWidth(130.0);

		Label ratingLabel = new Label("9");
		ratingLabel.setMinWidth(80.0);

		Label reviewedLabel = new Label("Yes");
		reviewedLabel.setMinWidth(130.0);

		Label infoLabel = new Label("View");
		infoLabel.setMinWidth(80.0);

		row.getChildren().addAll(buttonPlaceholder, numberLabel, titleLabel, creatorLabel, yearLabel, albumLabel, runtimeLabel, statusLabel, ratingLabel, reviewedLabel, infoLabel);
		resultsVBox.getChildren().add(row);
	}

	private void addTemporaryGameResult() {
		HBox row = new HBox();
		row.setSpacing(15.0);
		row.getChildren().addAll(new Label("Sample Game"), new Label("Sample Developer"));
		resultsVBox.getChildren().add(row);
	}

	private void addTemporaryShowResult() {
		HBox row = new HBox();
		row.setSpacing(15.0);
		row.getChildren().addAll(new Label("Sample Show"), new Label("Sample Creator"));
		resultsVBox.getChildren().add(row);
	}
}