package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.example.model.Choice;
import org.example.model.Question;
import org.example.service.QuizService;
import org.example.view.SceneNavigator;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class GameController {
    private final SceneNavigator nav;
    private final QuizService service;
    private final Window owner;

    private final AtomicInteger index = new AtomicInteger(0);
    private int score = 0;
    private String playerName = "";

    public GameController(SceneNavigator nav, QuizService service, Window owner) {
        this.nav = nav;
        this.service = service;
        this.owner = owner;
    }

    @FXML private Label questionLbl;
    @FXML private VBox choicesBox;
    @FXML private Label progressLbl;

    @FXML
    public void initialize() {
        // 2c) Ask for the user’s name before showing the first question.
        if (!askPlayerName()) {
            // User cancelled or empty name -> back to menu
            nav.go("/fxml/menu.fxml", new MenuController(nav, service, owner), "Quiz – Menu");
            return;
        }
        showCurrent();
    }

    private boolean askPlayerName() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.initOwner(owner);
        dlg.setTitle("Player Name");
        dlg.setHeaderText("Enter your name to start the quiz");
        dlg.setContentText("Name:");

        Optional<String> result = dlg.showAndWait();
        if (result.isEmpty()) return false;
        String name = result.get().trim();
        if (name.isBlank()) return false;

        playerName = name;
        return true;
    }

    private void showCurrent() {
        List<Question> qs = service.getQuestions();
        if (qs == null || qs.isEmpty()) {
            nav.go("/fxml/menu.fxml", new MenuController(nav, service, owner), "Quiz – Menu");
            return;
        }
        if (index.get() >= qs.size()) {
            nav.go("/fxml/results.fxml",
                    new ResultsController(nav, service, owner, playerName, score, qs.size()),
                    "Quiz – Results");
            return;
        }

        Question q = qs.get(index.get());
        questionLbl.setText(q.getText());
        progressLbl.setText("Question " + (index.get() + 1) + " / " + qs.size());

        choicesBox.getChildren().clear();
        for (Choice c : q.getChoices()) {
            Button b = new Button(c.getText());
            b.setMaxWidth(Double.MAX_VALUE);
            b.setOnAction(e -> {
                if (c.isCorrect()) score++;
                index.incrementAndGet();
                showCurrent();
            });
            choicesBox.getChildren().add(b);
        }
    }
}
