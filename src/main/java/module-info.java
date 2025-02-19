module org.example.mini_game {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.mini_game to javafx.fxml;
    exports org.example.mini_game;
}
