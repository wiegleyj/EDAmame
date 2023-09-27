/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.util.PairMutable;

import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;

import java.util.List;
import java.util.Map;

/**
 * Editor for managing footprint libraries.
 */
public class FootprintEditor extends Editor
{


    //// CALLBACK FUNCTIONS ////

    public void Editor_ViewportOnDragOver()
    {
        System.out.println("Footprint dragged over!");
    }

    public void Editor_ViewportOnDragDropped()
    {
        System.out.println("Footprint drag dropped!");
    }

    public void Editor_ViewportOnMouseMoved()
    {
        System.out.println("Footprint mouse moved!");
    }

    public void Editor_ViewportOnMousePressed()
    {
        System.out.println("Footprint mouse pressed!");
    }

    public void Editor_ViewportOnMouseReleased()
    {
        System.out.println("Footprint mouse released!");
    }

    public void Editor_ViewportOnMouseDragged(PairMutable mouseDiffPos)
    {
        System.out.println("Footprint mouse dragged!");
    }

    public void Editor_ViewportOnScroll()
    {
        System.out.println("Footprint mouse scrolled!");
    }
}
