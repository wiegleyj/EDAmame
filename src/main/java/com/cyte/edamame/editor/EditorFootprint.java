/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.EDAmame;
import com.cyte.edamame.util.PairMutable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.io.IOException;

/**
 * Editor for managing footprint libraries.
 */
public class EditorFootprint extends Editor
{
    //// MAIN FUNCTIONS ////

    @FXML
    private Button EditorFootprint_InnerButton;

    public static Editor EditorFootprint_Create() throws IOException
    {
        // Loading FXML file for the symbol editor
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorFootprint.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorSymbol editor = fxmlLoader.getController();
        editor.Editor_Init(1, "EditorFootprint");
        editor.Editor_Dissect(1, scene);
        editor.Editor_RenderSystem.RenderSystem_CanvasRenderGrid();
        editor.Editor_ListenersInit();

        return editor;
    }

    @FXML
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + this.EditorFootprint_InnerButton);
    }

    //// CALLBACK FUNCTIONS ////

    public void Editor_OnDragOverSpecific(DragEvent event)
    {
        System.out.println("Footprint dragged over!");
    }

    public void Editor_OnDragDroppedSpecific(DragEvent event)
    {
        System.out.println("Footprint drag dropped!");
    }

    public void Editor_OnMouseMovedSpecific(MouseEvent event)
    {
        System.out.println("Footprint mouse moved!");
    }

    public void Editor_OnMousePressedSpecific(MouseEvent event)
    {
        System.out.println("Footprint mouse pressed!");
    }

    public void Editor_OnMouseReleasedSpecific(MouseEvent event)
    {
        System.out.println("Footprint mouse released!");
    }

    public void Editor_OnMouseDraggedSpecific(MouseEvent event)
    {
        System.out.println("Footprint mouse dragged!");
    }

    public void Editor_OnScrollSpecific(ScrollEvent event)
    {
        System.out.println("Footprint mouse scrolled!");
    }

    public void Editor_OnKeyPressedSpecific(KeyEvent event)
    {
        System.out.println("Footprint key pressed!");
    }

    public void Editor_OnKeyReleasedSpecific(KeyEvent event)
    {
        System.out.println("Footprint key released!");
    }

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void Editor_PropsLoadSpecific()
    {
        System.out.println("Loading props footprint!");
    }

    public void Editor_PropsApplySpecific()
    {
        System.out.println("Applying props footprint!");
    }
}
