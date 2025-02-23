package org.example.mini_game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class MazeGame extends Application {
    private static final int STEP = 5; // Movement step size
    private ImageView robot;
    private PixelReader pixelReader;
    private double scaleX, scaleY; // Scaling factors

    // Load maze image
    private Image mazeImage = new Image(getClass().getResourceAsStream("/maze.png"));
    Pane root = new Pane();
    Scene scene = new Scene(root, 600, 600);

    private double startX = 15, startY = 380;
    private double endX = 570, endY = 345;
    private final double END_RANGE = 20; // Tolerance area for reaching the "End"

    @Override
    public void start(Stage stage) {

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
        robot.setX(startX);
        robot.setY(startY);

        root.getChildren().addAll(maze, label, robot);

        stage.setTitle("Maze Puzzle");
        stage.setScene(scene);
        stage.show();

        // Find the shortest path using A* algorithm
        findPath();
    }

    // This method is to trigger pathfinding again after reset
    public void restartGame() {
        // Reset the robot's position to the start position
        robot.setX(startX);
        robot.setY(startY);

        // Recalculate the scale and pixel reader in case we need it
        scaleX = mazeImage.getWidth() / 600;
        scaleY = mazeImage.getHeight() / 600;
        pixelReader = mazeImage.getPixelReader();

        // Trigger the pathfinding again
        findPath();
    }

    private String getKey(double x, double y) {
        return x + "," + y;  // A simple combination of x and y
    }

    private ArrayList<String> reconstructPath(Map<String, String> parents, String goalKey) {
        ArrayList<String> path = new ArrayList<>();
        String currentKey = goalKey;

        // Backtrack from the goal to the start using the parents map
        while (currentKey != null) {
            path.add(currentKey);  // Add the current node to the path
            currentKey = parents.get(currentKey);  // Move to the parent node
        }

        // Reverse the path so it goes from start to goal
        Collections.reverse(path);

        return path;
    }

    public void findPath() {
        // Priority Queue to explore the nodes with the smallest f-value (f = g + h)
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));

        // Map to track the shortest distance to each node (g-cost)
        Map<String, Integer> gCosts = new HashMap<>();
        // Map to track the parent of each node (for path reconstruction)
        Map<String, String> parents = new HashMap<>();

        // Add the start node to the priority queue
        String startKey = getKey(startX, startY);
        pq.add(new Node(startX, startY, 0, calculateHeuristic(startX, startY)));
        gCosts.put(startKey, 0);

        // Directions for movement (right, left, down, up)
        int[] moveX = {STEP, -STEP, 0, 0}; // Right, Left, Down, Up
        int[] moveY = {0, 0, STEP, -STEP}; // Right, Left, Down, Up

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            double currentX = current.x;
            double currentY = current.y;
            String currentKey = getKey(currentX, currentY);

            // If we reached the destination, reconstruct the path
            if (reachedEnd(currentX, currentY)) {
                ArrayList<String> path = reconstructPath(parents, currentKey);
                moveRobotAlongPath(path);
                return;
            }

            // Explore the neighboring positions
            for (int i = 0; i < 4; i++) {
                double newX = currentX + moveX[i];
                double newY = currentY + moveY[i];

                // Skip out-of-bounds or wall positions
                if (!isValidMove(newX, newY)) continue;

                String newKey = getKey(newX, newY);
                int newGCost = current.g + 1; // All movements cost 1 step
                int newHCost = calculateHeuristic(newX, newY);
                int newFCost = newGCost + newHCost;

                // If the new position provides a shorter path, update it
                if (newGCost < gCosts.getOrDefault(newKey, Integer.MAX_VALUE)) {
                    gCosts.put(newKey, newGCost);
                    parents.put(newKey, currentKey);
                    pq.add(new Node(newX, newY, newGCost, newHCost));
                }
            }
        }

    }

    // Check if the robot has reached the end (within tolerance range)
    private boolean reachedEnd(double x, double y) {
        return Math.abs(x - endX) < END_RANGE && Math.abs(y - endY) < END_RANGE;
    }

    // Check if a position is a valid move (not a wall)
    private boolean isValidMove(double x, double y) {
        // Map the robot's position to the original image coordinates
        int px = (int) (x * scaleX);
        int py = (int) (y * scaleY);

        // Ensure coordinates are within the maze boundaries
        if (px < 0 || py < 0 || px >= mazeImage.getWidth() || py >= mazeImage.getHeight()) {
            return false;
        }

        Color color = pixelReader.getColor(px, py);
        return !isWall(color); // Only allow movement if it's not a wall
    }

    // Helper method to check if a pixel is a wall (not white)
    private boolean isWall(Color color) {
        Color white = Color.web("#FFFFFF");
        return !color.equals(white);
    }

    // Calculate the heuristic using the Manhattan distance
    private int calculateHeuristic(double x, double y) {
        return (int) (Math.abs(x - endX) + Math.abs(y - endY)); // Manhattan distance
    }

    private void moveRobotAlongPath(ArrayList<String> path) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(path.size());

        for (int i = 0; i < path.size(); i++) {
            String nodeKey = path.get(i);
            String[] parts = nodeKey.split(",");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);

            // Adjust robot position with some offset or scaling tweak
            double adjustedX = x - 20;  // Adjust the robot position to fix the offset
            double adjustedY = y -10;

            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * 30), e -> {
                robot.setX(adjustedX);
                robot.setY(adjustedY);
            });

            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();  // Start the animation
    }


    // Node class to store information about each position
    static class Node {
        double x, y;
        int g, h, f;

        Node(double x, double y, int g, int h) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
