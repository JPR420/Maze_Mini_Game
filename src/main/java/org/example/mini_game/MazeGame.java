package org.example.mini_game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    Pane root = new Pane();
    Scene scene = new Scene(root, 600, 600);


    @Override
    public void start(Stage stage) {

//        Rectangle rectangle = new Rectangle(25,53);
//        rectangle.setFill(Color.rgb(255,0,0));
//        rectangle.setY(330);
//        rectangle.setX(569);

        Label label = new Label("End");
        label.setLayoutY(345);
        label.setLayoutX(570);


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
        robot.setX(15);
        robot.setY(380); // Start position

        root.getChildren().addAll(maze, label,robot);

//        Scene scene = new Scene(root, 600, 600);

        // Handle keyboard input
        scene.setOnKeyPressed(event -> {
            double newX = robot.getX();
            double newY = robot.getY();

            if (event.getCode() == KeyCode.UP) newY -= STEP;
            else if (event.getCode() == KeyCode.DOWN) newY += STEP;
            else if (event.getCode() == KeyCode.LEFT) newX -= STEP;
            else if (event.getCode() == KeyCode.RIGHT) newX += STEP;

            // Check if new position is on a valid path before moving
            if (isValidMove(newX + (robot.getFitWidth()/2), newY + (robot.getFitHeight()/2))) {
                robot.setX(newX);
                robot.setY(newY);
            }

                //If the end is reached, another window will show with a button in case the player wants to play again
            if (reachedEnd(newX, newY)) {
                Stage window = (Stage) scene.getWindow();
                Pane pane = new Pane();
                Scene wonScene = new Scene( pane,600, 600);
                Label wonLabel = new Label("You won!");
                wonLabel.setLayoutX(295);
                wonLabel.setLayoutY(275);
                Button playAgain = new Button("Play Again");
                playAgain.setLayoutX(285);
                playAgain.setLayoutY(300);
                pane.getChildren().addAll(wonLabel, playAgain);
                window.setResizable(false);
                window.setScene(wonScene);
                window.show();

                playAgain.setOnAction(e -> {
                    // Reset the robot's position
                    robot.setX(15);
                    robot.setY(380);

                    // Reinitialize or set the original game scene (not the wonScene)
                   stage.setScene(scene); // Or simply set the original game scene here
                   stage.show(); // Show the window again
                });




            }


        });

        stage.setTitle("Maze Puzzle");
        stage.setScene(scene);
        stage.show();
    }


    private double endX = 570; // X position of "End" label
    private double endY = 345; // Y position of "End" label
    private final double END_RANGE = 20;
    // Tolerance area for reaching the "End"
    private boolean reachedEnd(double x, double y) {
        return Math.abs(x - endX) < END_RANGE && Math.abs(y - endY) < END_RANGE;
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
        System.out.println(isWall(color));

        return !isWall(color); // Only allow movement if it's not a wall
    }

    // Helper method to check if a pixel is a wall (blue)
    private boolean isWall(Color color) {
        Color white = Color.web("#FFFFFF");

        return !(color.equals(white));
    }

    public void path(int x, int y) {


    }

    public static void main(String[] args) {
        launch();
    }
}
