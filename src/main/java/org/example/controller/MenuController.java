package org.example.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import org.example.service.QuizService;
import org.example.view.SceneNavigator;

import java.io.File;

public class MenuController {
    private final SceneNavigator nav;
    private final QuizService service;
    private final Window owner;

    public MenuController(SceneNavigator nav, QuizService service, Window owner) {
        this.nav = nav;
        this.service = service;
        this.owner = owner;
    }

    @FXML private Button startBtn;
    @FXML private Label statusLbl;

    @FXML
    public void initialize() {
        statusLbl.setText("Load a quiz JSON to begin.");
        startBtn.setDisable(true);
    }

    @FXML
    public void onLoad() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select quiz JSON");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
        File file = fc.showOpenDialog(owner);
        if (file == null) return;

        boolean ok = service.loadFromFile(file.toPath());
        if (ok) {
            statusLbl.setText("Loaded: " + file.getName() + " • " + service.getQuestions().size() + " questions");
            startBtn.setDisable(false);
        } else {
            statusLbl.setText("Failed to load quiz.");
            startBtn.setDisable(true);
            showError("Could not read the selected JSON file.\n" +
                    "Please check that it’s valid and try again.");
        }
    }

    private void showError(String msg) {
        var a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(owner);
        a.setTitle("Load Error");
        a.setHeaderText("Error loading quiz");
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    public void onStart() {
        nav.go("/fxml/game.fxml", new GameController(nav, service, owner), "Quiz – Game");
    }
}
