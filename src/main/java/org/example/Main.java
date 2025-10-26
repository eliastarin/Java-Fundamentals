package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.view.SceneNavigator;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        SceneNavigator nav = new SceneNavigator(stage);
        nav.go("/fxml/menu.fxml", new org.example.controller.MenuController(nav), "Quiz – Menu");

    }

    public static void main(String[] args) { launch(); }
}
