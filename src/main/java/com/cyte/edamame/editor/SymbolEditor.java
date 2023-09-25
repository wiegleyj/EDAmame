/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;
import com.cyte.edamame.EDAmameApplication;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.EDAmame;
import com.cyte.edamame.render.CanvasRenderSystem;
import com.cyte.edamame.render.CanvasRenderShape;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;

import java.io.IOException;
import java.io.InvalidClassException;

/**
 * Editor for maintaining Symbol libraries.
 */
public class SymbolEditor extends Editor
{
    //// GLOBAL VARIABLES ////

    @FXML
    private Tab etab;
    @FXML
    private Tab ctab1;
    @FXML
    private ToolBar toolBar;
    @FXML
    private Button innerButton;

    //// MAIN FUNCTIONS ////

    /**
     * Factory to create a single SymbolEditor and its UI attached to a particular symbol library.
     *
     * @throws IOException if there are problems loading the scene from FXML resources.
     */
    public static Editor create() throws IOException
    {
        // Loading FXML file for the symbol editor
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/SymbolEditor.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        SymbolEditor editor = fxmlLoader.getController();
        editor.editorName = "SymbolEditor";
        editor.dissect(0, scene);

        editor.renderSystem.InitListeners();

        // Creating test point grid
        CanvasRenderShape testShapeBlueprint = new CanvasRenderShape();
        testShapeBlueprint.AddPoint(0.0, 0.0, 5.0, Color.GRAY, 0.5);
        testShapeBlueprint.permanent = true;

        Double posX = -1000.0;
        Double posY = -100.0;

        for (int i = 0; i < 100; i++)
        {
            for (int j = 0; j < 100; j++)
            {
                CanvasRenderShape testShape = new CanvasRenderShape(testShapeBlueprint);
                testShape.posReal = new PairMutable(posX, posY);

                editor.renderSystem.AddShape(-1, testShape);
                posX += 100.0;
            }

            posX = -1000.0;
            posY += 100.0;
            System.out.println(posY);
        }

        return editor;
    }

    /**
     * Provides initialization of the Controller
     */
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + innerButton);
    }

    //// CALLBACK FUNCTIONS ////

    public void ViewportOnDragOver()
    {
        System.out.println("Symbol dragged over!");
    }

    public void ViewportOnDragDropped()
    {
        System.out.println("Symbol drag dropped!");
    }

    public void ViewportOnMouseMoved()
    {
        System.out.println("Symbol mouse moved!");
    }

    public void ViewportOnMousePressed()
    {
        System.out.println("Symbol mouse pressed!");
    }

    public void ViewportOnMouseReleased()
    {
        System.out.println("Symbol mouse released!");
    }

    public void ViewportOnMouseDragged()
    {
        System.out.println("Symbol mouse dragged!");
    }

    public void ViewportOnScroll()
    {
        System.out.println("Symbol mouse scrolled!");
    }

    //// TESTING FUNCTIONS ////

    /**
     * a test button for checking if controller is working and unique. (hint: it is.) this can be removed.
     */
    /*@FXML
    private void thisButton()
    {
        System.out.println("Button clicked on " + editorID);
    }*/
}
