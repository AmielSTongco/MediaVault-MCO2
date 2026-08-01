package application.view;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import application.model.Media;
import application.model.MediaPlaylist;
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
		mediaTable.setPrefHeight(550);
		mediaTable.setMinHeight(550);
		mediaTable.setMaxHeight(550);
		mediaTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

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
					cover.setImage(loadMediaImage(media.getImagePath()));
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
				cell.getValue().getStatus() == null ? "" : cell.getValue().getStatus().toString()
			)
		);
		statusColumn.setCellFactory(column -> createTextCell(30));

		ratingColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(cell.getValue().getUserRatingString())
		);
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

	public static void createPlaylistTable(PlaylistTableOwner owner) {
		TableView<MediaPlaylist> mediaPlaylistTable = owner.getMediaPlaylistTable();
		TableColumn<MediaPlaylist, Number> numberColumn = owner.getNumberColumn();
		TableColumn<MediaPlaylist, MediaPlaylist> titleColumn = owner.getTitleColumn();
		TableColumn<MediaPlaylist, String> totalColumn = owner.getTotalColumn();
		TableColumn<MediaPlaylist, String> completedColumn = owner.getCompletedColumn();
		TableColumn<MediaPlaylist, String> inProgressColumn = owner.getInProgressColumn();
		TableColumn<MediaPlaylist, String> plannedColumn = owner.getPlannedColumn();
		TableColumn<MediaPlaylist, String> avgRatingColumn = owner.getAvgRatingColumn();

		mediaPlaylistTable.setPrefWidth(1360);
		mediaPlaylistTable.setPrefHeight(550);
		mediaPlaylistTable.setMinHeight(550);
		mediaPlaylistTable.setMaxHeight(550);
		mediaPlaylistTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		configureColumn(numberColumn, 70);
		configureColumn(titleColumn, 310);
		configureColumn(totalColumn, 230);
		configureColumn(completedColumn, 130);
		configureColumn(inProgressColumn, 220);
		configureColumn(plannedColumn, 90);
		configureColumn(avgRatingColumn, 220);

		numberColumn.getStyleClass().add("number-column");

		setupTextHeader(numberColumn, "#");
		setupSearchHeader(titleColumn, "Title");
		setupSearchHeader(totalColumn, "Total");
		setupTextHeader(completedColumn, "Completed");
		setupTextHeader(inProgressColumn, "In Progress");
		setupTextHeader(plannedColumn, "Planned");
		setupTextHeader(avgRatingColumn, "Average Rating");

		numberColumn.setCellValueFactory(cell ->
			new ReadOnlyIntegerWrapper(mediaPlaylistTable.getItems().indexOf(cell.getValue()) + 1)
		);

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
			protected void updateItem(MediaPlaylist playlist, boolean empty) {
				super.updateItem(playlist, empty);

				if(empty || playlist == null) {
					title.setText(null);
					cover.setImage(null);
					setText(null);
					setGraphic(null);
				}
				else {
					title.setText(playlist.getTitle());
					cover.setImage(loadMediaImage(playlist.getImagePath()));
					setText(null);
					setGraphic(wrapper);
				}
			}
		});

		totalColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getTotalCount()))
		);
		totalColumn.setCellFactory(column -> createTextCell(30));

		completedColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getCompletedCount()))
		);
		completedColumn.setCellFactory(column -> createTextCell(4));

		inProgressColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getInProgressCount()))
		);
		inProgressColumn.setCellFactory(column -> createTextCell(30));

		plannedColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getPlannedCount()))
		);
		plannedColumn.setCellFactory(column -> createTextCell(6));

		avgRatingColumn.setCellValueFactory(cell ->
			new ReadOnlyStringWrapper(String.valueOf(cell.getValue().getAvgRatingCount()))
		);
		avgRatingColumn.setCellFactory(column -> createTextCell(30));
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

	private static Image loadMediaImage(String path) {
		if(path == null || path.isBlank())
			return null;

		URL resource = TableBuilder.class.getResource(path);

		if(resource != null)
			return new Image(resource.toExternalForm());

		File file = new File(path);

		if(file.exists())
			return new Image(file.toURI().toString());

		return null;
	}

	private static Image loadImage(String path) {
		return new Image(TableBuilder.class.getResourceAsStream(path));
	}
	
	protected abstract void loadTableData();
}