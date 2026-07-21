package application.controller;

import java.net.URL;
import java.util.ResourceBundle;

import application.model.Song;
import application.model.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SongsPlaylistsItemsController implements Initializable {

    @FXML 
    private TableView<Song> tableView;
    
    @FXML 
    private TextField songName;
    
    @FXML 
    private Label pageLabel;
    
    @FXML 
    private ImageView mediaVaultLogo;
    
    @FXML
    private ImageView mediaVaultTitle;
    
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

    @SuppressWarnings("unchecked")
	@Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        tableView.getColumns().clear();

        // Declare Columns
        TableColumn<Song, String> drag = new TableColumn<>("");
        TableColumn<Song, String> numCol = new TableColumn<>("#");
        TableColumn<Song, String> albumArtCol = new TableColumn<>("Album Art");
        TableColumn<Song, String> title = new TableColumn<>("Title");
        TableColumn<Song, String> creator = new TableColumn<>("Creator");
        TableColumn<Song, String> year = new TableColumn<>("Year");
        TableColumn<Song, String> album = new TableColumn<>("Album");
        TableColumn<Song, String> runtime = new TableColumn<>("Runtime");
        TableColumn<Song, Status> status = new TableColumn<>("Status");
        TableColumn<Song, String> userRating = new TableColumn<>("Rating");
        TableColumn<Song, String> review = new TableColumn<>("Reviewed");

        TableColumn<Song, String> dragCol = new TableColumn<>("");
        dragCol.setPrefWidth(10);
	
	     Image dragImage = new Image(getClass().getResourceAsStream("/resources/application/images/icons/drag-horizontal-svgrepo-com.png"));
	
	     // Add the drag icon
	     dragCol.setCellFactory(col -> new TableCell<Song, String>() {
	         private final ImageView dragImageView = new ImageView(dragImage);
	
	         {
	             dragImageView.setFitWidth(10);
	             dragImageView.setFitHeight(10);
	             dragImageView.setPreserveRatio(true);
	             setAlignment(Pos.CENTER);
	         }
	    });
        
        // Row numberings
        numCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(tableView.getItems().indexOf(cellData.getValue()) + 1))
        );
        numCol.setPrefWidth(10);

        albumArtCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl")); 
        albumArtCol.setPrefWidth(45);

        // Fetch URL from field
        albumArtCol.setCellFactory(col -> new TableCell<Song, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(45);
                imageView.setFitHeight(45);
                
                // Rounded frame corners matching the screenshot UI
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
                    // backgroundLoading = true prevents UI stutter when pulling from web
                    Image webImage = new Image(url, 45, 45, true, true, true);
                    imageView.setImage(webImage);
                    setGraphic(imageView);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Other column factories
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        creator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        year.setCellValueFactory(new PropertyValueFactory<>("yearString"));
        album.setCellValueFactory(new PropertyValueFactory<>("album"));
        runtime.setCellValueFactory(new PropertyValueFactory<>("runtimeString"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        userRating.setCellValueFactory(new PropertyValueFactory<>("userRatingString"));
        review.setCellValueFactory(new PropertyValueFactory<>("reviewedStatus"));

        // Add columns to Table View
        tableView.getColumns().addAll(
            drag, numCol, albumArtCol, title, creator, year, album, runtime, status, userRating, review
        );

        // Drag and drop rows
        tableView.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();

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
                    Song draggedSong = tableView.getItems().remove(draggedIndex);

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
        
        Image logoImg = new Image(getClass().getResourceAsStream("/resources/application/images/logos/logo.png"));
        Image titleImg = new Image(getClass().getResourceAsStream("/resources/application/images/logos/title.png"));
        Image settingsImg = new Image(getClass().getResourceAsStream("/resources/application/images/icons/settings-gear-svgrepo-com.png"));

        // Assign images to ImageView nodes
        mediaVaultLogo.setImage(logoImg);
        mediaVaultTitle.setImage(titleImg);
        settingsIcon.setImage(settingsImg);
        
        clipRect = new Rectangle();
		clipRect.setWidth(extendableNavigationPane.getPrefWidth());
		setIcon(navButton1, "/resources/application/images/icons/plus-svgrepo-com.png");
		setIcon(navButton2, "/resources/application/images/icons/back-reply-svgrepo-com.png");
		hidePane();
 
		dropShadowForSelectedPane = new DropShadow(BlurType.THREE_PASS_BOX, Color.BLUE, 7, 0.2, 0, 1);
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
		final KeyFrame kfDwn = new KeyFrame(Duration.millis(100), createBouncingEffect(extendableNavigationPane.getHeight()), kvDwn1, kvDwn2,
				kvDwn3);
 
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
 
		// Event handler to call bouncing effect after the scroll down is
		// finished.
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