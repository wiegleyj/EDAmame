/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * EDAmame JavaFX {@link Application}
 *
 * The head JavaFX class that loads the initial stage, main scene, and UI controller.
 *
 *  @author Jeff Wiegley, Ph.D.
 *  @author jeffrey.wiegley@gmail.com
 */
public class EDAmameApplication extends Application {
    /**
     * Override for {@link Application} start method that is called as a result of the launch method.
     *
     * @param stage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws IOException Thrown when FXML loading problems occur. This likely means there is an
     * error in the FXML or that FXML specifies needed controller elements that cannot be injected
     * into the controller.
     */
    @Override
    public void start(Stage stage) throws IOException {
        Locale locale = new Locale("en", "US");
        ResourceBundle bundle = ResourceBundle.getBundle("com.cyte.edamame.strings", locale);
        FXMLLoader loader = new FXMLLoader(EDAmame.class.getResource("fxml/EDAmame.fxml"));
        loader.setResources(bundle); // Internationalization is a priority but not well understood.
        loader.setControllerFactory(c -> new EDAmameController(stage));

        Scene scene = new Scene(loader.load());
        URL url = EDAmame.class.getResource("css/EDAmame.css");
        if (url != null)
            scene.getStylesheets().add(url.toExternalForm());

        stage.setTitle("\u679d\u8c46 EDAmame");
        stage.setScene(scene);

        stage.show();
    }

    /**
     * The {@link Application} main start point.
     * @param args Command line Arguments. (Should be copied from the {@link EDAmame} startup veneer class.
     */
    public static void main(String[] args) {
        launch();
    }
}