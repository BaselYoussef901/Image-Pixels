package com.example.image_position;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ImagePixelPositionViewer extends Application {
    private ArrayList<Point2D> clickedPositions;
    private String imagePath = "";
    private Canvas canvas;
    private Label positionLabel;
    private Label positionsLabel;
    private GraphicsContext gc;
    private Image originalImage;
    private Image prevImage;
    private Image nextImage;
    private double startX , startY , endX, endY;
    private boolean selecting = false;
    private Button prevButtonImage;
    private Button nextButtonImage;
    private boolean isPrevDisabled=true , isNextDisabled=true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Image Pixel Position Viewer");

        BorderPane root = new BorderPane();

        canvas = new Canvas();
        canvas.setOnMousePressed(e -> onMousePressed(e.getX(), e.getY()));
        canvas.setOnMouseDragged(e -> onMouseDragged(e.getX(), e.getY()));
        canvas.setOnMouseReleased(e -> onMouseReleased());
        //canvas.setOnMouseClicked(e -> mouseClicked(e));
        canvas.setOnMouseMoved(e -> showPixelPosition(e));
        root.setCenter(createScrollPane(canvas));

        VBox infoBox = new VBox(10);
        positionLabel = new Label("");
        positionsLabel = new Label("");
        infoBox.getChildren().addAll(positionLabel, positionsLabel);
        root.setTop(infoBox);

        Button openButton = new Button("Open Image");
        openButton.setPrefWidth(250);
        openButton.setPrefHeight(35);
        openButton.setOnAction(e -> openImage(primaryStage));
        Button cropButton = new Button("Crop Image");
        cropButton.setPrefWidth(250);
        cropButton.setPrefHeight(35);
        cropButton.setOnAction(e -> cropImage());
        Button showButton = new Button("Show Positions");
        showButton.setPrefWidth(250);
        showButton.setPrefHeight(35);
        showButton.setOnAction(e -> displaySavedPositions());

        prevButtonImage = new Button("Previous Image");
        prevButtonImage.setPrefWidth(100);
        prevButtonImage.setPrefHeight(20);
        prevButtonImage.setDisable(true);
        prevButtonImage.setOnAction(e -> displayPrevImage());
        nextButtonImage = new Button("Next Image");
        nextButtonImage.setPrefWidth(100);
        nextButtonImage.setPrefHeight(20);
        nextButtonImage.setDisable(true);
        nextButtonImage.setOnAction(e -> displayNextImage());

        HBox imageBox = new HBox(prevButtonImage , nextButtonImage);
        imageBox.setAlignment(javafx.geometry.Pos.CENTER); // Center align buttons in the HBox
        HBox.setHgrow(prevButtonImage, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(nextButtonImage, javafx.scene.layout.Priority.ALWAYS);

        HBox buttonBox = new HBox(openButton, cropButton, showButton);
        buttonBox.setSpacing(100);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER); // Center align buttons in the HBox
        HBox.setHgrow(cropButton, Priority.ALWAYS);

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(imageBox , buttonBox);
        root.setBottom(vbox);

        clickedPositions = new ArrayList<>();

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    private ScrollPane createScrollPane(Canvas content) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        return scrollPane;
    }
    private void clearCanvas() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        canvas.setWidth(originalImage.getWidth());
        canvas.setHeight(originalImage.getHeight());
        gc = canvas.getGraphicsContext2D();
        gc.drawImage(originalImage, 0, 0);
    }
    private void drawSelectionBox(){
        clearCanvas();
        double minX = Math.min(startX, endX);
        double minY = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);


        Color transparentWhite = Color.rgb(255,255,255,0.3);
        gc.setFill(transparentWhite);
        gc.fillRect(minX, minY, width, height);

        gc.setStroke(Color.LIGHTBLUE);
        gc.setLineWidth(2);
        gc.strokeRect(minX, minY, width, height);
    }
    private void onMousePressed(double x, double y){
        startX = x;
        startY = y;
        endX = x;
        endY = y;
        selecting = true;

        clickedPositions.add(new Point2D(startX, startY));
    }
    private void onMouseDragged(double x,double y){
        if(selecting){
            endX = x;
            endY = y;
            drawSelectionBox();
        }
    }
    private void onMouseReleased(){
        if(selecting){
            selecting = false;
            drawSelectionBox();
            //clearCanvas();

            clickedPositions.add(new Point2D(endX, endY));
        }
    }

    private void openImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            imagePath = selectedFile.toURI().toString();
            displayImage();
            clickedPositions.clear();
            positionsLabel.setText("");
        }
    }

    private void displayImage() {
        if (!imagePath.isEmpty()) {
            Image image = new Image(imagePath);
            if(originalImage != null){
                prevImage = originalImage;
                isPrevDisabled = false;
                prevButtonImage.setDisable(false);
            }
            originalImage = image;
            canvas.setWidth(image.getWidth());
            canvas.setHeight(image.getHeight());
            gc = canvas.getGraphicsContext2D();
            gc.drawImage(image, 0, 0);
        }
    }
    private void displayPrevImage(){
        if(!isPrevDisabled){
            canvas.setWidth(prevImage.getWidth());
            canvas.setHeight(prevImage.getHeight());
            gc = canvas.getGraphicsContext2D();
            gc.drawImage(prevImage, 0, 0);
            isPrevDisabled = true;
            isNextDisabled = false;
            nextImage = originalImage;
            originalImage = prevImage;
            nextButtonImage.setDisable(false);
            prevButtonImage.setDisable(true);
        }
    }
    private void displayNextImage(){
        if(!isNextDisabled){
            canvas.setWidth(nextImage.getWidth());
            canvas.setHeight(nextImage.getHeight());
            gc = canvas.getGraphicsContext2D();
            gc.drawImage(nextImage, 0, 0);
            isNextDisabled = true;
            isPrevDisabled = false;
            prevImage = originalImage;
            originalImage = nextImage;
            prevButtonImage.setDisable(false);
            nextButtonImage.setDisable(true);
        }
    }
    private void mouseClicked(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        clickedPositions.add(new Point2D(x, y));
    }

    private void showPixelPosition(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        positionLabel.setText("Position: (" + x + ", " + y + ")");
    }

    private void cropImage() {
        if (clickedPositions.size() >= 2 && !imagePath.isEmpty()) {
            Point2D p1 = clickedPositions.get(clickedPositions.size() - 2);
            Point2D p2 = clickedPositions.get(clickedPositions.size() - 1);

            double x1 = Math.min(p1.getX(), p2.getX());
            double y1 = Math.min(p1.getY(), p2.getY());
            double x2 = Math.max(p1.getX(), p2.getX());
            double y2 = Math.max(p1.getY(), p2.getY());

            Image image;
            image = Objects.requireNonNullElseGet(originalImage, () -> new Image(imagePath));

            int width = (int) (x2 - x1);
            int height = (int) (y2 - y1);

            WritableImage croppedImage = new WritableImage(width, height);
            PixelReader pixelReader = image.getPixelReader();
            PixelWriter pixelWriter = croppedImage.getPixelWriter();

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    Color color = pixelReader.getColor((int) (x1 + i), (int) (y1 + j));
                    pixelWriter.setColor(i, j, color);
                }
            }




            canvas.setWidth(width);
            canvas.setHeight(height);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.drawImage(croppedImage, 0, 0);

            prevImage = originalImage;
            isPrevDisabled = false;
            prevButtonImage.setDisable(false);
            originalImage = croppedImage;
        }
    }

    private void displaySavedPositions() {
        StringBuilder positionsText = new StringBuilder();
        for (Point2D point : clickedPositions) {
            positionsText.append("(").append(point.getX()).append(", ").append(point.getY()).append(")\n");
        }
        positionsLabel.setText(positionsText.toString());
    }
}