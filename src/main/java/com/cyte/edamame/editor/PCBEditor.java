/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;

import java.util.List;
import java.util.Map;

/**
 * Editor for developing Printed Circuit Boards
 */
public class PCBEditor extends Editor
{


    //// CALLBACK FUNCTIONS ////

    public void ViewportOnDragOver()
    {
        System.out.println("PCB dragged over!");
    }

    public void ViewportOnDragDropped()
    {
        System.out.println("PCB drag dropped!");
    }

    public void ViewportOnMouseMoved()
    {
        System.out.println("PCB mouse moved!");
    }

    public void ViewportOnMousePressed()
    {
        System.out.println("PCB mouse pressed!");
    }

    public void ViewportOnMouseReleased()
    {
        System.out.println("PCB mouse released!");
    }

    public void ViewportOnMouseDragged()
    {
        System.out.println("PCB mouse dragged!");
    }

    public void ViewportOnScroll()
    {
        System.out.println("PCB mouse scrolled!");
    }
}
