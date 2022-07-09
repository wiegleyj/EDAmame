package com.cyte.edamame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class EDAmameApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EDAmame.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        URL url = EDAmame.class.getResource("css/EDAmame.css");
        if (url != null)
            scene.getStylesheets().add(url.toExternalForm());

        stage.setTitle("\u679d\u8c46 EDAmame");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}