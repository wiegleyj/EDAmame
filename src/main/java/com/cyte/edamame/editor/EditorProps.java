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

public class EditorProps
{
    //// GLOBAL VARIABLES ////

    @FXML
    public VBox propsBox;

    public Stage stage;
    public Editor editor = null;

    //// MAIN FUNCTIONS ////

    static public EditorProps Create(Editor editorValue)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorProps.fxml"));
            Scene scene = new Scene(loader.load());
            EditorProps propsWindow = loader.getController();

            propsWindow.stage = new Stage();
            propsWindow.stage.setScene(scene);
            propsWindow.stage.setTitle("Element Properties");
            propsWindow.stage.setAlwaysOnTop(true);
            propsWindow.stage.setResizable(false);
            propsWindow.propsBox.setSpacing(10);
            propsWindow.propsBox.getChildren().add(new Label("Press \"Load Properties\" to load all element type properties\nfrom currently-active editor."));
            propsWindow.editor = editorValue;

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
    public void PropsLoad()
    {
        if (this.editor == null)
            throw new java.lang.Error("ERROR: Attempting to load into properties window with null editor reference!");

        // Clearing any existing properties...
        EDAmameController.editorPropertiesWindow.propsBox.getChildren().clear();

        // Only attempting to load node properties if we have some nodes selected...
        if (this.editor.shapesSelected == 0)
        {
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Label("Press \"Load Properties\" to load all element type properties\nfrom currently-active editor."));
            EDAmameController.SetStatusBar("Unable to load element properties because no elements are selected!");

            return;
        }

        // Loading both the global & editor-specific properties...
        this.editor.PropsLoadGlobal();
        this.editor.PropsLoadSpecific();
    }

    @FXML
    public void PropsApply()
    {
        if (this.editor == null)
            throw new java.lang.Error("ERROR: Attempting to apply from properties window with null editor reference!");

        // Only attempting to apply node properties if we have some nodes selected...
        if (this.editor.shapesSelected == 0)
        {
            EDAmameController.SetStatusBar("Unable to apply element properties because no elements are selected!");

            return;
        }

        // Applying both the global & editor-specific properties...
        this.editor.PropsApplyGlobal();
        this.editor.PropsApplySpecific();

        // Refreshing all highlighted & selected shapes...
        for (int i = 0; i < this.editor.nodes.size(); i++)
            this.editor.nodes.get(i).ShapeSelectedRefresh();
            //this.EditorProps_Editor.Editor_RenderSystem.RenderSystem_Nodes.get(i).RenderNode_BoundsRefresh();
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
