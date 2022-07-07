package com.cyte.edamame;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EDAmameController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}