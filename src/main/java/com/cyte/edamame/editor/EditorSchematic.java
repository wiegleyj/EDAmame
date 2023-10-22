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
 * Editor for developing schematics
 */
public class EditorSchematic extends Editor
{


    //// CALLBACK FUNCTIONS ////

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

    public void Editor_OnMouseDraggedSpecific(MouseEvent event, PairMutable mouseDiffPos)
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
