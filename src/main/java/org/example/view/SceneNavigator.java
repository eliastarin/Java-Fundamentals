package org.example.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {
    private final Stage stage;

    public SceneNavigator(Stage stage) {
        this.stage = stage;
    }

    public void go(String fxmlResource, Object controller, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResource));
            if (controller != null) loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 400);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
