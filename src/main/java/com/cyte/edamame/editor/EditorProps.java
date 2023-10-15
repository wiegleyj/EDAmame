/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.EDAmame;
import com.cyte.edamame.render.RenderShape;
import javafx.fxml.*;
import javafx.scene.*;
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
            propsWindow.EditorProps_Stage.setAlwaysOnTop(true);
            propsWindow.EditorProps_PropsBox.getChildren().add(new Label("Press \"Load Properties\" to load all element properties from currently-active editor."));

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

        this.EditorProps_Editor.Editor_ElemPropsLoad();
    }

    @FXML
    public void EditorProps_PropsApply()
    {
        if (this.EditorProps_Editor == null)
            throw new java.lang.Error("ERROR: Attempting to apply from properties window with null editor reference!");

        this.EditorProps_Editor.Editor_ElemPropsApply();
    }
}
