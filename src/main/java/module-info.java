module org.example.mini_game {
    requires javafx.controls;
    requires javafx.fxml;
<<<<<<< HEAD
=======
    requires javafx.graphics;
>>>>>>> da5a30d (Fixed the robot movement and added some debugging tools)


    opens org.example.mini_game to javafx.fxml;
    exports org.example.mini_game;
}