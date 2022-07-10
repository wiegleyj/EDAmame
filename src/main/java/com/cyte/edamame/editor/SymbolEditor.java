/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.EDAmame;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

/**
 * Editor for maintaining Symbol libraries.
 */
public class SymbolEditor extends Editor {
    /**
     * Constructs a single SymbolEditor attached to a particular symbol library.
     *
     * @throws IOException if there are problems loading the scene from FXML resources.
     */
    public SymbolEditor() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/SymbolEditor.fxml"));
        fxmlLoader.setController(this);
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        dissect(scene);
    }

    /**
     * Provides initialization of the Controller
     */
    public void initialize() {
    }

    /**
     * a test button for checking if controller is working and unique. (hint: it is.) this can be removed.
     */
    @FXML
    private void thisButton() {
        System.out.println("Button clicked on " + editorID);
    }
}
