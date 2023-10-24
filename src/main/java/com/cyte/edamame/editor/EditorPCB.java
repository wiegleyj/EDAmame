/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.util.PairMutable;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Editor for developing Printed Circuit Boards
 */
public class EditorPCB extends Editor
{


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
