/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;
import com.cyte.edamame.EDAmame;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.file.File;
import com.cyte.edamame.render.RenderNode;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.util.Utils;
import com.cyte.edamame.netlist.NetListExperimental;
import com.cyte.edamame.netlist.NetListExperimentalNode;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

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
    @FXML
    public ToggleGroup EditorSchematic_ToggleGroup;
    @FXML
    public TextField EditorSchematic_WireWidth;
    @FXML
    public ColorPicker EditorSchematic_WireColor;

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

        Editor.Editor_TextFieldListenerInit(editor.EditorSchematic_WireWidth);

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
        NetListExperimental<String> netList = new NetListExperimental<String>();

        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);
            NetListExperimentalNode<String> netListNode = new NetListExperimentalNode<String>(renderNode.RenderNode_Name);

            netList.Append(netListNode);
        }

        File.File_Write("C:\\Users\\SVARUN\\Downloads\\netlist.txt", netList.ToString(), true);
    }

    @FXML
    public void EditorSchematic_Load()
    {
        System.out.println("Loading schematic!");
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

        LinkedList<PairMutable> snapsManualPos = null;

        for (int i = 0; i < symbolNode.getChildren().size(); i++)
        {
            Node node = symbolNode.getChildren().get(i);

            if (node.getClass() == Group.class)
            {
                if (snapsManualPos == null)
                    snapsManualPos = new LinkedList<PairMutable>();

                Group group = (Group)node;

                for (int j = 0; j < group.getChildren().size(); j++)
                {
                    Node currChild = group.getChildren().get(j);

                    if (currChild.getClass() == Circle.class)
                    {
                        snapsManualPos.add(Utils.GetPosInNodeParent(group, new PairMutable(currChild.getTranslateX(), currChild.getTranslateY())));
                        //this.Editor_RenderSystem.RenderSystem_TestShapeAdd(Utils.GetPosInNodeParent(group, new PairMutable(currChild.getTranslateX(), currChild.getTranslateY())), 5.0, Color.RED, false);
                    }
                }
            }
        }

        //System.out.println((nodeBounds.GetLeftPair().GetRightDouble() - nodeBounds.GetLeftPair().GetLeftDouble()) + ", " + (nodeBounds.GetRightPair().GetRightDouble() - nodeBounds.GetRightPair().GetLeftDouble()));

        /*symbolNode.setMinWidth(Pane.USE_COMPUTED_SIZE);
        symbolNode.setMinHeight(Pane.USE_COMPUTED_SIZE);
        symbolNode.setMaxWidth(Pane.USE_COMPUTED_SIZE);
        symbolNode.setMaxHeight(Pane.USE_COMPUTED_SIZE);
        symbolNode.setPrefWidth(Pane.USE_COMPUTED_SIZE);
        symbolNode.setPrefHeight(Pane.USE_COMPUTED_SIZE);*/

        RenderNode symbolRenderNode = new RenderNode("LoadedSymbolNode", symbolNode, false, snapsManualPos, false, false, this.Editor_RenderSystem);
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
    {
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        dropPos = this.Editor_MagneticSnapCheck(dropPos);
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

        // Handling element dropping (only if we're not hovering over, selecting, moving any shapes or box selecting)
        if ((this.Editor_ShapesSelected == 0) &&
                !this.Editor_ShapesMoving &&
                (this.Editor_SelectionBox == null) &&
                !this.Editor_ShapesWereSelected &&
                !this.Editor_WasSelectionBox)
        {
            RadioButton selectedShapeButton = (RadioButton)EditorSchematic_ToggleGroup.getSelectedToggle();

            // Only dropping the element within the theater limits...
            if (selectedShapeButton != null)
            {
                if (!selectedShapeButton.getText().equals("Wire"))
                    this.EditorSymbol_LinePreview = null;

                boolean lineStarted = false;

                if (this.Editor_ShapesHighlighted == 0)
                {
                    if (selectedShapeButton.getText().equals("Wire"))
                    {
                        // If we're starting the line drawing...
                        if (this.EditorSymbol_LinePreview == null)
                        {
                            String stringWidth = this.EditorSchematic_WireWidth.getText();
                            Color color = this.EditorSchematic_WireColor.getValue();

                            if (EDAmameController.Controller_IsStringNum(stringWidth))
                            {
                                double width = Double.parseDouble(stringWidth);

                                if (((width >= EDAmameController.Editor_WireWidthMin) && (width <= EDAmameController.Editor_WireWidthMax)))
                                {
                                    if ((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                                    {
                                        this.EditorSymbol_LinePreview = new Line();

                                        this.EditorSymbol_LinePreview.setStartX(dropPos.GetLeftDouble());
                                        this.EditorSymbol_LinePreview.setStartY(dropPos.GetRightDouble());
                                        this.EditorSymbol_LinePreview.setEndX(dropPos.GetLeftDouble());
                                        this.EditorSymbol_LinePreview.setEndY(dropPos.GetRightDouble());

                                        this.EditorSymbol_LinePreview.setStrokeWidth(width);
                                        this.EditorSymbol_LinePreview.setStroke(color);

                                        RenderNode renderNode = new RenderNode("linePreview", this.EditorSymbol_LinePreview, true, null, true, false, this.Editor_RenderSystem);
                                        this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);

                                        lineStarted = true;
                                    }
                                    else
                                    {
                                        EDAmameController.Controller_SetStatusBar("Unable to drop wire because the entered color field is transparent!");
                                    }
                                }
                                else
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop wire because the entered width is outside the limits! (Width limits: " + EDAmameController.Editor_WireWidthMin + ", " + EDAmameController.Editor_WireWidthMax + ")");
                                }
                            }
                            else
                            {
                                EDAmameController.Controller_SetStatusBar("Unable to drop wire because the entered width field is non-numeric!");
                            }
                        }
                    }
                    else
                    {
                        throw new java.lang.Error("ERROR: Attempt to drop an element shape in a Schematic Editor!");
                    }
                }

                if (selectedShapeButton.getText().equals("Wire"))
                {
                    // If we're finishing the line drawing...
                    if ((this.EditorSymbol_LinePreview != null) && !lineStarted)
                    {
                        PairMutable posStart = new PairMutable(this.EditorSymbol_LinePreview.getStartX(), this.EditorSymbol_LinePreview.getStartY());
                        PairMutable posEnd = new PairMutable(dropPos.GetLeftDouble(), dropPos.GetRightDouble());

                        Line line = new Line();
                        Editor.Editor_LineDropPosCalculate(line, posStart, posEnd);

                        line.setStroke(this.EditorSymbol_LinePreview.getStroke());
                        line.setStrokeWidth(this.EditorSymbol_LinePreview.getStrokeWidth());

                        this.Editor_LinePreviewRemove();

                        RenderNode renderNode = new RenderNode("Wire", line, true, null, false, false, this.Editor_RenderSystem);
                        this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
                    }
                }
            }
        }
    }

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
