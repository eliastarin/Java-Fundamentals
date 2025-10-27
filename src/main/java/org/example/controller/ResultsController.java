package org.example.controller;

import javafx.stage.FileChooser;
import java.io.File;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import org.example.service.QuizService;
import org.example.service.ResultsService;
import org.example.view.SceneNavigator;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

public class ResultsController {

    private final SceneNavigator nav;
    private final QuizService service;
    private final Window owner;
    private final String playerName;
    private final int score;
    private final int total;

    private final ResultsService resultsService = new ResultsService();

    @FXML private Label quizNameLbl;
    @FXML private Label scoreLbl;
    @FXML private WebView completedHtmlView;

    @FXML private TableView<LeaderboardRow> leaderboardTable;
    @FXML private TableColumn<LeaderboardRow, String> playerCol;
    @FXML private TableColumn<LeaderboardRow, String> scoreCol;
    @FXML private TableColumn<LeaderboardRow, String> dateCol;

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
        // Quiz name + your score (rounded to int %)
        String quizName = service.getLastQuizName();
        quizNameLbl.setText("Quiz name: " + quizName);
        int percent = (total > 0) ? (int)Math.round(100.0 * score / total) : 0;
        scoreLbl.setText("Your score: " + percent + "%");

        // Completed HTML message
        WebEngine engine = completedHtmlView.getEngine();
        String completedHtml = service.renderCompletedHtml(total, score);
        engine.loadContent(completedHtml, "text/html");

        // Leaderboard table columns
        playerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().playerName()));
        scoreCol.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().scorePercent() + "%"));
        dateCol.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().dateISO()));

        refreshLeaderboard();
    }

    @FXML
    public void onFinish() {
        try {
            String quizId = service.getLastQuizId();
            String quizName = service.getLastQuizName();
            var entry = ResultsService.makeEntry(playerName, total, score);

            resultsService.appendAndReadAll(quizId, quizName, entry);
            refreshLeaderboard();

            showInfo("Saved to: results/" + quizId + "-results.json");
        } catch (IOException e) {
            showError("Failed to save results: " + e.getMessage());
        }
    }

    // exporting the CSV
    @FXML
    public void onExportCsv() {
        try {
            String quizId = service.getLastQuizId();
            String quizName = service.getLastQuizName();

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export Leaderboard CSV");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
            chooser.setInitialFileName(quizId + "-leaderboard.csv");

            File file = chooser.showSaveDialog(owner);
            if (file == null) return;

            // export
            resultsService.exportCsv(quizId, quizName, file);
            showInfo("Exported to: " + file.getAbsolutePath());
        } catch (IOException e) {
            showError("Failed to export CSV: " + e.getMessage());
        }
    }


    @FXML
    public void onBack() {
        nav.go("/fxml/menu.fxml", new MenuController(nav, service, owner), "Quiz – Menu");
    }

    private void refreshLeaderboard() {
        try {
            String quizId = service.getLastQuizId();
            String quizName = service.getLastQuizName();

            // Load all saved results
            List<ResultsService.ResultEntry> all = resultsService.loadAll(quizId, quizName);

            // Sort by score desc
            var rows = all.stream()
                    .map(LeaderboardRow::fromEntry)
                    .sorted(Comparator.comparingInt(LeaderboardRow::scorePercent).reversed())
                    .toList();

            leaderboardTable.getItems().setAll(rows);
        } catch (IOException e) {
            leaderboardTable.getItems().clear();
        }
    }

    private void showError(String msg) {
        var a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(owner);
        a.setTitle("Error");
        a.setHeaderText("Could not complete action");
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

    public static final class LeaderboardRow {
        private final String playerName;
        private final int totalQuestions;
        private final int correctQuestions;
        private final String dateISO;

        public LeaderboardRow(String playerName, int totalQuestions, int correctQuestions, String dateISO) {
            this.playerName = playerName;
            this.totalQuestions = totalQuestions;
            this.correctQuestions = correctQuestions;
            this.dateISO = dateISO;
        }

        public static LeaderboardRow fromEntry(ResultsService.ResultEntry e) {
            return new LeaderboardRow(
                    e.getPlayerName(),
                    e.getTotalQuestions(),
                    e.getCorrectQuestions(),
                    e.getDate()
            );
        }

        public String playerName() { return playerName; }

        public int scorePercent() {
            return (totalQuestions > 0)
                    ? (int)Math.round(100.0 * correctQuestions / totalQuestions)
                    : 0;
        }

        public String dateISO() {
            // YYYY-MM-DD
            try {
                return OffsetDateTime.parse(dateISO).toLocalDate().toString();
            } catch (Exception ex) {
                return dateISO;
            }
        }
    }
}
