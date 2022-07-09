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
        FXMLLoader loader = new FXMLLoader(EDAmame.class.getResource("fxml/EDAmame.fxml"));
        loader.setControllerFactory(c -> new EDAmameController(stage));

        Scene scene = new Scene(loader.load());
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