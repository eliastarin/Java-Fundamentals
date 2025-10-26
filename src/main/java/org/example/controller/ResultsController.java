package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Window;
import org.example.service.QuizService;
import org.example.view.SceneNavigator;

public class ResultsController {
    private final SceneNavigator nav;
    private final QuizService service;
    private final Window owner;
    private final String playerName;
    private final int score;
    private final int total;

    public ResultsController(SceneNavigator nav, QuizService service, Window owner,
                             String playerName, int score, int total) {
        this.nav = nav;
        this.service = service;
        this.owner = owner;
        this.playerName = playerName;
        this.score = score;
        this.total = total;
    }

    @FXML private Label scoreLbl;

    @FXML
    public void initialize() {
        scoreLbl.setText(playerName + ", your score: " + score + " / " + total);
    }

    @FXML
    public void onBack() {
        nav.go("/fxml/menu.fxml", new MenuController(nav, service, owner), "Quiz – Menu");
    }
}
