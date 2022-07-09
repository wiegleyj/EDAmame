package com.cyte.edamame.editor;

import com.cyte.edamame.EDAmame;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

public class SymbolEditor extends Editor {
    public static Editor create() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/SymbolEditor.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        SymbolEditor result = fxmlLoader.getController();

        result.dissect(scene);
        return result;
    }

    public void initialize() {
    }

    @FXML
    private void thisButton() {
        System.out.println("Button clicked on " + editorID);
    }
}
