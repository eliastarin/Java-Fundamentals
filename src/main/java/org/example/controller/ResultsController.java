package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Window;
import org.example.service.QuizService;
import org.example.service.ResultsService;
import org.example.view.SceneNavigator;

import java.io.IOException;
import java.util.List;

public class ResultsController {
    private final SceneNavigator nav;
    private final QuizService service;
    private final Window owner;
    private final String playerName;
    private final int score;
    private final int total;

    private final ResultsService resultsService = new ResultsService();

    @FXML private Label scoreLbl;
    @FXML private ListView<String> resultsList;

    public ResultsController(SceneNavigator nav,
                             QuizService service,
                             Window owner,
                             String playerName,
                             int score,
                             int total) {
        this.nav = nav;
        this.service = service;
        this.owner = owner;
        this.playerName = playerName;
        this.score = score;
        this.total = total;
    }

    @FXML
    public void initialize() {
        int percent = (total > 0) ? (int)Math.round(100.0 * score / total) : 0;
        scoreLbl.setText(playerName + ", your score: " + score + " / " + total + " (" + percent + "%)");

        // Show existing saved results (if any)
        refreshSavedList(false);
    }

    @FXML
    public void onFinish() {
        try {
            String quizId = service.getLastQuizId();
            String quizName = service.getLastQuizName();

            var entry = ResultsService.makeEntry(playerName, total, score);
            resultsService.appendAndReadAll(quizId, quizName, entry);

            refreshSavedList(true);
        } catch (IOException e) {
            showError("Failed to save results: " + e.getMessage());
        }
    }

    @FXML
    public void onBack() {
        nav.go("/fxml/menu.fxml", new MenuController(nav, service, owner), "Quiz – Menu");
    }

    private void refreshSavedList(boolean showOk) {
        try {
            String quizId = service.getLastQuizId();
            String quizName = service.getLastQuizName();
            List<ResultsService.ResultEntry> all = resultsService.appendAndReadAll(quizId, quizName, null);
            resultsList.getItems().setAll(all.stream().map(Object::toString).toList());
            if (showOk) showInfo("Saved to: results/" + quizId + "-results.json");
        } catch (IOException ignore) {
            // No file yet -> nothing to list
        }
    }

    private void showError(String msg) {
        var a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(owner);
        a.setTitle("Save Error");
        a.setHeaderText("Could not save results");
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        var a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(owner);
        a.setTitle("Saved");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
