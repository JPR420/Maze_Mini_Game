package org.example.mini_game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Objects;

public class MazeGame extends Application {
    private static final int STEP = 5; // Movement step size
    private ImageView robot;
    private PixelReader pixelReader;
    private double scaleX, scaleY; // Scaling factors

    // Load maze image
    private Image mazeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/maze.png"))); // Store maze image reference

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();

        ImageView maze = new ImageView(mazeImage);
        maze.setFitWidth(600);
        maze.setFitHeight(600);

        // Calculate scaling factors
        scaleX = mazeImage.getWidth() / maze.getFitWidth();
        scaleY = mazeImage.getHeight() / maze.getFitHeight();

        // Get pixel reader from the maze image
        pixelReader = mazeImage.getPixelReader();

        // Load robot image
        robot = new ImageView(new Image(getClass().getResourceAsStream("/robot.png")));
        robot.setFitWidth(20);
        robot.setFitHeight(20);
        robot.setX(50);
        robot.setY(50); // Start position

        root.getChildren().addAll(maze, robot);

        Scene scene = new Scene(root, 600, 600);

        // Handle keyboard input
        scene.setOnKeyPressed(event -> {
            double newX = robot.getX();
            double newY = robot.getY();

            if (event.getCode() == KeyCode.UP) newY -= STEP;
            else if (event.getCode() == KeyCode.DOWN) newY += STEP;
            else if (event.getCode() == KeyCode.LEFT) newX -= STEP;
            else if (event.getCode() == KeyCode.RIGHT) newX += STEP;

            // Check if new position is on a valid path before moving
            if (isValidMove(newX, newY)) {
                robot.setX(newX);
                robot.setY(newY);
            }
        });

        stage.setTitle("Maze Puzzle");
        stage.setScene(scene);
        stage.show();
    }

    // Check if the new position is valid (not on a blue pixel)
    private boolean isValidMove(double x, double y) {
        // Map the robot's position to the original image coordinates
        int px = (int) (x * scaleX);
        int py = (int) (y * scaleY);

        // Ensure coordinates are within the maze boundaries
        if (px < 0 || py < 0 || px >= (int) mazeImage.getWidth() || py >= (int) mazeImage.getHeight()) {
            return false;
        }

        Color color = pixelReader.getColor(px, py);

        // Print the color value for debugging
        System.out.println("Pixel at (" + px + ", " + py + ") - Color: " + color);

        return !isWall(color); // Only allow movement if it's not a wall
    }

    // Helper method to check if a pixel is a wall (blue)
    private boolean isWall(Color color) {
        // Check if the color is close to blue with tolerance
        return (Math.abs(color.getRed() - 0.0) < 0.1 &&
                Math.abs(color.getGreen() - 0.0) < 0.1 &&
                Math.abs(color.getBlue() - 1.0) < 0.1);
    }

    public static void main(String[] args) {
        launch();
    }
}
