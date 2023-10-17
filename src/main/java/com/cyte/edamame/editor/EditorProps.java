/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.EDAmame;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.render.RenderShape;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;

import java.io.IOException;

public class EditorProps
{
    //// GLOBAL VARIABLES ////

    @FXML
    public VBox EditorProps_PropsBox;

    public Stage EditorProps_Stage;
    public Editor EditorProps_Editor = null;

    //// MAIN FUNCTIONS ////

    static public EditorProps EditorProps_Create()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorProps.fxml"));
            Scene scene = new Scene(loader.load());
            EditorProps propsWindow = loader.getController();

            propsWindow.EditorProps_Stage = new Stage();
            propsWindow.EditorProps_Stage.setScene(scene);
            propsWindow.EditorProps_Stage.setTitle("Element Properties");
            propsWindow.EditorProps_Stage.setAlwaysOnTop(true);
            propsWindow.EditorProps_Stage.setResizable(false);
            propsWindow.EditorProps_PropsBox.setSpacing(10);
            propsWindow.EditorProps_PropsBox.getChildren().add(new Label("Press \"Load Properties\" to load all element type properties\nfrom currently-active editor."));

            return propsWindow;
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: " + exception.getMessage());
            exception.printStackTrace();
        }

        return null;
    }

    //// CALLBACK FUNCTIONS ////

    @FXML
    public void EditorProps_PropsLoad()
    {
        if (this.EditorProps_Editor == null)
            throw new java.lang.Error("ERROR: Attempting to load into properties window with null editor reference!");

        // Clearing any existing properties...
        EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().clear();

        // Only attempting to load node properties if we have some nodes selected...
        if (this.EditorProps_Editor.Editor_RenderSystem.shapesSelected == 0)
        {
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Label("Press \"Load Properties\" to load all element type properties\nfrom currently-active editor."));
            EDAmameController.Controller_SetStatusBar("Unable to load element properties because no elements are selected!");

            return;
        }

        // Loading both the global & editor-specific properties...
        this.EditorProps_Editor.Editor_PropsGlobalLoad();
        this.EditorProps_Editor.Editor_PropsSpecificLoad();
    }

    @FXML
    public void EditorProps_PropsApply()
    {
        if (this.EditorProps_Editor == null)
            throw new java.lang.Error("ERROR: Attempting to apply from properties window with null editor reference!");

        // Only attempting to apply node properties if we have some nodes selected...
        if (this.EditorProps_Editor.Editor_RenderSystem.shapesSelected == 0)
        {
            EDAmameController.Controller_SetStatusBar("Unable to apply element properties because no elements are selected!");

            return;
        }

        // Applying both the global & editor-specific properties...
        this.EditorProps_Editor.Editor_PropsGlobalApply();
        this.EditorProps_Editor.Editor_PropsSpecificApply();

        // Refreshing any highlighted or selected shapes...
        for (int i = 0; i < this.EditorProps_Editor.Editor_RenderSystem.shapes.size(); i++)
        {
            RenderShape shape = this.EditorProps_Editor.Editor_RenderSystem.shapes.get(i);

            shape.CalculateShapeHighlighted();
            int shapeHighlightedIdx = EDAmameController.Controller_FindNodeById(this.EditorProps_Editor.Editor_RenderSystem.paneHighlights.getChildren(), shape.id);

            if (shapeHighlightedIdx != -1)
            {
                this.EditorProps_Editor.Editor_RenderSystem.paneHighlights.getChildren().remove(shapeHighlightedIdx);
                this.EditorProps_Editor.Editor_RenderSystem.paneHighlights.getChildren().add(shapeHighlightedIdx, shape.shapeHighlighted);
            }

            shape.CalculateShapeSelected();
            int shapeSelectedIdx = EDAmameController.Controller_FindNodeById(this.EditorProps_Editor.Editor_RenderSystem.paneSelections.getChildren(), shape.id);

            if (shapeSelectedIdx != -1)
            {
                this.EditorProps_Editor.Editor_RenderSystem.paneSelections.getChildren().remove(shapeSelectedIdx);
                this.EditorProps_Editor.Editor_RenderSystem.paneSelections.getChildren().add(shapeSelectedIdx, shape.shapeSelected);
            }
        }
    }

    @FXML
    public void EditorProps_KeyPressed(KeyEvent event)
    {
        EDAmameController.Controller_KeyPressed(event.getCode());
    }

    @FXML
    public void EditorProps_KeyReleased(KeyEvent event)
    {
        EDAmameController.Controller_KeyReleased(event.getCode());
    }
}
