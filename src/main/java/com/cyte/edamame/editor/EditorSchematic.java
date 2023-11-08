/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.EDAmame;
import com.cyte.edamame.file.File;
import com.cyte.edamame.render.RenderNode;
import com.cyte.edamame.util.PairMutable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Editor for developing schematics
 */
public class EditorSchematic extends Editor
{
    //// GLOBAL VARIABLES ////

    @FXML
    private Button EditorSchematic_InnerButton;

    //// MAIN FUNCTIONS ////

    public static Editor EditorSchematic_Create() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorSchematic.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorSchematic editor = fxmlLoader.getController();
        editor.Editor_Init(1, "EditorSchematic");
        editor.Editor_Dissect(1, scene);
        editor.Editor_RenderSystem.RenderSystem_CanvasRenderGrid();
        editor.Editor_ListenersInit();

        return editor;
    }

    @FXML
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + this.EditorSchematic_InnerButton);
    }

    //// CALLBACK FUNCTIONS ////

    @FXML
    public void EditorSchematic_Save()
    {
        System.out.println("Loading schematic!");
    }

    @FXML
    public void EditorSchematic_Load()
    {
        System.out.println("Saving schematic!");
    }

    @FXML
    public void EditorSchematic_LoadSymbol()
    {
        System.out.println("Loading symbol!");
    }

    public void Editor_OnDragOverSpecific(DragEvent event)
    {
        System.out.println("Schematic dragged over!");
    }

    public void Editor_OnDragDroppedSpecific(DragEvent event)
    {
        System.out.println("Schematic drag dropped!");
    }

    public void Editor_OnMouseMovedSpecific(MouseEvent event)
    {
        System.out.println("Schematic mouse moved!");
    }

    public void Editor_OnMousePressedSpecific(MouseEvent event)
    {
        System.out.println("Schematic mouse pressed!");
    }

    public void Editor_OnMouseReleasedSpecific(MouseEvent event)
    {
        System.out.println("Schematic mouse released!");
    }

    public void Editor_OnMouseDraggedSpecific(MouseEvent event)
    {
        System.out.println("Schematic mouse dragged!");
    }

    public void Editor_OnScrollSpecific(ScrollEvent event)
    {
        System.out.println("Schematic mouse scrolled!");
    }

    public void Editor_OnKeyPressedSpecific(KeyEvent event)
    {
        System.out.println("Schematic key pressed!");
    }

    public void Editor_OnKeyReleasedSpecific(KeyEvent event)
    {
        System.out.println("Schematic key released!");
    }

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void Editor_PropsLoadSpecific()
    {
        System.out.println("Loading props schematic!");
    }

    public void Editor_PropsApplySpecific()
    {
        System.out.println("Applying props schematic!");
    }
}
