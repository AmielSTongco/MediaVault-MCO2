package application.view;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import application.model.Media;
//import application.model.Song;
import application.model.Season;
import application.model.Game;
import application.model.Show;
import application.model.MediaPlaylist;
//import application.model.Type;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.scene.control.TableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import java.util.function.Predicate;
import javafx.geometry.Rectangle2D;

public abstract class TableBuilder {

	private TableBuilder() {}

	// ==========================
	// PUBLIC
	// ==========================
	
	public static void createMediaTable(MediaTableOwner owner) {
		TableView<Media> mediaTable = owner.getMediaTable();
		TableColumn<Media, Number> numberColumn = owner.getNumberColumn();
		TableColumn<Media, Media> titleColumn = owner.getTitleColumn();
		TableColumn<Media, String> creatorColumn = owner.getCreatorColumn();
		TableColumn<Media, String> yearColumn = owner.getYearColumn();
		TableColumn<Media, String> statusColumn = owner.getStatusColumn();
		TableColumn<Media, String> ratingColumn = owner.getRatingColumn();
		TableColumn<Media, String> reviewColumn = owner.getReviewColumn();
		TableColumn<Media, String> infoColumn = owner.getInfoColumn();

		mediaTable.setPrefWidth(1360);
		mediaTable.setPrefHeight(616);
		mediaTable.setMinHeight(616);
		mediaTable.setMaxHeight(616);
		mediaTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		mediaTable.setFixedCellSize(55);
		
		mediaTable.setTranslateY(20);
		
		VBox.setVgrow(mediaTable, Priority.NEVER);

		configureColumn(numberColumn, 70);
		configureColumn(titleColumn, 310);
		configureColumn(creatorColumn, 230);
		configureColumn(yearColumn, 130);
		configureColumn(statusColumn, 220);
		configureColumn(ratingColumn, 90);
		configureColumn(reviewColumn, 220);
		configureColumn(infoColumn, 300);

		numberColumn.getStyleClass().add("number-column");

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

		numberColumn.setCellValueFactory(cell ->
			new ReadOnlyIntegerWrapper(mediaTable.getItems().indexOf(cell.getValue()) + 1)
		);

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

		titleColumn.setCellValueFactory(cell ->
			new ReadOnlyObjectWrapper<>(cell.getValue())
		);

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
					
					String defaultPath = "/resources/application/images/icons/default-song-icon.png";

					if(media instanceof Game)
						defaultPath = "/resources/application/images/icons/default-game-icon.png";

					if(media instanceof Show)
						defaultPath = "/resources/application/images/icons/default-show-icon.png";

					Image image = loadMediaImage(media.getImagePath(), defaultPath);

					if(image != null && image.isBackgroundLoading()) {
						cover.setImage(image);

						image.progressProperty().addListener((observable, oldValue, progress) -> {
							if(progress.doubleValue() >= 1)
								setCenterCroppedImage(cover, image, 48);
						});
					}
					else
						setCenterCroppedImage(cover, image, 48);
					
					setText(null);
					setGraphic(wrapper);
				}
			}
		});

		creatorColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(cell.getValue().getCreator())
		);
		creatorColumn.setCellFactory(column -> createTextCell(30));

		yearColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(cell.getValue().getYearString())
		);
		yearColumn.setCellFactory(column -> createTextCell(4));

		statusColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(
				cell.getValue().getStatus() == null
					? ""
					: cell.getValue().getStatus().toString().replace('_', ' ')
			)
		);
		statusColumn.setCellFactory(column -> createTextCell(30));

		ratingColumn.setCellValueFactory(cell -> {
			double rating = cell.getValue().getUserRating();

			if(rating <= 0)
				return new ReadOnlyStringWrapper("/--/");

			return new ReadOnlyStringWrapper(String.format("%.1f", rating));
		});
		ratingColumn.setCellFactory(column -> createTextCell(6));

		reviewColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(cell.getValue().getReviewedStatus())
		);
		reviewColumn.setCellFactory(column -> createTextCell(30));

		infoColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(cell.getValue().getMediaInfo())
		);
		infoColumn.setCellFactory(column -> createTextCell(4));
	}

	public static <T extends MediaPlaylist> void createPlaylistTable(PlaylistTableOwner<T> owner) {
		TableView<T> mediaPlaylistTable = owner.getMediaPlaylistTable();
		TableColumn<T, Number> numberColumn = owner.getNumberColumn();
		TableColumn<T, T> titleColumn = owner.getTitleColumn();
		TableColumn<T, String> totalColumn = owner.getTotalColumn();
		TableColumn<T, String> completedColumn = owner.getCompletedColumn();
		TableColumn<T, String> inProgressColumn = owner.getInProgressColumn();
		TableColumn<T, String> plannedColumn = owner.getPlannedColumn();
		TableColumn<T, String> avgRatingColumn = owner.getAvgRatingColumn();

		mediaPlaylistTable.setPrefWidth(1360);
		mediaPlaylistTable.setMinHeight(616);
		mediaPlaylistTable.setPrefHeight(616);
		mediaPlaylistTable.setMaxHeight(616);
		mediaPlaylistTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		mediaPlaylistTable.setFixedCellSize(55);
		
		mediaPlaylistTable.setTranslateY(20);
		
		VBox.setVgrow(mediaPlaylistTable, Priority.NEVER);
		
		configureColumn(numberColumn, 70);
		configureColumn(titleColumn, 350);
		configureColumn(totalColumn, 210);
		configureColumn(completedColumn, 210);
		configureColumn(inProgressColumn, 210);
		configureColumn(plannedColumn, 210);
		configureColumn(avgRatingColumn, 320);

		numberColumn.getStyleClass().add("number-column");

		setupTextHeader(numberColumn, "#");
		setupSearchHeader(titleColumn, "Title");
		setupTextHeader(totalColumn, "Total");
		setupTextHeader(completedColumn, "Completed");
		setupTextHeader(inProgressColumn, "In Progress");
		setupTextHeader(plannedColumn, "Planned");
		setupTextHeader(avgRatingColumn, "Average Rating across Completed Entries");

		numberColumn.setCellValueFactory(cell ->
			new ReadOnlyIntegerWrapper(mediaPlaylistTable.getItems().indexOf(cell.getValue()) + 1)
		);

		numberColumn.setCellFactory(column -> new TableCell<T, Number>() {
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

		titleColumn.setCellFactory(column -> new TableCell<T, T>() {
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
			protected void updateItem(T playlist, boolean empty) {
				super.updateItem(playlist, empty);

				
				if(empty || playlist == null)
				{
					title.setText(null);
					cover.setImage(null);
					setText(null);
					setGraphic(null);
				}
				else
				{
					title.setText(playlist.getTitle());
					
					if(playlist.getTitle().equals("all_songs"))
						title.setText("All Songs");
					if(playlist.getTitle().equals("all_games"))
						title.setText("All Games");
					if(playlist.getTitle().equals("all_shows"))
						title.setText("All Shows");

					String defaultPath = null;

					if(playlist instanceof Season)
						defaultPath = "/resources/application/images/icons/default-show-icon.png";
					else {
						switch(playlist.getTitle()) {
							case "all_songs":
								defaultPath = "/resources/application/images/icons/default-song-playlist-icon.png";
								break;
							case "all_games":
								defaultPath = "/resources/application/images/icons/default-game-playlist-icon.png";
								break;
							case "all_shows":
								defaultPath = "/resources/application/images/icons/default-show-playlist-icon.png";
								break;
						}
					}

					Image image = loadMediaImage(playlist.getImagePath(), defaultPath);

					if(image != null && image.isBackgroundLoading()) {
						cover.setImage(image);

						image.progressProperty().addListener((observable, oldValue, progress) -> {
							if(progress.doubleValue() >= 1)
								setCenterCroppedImage(cover, image, 48);
						});
					}
					else
						setCenterCroppedImage(cover, image, 48);
					
					setText(null);
					setGraphic(wrapper);
				}
			}
		});

		totalColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getTotalCount()))
		);
		totalColumn.setCellFactory(column -> createTextCell(8));

		completedColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getCompletedCount()))
		);
		completedColumn.setCellFactory(column -> createTextCell(8));

		inProgressColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getInProgressCount()))
		);
		inProgressColumn.setCellFactory(column -> createTextCell(8));

		plannedColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getPlannedCount()))
		);
		plannedColumn.setCellFactory(column -> createTextCell(8));

		avgRatingColumn.setCellValueFactory(cell -> {
			double rating = cell.getValue().getAvgRatingCount();

			if(rating <= 0)
				return new ReadOnlyStringWrapper("/--/");

			return new ReadOnlyStringWrapper(String.format("%.1f", rating));
		});
		avgRatingColumn.setCellFactory(column -> createTextCell(8));
	}

	private static void configureColumn(TableColumn<?, ?> column, double width) {
		column.setPrefWidth(width);
		column.setResizable(false);
		column.setReorderable(false);
	}

	private static void setupTextHeader(TableColumn<?, ?> column, String text) {
		Label label = new Label(text);
		label.getStyleClass().add("header-label");

		StackPane header = new StackPane(label);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(0, 30, 0, 10));

		column.setText(null);
		column.setGraphic(header);
	}

	private static TextField setupSearchHeader(TableColumn<?, ?> column, String prompt) {
		TextField searchField = new TextField();
		searchField.setPromptText(prompt);
		searchField.getStyleClass().add("header-search");
		searchField.setPrefHeight(32);
		searchField.setMinHeight(32);
		searchField.setMaxHeight(32);

		ImageView searchIcon = new ImageView(loadImage("/resources/application/images/icons/search-icon.png"));
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

		return searchField;
	}

	private static ComboBox<String> setupDropdownHeader(TableColumn<?, ?> column, String prompt, List<String> items) {
		ComboBox<String> comboBox = new ComboBox<>();
		comboBox.getItems().addAll(items);
		comboBox.setPromptText(prompt);
		comboBox.getStyleClass().add("header-combo");
		comboBox.setPrefHeight(32);
		comboBox.setMinHeight(32);
		comboBox.setMaxHeight(32);

		ImageView dropdownIcon = new ImageView(loadImage("/resources/application/images/icons/dropdown-icon.png"));
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

		return comboBox;
	}

	private static <T> TableCell<T, String> createTextCell(double leftPadding) {
		return new TableCell<T, String>() {
			private final Label label = new Label();
			private final StackPane wrapper = new StackPane(label);

			{
				label.getStyleClass().add("media-cell");
				wrapper.setAlignment(Pos.CENTER_LEFT);
				wrapper.setPadding(new Insets(0, 0, 0, leftPadding));
			}

			@Override
			protected void updateItem(String value, boolean empty) {
				super.updateItem(value, empty);

				if(empty || value == null) {
					label.setText(null);
					setText(null);
					setGraphic(null);
				}
				else {
					label.setText(value);
					setText(null);
					setGraphic(wrapper);
				}
			}
		};
	}

	private static Image loadMediaImage(String path, String defaultPath) {
		String finalPath = path;

		if(finalPath == null || finalPath.isBlank())
			finalPath = defaultPath;

		if(finalPath == null || finalPath.isBlank())
			return null;

		if(finalPath.startsWith("http://") || finalPath.startsWith("https://"))
			return new Image(finalPath, true);

		URL resource = TableBuilder.class.getResource(finalPath);

		if(resource != null)
			return new Image(resource.toExternalForm());

		File file = new File(finalPath);

		if(file.exists())
			return new Image(file.toURI().toString());

		if(defaultPath != null && !defaultPath.isBlank()) {
			URL defaultResource = TableBuilder.class.getResource(defaultPath);

			if(defaultResource != null)
				return new Image(defaultResource.toExternalForm());
		}

		return null;
	}

	private static Image loadImage(String path) {
		return new Image(TableBuilder.class.getResourceAsStream(path));
	}
	
	public static <T> void enableRowReordering(TableView<T> table, Predicate<T> canMove, Runnable saveAction) {
		final Object[] draggedItem = new Object[1];

		table.setRowFactory(view -> {
			TableRow<T> row = new TableRow<>();

			row.setOnDragDetected(event -> {
				if(!row.isEmpty() && canMove.test(row.getItem())) {
					draggedItem[0] = row.getItem();

					Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
					ClipboardContent content = new ClipboardContent();
					content.putString(String.valueOf(row.getIndex()));
					dragboard.setContent(content);

					event.consume();
				}
			});

			row.setOnDragOver(event -> {
				if(draggedItem[0] != null && row.getItem() != draggedItem[0]) {
					if(row.isEmpty() || canMove.test(row.getItem()))
						event.acceptTransferModes(TransferMode.MOVE);
				}

				event.consume();
			});

			row.setOnDragDropped(event -> {
				boolean completed = false;

				if(draggedItem[0] != null) {
					@SuppressWarnings("unchecked")
					T item = (T)draggedItem[0];

					int fromIndex = table.getItems().indexOf(item);
					int toIndex = row.isEmpty() ? table.getItems().size() : row.getIndex();

					if(toIndex == 0)
						toIndex = 1;

					T movedItem = table.getItems().remove(fromIndex);

					if(fromIndex < toIndex)
						toIndex--;

					if(toIndex < 1)
						toIndex = 1;

					if(toIndex > table.getItems().size())
						toIndex = table.getItems().size();

					table.getItems().add(toIndex, movedItem);
					table.getSelectionModel().select(toIndex);

					if(saveAction != null)
						saveAction.run();

					completed = true;
				}

				draggedItem[0] = null;
				event.setDropCompleted(completed);
				event.consume();
			});

			return row;
		});
	}
	
	private static void setCenterCroppedImage(ImageView imageView, Image image, double size) {
		imageView.setFitWidth(size);
		imageView.setFitHeight(size);
		imageView.setPreserveRatio(false);
		imageView.setViewport(null);
		imageView.setImage(image);

		if(image != null && image.getWidth() > 0 && image.getHeight() > 0) {
			double imageWidth = image.getWidth();
			double imageHeight = image.getHeight();
			double targetRatio = size/size;
			double imageRatio = imageWidth/imageHeight;
			double cropWidth = imageWidth;
			double cropHeight = imageHeight;
			double cropX = 0;
			double cropY = 0;

			if(imageRatio > targetRatio) {
				cropWidth = imageHeight*targetRatio;
				cropX = (imageWidth - cropWidth)/2;
			}
			else {
				cropHeight = imageWidth/targetRatio;
				cropY = (imageHeight - cropHeight)/2;
			}

			imageView.setViewport(new Rectangle2D(cropX, cropY, cropWidth, cropHeight));
		}

		Rectangle clip = new Rectangle(size, size);
		clip.setArcWidth(8);
		clip.setArcHeight(8);
		imageView.setClip(clip);
	}
	
	protected abstract void loadTableData();
}