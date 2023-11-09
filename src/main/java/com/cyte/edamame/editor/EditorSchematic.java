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
import javafx.scene.paint.Color;

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
        LinkedList<Node> nodes = File.File_NodesLoad(true);

        if ((nodes == null) || nodes.isEmpty())
            return;

        Group symbolNode = new Group();
        //symbolNode.setStyle("-fx-background-color:black");
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealCenter();

        symbolNode.setTranslateX(dropPos.GetLeftDouble());
        symbolNode.setTranslateY(dropPos.GetRightDouble());
        symbolNode.getChildren().addAll(nodes);

        //System.out.println((nodeBounds.GetLeftPair().GetRightDouble() - nodeBounds.GetLeftPair().GetLeftDouble()) + ", " + (nodeBounds.GetRightPair().GetRightDouble() - nodeBounds.GetRightPair().GetLeftDouble()));

        /*symbolNode.setMinWidth(Pane.USE_COMPUTED_SIZE);
        symbolNode.setMinHeight(Pane.USE_COMPUTED_SIZE);
        symbolNode.setMaxWidth(Pane.USE_COMPUTED_SIZE);
        symbolNode.setMaxHeight(Pane.USE_COMPUTED_SIZE);
        symbolNode.setPrefWidth(Pane.USE_COMPUTED_SIZE);
        symbolNode.setPrefHeight(Pane.USE_COMPUTED_SIZE);*/

        RenderNode symbolRenderNode = new RenderNode("LoadedSymbolNode", symbolNode, false, this.Editor_RenderSystem);
        this.Editor_RenderSystem.RenderSystem_NodeAdd(symbolRenderNode);

        //this.Editor_RenderSystem.RenderSystem_TestShapeAdd(dropPos, 15.0, Color.BLUE, false);
    }

    public void Editor_OnDragOverSpecific(DragEvent event)
    {}

    public void Editor_OnDragDroppedSpecific(DragEvent event)
    {}

    public void Editor_OnMouseMovedSpecific(MouseEvent event)
    {}

    public void Editor_OnMousePressedSpecific(MouseEvent event)
    {}

    public void Editor_OnMouseReleasedSpecific(MouseEvent event)
    {}

    public void Editor_OnMouseDraggedSpecific(MouseEvent event)
    {}

    public void Editor_OnScrollSpecific(ScrollEvent event)
    {}

    public void Editor_OnKeyPressedSpecific(KeyEvent event)
    {}

    public void Editor_OnKeyReleasedSpecific(KeyEvent event)
    {}

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void Editor_PropsLoadSpecific()
    {}

    public void Editor_PropsApplySpecific()
    {}
}
