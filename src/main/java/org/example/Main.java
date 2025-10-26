package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.view.SceneNavigator;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        SceneNavigator nav = new SceneNavigator(stage);
        nav.go("/fxml/menu.fxml", new org.example.controller.MenuController(nav), "Quiz – Menu");
        // --- quick smoke test of the model ---
        var q1 = new org.example.model.Question();
        q1.setCategory("General");
        q1.setText("Which class do you extend for a JavaFX app?");
        q1.setChoices(java.util.List.of(
                new org.example.model.Choice("Stage", false),
                new org.example.model.Choice("Application", true),
                new org.example.model.Choice("Scene", false),
                new org.example.model.Choice("FXMLLoader", false)
        ));

        var quiz = new org.example.model.Quiz("Sample", java.util.List.of(q1));
        System.out.println("Model test: " + quiz.getTitle() +
                " • questions=" + quiz.getQuestions().size());
        // --- end test ---

    }

    public static void main(String[] args) { launch(); }
}
