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
 * Editor for managing footprint libraries.
 */
public class EditorFootprint extends Editor
{


    //// CALLBACK FUNCTIONS ////

    public void Editor_ViewportOnDragOver(DragEvent event)
    {
        System.out.println("Footprint dragged over!");
    }

    public void Editor_ViewportOnDragDropped(DragEvent event)
    {
        System.out.println("Footprint drag dropped!");
    }

    public void Editor_ViewportOnMouseMoved(MouseEvent event)
    {
        System.out.println("Footprint mouse moved!");
    }

    public void Editor_ViewportOnMousePressed(MouseEvent event)
    {
        System.out.println("Footprint mouse pressed!");
    }

    public void Editor_ViewportOnMouseReleased(MouseEvent event)
    {
        System.out.println("Footprint mouse released!");
    }

    public void Editor_ViewportOnMouseDragged(MouseEvent event)
    {
        System.out.println("Footprint mouse dragged!");
    }

    public void Editor_ViewportOnScroll(ScrollEvent event)
    {
        System.out.println("Footprint mouse scrolled!");
    }

    public void Editor_ViewportOnKeyPressed(KeyEvent event)
    {
        System.out.println("Footprint key pressed!");
    }

    public void Editor_ViewportOnKeyReleased(KeyEvent event)
    {
        System.out.println("Footprint key released!");
    }
}
