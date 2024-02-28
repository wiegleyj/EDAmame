/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.EDAmame;
import com.cyte.edamame.EDAmameController;

import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;

public class EditorSettings
{
    //// GLOBAL VARIABLES ////

    @FXML
    public VBox settingsBox;

    public Stage stage;
    public Editor editor = null;

    //// MAIN FUNCTIONS ////

    static public EditorSettings Create(Editor editorValue)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorSettings.fxml"));
            Scene scene = new Scene(loader.load());
            EditorSettings settingsWindow = loader.getController();

            settingsWindow.stage = new Stage();
            settingsWindow.stage.setScene(scene);

            switch (editorValue.GetType())
            {
                case 0:
                    settingsWindow.stage.setTitle("Symbol Editor Settings");
                    break;

                case 1:
                    settingsWindow.stage.setTitle("Schematic Editor Settings");
                    break;

                case 2:
                    settingsWindow.stage.setTitle("Footprint Editor Settings");
                    break;

                case 3:
                    settingsWindow.stage.setTitle("PCB Editor Settings");
                    break;

                default:
                    throw new java.lang.Error("ERROR: Attempting to create an editor settings window with an editor of invalid type!");
            }

            settingsWindow.stage.setAlwaysOnTop(true);
            settingsWindow.stage.setResizable(false);
            settingsWindow.settingsBox.setSpacing(10);
            settingsWindow.editor = editorValue;

            return settingsWindow;
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: " + exception.getMessage());
            exception.printStackTrace();
        }

        return null;
    }

    //// CALLBACK FUNCTIONS ////

    public void SettingsLoad()
    {
        if (this.editor == null)
            throw new java.lang.Error("ERROR: Attempting to load into editor settings window with null editor reference!");

        // Clearing any existing settings...
        EDAmameController.editorSettingsWindow.settingsBox.getChildren().clear();

        this.editor.SettingsLoadGlobal();
        this.editor.SettingsLoadSpecific();
    }

    @FXML
    public void SettingsApply()
    {
        if (this.editor == null)
            throw new java.lang.Error("ERROR: Attempting to apply from editor settings window with null editor reference!");

        this.editor.SettingsApplyGlobal();
        this.editor.SettingsApplySpecific();
    }

    @FXML
    public void KeyPressed(KeyEvent event)
    {
        EDAmameController.KeyPressed(event.getCode());
    }

    @FXML
    public void KeyReleased(KeyEvent event)
    {
        EDAmameController.KeyReleased(event.getCode());
    }
}
