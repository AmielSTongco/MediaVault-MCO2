package application.controller;

import java.util.List;
import java.util.ArrayList;
import application.api.SpotifyClient;
import application.model.Song;
import application.model.Status;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
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

public class MainController implements Initializable{
	
	@FXML private Label pageLabel;

    // 1. Create your master data list and track pagination states
    private final ObservableList<Song> masterData = FXCollections.observableArrayList();
    private int currentPage = 0;
    private final int ROWS_PER_PAGE = 10;
	
	private static SpotifyClient spotifyClient = new SpotifyClient("266e17b3bb8e432d82b803598192fc5f", "f38ada98c91f4bf9bf6ed4f4490d7b12");
	
	@FXML
    private Button searchButton;
	
	@FXML
    private TextField songName;
	
	@FXML
    private TableView<Song> tableView;
	
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
	
	@FXML
	void searchSong(ActionEvent event) throws IOException, InterruptedException {
	    List<Song> results = spotifyClient.searchTracks(songName.getText());
	    
	    if (results == null) {
	        results = new ArrayList<>();   // avoid NPE if search fails/returns nothing
	    }

	    ObservableList<Song> data = FXCollections.observableArrayList(results);
	    tableView.setItems(data);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	    tableView.getColumns().clear();

	    // 3. Create them entirely in Java
	    TableColumn<Song, String> title = new TableColumn<>("TITLE");
	    TableColumn<Song, String> creator = new TableColumn<>("CREATOR");
	    TableColumn<Song, String> year = new TableColumn<>("YEAR");
	    TableColumn<Song, Status> status = new TableColumn<>("STATUS");
	    TableColumn<Song, String> userRating = new TableColumn<>("USER RATING");
	    TableColumn<Song, String> review = new TableColumn<>("REVIEW");
	    TableColumn<Song, String> album = new TableColumn<>("ALBUM");
	    TableColumn<Song, String> runtime = new TableColumn<>("RUNTIME");

	    title.setCellValueFactory(new PropertyValueFactory<>("title"));
	    creator.setCellValueFactory(new PropertyValueFactory<>("creator"));
	    year.setCellValueFactory(new PropertyValueFactory<>("yearString"));
	    status.setCellValueFactory(new PropertyValueFactory<>("status"));
	    userRating.setCellValueFactory(new PropertyValueFactory<>("userRatingString"));
	    review.setCellValueFactory(new PropertyValueFactory<>("reviewedStatus"));
	    album.setCellValueFactory(new PropertyValueFactory<>("album"));
	    runtime.setCellValueFactory(new PropertyValueFactory<>("runtimeString"));

	    tableView.getColumns().addAll(title, creator, year, status, userRating, review, album, runtime);

	    // Row drag-and-drop reordering
	    // Adapted from: https://stackoverflow.com/a/28606524
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
	    
	    clipRect = new Rectangle();
		clipRect.setWidth(extendableNavigationPane.getPrefWidth());
		setIcon(navButton1, "/resources/application/images/back-reply-svgrepo-com.png");
		setIcon(navButton2, "/resources/application/images/stack-overflow-svgrepo-com.png");
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
 
		navButton1.setText("Back");
		navButton2.setText("Playlists");
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
        int fromIndex = currentPage * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, masterData.size());

        // Sublist creates a view of exactly max 10 items
        ObservableList<Song> pageData = FXCollections.observableArrayList(
                masterData.subList(fromIndex, toIndex)
        );
        
        tableView.setItems(pageData);

        // Update the "Showing 10 of 22" text dynamically
        if (pageLabel != null) {
            pageLabel.setText("Showing " + (toIndex - fromIndex) + " of " + masterData.size());
        }
    }

    // 4. OnAction method for your "Previous" Button
    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateTablePage();
        }
    }

    // 5. OnAction method for your "Next" Button
    @FXML
    private void handleNextPage() {
        if ((currentPage + 1) * ROWS_PER_PAGE < masterData.size()) {
            currentPage++;
            updateTablePage();
        }
    }
}
