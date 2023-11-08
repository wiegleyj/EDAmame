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
 * Editor for developing Printed Circuit Boards
 */
public class EditorPCB extends Editor
{
    //// GLOBAL VARIABLES ////

    @FXML
    private Button EditorPCB_InnerButton;

    //// MAIN FUNCTIONS ////

    public static Editor EditorPCB_Create() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorPCB.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorPCB editor = fxmlLoader.getController();
        editor.Editor_Init(3, "EditorPCB");
        editor.Editor_Dissect(3, scene);
        editor.Editor_RenderSystem.RenderSystem_CanvasRenderGrid();
        editor.Editor_ListenersInit();

        return editor;
    }

    @FXML
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + this.EditorPCB_InnerButton);
    }

    //// CALLBACK FUNCTIONS ////

    public void Editor_OnDragOverSpecific(DragEvent event)
    {
        System.out.println("PCB dragged over!");
    }

    public void Editor_OnDragDroppedSpecific(DragEvent event)
    {
        System.out.println("PCB drag dropped!");
    }

    public void Editor_OnMouseMovedSpecific(MouseEvent event)
    {
        System.out.println("PCB mouse moved!");
    }

    public void Editor_OnMousePressedSpecific(MouseEvent event)
    {
        System.out.println("PCB mouse pressed!");
    }

    public void Editor_OnMouseReleasedSpecific(MouseEvent event)
    {
        System.out.println("PCB mouse released!");
    }

    public void Editor_OnMouseDraggedSpecific(MouseEvent event)
    {
        System.out.println("PCB mouse dragged!");
    }

    public void Editor_OnScrollSpecific(ScrollEvent event)
    {
        System.out.println("PCB mouse scrolled!");
    }

    public void Editor_OnKeyPressedSpecific(KeyEvent event)
    {
        System.out.println("PCB key pressed!");
    }

    public void Editor_OnKeyReleasedSpecific(KeyEvent event)
    {
        System.out.println("PCB key released!");
    }

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void Editor_PropsLoadSpecific()
    {
        System.out.println("Loading props PCB!");
    }

    public void Editor_PropsApplySpecific()
    {
        System.out.println("Applying props PCB!");
    }
}
