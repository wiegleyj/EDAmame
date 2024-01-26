/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.EDAmame;
import com.cyte.edamame.EDAmameController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.collections.*;

import java.io.IOException;

/**
 * Editor for managing footprint libraries.
 */
public class EditorFootprint extends Editor
{
    //// GLOBAL VARIABLES ////

    @FXML
    private Button innerButton;

    public double snapGridSpacing;

    //// MAIN FUNCTIONS ////

    public static Editor Create() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorFootprint.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorFootprint editor = fxmlLoader.getController();
        editor.Init(2, "EditorFootprint");
        editor.Dissect(2, scene);
        editor.snapGridSpacing = EDAmameController.Editor_SnapGridSpacings[EDAmameController.Editor_SnapGridSpacings.length / 2];
        editor.CanvasRenderGrid();
        editor.ListenersInit();

        return editor;
    }

    @FXML
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + this.innerButton);
    }

    //// CALLBACK FUNCTIONS ////

    @FXML
    public void SettingsKeyPressed()
    {
        // Handling element properties window...
        if (EDAmameController.editorSettingsWindow == null)
        {
            // Attempting to create the properties window...
            EditorSettings settingsWindow = EditorSettings.Create(this);

            if (settingsWindow != null)
            {
                settingsWindow.stage.setOnHidden(e -> {
                    EDAmameController.editorSettingsWindow = null;
                });
                settingsWindow.stage.show();

                EDAmameController.editorSettingsWindow = settingsWindow;
                settingsWindow.SettingsLoad();
            }
        }
    }

    public void OnDragOverSpecific(DragEvent event)
    {}

    public void OnDragDroppedSpecific(DragEvent event)
    {}

    public void OnMouseMovedSpecific(MouseEvent event)
    {}

    public void OnMousePressedSpecific(MouseEvent event)
    {}

    public void OnMouseReleasedSpecific(MouseEvent event)
    {}

    public void OnMouseDraggedSpecific(MouseEvent event)
    {}

    public void OnScrollSpecific(ScrollEvent event)
    {}

    public void OnKeyPressedSpecific(KeyEvent event)
    {}

    public void OnKeyReleasedSpecific(KeyEvent event)
    {}

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void PropsLoadSpecific()
    {}

    public void PropsApplySpecific()
    {}

    //// SETTINGS WINDOW FUNCTIONS ////

    public void SettingsLoadSpecific()
    {
        Text globalHeader = new Text("Footprint Editor Settings:");
        globalHeader.setStyle("-fx-font-weight: bold;");
        globalHeader.setStyle("-fx-font-size: 16px;");
        EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(globalHeader);
        EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(new Separator());

        // Creating grid spacing distance box...
        {
            HBox gridSpacingBox = new HBox(10);
            gridSpacingBox.setId("gridSpacingBox");
            gridSpacingBox.getChildren().add(new Label("Grid Spacing Distance: "));
            ChoiceBox<Double> gridSpacing = new ChoiceBox<Double>();

            for (int i = 0; i < EDAmameController.Editor_SnapGridSpacings.length; i++)
                gridSpacing.getItems().add(EDAmameController.Editor_SnapGridSpacings[i]);

            gridSpacing.setId("gridSpacing");
            gridSpacingBox.getChildren().add(gridSpacing);

            gridSpacing.setValue(this.snapGridSpacing);

            EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(gridSpacingBox);
        }
    }

    public void SettingsApplySpecific()
    {
        // Applying grid spacing distance...
        {
            Integer gridSpacingBoxIdx = EDAmameController.FindNodeById(EDAmameController.editorSettingsWindow.settingsBox.getChildren(), "gridSpacingBox");

            if (gridSpacingBoxIdx != -1)
            {
                HBox gridSpacingBox = (HBox)EDAmameController.editorSettingsWindow.settingsBox.getChildren().get(gridSpacingBoxIdx);
                ChoiceBox<Double> choiceBox = (ChoiceBox<Double>)EDAmameController.GetNodeById(gridSpacingBox.getChildren(), "gridSpacing");

                this.snapGridSpacing = choiceBox.getValue();
            }
        }
    }
}
