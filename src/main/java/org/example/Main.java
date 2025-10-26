package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.service.QuizService;
import org.example.view.SceneNavigator;
import org.example.controller.MenuController;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        SceneNavigator nav = new SceneNavigator(stage);
        QuizService service = new QuizService();
        nav.go("/fxml/menu.fxml", new MenuController(nav, service, stage), "Quiz – Menu");
    }

    public static void main(String[] args) { launch(); }
}
