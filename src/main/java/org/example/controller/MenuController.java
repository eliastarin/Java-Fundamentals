package org.example.controller;

import javafx.event.ActionEvent;
import org.example.view.SceneNavigator;

public class MenuController {
    private final SceneNavigator nav;

    public MenuController(SceneNavigator nav) {
        this.nav = nav;
    }

    public void onStart(ActionEvent e) {
        nav.go("/fxml/game.fxml", null, "Quiz – Game");
    }
}
