package org.example.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {
    private final Stage stage;

    public SceneNavigator(Stage stage) { this.stage = stage; }

    public void go(String fxmlResource, Object controller, String title) {
        try {
            var url = getClass().getResource(fxmlResource);
            if (url == null) throw new IllegalArgumentException("FXML not found: " + fxmlResource);
            FXMLLoader loader = new FXMLLoader(url);
            if (controller != null) loader.setController(controller);
            Parent root = loader.load();

            // add the stylesheet
            var css = getClass().getResource("/css/app.css");
            if (css != null) root.getStylesheets().add(css.toExternalForm());

            stage.setTitle(title);
            stage.setScene(new Scene(root, 640, 400));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
