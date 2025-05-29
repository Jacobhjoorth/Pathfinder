import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.Pos;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PathFinder extends Application {

    // File paths for the saved graph data and the background image
    private final String graphFilePath = "europa.graph";
    private final String imageFilePath = "file:europa.gif";

    // The main graph holding the cities and connections
    private ListGraph<City> listGraph = new ListGraph<>();

    // References to the currently selected cities
    private City firstCity;
    private City secondCity;

    // UI components
    private Pane center;
    private BorderPane root;
    private Stage stage;
    private Image image;
    private ImageView imageView;

    // Tracks if changes were made to the map
    private boolean changed;

    // Buttons for user actions
    private Button findPath = new Button("Find Path");
    private Button showConnection = new Button("Show connection");
    private Button btnNewPlace = new Button("New Place");
    private Button newConnection = new Button("New connection");
    private Button changeConnection = new Button("Change connection");

    // Menu bar and file menu options
    private MenuBar menuBar = new MenuBar();
    private Menu fileMenu = new Menu("File");
    private MenuItem newMap = new MenuItem("New Map");
    private MenuItem open = new MenuItem("Open");
    private MenuItem save = new MenuItem("Save");
    private MenuItem saveImage = new MenuItem("Save image");
    private MenuItem exit = new MenuItem("Exit");

    @Override
    public void start(Stage primaryStage) {
        // Store the stage and set title
        this.stage = primaryStage;
        primaryStage.setTitle("PathFinder");

        // Initialize layout
        root = new BorderPane();
        center = new Pane();

        // Create a horizontal button bar
        HBox buttonBar = new HBox();
        buttonBar.getChildren().add(findPath);
        buttonBar.getChildren().add(showConnection);
        buttonBar.getChildren().add(btnNewPlace);
        buttonBar.getChildren().add(newConnection);
        buttonBar.getChildren().add(changeConnection);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setSpacing(10);

        // Add file menu items and add menu to menu bar
        fileMenu.getItems().addAll(newMap, open, save, saveImage, exit);
        menuBar.getMenus().add(fileMenu);

        // Assign handlers for file menu actions
        newMap.setOnAction(new NewMapHandler());
        open.setOnAction(new OpenHandler());
        save.setOnAction(new SaveHandler());
        saveImage.setOnAction(new SaveImageHandler());

        // Exit triggers window close
        exit.setOnAction(event -> {
            primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        // Confirm exit if there are unsaved changes
        primaryStage.setOnCloseRequest(event -> {
            if (changed) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Unsaved changes, exit anyway?");
                alert.setTitle("Warning!");
                alert.setHeaderText("");
                Optional<ButtonType> button = alert.showAndWait();

                if (button.isPresent() & button.get().equals(ButtonType.CANCEL)) {
                    event.consume();
                }
            }
        });

        // Assign handlers for main buttons
        btnNewPlace.setOnAction(new NewPlaceHandler());
        newConnection.setOnAction(new NewConnectionHandler());
        showConnection.setOnAction(new ShowConnectionHandler());
        changeConnection.setOnAction(new ChangeConnectionHandler());
        findPath.setOnAction(new FindPathHandler());

        // Set IDs for CSS or testing
        menuBar.setId("menu");
        fileMenu.setId("menuFile");
        newMap.setId("menuNewMap");
        open.setId("menuOpenFile");
        save.setId("menuSaveFile");
        saveImage.setId("menuSaveImage");
        exit.setId("menuExit");
        findPath.setId("btnFindPath");
        showConnection.setId("btnShowConnection");
        btnNewPlace.setId("btnNewPlace");
        changeConnection.setId("btnChangeConnection");
        newConnection.setId("btnNewConnection");
        center.setId("outputArea");

        // Place menu and buttons at the top of the layout
        VBox topContainer = new VBox(menuBar, buttonBar);
        root.setTop(topContainer);
        root.setCenter(center);

        // Finalize and show scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Handler for creating a new map
    class NewMapHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            // Ask for confirmation if changes were made
            if (changed) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Unsaved changes, continue anyway?");
                Optional<ButtonType> button = alert.showAndWait();

                if (button.isPresent() & button.get().equals(ButtonType.CANCEL)) {
                    event.consume();
                }
            }
            changed = true;

            // Load image as map background
            image = new Image(imageFilePath);
            imageView = new ImageView(image);

            // Clear existing cities and connections
            center.getChildren().clear();
            center.getChildren().add(imageView);
            firstCity = null;
            secondCity = null;

            // Remove all cities from graph
            ArrayList<City> cityList = new ArrayList<>(listGraph.getNodes());
            for (City city : cityList) {
                listGraph.remove(city);
            }

            stage.sizeToScene();
        }
    }

    // Handler for opening a saved map
    class OpenHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            // Ask for confirmation if changes were made
            if (changed) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Unsaved changes, continue anyway?");
                alert.setTitle("Warning!");
                alert.setHeaderText("");
                Optional<ButtonType> button = alert.showAndWait();

                if (button.isPresent() && button.get().equals(ButtonType.CANCEL)) {
                    event.consume();
                    return;
                }
            }

            // Remove existing cities and clear UI
            ArrayList<City> cityList = new ArrayList<>(listGraph.getNodes());
            for (City city : cityList) {
                listGraph.remove(city);
            }

            center.getChildren().clear();
            firstCity = null;
            secondCity = null;
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new FileReader(graphFilePath));
                String line;
                int lineCount = 0;

                while ((line = reader.readLine()) != null) {
                    lineCount++;

                    // Line 1: map background image
                    if (lineCount == 1) {
                        image = new Image(line);
                        imageView = new ImageView(image);
                        center.getChildren().add(imageView);
                        continue;
                    }

                    // Line 2: list of cities
                    if (lineCount == 2) {
                        String[] parts = line.split(";");
                        for (int i = 0; i < parts.length; i += 3) {
                            String name = parts[i];
                            double x = Double.parseDouble(parts[i + 1]);
                            double y = Double.parseDouble(parts[i + 2]);
                            City city = new City(name, x, y);
                            center.getChildren().add(city);
                            listGraph.add(city);
                            city.setOnMouseClicked(new ClickHandler());
                            city.toFront();
                        }
                        continue;
                    }

                    // Lines 3+: connections between cities
                    if (lineCount >= 3) {
                        String[] parts = line.split(";");
                        String sourceName = parts[0];
                        String destinationName = parts[1];
                        String connectionName = parts[2];
                        int weight = Integer.parseInt(parts[3]);

                        City source = null;
                        City destination = null;

                        // Find source and destination cities
                        for (City city : listGraph.getNodes()) {
                            if (city.getName().equals(sourceName)) {
                                source = city;
                            }
                            if (city.getName().equals(destinationName)) {
                                destination = city;
                            }
                        }

                        // If connection does not already exist, add it
                        if (listGraph.getEdgeBetween(source, destination) == null) {
                            listGraph.connect(source, destination, connectionName, weight);
                            Line drawLine = new Line(
                                    source.getCenterX(), source.getCenterY(),
                                    destination.getCenterX(), destination.getCenterY());
                            center.getChildren().add(drawLine);
                            drawLine.setStrokeWidth(2);
                            drawLine.setDisable(true);
                            source.toFront();
                            destination.toFront();
                        }
                    }
                }

                stage.sizeToScene();
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Open");
                alert.setHeaderText("Open failed");
                alert.setContentText("The file was not found");
                alert.showAndWait();
            } catch (IOException e) {
                return;
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Handler for saving the current map to file
    class SaveHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(graphFilePath));

                Set<City> cities = listGraph.getNodes();
                String edges = "";
                String nodes = "";
                boolean first = false;

                // Save city information
                for (City city : cities) {
                    if (!first) {
                        nodes += city.saveInfo();
                        first = true;
                    } else {
                        nodes += ";" + city.saveInfo();
                    }

                    // Save connections (edges) from this city
                    for (var edge : listGraph.getEdgesFrom(city)) {
                        edges += city.getName() + ";" +
                                edge.getDestination().getName() + ";" +
                                edge.getName() + ";" +
                                edge.getWeight() + "\n";
                    }
                }

                // Write map image path
                writer.write(imageFilePath);
                writer.newLine();

                // Write city data
                writer.write(nodes);
                writer.newLine();

                // Write connection data
                writer.write(edges);
                writer.close();

            } catch (IOException e) {
                return;
            }
            changed = false; // Reset change flag after save
        }
    }

    // Saves a screenshot of the current map view as a PNG image
    class SaveImageHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            try {
                WritableImage image = center.snapshot(null, null); // Take a snapshot of the Pane
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null); // Convert to AWT image
                ImageIO.write(bufferedImage, "png", new File("capture.png")); // Save as PNG
            } catch (IOException e) {
                // Show error alert if saving fails
                Alert alert = new Alert(Alert.AlertType.ERROR, "IO-fel " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    // Allows the user to add a new city/place to the map
    class NewPlaceHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            center.setCursor(Cursor.CROSSHAIR); // Change cursor to crosshair
            btnNewPlace.setDisable(true); // Disable the button while placing
            center.setOnMouseClicked(new CityHandler()); // Wait for map click to place city
            changed = true; // Mark as modified
        }
    }

    // Creates a new connection between two selected cities
    class NewConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            TextField connectionName = new TextField();
            TextField connectionTime = new TextField();

            if (firstCity == null || secondCity == null) {
                // Show error if cities are not selected
                showError("Two places must be selected!");
            } else if (listGraph.getEdgeBetween(firstCity, secondCity) != null) {
                // Show error if connection already exists
                showError("Connection already exists!");
            } else {
                // Show input dialog for new connection
                Alert connectionWindow = new Alert(Alert.AlertType.CONFIRMATION);
                connectionWindow.setTitle("Connection");
                connectionWindow
                        .setHeaderText("Connection from " + firstCity.getName() + " to " + secondCity.getName());

                GridPane gridPane = new GridPane();
                gridPane.addRow(0, new Label("Name: "), connectionName);
                gridPane.addRow(1, new Label("Time: "), connectionTime);
                gridPane.setAlignment(Pos.CENTER);
                gridPane.setVgap(10);

                connectionWindow.getDialogPane().setContent(gridPane);
                Optional<ButtonType> answer = connectionWindow.showAndWait();

                if (answer.isPresent() && answer.get() == ButtonType.OK) {
                    String nameAnswer = connectionName.getText();
                    String timeAnswer = connectionTime.getText();

                    if (nameAnswer.isEmpty() || timeAnswer.isEmpty()) {
                        showError("Name and time must be provided!");
                    } else {
                        try {
                            int timeAnswerInteger = Integer.parseInt(timeAnswer);
                            listGraph.connect(firstCity, secondCity, nameAnswer, timeAnswerInteger);
                            Line line = new Line(firstCity.getCenterX(), firstCity.getCenterY(),
                                    secondCity.getCenterX(), secondCity.getCenterY());
                            center.getChildren().add(line);
                            line.setStrokeWidth(2);
                            line.setDisable(true);
                            firstCity.toFront();
                            secondCity.toFront();
                            changed = true;
                        } catch (NumberFormatException ex) {
                            showError("Wrong input for time, must be an Integer!");
                        }
                    }
                }
            }
        }

        private void showError(String msg) {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.setTitle("Error!");
            alert.setHeaderText("");
            alert.showAndWait();
        }
    }

    // Displays information about the connection between two selected cities
    class ShowConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (firstCity == null || secondCity == null) {
                showError("Two places must be selected");
            } else if (listGraph.getEdgeBetween(firstCity, secondCity) == null) {
                showError("No connection exists between the two cities");
            } else {
                TextInputDialog showConnectionWindow = new TextInputDialog();
                showConnectionWindow.setTitle("Connection");
                showConnectionWindow
                        .setHeaderText("Connection from " + firstCity.getName() + " to " + secondCity.getName());

                TextField name = new TextField(listGraph.getEdgeBetween(secondCity, firstCity).getName());
                TextField time = new TextField(
                        Integer.toString(listGraph.getEdgeBetween(secondCity, firstCity).getWeight()));
                name.setEditable(false);
                time.setEditable(false);

                GridPane gridPane = new GridPane();
                gridPane.addRow(0, new Label("Name: "), name);
                gridPane.addRow(1, new Label("Time: "), time);
                gridPane.setAlignment(Pos.CENTER);
                gridPane.setVgap(10);

                showConnectionWindow.getDialogPane().setContent(gridPane);
                showConnectionWindow.showAndWait();
            }
        }

        private void showError(String msg) {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.setTitle("Error!");
            alert.setHeaderText("");
            alert.showAndWait();
        }
    }

    // Lets user update the travel time of an existing connection
    class ChangeConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (firstCity == null || secondCity == null) {
                showError("Two places must be selected");
            } else if (listGraph.getEdgeBetween(firstCity, secondCity) == null) {
                showError("No connection exists between the two cities");
            } else {
                Edge<City> existingConnection = listGraph.getEdgeBetween(firstCity, secondCity);

                TextInputDialog changeConnectionWindow = new TextInputDialog();
                changeConnectionWindow.setTitle("Change Connection");
                changeConnectionWindow
                        .setHeaderText("Change Connection from " + firstCity.getName() + " to " + secondCity.getName());

                TextField name = new TextField(existingConnection.getName());
                TextField time = new TextField(Integer.toString(existingConnection.getWeight()));
                name.setEditable(false);

                GridPane gridPane = new GridPane();
                gridPane.addRow(0, new Label("Name: "), name);
                gridPane.addRow(1, new Label("Time: "), time);
                gridPane.setAlignment(Pos.CENTER);
                gridPane.setVgap(10);

                changeConnectionWindow.getDialogPane().setContent(gridPane);
                Optional<String> answer = changeConnectionWindow.showAndWait();

                if (answer.isPresent()) {
                    String newTime = time.getText();
                    if (newTime.isEmpty()) {
                        showError("Name and time must be provided!");
                    } else {
                        try {
                            int newWeight = Integer.parseInt(newTime);
                            listGraph.setConnectionWeight(firstCity, secondCity, newWeight);
                            changed = true;
                        } catch (NumberFormatException ex) {
                            showError("Wrong input for time, must be an integer!");
                        }
                    }
                }
            }
        }

        private void showError(String msg) {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.setTitle("Error!");
            alert.setHeaderText("");
            alert.showAndWait();
        }
    }

    // Calculates and displays the shortest path between two cities
    class FindPathHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (firstCity == null || secondCity == null) {
                showError("Two places must be selected");
            } else {
                List<Edge<City>> path = listGraph.getPath(firstCity, secondCity);

                if (path == null) {
                    showError("No path exists between the selected cities");
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Message");
                    alert.setHeaderText("The Path from " + firstCity.getName() + " to " + secondCity.getName() + ":");

                    StringBuilder contentText = new StringBuilder();
                    int totalTravelTime = 0;

                    // Reverse path for display
                    List<Edge<City>> reversedPath = new ArrayList<>(path);
                    Collections.reverse(reversedPath);

                    for (Edge<City> edge : reversedPath) {
                        contentText.append(edge.toString()).append("\n");
                        totalTravelTime += edge.getWeight();
                    }

                    contentText.append("Total ").append(totalTravelTime);
                    alert.setContentText(contentText.toString());
                    alert.showAndWait();
                }
            }
        }

        private void showError(String msg) {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.setTitle("Error!");
            alert.setHeaderText("");
            alert.showAndWait();
        }
    }

    // Handles city selection (red/blue highlighting and toggling)
    class ClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            City city = (City) event.getSource();

            if (city.isMarked() && city.equals(firstCity)) {
                firstCity.setMarked(false);
                firstCity.paintBlue();
                firstCity = null;
            } else if (city.isMarked() && city.equals(secondCity)) {
                secondCity.setMarked(false);
                secondCity.paintBlue();
                secondCity = null;
            } else if (firstCity == null && !city.equals(secondCity)) {
                firstCity = city;
                firstCity.setMarked(true);
                firstCity.paintRed();
            } else if (secondCity == null && !city.equals(firstCity)) {
                secondCity = city;
                secondCity.setMarked(true);
                secondCity.paintRed();
            }
        }
    }

    // Handles the creation of a new city when user clicks on the map
    class CityHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            TextInputDialog newPlaceWindow = new TextInputDialog();
            newPlaceWindow.setTitle("Name");
            newPlaceWindow.setHeaderText("");
            newPlaceWindow.setContentText("Name of place: ");
            Optional<String> answer = newPlaceWindow.showAndWait();

            if (answer.isPresent() && !answer.get().strip().isEmpty()) {
                String name = answer.get();
                double x = event.getX();
                double y = event.getY();
                City city = new City(name, x, y);
                center.getChildren().add(city);
                listGraph.add(city);
                city.setOnMouseClicked(new ClickHandler());
                changed = true;
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Name can't be empty!", ButtonType.OK);
                alert.setTitle("Error!");
                alert.setHeaderText("");
                alert.showAndWait();
            }

            // Reset cursor and re-enable button
            center.setCursor(Cursor.DEFAULT);
            btnNewPlace.setDisable(false);
            center.setOnMouseClicked(null);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}