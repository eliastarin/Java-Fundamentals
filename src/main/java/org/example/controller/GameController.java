package org.example.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;
import org.example.model.Choice;
import org.example.model.Question;
import org.example.service.QuizService;
import org.example.view.SceneNavigator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GameController {
    private final SceneNavigator nav;
    private final QuizService service;
    private final Window owner;
    private final AtomicInteger index = new AtomicInteger(0);
    private int score = 0;
    private String playerName = "";
    private Timeline timer;
    private int timeLeft;        // seconds
    private int timeTotal;       // seconds

    public GameController(SceneNavigator nav, QuizService service, Window owner) {
        this.nav = nav;
        this.service = service;
        this.owner = owner;
    }

    @FXML private Label nameLbl;
    @FXML private Label questionLbl;
    @FXML private VBox choicesBox;
    @FXML private Label progressLbl;
    @FXML private Label timerLbl;
    @FXML private ProgressBar timerBar;

    @FXML
    public void initialize() {
        // ask for name
        var name = new javafx.scene.control.TextInputDialog().showAndWait().orElse("").trim();
        if (name.isBlank()) {
            nav.go("/fxml/menu.fxml", new MenuController(nav, service, owner), "Quiz – Menu");
            return;
        }
        playerName = name;
        nameLbl.setText("Player: " + playerName);

        showCurrent();
    }

    private void showCurrent() {
        List<Question> qs = service.getQuestions();
        if (qs == null || qs.isEmpty()) {
            nav.go("/fxml/menu.fxml", new MenuController(nav, service, owner), "Quiz – Menu");
            return;
        }
        if (index.get() >= qs.size()) {
            stopTimer();
            nav.go("/fxml/results.fxml",
                    new ResultsController(nav, service, owner, playerName, score, qs.size()),
                    "Quiz – Results");
            return;
        }

        Question q = qs.get(index.get());
        questionLbl.setText(q.getText());
        progressLbl.setText("Question " + (index.get() + 1) + " / " + qs.size());

        // shuffle
        choicesBox.getChildren().clear();
        List<Choice> shuffled = new ArrayList<>(q.getChoices());
        Collections.shuffle(shuffled);

        for (Choice c : shuffled) {
            Button b = new Button(c.getText());
            b.setMaxWidth(Double.MAX_VALUE);
            b.setOnAction(e -> {
                stopTimer();
                if (c.isCorrect()) score++;
                index.incrementAndGet();
                showCurrent();
            });
            choicesBox.getChildren().add(b);
        }

        // start timer for this question
        int defaultSec = 15;
        timeTotal = (q.getTimeLimitSec() != null && q.getTimeLimitSec() > 0) ? q.getTimeLimitSec() : defaultSec;
        startTimer(timeTotal);
    }
    // start the timer
    private void startTimer(int seconds) {
        stopTimer(); // clear previous
        timeLeft = seconds;
        updateTimerUI();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            updateTimerUI();
            if (timeLeft <= 0) {
                stopTimer();
                // time is up
                index.incrementAndGet();
                showCurrent();
            }
        }));
        timer.setCycleCount(seconds);
        timer.playFromStart();
    }
    // stop the timer
    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
    // timer UI
    private void updateTimerUI() {
        timerLbl.setText(timeLeft + "s");
        double progress = (timeTotal == 0) ? 0 : (double) timeLeft / (double) timeTotal;
        timerBar.setProgress(progress);
        timerLbl.setStyle(timeLeft <= 5 ? "-fx-font-weight: bold; -fx-text-fill: #c0392b;" : "");
    }
}
