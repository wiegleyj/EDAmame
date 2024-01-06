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
import com.cyte.edamame.node.EDANode;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.util.Utils;
import com.cyte.edamame.netlist.NetListExperimental;

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
    private Button innerButton;
    @FXML
    public ToggleGroup toggleGroup;
    @FXML
    public TextField wireWidth;
    @FXML
    public ColorPicker wireColor;

    //// MAIN FUNCTIONS ////

    public static Editor Create() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorSchematic.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorSchematic editor = fxmlLoader.getController();
        editor.Init(1, "EditorSchematic");
        editor.Dissect(1, scene);
        editor.CanvasRenderGrid();
        editor.ListenersInit();

        Editor.TextFieldListenerInit(editor.wireWidth);

        return editor;
    }

    @FXML
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + this.innerButton);
    }

    //// CALLBACK FUNCTIONS ////

    @FXML
    public void Save()
    {
        NetListExperimental<String> netList = this.ToNetList();

        File.Write("C:\\Users\\SVARUN\\Downloads\\netlist.txt", netList.ToString(), true);
    }

    @FXML
    public void Load()
    {
        System.out.println("Loading schematic!");
    }

    @FXML
    public void LoadSymbol()
    {
        LinkedList<Node> nodes = File.NodesLoad(true);

        if ((nodes == null) || nodes.isEmpty())
            return;

        Group symbolNode = new Group();
        //symbolNode.setStyle("-fx-background-color:black");
        PairMutable dropPos = this.PaneHolderGetRealCenter();

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

        EDANode symbolRenderNode = new EDANode("LoadedSymbolNode", symbolNode, false, snapsManualPos, false, false, this);
        this.NodeAdd(symbolRenderNode);

        //this.Editor_RenderSystem.RenderSystem_TestShapeAdd(dropPos, 15.0, Color.BLUE, false);
    }

    public void OnDragOverSpecific(DragEvent event)
    {}

    public void OnDragDroppedSpecific(DragEvent event)
    {}

    public void OnMouseMovedSpecific(MouseEvent event)
    {}

    public void OnMousePressedSpecific(MouseEvent event)
    {}

    public void OnMouseReleasedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        dropPos = this.MagneticSnapCheck(dropPos);
        PairMutable realPos = this.PaneHolderGetRealPos(dropPos);

        // Handling element dropping (only if we're not hovering over, selecting, moving any shapes or box selecting)
        if ((this.shapesSelected == 0) &&
                !this.shapesMoving &&
                (this.selectionBox == null) &&
                !this.shapesWereSelected &&
                !this.wasSelectionBox)
        {
            RadioButton selectedShapeButton = (RadioButton) toggleGroup.getSelectedToggle();

            // Only dropping the element within the theater limits...
            if (selectedShapeButton != null)
            {
                if (!selectedShapeButton.getText().equals("Wire"))
                    this.linePreview = null;

                boolean lineStarted = false;

                if (this.shapesHighlighted == 0)
                {
                    if (selectedShapeButton.getText().equals("Wire"))
                    {
                        // If we're starting the line drawing...
                        if (this.linePreview == null)
                        {
                            String stringWidth = this.wireWidth.getText();
                            Color color = this.wireColor.getValue();

                            if (EDAmameController.IsStringNum(stringWidth))
                            {
                                double width = Double.parseDouble(stringWidth);

                                if (((width >= EDAmameController.Editor_WireWidthMin) && (width <= EDAmameController.Editor_WireWidthMax)))
                                {
                                    if ((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                                    {
                                        this.linePreview = new Line();

                                        this.linePreview.setStartX(dropPos.GetLeftDouble());
                                        this.linePreview.setStartY(dropPos.GetRightDouble());
                                        this.linePreview.setEndX(dropPos.GetLeftDouble());
                                        this.linePreview.setEndY(dropPos.GetRightDouble());

                                        this.linePreview.setStrokeWidth(width);
                                        this.linePreview.setStroke(color);

                                        EDANode renderNode = new EDANode("linePreview", this.linePreview, true, null, true, false, this);
                                        this.NodeAdd(renderNode);

                                        lineStarted = true;
                                    }
                                    else
                                    {
                                        EDAmameController.SetStatusBar("Unable to drop wire because the entered color field is transparent!");
                                    }
                                }
                                else
                                {
                                    EDAmameController.SetStatusBar("Unable to drop wire because the entered width is outside the limits! (Width limits: " + EDAmameController.Editor_WireWidthMin + ", " + EDAmameController.Editor_WireWidthMax + ")");
                                }
                            }
                            else
                            {
                                EDAmameController.SetStatusBar("Unable to drop wire because the entered width field is non-numeric!");
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
                    if ((this.linePreview != null) && !lineStarted)
                    {
                        PairMutable posStart = new PairMutable(this.linePreview.getStartX(), this.linePreview.getStartY());
                        PairMutable posEnd = new PairMutable(dropPos.GetLeftDouble(), dropPos.GetRightDouble());

                        Line line = new Line();
                        Editor.LineDropPosCalculate(line, posStart, posEnd);

                        line.setStroke(this.linePreview.getStroke());
                        line.setStrokeWidth(this.linePreview.getStrokeWidth());

                        this.LinePreviewRemove();

                        EDANode renderNode = new EDANode("Wire", line, true, null, false, false, this);
                        this.NodeAdd(renderNode);
                    }
                }
            }
        }
    }

    public void OnMouseDraggedSpecific(MouseEvent event)
    {}

    public void OnScrollSpecific(ScrollEvent event)
    {}

    public void OnKeyPressedSpecific(KeyEvent event)
    {}

    public void OnKeyReleasedSpecific(KeyEvent event)
    {}

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void PropsLoadSpecific()
    {}

    public void PropsApplySpecific()
    {}
}
