package application.controller;

import java.util.List;
import java.util.ArrayList;
import application.api.SpotifyClient;
import application.model.Media;
import application.model.Song;
import application.model.Status;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

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

	private static String fitToSpace(String text, int width) {
	    if (text == null) {
	        return "";
	    }

	    if (text.length() <= width) {
	        return text;
	    }

	    return text.substring(0, width - 3) + "...";
	}
	
	public static void printMedia(List<? extends Media> mediaList, boolean isAllMedias) {
		
		String mediaType = "";
		
	    System.out.println();
	    if(isAllMedias)
	    	System.out.print("-------");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        if(isAllMedias)
	    	System.out.printf("| %-4s ", "Type");
        System.out.printf("| %-3s | %-25s | %-20s | %-11s | %-11s | %-20s | %-22s | %-50s |%n", "No.", "Title", "Creator", "Year", "Status", "Rating", "Reviewed By User", "Info");
        if(isAllMedias)
	    	System.out.print("-------");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
	    int ctr = 1;

	    for (Media media: mediaList)
	    {
	    	
	    	if (media instanceof Song)
				mediaType = "SONG";
	    	
	    	if(isAllMedias)
	    		System.out.printf("| %-4s ", mediaType);
	    	System.out.printf("| %-3s | %-25s | %-20s | %-11s | %-11s | %-20s | %-22s | %-50s |%n", ctr++, fitToSpace(media.getTitle(), 25), fitToSpace(media.getCreator(), 20), media.getYearString(), media.getStatus().toDbString(), media.getUserRatingString(), media.getReviewedStatus(), fitToSpace(media.getMediaInfo(), 50));	    
	    }
	    
	    if(isAllMedias)
	    	System.out.print("-------");
	    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
	}
	
	@FXML
	void searchSong(ActionEvent event) throws IOException, InterruptedException {
		System.out.println("Searching...");
	    List<Song> results = spotifyClient.searchTracks(songName.getText());
	    System.out.println("Done!");
	    printMedia(results, false);
	    
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
	    TableColumn<Song, Integer> id = new TableColumn<>("ID");
	    TableColumn<Song, String> title = new TableColumn<>("TITLE");
	    TableColumn<Song, String> creator = new TableColumn<>("CREATOR");
	    TableColumn<Song, String> year = new TableColumn<>("YEAR");
	    TableColumn<Song, Status> status = new TableColumn<>("STATUS");
	    TableColumn<Song, String> userRating = new TableColumn<>("USER RATING");
	    TableColumn<Song, String> review = new TableColumn<>("REVIEW");
	    TableColumn<Song, String> album = new TableColumn<>("ALBUM");
	    TableColumn<Song, String> runtime = new TableColumn<>("RUNTIME");

	    id.setCellValueFactory(new PropertyValueFactory<>("mediaId"));
	    title.setCellValueFactory(new PropertyValueFactory<>("title"));
	    creator.setCellValueFactory(new PropertyValueFactory<>("creator"));
	    year.setCellValueFactory(new PropertyValueFactory<>("yearString"));
	    status.setCellValueFactory(new PropertyValueFactory<>("status"));
	    userRating.setCellValueFactory(new PropertyValueFactory<>("userRatingString"));
	    review.setCellValueFactory(new PropertyValueFactory<>("reviewedStatus"));
	    album.setCellValueFactory(new PropertyValueFactory<>("album"));
	    runtime.setCellValueFactory(new PropertyValueFactory<>("runtimeString"));

	    tableView.getColumns().addAll(id, title, creator, year, status, userRating, review, album, runtime);

	    // 4. Enable row drag-and-drop reordering
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
