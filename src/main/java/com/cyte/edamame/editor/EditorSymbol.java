/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.render.RenderNode;
import com.cyte.edamame.file.File;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.EDAmame;

import java.io.*;
import java.util.LinkedList;

import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
//import java.awt.*;
//import java.awt.event.*;

/**
 * Editor for maintaining Symbol libraries.
 */
public class EditorSymbol extends Editor
{
    //// GLOBAL VARIABLES ////

    @FXML
    private Button EditorSymbol_InnerButton;
    @FXML
    public ToggleGroup EditorSymbol_ToggleGroup;
    @FXML
    public ColorPicker EditorSymbol_CircleColor;
    @FXML
    public TextField EditorSymbol_CircleRadius;
    @FXML
    public ColorPicker EditorSymbol_CircleBorderColor;
    @FXML
    public TextField EditorSymbol_CircleBorderSize;
    @FXML
    public ColorPicker EditorSymbol_RectangleColor;
    @FXML
    public TextField EditorSymbol_RectangleWidth;
    @FXML
    public TextField EditorSymbol_RectangleHeight;
    @FXML
    public ColorPicker EditorSymbol_RectangleBorderColor;
    @FXML
    public TextField EditorSymbol_RectangleBorderSize;
    @FXML
    public ColorPicker EditorSymbol_TriangleBorderColor;
    @FXML
    public TextField EditorSymbol_TriangleBorderSize;
    @FXML
    public ColorPicker EditorSymbol_TriangleColor;
    @FXML
    public TextField EditorSymbol_TriangleHeight;
    @FXML
    public TextField EditorSymbol_LineWidth;
    @FXML
    public ColorPicker EditorSymbol_LineColor;
    @FXML
    public TextField EditorSymbol_TextContent;
    @FXML
    public TextField EditorSymbol_TextSize;
    @FXML
    public ColorPicker EditorSymbol_TextColor;
    @FXML
    public TextField EditorSymbol_PinLabel;
    @FXML
    public TextField EditorSymbol_PinRadius;
    @FXML
    public ColorPicker EditorSymbol_PinColor;



    //// MAIN FUNCTIONS ////

    /**
     * Factory to create a single EditorSymbol and its UI attached to a particular symbol library.
     *
     * @throws IOException if there are problems loading the scene from FXML resources.
     */
    static public Editor EditorSymbol_Create() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorSymbol.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorSymbol editor = fxmlLoader.getController();
        editor.Editor_Init(0, "EditorSymbol");
        editor.Editor_Dissect(0, scene);
        editor.Editor_RenderSystem.RenderSystem_CanvasRenderGrid();
        editor.Editor_ListenersInit();

        return editor;
    }

    /**
     * Provides initialization of the Controller
     */
    @FXML
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + this.EditorSymbol_InnerButton);
    }

    //// CALLBACK FUNCTIONS ////

    @FXML
    public void EditorSymbol_Save()
    {
        LinkedList<Node> nodes = new LinkedList<Node>();

        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
            nodes.add(this.Editor_RenderSystem.RenderSystem_Nodes.get(i).RenderNode_Node);

        File.File_NodesSave(nodes, true);
    }

    @FXML
    public void EditorSymbol_Load()
    {
        LinkedList<Node> nodes = File.File_NodesLoad(true);

        if (nodes == null)
            return;

        for (int i = 0; i < nodes.size(); i++)
        {
            Node node = nodes.get(i);
            PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetDrawPos(new PairMutable(node.getTranslateX(), node.getTranslateY()));
            node.setTranslateX(realPos.GetLeftDouble());
            node.setTranslateY(realPos.GetRightDouble());

            boolean edgeSnaps = true;
            LinkedList<PairMutable> snapManualPos = null;

            if (node.getClass() == Group.class)
            {
                edgeSnaps = false;
                snapManualPos = new LinkedList<PairMutable>();
                Group group = (Group)node;

                for (int j = 0; j < group.getChildren().size(); j++)
                {
                    Node currChild = group.getChildren().get(j);

                    if (currChild.getClass() == Circle.class)
                        snapManualPos.add(new PairMutable(currChild.getTranslateX(), currChild.getTranslateY()));
                }
            }

            RenderNode renderNode = new RenderNode("LoadedNode", node, edgeSnaps, snapManualPos, false, this.Editor_RenderSystem);
            this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
        }
    }

    public void Editor_OnDragOverSpecific(DragEvent event)
    {
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

        /*// Handling symbol dragging preview
        Dragboard db = event.getDragboard();

        // If we're dragging something from outside the viewport...
        if (db.hasString() && (event.getGestureSource() != this.canvas))
        {
            String symbolName = db.getString();
            Integer symbolIdx = EditorSchematic_SearchSymbolsByName(this.symbols, symbolName);

            // If we are dragging a symbol...
            if (symbolIdx != -1)
            {
                // Accept the transfer mode
                event.acceptTransferModes(TransferMode.MOVE);

                // Create preview of dragged symbol
                EditorSchematic_Symbol symbol = this.symbols.get(symbolIdx);
                Editor_CanvasShape shape = new Editor_CanvasShape(symbol.shape);
                PairMutable previewPos = new PairMutable(event.getX(), event.getY());

                if (!symbol.id.equals(this.EditorSchematic_SymbolPreviewID))
                {
                    shape.posDraw = previewPos;
                    shape.globalOpacity = this.EditorSchematic_SymbolDroppingPreviewOpacity;
                    shape.permanent = true;
                    //this.EditorSchematic_ViewportAddShape(-1, shape);
                    this.EditorSchematic_RenderSystem.AddShape(-1, shape, null);
                    this.EditorSchematic_SymbolPreviewID = symbol.id;
                    this.EditorSchematic_SymbolShapePreviewID = shape.id;
                }
                else
                {
                    Integer previewShapeIdx = this.EditorSchematic_SearchShapesById(this.EditorSchematic_RenderSystem.shapes, this.EditorSchematic_SymbolShapePreviewID);
                    Editor_CanvasShape previewShape = this.EditorSchematic_RenderSystem.shapes.get(previewShapeIdx);
                    previewShape.posDraw = previewPos;
                    previewShape.globalOpacity = this.EditorSchematic_SymbolDroppingPreviewOpacity;

                    this.EditorSchematic_RenderSystem.shapes.set(previewShapeIdx, previewShape);
                }
            }
        }

        this.EditorSchematic_CheckSymbolsDroppedMouseHighlights(new PairMutable(event.getX(), event.getY()));*/
    }

    public void Editor_OnDragDroppedSpecific(DragEvent event)
    {
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

        /*// Handling dropping of symbols
        Dragboard db = event.getDragboard();
        boolean success = false;

        // If we are dragging something from outside the viewport...
        if (db.hasString())
        {
            String symbolName = db.getString();
            Integer symbolIdx = EditorSchematic_SearchSymbolsByName(this.symbols, symbolName);

            // If we are dragging a symbol...
            if (symbolIdx != -1)
            {
                System.out.println("Dropped! (" + symbolName + ", " + event.getX() + ", " + event.getY() + ")");

                PairMutable symbolPosReal = new PairMutable((event.getX() - (this.EditorSchematic_Viewport.getWidth() / 2)) / this.EditorSchematic_RenderSystem.zoom,
                        (event.getY() - (this.EditorSchematic_Viewport.getHeight() / 2)) / this.EditorSchematic_RenderSystem.zoom);
                PairMutable viewportPos = new PairMutable(this.EditorSchematic_ViewportCenter);

                // If we are within the limits of the theater, drop the symbol. Otherwise, do nothing.
                if ((symbolPosReal.GetLeftDouble() > -this.EditorSchematic_TheaterSize.GetLeftDouble() / 2 + viewportPos.GetLeftDouble()) &&
                        (symbolPosReal.GetLeftDouble() < this.EditorSchematic_TheaterSize.GetLeftDouble() / 2 + viewportPos.GetLeftDouble()) &&
                        (symbolPosReal.GetRightDouble() > -this.EditorSchematic_TheaterSize.GetRightDouble() / 2 + viewportPos.GetRightDouble()) &&
                        (symbolPosReal.GetRightDouble() < this.EditorSchematic_TheaterSize.GetRightDouble() / 2 + viewportPos.GetRightDouble()))
                {
                    EditorSchematic_Symbol symbol = new EditorSchematic_Symbol(symbols.get(symbolIdx));
                    symbol.posReal = new PairMutable(symbolPosReal);
                    this.EditorSchematic_ViewportSymbolsDropped.add(symbol);
                }

                // Deleting the preview symbol
                for (int i = 0; i < this.EditorSchematic_RenderSystem.shapes.size(); i++)
                {
                    if (this.EditorSchematic_RenderSystem.shapes.get(i).id.equals(this.EditorSchematic_SymbolShapePreviewID))
                    {
                        //this.EditorSchematic_RenderSystem.shapes.remove(this.EditorSchematic_RenderSystem.shapes.get(i));
                        //this.EditorSchematic_RenderSystem.shapesPosReal.remove(this.EditorSchematic_RenderSystem.shapesPosReal.get(i));
                        this.EditorSchematic_RenderSystem.RemoveShape(i);
                        this.EditorSchematic_SymbolPreviewID = -1;
                        this.EditorSchematic_SymbolShapePreviewID = -1;
                    }
                }
            }

            success = true;
        }

        event.setDropCompleted(success);*/
    }

    public void Editor_OnMouseMovedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);
    }

    public void Editor_OnMousePressedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

        if (this.Editor_PressedLMB)
        {
            /*// Handling the wire drawing
            this.EditorSchematic_WireDrawingType = this.EditorSchematic_GetSelectedWire();

            if (this.EditorSchematic_WireDrawingType != -1)
                this.EditorSchematic_WireFirstPos = new PairMutable(event.getX(), event.getY());

            // Handling the symbol selection (only if we're not drawing any wires)
            this.EditorSchematic_SymbolPressedIDs.clear();

            if (this.EditorSchematic_WireDrawingType == -1)
            {
                for (int i = 0; i < this.EditorSchematic_ViewportSymbolsDropped.size(); i++)
                {
                    EditorSchematic_Symbol symbol = this.EditorSchematic_ViewportSymbolsDropped.get(i);

                    if (symbol.highlightedMouse)
                        this.EditorSchematic_SymbolPressedIDs.add(symbol.id);
                }
            }

            // Updating left mouse button flag
            this.EditorSchematic_PressedMouseLeft = true;*/
        }
        else if (this.Editor_PressedRMB)
        {}
    }

    public void Editor_OnMouseReleasedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        dropPos = this.Editor_MagneticSnapCheck(dropPos);
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

        if (this.Editor_PressedLMB)
        {
            // Handling shape dropping (only if we're not hovering over, selecting, moving any shapes or box selecting)
            if ((this.Editor_ShapesSelected == 0) &&
                !this.Editor_ShapesMoving &&
                (this.Editor_SelectionBox == null) &&
                !this.Editor_ShapesWereSelected &&
                !this.Editor_WasSelectionBox)
            {
                RadioButton selectedShapeButton = (RadioButton) EditorSymbol_ToggleGroup.getSelectedToggle();

                // Only dropping the shape within the theater limits...
                if (selectedShapeButton != null)
                {
                    /*if ((realPos.GetLeftDouble() > -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2) &&
                        (realPos.GetLeftDouble() < EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2) &&
                        (realPos.GetRightDouble() > -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2) &&
                        (realPos.GetRightDouble() < EDAmameController.Editor_TheaterSize.GetRightDouble() / 2))
                    {*/
                        if (!selectedShapeButton.getText().equals("Line"))
                            this.EditorSymbol_LinePreview = null;

                        boolean lineStarted = false;

                        if (this.Editor_ShapesHighlighted == 0)
                        {
                            if (selectedShapeButton.getText().equals("Circle"))
                            {
                                EDAmameController.Controller_SetStatusBar("EDAmame Status Area");

                                String stringRadius = this.EditorSymbol_CircleRadius.getText();
                                String stringStrokeSize = this.EditorSymbol_CircleBorderSize.getText();
                                Color fillColor = this.EditorSymbol_CircleColor.getValue();
                                Paint strokeColor = this.EditorSymbol_CircleBorderColor.getValue();

                                if (EditorSymbol_checkStringCircleBounds(stringRadius) && EditorSymbol_checkStringBorderSize(stringStrokeSize) && EditorSymbol_checkShapeTransparency(stringStrokeSize, strokeColor, fillColor))
                                {
                                    Circle circle = new Circle(Double.parseDouble(stringRadius), fillColor);

                                    circle.setTranslateX(dropPos.GetLeftDouble());
                                    circle.setTranslateY(dropPos.GetRightDouble());

                                    circle.setStroke(strokeColor);
                                    circle.setStrokeWidth(Double.parseDouble(stringStrokeSize));

                                    RenderNode renderNode = new RenderNode("Circle", circle, true, null, false, this.Editor_RenderSystem);
                                    this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Rectangle"))
                            {
                                EDAmameController.Controller_SetStatusBar("EDAmame Status Area");

                                String stringWidth = this.EditorSymbol_RectangleWidth.getText();
                                String stringHeight = this.EditorSymbol_RectangleHeight.getText();
                                String stringStrokeSize = this.EditorSymbol_RectangleBorderSize.getText();
                                Color fillColor = this.EditorSymbol_RectangleColor.getValue();
                                Paint strokeColor = this.EditorSymbol_RectangleBorderColor.getValue();

                                if (EditorSymbol_checkStringRectBounds(stringWidth, stringHeight) && EditorSymbol_checkStringBorderSize(stringStrokeSize) && EditorSymbol_checkShapeTransparency(stringStrokeSize, strokeColor, fillColor))
                                {
                                    Rectangle rectangle = new Rectangle(Double.parseDouble(stringWidth), Double.parseDouble(stringHeight), fillColor);

                                    rectangle.setTranslateX(dropPos.GetLeftDouble());
                                    rectangle.setTranslateY(dropPos.GetRightDouble());

                                    rectangle.setStroke(strokeColor);
                                    rectangle.setStrokeWidth(Double.parseDouble(stringStrokeSize));

                                    RenderNode renderNode = new RenderNode("Rectangle", rectangle, true, null, false, this.Editor_RenderSystem);
                                    this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Triangle"))
                            {
                                EDAmameController.Controller_SetStatusBar("EDAmame Status Area");

                                String stringMiddleHeight = this.EditorSymbol_TriangleHeight.getText();
                                String stringStrokeSize = this.EditorSymbol_TriangleBorderSize.getText();
                                Color fillColor = this.EditorSymbol_TriangleColor.getValue();
                                Paint strokeColor = this.EditorSymbol_TriangleBorderColor.getValue();

                                if (EditorSymbol_checkStringTriBounds(stringMiddleHeight) && EditorSymbol_checkStringBorderSize(stringStrokeSize) && EditorSymbol_checkShapeTransparency(stringStrokeSize, strokeColor, fillColor))
                                {
                                    Polygon triangle = new Polygon();
                                    double middleLength = Double.parseDouble(stringMiddleHeight);
                                    triangle.getPoints().setAll(-middleLength / 2, middleLength / 2,
                                            middleLength / 2, middleLength / 2,
                                            0.0, -middleLength / 2);
                                    triangle.setFill(fillColor);

                                    triangle.setTranslateX(dropPos.GetLeftDouble());
                                    triangle.setTranslateY(dropPos.GetRightDouble());

                                    triangle.setStroke(strokeColor);
                                    triangle.setStrokeWidth(Double.parseDouble(stringStrokeSize));

                                    RenderNode renderNode = new RenderNode("Triangle", triangle, true, null, false, this.Editor_RenderSystem);
                                    this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Line"))
                            {
                                EDAmameController.Controller_SetStatusBar("EDAmame Status Area");

                                // If we're starting the line drawing...
                                if (this.EditorSymbol_LinePreview == null)
                                {
                                    String stringWidth = this.EditorSymbol_LineWidth.getText();
                                    Color color = this.EditorSymbol_LineColor.getValue();
                                    boolean canDrop = true;

                                    if (stringWidth.isEmpty() || !EDAmameController.Controller_IsStringNum(stringWidth))
                                    {
                                        EDAmameController.Controller_SetStatusBar("Unable to drop line because the entered width field is non-numeric!");
                                        canDrop = false;
                                    }
                                    double width = Double.parseDouble(stringWidth);
                                    if (!((width >= EDAmameController.Editor_LineWidthMin) && (width <= EDAmameController.Editor_LineWidthMax)))
                                    {
                                        EDAmameController.Controller_SetStatusBar("Unable to drop line because the entered width is outside the limits! (Width limits: " + EDAmameController.Editor_LineWidthMin + ", " + EDAmameController.Editor_LineWidthMax + ")");
                                        canDrop = false;
                                    }

                                    if (!((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000)))
                                    {
                                        EDAmameController.Controller_SetStatusBar("Unable to drop line because the entered color field is transparent!");
                                        canDrop = false;
                                    }

                                    if (canDrop)
                                    {
                                        this.EditorSymbol_LinePreview = new Line();
                                        //this.EditorSymbol_LinePreview.setId("linePreview");

                                        this.EditorSymbol_LinePreview.setStartX(dropPos.GetLeftDouble());
                                        this.EditorSymbol_LinePreview.setStartY(dropPos.GetRightDouble());
                                        this.EditorSymbol_LinePreview.setEndX(dropPos.GetLeftDouble());
                                        this.EditorSymbol_LinePreview.setEndY(dropPos.GetRightDouble());

                                        this.EditorSymbol_LinePreview.setStrokeWidth(width);
                                        this.EditorSymbol_LinePreview.setStroke(color);

                                        RenderNode renderNode = new RenderNode("linePreview", this.EditorSymbol_LinePreview, true, null, true, this.Editor_RenderSystem);
                                        this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);

                                        lineStarted = true;
                                    }
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Text"))
                            {
                                EDAmameController.Controller_SetStatusBar("EDAmame Status Area");

                                String stringTextContent = EditorSymbol_TextContent.getText();
                                boolean canDrop = true;

                                if (stringTextContent.isEmpty())
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop text because the entered text field is empty!");
                                    canDrop = false;
                                }
                                String stringFontSize = this.EditorSymbol_TextSize.getText();
                                if (stringFontSize.isEmpty() || !EDAmameController.Controller_IsStringNum(stringFontSize))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop text because the entered font size field is non-numeric!");
                                    canDrop = false;
                                }
                                double fontSize = Double.parseDouble(stringFontSize);
                                Color color = this.EditorSymbol_TextColor.getValue();

                                if (!((fontSize >= EDAmameController.Editor_TextFontSizeMin) && (fontSize <= EDAmameController.Editor_TextFontSizeMax)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop text because the entered font size field is outside the limits! (Font size limits: " + EDAmameController.Editor_TextFontSizeMin + ", " + EDAmameController.Editor_TextFontSizeMax + ")");
                                    canDrop = false;
                                }

                                if (!((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop text because the entered font color field is transparent!");
                                    canDrop = false;
                                }

                                if (canDrop) {
                                    Text text = new Text(stringTextContent);
                                    text.setFont(new Font("Arial", fontSize));
                                    text.setFill(color);

                                    text.setTranslateX(dropPos.GetLeftDouble());
                                    text.setTranslateY(dropPos.GetRightDouble());
                                    /*Label text = new Label(stringTextContent);
                                    text.setFont(new Font("Arial", fontSize));
                                    text.setTextFill(color);

                                    text.setTranslateX(dropPos.GetLeftDouble());
                                    text.setTranslateY(dropPos.GetRightDouble());*/

                                    RenderNode renderNode = new RenderNode("Text", text, true, null, false, this.Editor_RenderSystem);
                                    this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Pin"))
                            {
                                EDAmameController.Controller_SetStatusBar("EDAmame Status Area");

                                String stringPinLabel = this.EditorSymbol_PinLabel.getText();
                                boolean canDrop = true;

                                if (stringPinLabel.isEmpty())
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop pin because the entered label field is empty!");
                                    canDrop = false;
                                }
                                String stringPinRadius = this.EditorSymbol_PinRadius.getText();

                                if (stringPinRadius.isEmpty() || !EDAmameController.Controller_IsStringNum(stringPinRadius))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop pin because the entered radius field is non-numeric!");
                                    canDrop = false;
                                }

                                double pinRadius = Double.parseDouble(stringPinRadius);
                                Color pinColor = this.EditorSymbol_PinColor.getValue();

                                if (!((pinRadius >= EDAmameController.Editor_PinRadiusMin) && (pinRadius <= EDAmameController.Editor_PinRadiusMax)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop pin because the entered radius field is outside the limits! (Font size limits: " + EDAmameController.Editor_PinRadiusMin + ", " + EDAmameController.Editor_PinRadiusMax + ")");
                                    canDrop = false;
                                }
                                if (!((pinColor != null) && (pinColor != Color.TRANSPARENT) && (pinColor.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop pin because the entered font color field is transparent!");
                                    canDrop = false;
                                }

                                if (canDrop)
                                {
                                    Group pin = new Group();
                                    pin.setId("PIN_" + stringPinLabel);

                                    Circle pinCircle = new Circle(pinRadius, pinColor);

                                    Text pinLabel = new Text(stringPinLabel);
                                    pinLabel.setFont(new Font("Arial", EDAmameController.Editor_PinLabelFontSize));
                                    pinLabel.setFill(pinColor);
                                    /*Label pinLabel = new Label(stringPinLabel);
                                    pinLabel.setFont(new Font("Arial", EDAmameController.Editor_PinLabelFontSize));
                                    pinLabel.setTextFill(pinColor);*/

                                    pin.getChildren().add(pinCircle);
                                    pin.getChildren().add(pinLabel);

                                    pin.setTranslateX(dropPos.GetLeftDouble());
                                    pin.setTranslateY(dropPos.GetRightDouble());

                                    pinLabel.setTranslateX(EDAmameController.Editor_PinLabelOffset.GetLeftDouble());
                                    pinLabel.setTranslateY(EDAmameController.Editor_PinLabelOffset.GetRightDouble());

                                    LinkedList<PairMutable> snap = new LinkedList<PairMutable>();
                                    snap.add(new PairMutable(0.0, 0.0));

                                    RenderNode renderNode = new RenderNode("Pin", pin, false, snap, false, this.Editor_RenderSystem);
                                    this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
                                }
                            }
                            else
                            {
                                throw new java.lang.Error("ERROR: Attempt to drop an unrecognized shape in a Symbol Editor!");
                            }
                        }

                        if (selectedShapeButton.getText().equals("Line"))
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

                                RenderNode renderNode = new RenderNode("Line", line, true, null, false, this.Editor_RenderSystem);
                                this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
                            }
                        }
                    /*}
                    else
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to drop element because the dropping position is outside the theater limits!");
                    }*/
                }
            }

            this.Editor_ShapesWereSelected = false;
        }
        else if (this.Editor_PressedRMB)
        {}
    }

    public void Editor_OnMouseDraggedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

        if (this.Editor_PressedLMB)
        {}
        else if (this.Editor_PressedRMB)
        {}

        // Handling symbol highlighting
        //this.EditorSchematic_CheckSymbolsDroppedMouseHighlights(new PairMutable(event.getX(), event.getY()));
    }

    public void Editor_OnScrollSpecific(ScrollEvent event)
    {}

    public void Editor_OnKeyPressedSpecific(KeyEvent event)
    {
        //System.out.println("Symbol key pressed!");

        // Handling the shape properties window (only if there's not another properties window already open)...
        if (EDAmameController.Controller_IsKeyPressed(KeyCode.E) && (EDAmameController.Controller_EditorPropertiesWindow == null))
        {
            // Attempting to create the properties window...
            EditorProps propsWindow = EditorProps.EditorProps_Create();

            if ((propsWindow != null))
            {
                propsWindow.EditorProps_Stage.setOnHidden(e -> {
                    EDAmameController.Controller_EditorPropertiesWindow = null;
                });
                propsWindow.EditorProps_Editor = this;
                propsWindow.EditorProps_Stage.show();

                EDAmameController.Controller_EditorPropertiesWindow = propsWindow;
            }
        }
    }

    public void Editor_OnKeyReleasedSpecific(KeyEvent event)
    {}

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void Editor_PropsLoadSpecific()
    {
        if (this.Editor_ShapesSelected == 0)
            return;

        boolean needHeader = false;

        // Reading all shape type properties...
        LinkedList<Double> circlesRadii = new LinkedList<Double>();
        LinkedList<Double> rectsWidths = new LinkedList<Double>();
        LinkedList<Double> rectsHeights = new LinkedList<Double>();
        LinkedList<Double> trisLens = new LinkedList<Double>();
        LinkedList<Double> lineStartPosX = new LinkedList<Double>();
        LinkedList<Double> lineStartPosY = new LinkedList<Double>();
        LinkedList<Double> lineEndPosX = new LinkedList<Double>();
        LinkedList<Double> lineEndPosY = new LinkedList<Double>();
        LinkedList<Double> lineWidths = new LinkedList<Double>();
        LinkedList<Double> strokeWidths = new LinkedList<Double>();
        LinkedList<Paint> strokes = new LinkedList<Paint>();
        LinkedList<String> textContents = new LinkedList<String>();
        LinkedList<Double> textFontSizes = new LinkedList<Double>();
        LinkedList<String> pinLabels = new LinkedList<String>();

        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            if (!renderNode.RenderNode_Selected)
                continue;

            if (renderNode.RenderNode_Node.getClass() == Circle.class)
            {
                circlesRadii.add(((Circle)renderNode.RenderNode_Node).getRadius());
                strokeWidths.add(((Circle)renderNode.RenderNode_Node).getStrokeWidth());
                strokes.add(((Circle)renderNode.RenderNode_Node).getStroke());

                needHeader = true;
            }
            else if (renderNode.RenderNode_Node.getClass() == Rectangle.class)
            {
                rectsWidths.add(((Rectangle)renderNode.RenderNode_Node).getWidth());
                rectsHeights.add(((Rectangle)renderNode.RenderNode_Node).getHeight());
                strokeWidths.add(((Rectangle)renderNode.RenderNode_Node).getStrokeWidth());
                strokes.add(((Rectangle)renderNode.RenderNode_Node).getStroke());

                needHeader = true;
            }
            else if (renderNode.RenderNode_Node.getClass() == Polygon.class)
            {
                trisLens.add(((Polygon)renderNode.RenderNode_Node).getPoints().get(2) - ((Polygon) renderNode.RenderNode_Node).getPoints().get(0));
                strokeWidths.add(((Polygon)renderNode.RenderNode_Node).getStrokeWidth());
                strokes.add(((Polygon)renderNode.RenderNode_Node).getStroke());

                needHeader = true;
            }
            else if (renderNode.RenderNode_Node.getClass() == Line.class)
            {
                Line line = (Line)renderNode.RenderNode_Node;
                //PairMutable linePoints = Editor.Editor_LineEndPointsCalculate(line);
                //PairMutable startPoint = linePoints.GetLeftPair();
                //PairMutable endPoint = linePoints.GetRightPair();

                //lineStartPosX.add(startPoint.GetLeftDouble());
                //lineStartPosY.add(startPoint.GetRightDouble());
                //lineEndPosX.add(endPoint.GetLeftDouble());
                //lineEndPosY.add(endPoint.GetRightDouble());

                lineStartPosX.add(line.getStartX());
                lineStartPosY.add(line.getStartY());
                lineEndPosX.add(line.getEndX());
                lineEndPosY.add(line.getEndY());
                lineWidths.add(line.getStrokeWidth());

                needHeader = true;
            }
            else if (renderNode.RenderNode_Node.getClass() == Text.class)
            {
                textContents.add(((Text)renderNode.RenderNode_Node).getText());
                textFontSizes.add(((Text)renderNode.RenderNode_Node).getFont().getSize());

                needHeader = true;
            }
            else if (renderNode.RenderNode_Node.getClass() == Group.class)
            {
                Group group = (Group)renderNode.RenderNode_Node;

                if (group.getChildren().size() != 2)
                    throw new java.lang.Error("ERROR: Attempting to load pin into symbol properties window without 2 children!");
                if (group.getChildren().get(1).getClass() != Text.class)
                    throw new java.lang.Error("ERROR: Attempting to load pin into symbol properties window without a text node!");

                pinLabels.add(((Text)group.getChildren().get(1)).getText());

                needHeader = true;
            }
        }

        // Creating header...
        if (needHeader)
        {
            Text shapeHeader = new Text("Symbol Editor Properties:");
            shapeHeader.setStyle("-fx-font-weight: bold;");
            shapeHeader.setStyle("-fx-font-size: 16px;");
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(shapeHeader);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
        }

        // Creating circle radius box...
        if (!circlesRadii.isEmpty())
        {
            HBox circleHBox = new HBox(10);
            circleHBox.setId("circleBox");
            circleHBox.getChildren().add(new Label("Circle Radii: "));
            TextField radiusText = new TextField();
            radiusText.setId("circleRadii");
            circleHBox.getChildren().add(radiusText);

            if (EDAmameController.Controller_IsListAllEqual(circlesRadii))
                radiusText.setText(Double.toString(circlesRadii.get(0)));
            else
                radiusText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(circleHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
        }

        // Creating rectangle width & height box...
        if (!rectsWidths.isEmpty() && !rectsHeights.isEmpty())
        {
            HBox rectHBox = new HBox(10);
            rectHBox.setId("rectBox");
            rectHBox.getChildren().add(new Label("Rectangle Widths: "));
            TextField widthText = new TextField();
            widthText.setMinWidth(100);
            widthText.setPrefWidth(100);
            widthText.setMaxWidth(100);
            widthText.setId("rectWidths");
            rectHBox.getChildren().add(widthText);
            rectHBox.getChildren().add(new Label("Heights: "));
            TextField heightText = new TextField();
            heightText.setId("rectHeights");
            heightText.setMinWidth(100);
            heightText.setPrefWidth(100);
            heightText.setMaxWidth(100);
            rectHBox.getChildren().add(heightText);

            if (EDAmameController.Controller_IsListAllEqual(rectsWidths))
                widthText.setText(Double.toString(rectsWidths.get(0)));
            else
                widthText.setText("<mixed>");

            if (EDAmameController.Controller_IsListAllEqual(rectsHeights))
                heightText.setText(Double.toString(rectsHeights.get(0)));
            else
                heightText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(rectHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
        }

        // Creating triangle lengths box...
        if (!trisLens.isEmpty())
        {
            HBox triLenHBox = new HBox(10);
            triLenHBox.setId("triBox");
            triLenHBox.getChildren().add(new Label("Triangle Lengths: "));
            TextField triLenText = new TextField();
            triLenText.setId("triLens");
            triLenHBox.getChildren().add(triLenText);

            if (EDAmameController.Controller_IsListAllEqual(trisLens))
                triLenText.setText(Double.toString(trisLens.get(0)));
            else
                triLenText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(triLenHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
        }

        // Creating line box...
        if (!lineStartPosX.isEmpty() && !lineStartPosY.isEmpty() && !lineEndPosX.isEmpty() && !lineEndPosY.isEmpty() && !lineWidths.isEmpty())
        {
            // Start point
            HBox lineStartPointsHBox = new HBox(10);
            lineStartPointsHBox.setId("lineStartPointsBox");
            lineStartPointsHBox.getChildren().add(new Label("Line Start Points X: "));
            TextField lineStartPointsXText = new TextField();
            lineStartPointsXText.setMinWidth(100);
            lineStartPointsXText.setPrefWidth(100);
            lineStartPointsXText.setMaxWidth(100);
            lineStartPointsXText.setId("lineStartPointsX");
            lineStartPointsHBox.getChildren().add(lineStartPointsXText);
            lineStartPointsHBox.getChildren().add(new Label("Y: "));
            TextField lineStartPointsYText = new TextField();
            lineStartPointsYText.setId("lineStartPointsY");
            lineStartPointsYText.setMinWidth(100);
            lineStartPointsYText.setPrefWidth(100);
            lineStartPointsYText.setMaxWidth(100);
            lineStartPointsHBox.getChildren().add(lineStartPointsYText);

            if (EDAmameController.Controller_IsListAllEqual(lineStartPosX))
                lineStartPointsXText.setText(Double.toString(lineStartPosX.get(0)));
            else
                lineStartPointsXText.setText("<mixed>");

            if (EDAmameController.Controller_IsListAllEqual(lineStartPosY))
                lineStartPointsYText.setText(Double.toString(lineStartPosY.get(0)));
            else
                lineStartPointsYText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(lineStartPointsHBox);

            // End point
            HBox lineEndPointsHBox = new HBox(10);
            lineEndPointsHBox.setId("lineEndPointsBox");
            lineEndPointsHBox.getChildren().add(new Label("Line End Points X: "));
            TextField lineEndPointsXText = new TextField();
            lineEndPointsXText.setMinWidth(100);
            lineEndPointsXText.setPrefWidth(100);
            lineEndPointsXText.setMaxWidth(100);
            lineEndPointsXText.setId("lineEndPointsX");
            lineEndPointsHBox.getChildren().add(lineEndPointsXText);
            lineEndPointsHBox.getChildren().add(new Label("Y: "));
            TextField lineEndPointsYText = new TextField();
            lineEndPointsYText.setId("lineEndPointsY");
            lineEndPointsYText.setMinWidth(100);
            lineEndPointsYText.setPrefWidth(100);
            lineEndPointsYText.setMaxWidth(100);
            lineEndPointsHBox.getChildren().add(lineEndPointsYText);

            if (EDAmameController.Controller_IsListAllEqual(lineEndPosX))
                lineEndPointsXText.setText(Double.toString(lineEndPosX.get(0)));
            else
                lineEndPointsXText.setText("<mixed>");

            if (EDAmameController.Controller_IsListAllEqual(lineEndPosY))
                lineEndPointsYText.setText(Double.toString(lineEndPosY.get(0)));
            else
                lineEndPointsYText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(lineEndPointsHBox);

            // Width
            HBox lineWidthsHBox = new HBox(10);
            lineWidthsHBox.setId("lineWidthsBox");
            lineWidthsHBox.getChildren().add(new Label("Line Widths: "));
            TextField lineWidthsText = new TextField();
            lineWidthsText.setId("lineWidths");
            lineWidthsHBox.getChildren().add(lineWidthsText);

            if (EDAmameController.Controller_IsListAllEqual(lineWidths))
                lineWidthsText.setText(Double.toString(lineWidths.get(0)));
            else
                lineWidthsText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(lineWidthsHBox);

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
        }

        // Creating border box...
        if (!strokes.isEmpty() && !strokeWidths.isEmpty())
        {
            HBox strokesWidthHBox = new HBox(10);
            strokesWidthHBox.setId("strokesWidthBox");
            strokesWidthHBox.getChildren().add(new Label("Shape Borders: "));
            TextField strokesWidthTextBox = new TextField();
            strokesWidthTextBox.setId("strokeWidth");
            strokesWidthHBox.getChildren().add(strokesWidthTextBox);

            if (EDAmameController.Controller_IsListAllEqual(strokeWidths))
                strokesWidthTextBox.setText(Double.toString(strokeWidths.get(0)));
            else
                strokesWidthTextBox.setText("<mixed>");

            HBox strokeColorHBox = new HBox(10);
            strokeColorHBox.setId("strokeColorBox");
            strokeColorHBox.getChildren().add(new Label("Shape Border Colors: "));
            ColorPicker strokeColorPicker = new ColorPicker();
            strokeColorPicker.setId("strokeColor");
            strokeColorHBox.getChildren().add(strokeColorPicker);

            if (EDAmameController.Controller_IsListAllEqual(strokes))
                strokeColorPicker.setValue((Color)strokes.get(0));
            else
                strokeColorPicker.setValue(null);

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(strokesWidthHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(strokeColorHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
        }

        // Creating text box...
        if (!textFontSizes.isEmpty())
        {
            HBox textContentHBox = new HBox(10);
            textContentHBox.setId("textContentBox");
            textContentHBox.getChildren().add(new Label("Text Contents: "));
            TextField textContentText = new TextField();
            textContentText.setId("textContent");
            textContentHBox.getChildren().add(textContentText);

            HBox textFontSizeHBox = new HBox(10);
            textFontSizeHBox.setId("fontSizeBox");
            textFontSizeHBox.getChildren().add(new Label("Text Font Sizes: "));
            TextField textFontSizeText = new TextField();
            textFontSizeText.setId("fontSize");
            textFontSizeHBox.getChildren().add(textFontSizeText);

            if (EDAmameController.Controller_IsListAllEqual(textContents))
                textContentText.setText(textContents.get(0));
            else
                textContentText.setText("<mixed>");

            if (EDAmameController.Controller_IsListAllEqual(textFontSizes))
                textFontSizeText.setText(Double.toString(textFontSizes.get(0)));
            else
                textFontSizeText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(textContentHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(textFontSizeHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
        }

        // Creating pin label box...
        if (!pinLabels.isEmpty())
        {
            HBox pinLabelHBox = new HBox(10);
            pinLabelHBox.setId("pinLabelBox");
            pinLabelHBox.getChildren().add(new Label("Pin Labels: "));
            TextField pinLabelText = new TextField();
            pinLabelText.setId("pinLabels");
            pinLabelHBox.getChildren().add(pinLabelText);

            if (EDAmameController.Controller_IsListAllEqual(pinLabels))
                pinLabelText.setText(pinLabels.get(0));
            else
                pinLabelText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(pinLabelHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
        }
    }

    public void Editor_PropsApplySpecific()
    {
        if (this.Editor_ShapesSelected == 0)
            return;

        VBox propsBox = EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox;

        // Iterating over all the shapes & attempting to apply shape properties if selected...
        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            if (!renderNode.RenderNode_Selected)
                continue;

            // Applying circle radius...
            if (renderNode.RenderNode_Node.getClass() == Circle.class)
            {
                Integer circleBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "circleBox");

                if (circleBoxIdx != -1)
                {
                    HBox circleBox = (HBox) propsBox.getChildren().get(circleBoxIdx);
                    TextField radiiText = (TextField) EDAmameController.Controller_GetNodeById(circleBox.getChildren(), "circleRadii");

                    if (radiiText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"circleRadii\" node in Symbol Editor properties window \"circleBox\" entry!");

                    String radiusStr = radiiText.getText();

                    if (EDAmameController.Controller_IsStringNum(radiusStr))
                    {
                        Double newRadius = Double.parseDouble(radiusStr);

                        if ((newRadius >= EDAmameController.Editor_CircleRadiusMin) && (newRadius <= EDAmameController.Editor_CircleRadiusMax))
                        {
                            ((Circle) renderNode.RenderNode_Node).setRadius(newRadius);
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply circle radii because the entered field is outside the limits! (Radius limits: " + EDAmameController.Editor_CircleRadiusMin + ", " + EDAmameController.Editor_CircleRadiusMax + ")");
                        }
                    }
                    else if (!radiusStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply circle radii because the entered field is non-numeric!");
                    }
                }
            }
            // Applying rectangle width & height...
            else if (renderNode.RenderNode_Node.getClass() == Rectangle.class)
            {
                Integer rectBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "rectBox");

                if (rectBoxIdx != -1)
                {
                    HBox rectBox = (HBox) propsBox.getChildren().get(rectBoxIdx);
                    TextField widthText = (TextField) EDAmameController.Controller_GetNodeById(rectBox.getChildren(), "rectWidths");
                    TextField heightText = (TextField) EDAmameController.Controller_GetNodeById(rectBox.getChildren(), "rectHeights");

                    if (widthText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"rectWidths\" node in Symbol Editor properties window \"rectBox\" entry!");
                    if (heightText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"rectHeights\" node in Symbol Editor properties window \"rectBox\" entry!");

                    String widthStr = widthText.getText();
                    String heightStr = heightText.getText();

                    if (EDAmameController.Controller_IsStringNum(widthStr))
                    {
                        Double newWidth = Double.parseDouble(widthStr);

                        if ((newWidth >= EDAmameController.Editor_RectWidthMin) && (newWidth <= EDAmameController.Editor_RectWidthMax))
                        {
                            ((Rectangle) renderNode.RenderNode_Node).setWidth(newWidth);
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply rectangle widths because the entered field is outside the limits! (Width limits: " + EDAmameController.Editor_RectWidthMin + ", " + EDAmameController.Editor_RectWidthMax + ")");
                        }
                    }
                    else if (!widthStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply rectangle widths because the entered field is non-numeric!");
                    }

                    if (EDAmameController.Controller_IsStringNum(heightStr))
                    {
                        Double newHeight = Double.parseDouble(heightStr);

                        if ((newHeight >= EDAmameController.Editor_RectHeightMin) && (newHeight <= EDAmameController.Editor_RectHeightMax))
                        {
                            ((Rectangle) renderNode.RenderNode_Node).setHeight(newHeight);
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply rectangle heights because the entered field is outside the limits! (Height limits: " + EDAmameController.Editor_RectHeightMin + ", " + EDAmameController.Editor_RectHeightMax + ")");
                        }
                    }
                    else if (!heightStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply rectangle heights because the entered field is non-numeric!");
                    }
                }
            }
            // Applying triangle length...
            else if (renderNode.RenderNode_Node.getClass() == Polygon.class)
            {
                Integer triBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "triBox");

                if (triBoxIdx != -1)
                {
                    HBox triBox = (HBox) propsBox.getChildren().get(triBoxIdx);
                    TextField lensText = (TextField) EDAmameController.Controller_GetNodeById(triBox.getChildren(), "triLens");

                    if (lensText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"triLens\" node in Symbol Editor properties window \"triBox\" entry!");

                    String lenStr = lensText.getText();

                    if (EDAmameController.Controller_IsStringNum(lenStr))
                    {
                        Double newLen = Double.parseDouble(lenStr);

                        if ((newLen >= EDAmameController.Editor_TriLenMin) && (newLen <= EDAmameController.Editor_TriLenMax))
                        {
                            ((Polygon) renderNode.RenderNode_Node).getPoints().setAll(-newLen / 2, newLen / 2,
                                    newLen / 2, newLen / 2,
                                    0.0, -newLen / 2);
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply triangle lengths because the entered field is outside the limits! (Length limits: " + EDAmameController.Editor_TriLenMin + ", " + EDAmameController.Editor_TriLenMax + ")");
                        }
                    }
                    else if (!lenStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply triangle lengths because the entered field is non-numeric!");
                    }
                }
            }
            // Applying lines...
            else if (renderNode.RenderNode_Node.getClass() == Line.class)
            {
                Integer lineStartPointsBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "lineStartPointsBox");

                if (lineStartPointsBoxIdx != -1)
                {
                    HBox lineStartPointsBox = (HBox) propsBox.getChildren().get(lineStartPointsBoxIdx);
                    TextField startPointXText = (TextField) EDAmameController.Controller_GetNodeById(lineStartPointsBox.getChildren(), "lineStartPointsX");
                    TextField startPointYText = (TextField) EDAmameController.Controller_GetNodeById(lineStartPointsBox.getChildren(), "lineStartPointsY");

                    if (startPointXText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineStartPointsX\" node in Symbol Editor properties window \"lineStartPointsBox\" entry!");
                    if (startPointYText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineStartPointsY\" node in Symbol Editor properties window \"lineStartPointsBox\" entry!");

                    String startPointXStr = startPointXText.getText();
                    String startPointYStr = startPointYText.getText();

                    if (EDAmameController.Controller_IsStringNum(startPointXStr))
                        ((Line)renderNode.RenderNode_Node).setStartX(Double.parseDouble(startPointXStr));
                    else if (!startPointXStr.equals("<mixed>"))
                        EDAmameController.Controller_SetStatusBar("Unable to apply line start point X because the entered field is non-numeric!");

                    if (EDAmameController.Controller_IsStringNum(startPointYStr))
                        ((Line)renderNode.RenderNode_Node).setStartY(Double.parseDouble(startPointYStr));
                    else if (!startPointYStr.equals("<mixed>"))
                        EDAmameController.Controller_SetStatusBar("Unable to apply line start point Y because the entered field is non-numeric!");
                }

                Integer lineEndPointsBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "lineEndPointsBox");

                if (lineEndPointsBoxIdx != -1)
                {
                    HBox lineEndPointsBox = (HBox) propsBox.getChildren().get(lineEndPointsBoxIdx);
                    TextField endPointXText = (TextField) EDAmameController.Controller_GetNodeById(lineEndPointsBox.getChildren(), "lineEndPointsX");
                    TextField endPointYText = (TextField) EDAmameController.Controller_GetNodeById(lineEndPointsBox.getChildren(), "lineEndPointsY");

                    if (endPointXText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineEndPointsX\" node in Symbol Editor properties window \"lineEndPointsBox\" entry!");
                    if (endPointYText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineEndPointsY\" node in Symbol Editor properties window \"lineEndPointsBox\" entry!");

                    String endPointXStr = endPointXText.getText();
                    String endPointYStr = endPointYText.getText();

                    if (EDAmameController.Controller_IsStringNum(endPointXStr))
                        ((Line)renderNode.RenderNode_Node).setEndX(Double.parseDouble(endPointXStr));
                    else if (!endPointXStr.equals("<mixed>"))
                        EDAmameController.Controller_SetStatusBar("Unable to apply line end point X because the entered field is non-numeric!");

                    if (EDAmameController.Controller_IsStringNum(endPointYStr))
                        ((Line)renderNode.RenderNode_Node).setEndY(Double.parseDouble(endPointYStr));
                    else if (!endPointYStr.equals("<mixed>"))
                        EDAmameController.Controller_SetStatusBar("Unable to apply line end point Y because the entered field is non-numeric!");
                }

                Integer lineWidthsBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "lineWidthsBox");

                if (lineWidthsBoxIdx != -1)
                {
                    HBox lineWidthsBox = (HBox) propsBox.getChildren().get(lineWidthsBoxIdx);
                    TextField lineWidthsText = (TextField) EDAmameController.Controller_GetNodeById(lineWidthsBox.getChildren(), "lineWidths");

                    if (lineWidthsText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineWidths\" node in Symbol Editor properties window \"lineWidthsBox\" entry!");

                    String widthStr = lineWidthsText.getText();

                    if (EDAmameController.Controller_IsStringNum(widthStr))
                    {
                        Double newWidth = Double.parseDouble(widthStr);

                        if ((newWidth >= EDAmameController.Editor_LineWidthMin) && (newWidth <= EDAmameController.Editor_LineWidthMax))
                        {
                            ((Line)renderNode.RenderNode_Node).setStrokeWidth(newWidth);
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply line widths because the entered field is outside the limits! (Width limits: " + EDAmameController.Editor_LineWidthMin + ", " + EDAmameController.Editor_LineWidthMax + ")");
                        }
                    }
                    else if (!widthStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply line widths because the entered field is non-numeric!");
                    }
                }
            }
            // Applying texts...
            else if (renderNode.RenderNode_Node.getClass() == Text.class)
            {
                Integer contentBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "textContentBox");

                if (contentBoxIdx != -1)
                {
                    HBox contentBox = (HBox)propsBox.getChildren().get(contentBoxIdx);
                    TextField contentText = (TextField)EDAmameController.Controller_GetNodeById(contentBox.getChildren(), "textContent");

                    if (contentText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"textContent\" node in global properties window \"textContentBox\" entry!");

                    String content = contentText.getText();

                    if (!content.isEmpty())
                    {
                        if (!content.equals("<mixed>"))
                            ((Text)renderNode.RenderNode_Node).setText(content);
                    }
                    else
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply text contents because the entered field is empty!");
                    }
                }

                Integer fontSizeBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "fontSizeBox");

                if (fontSizeBoxIdx != -1)
                {
                    HBox fontSizeBox = (HBox)propsBox.getChildren().get(fontSizeBoxIdx);
                    TextField fontSizeText = (TextField)EDAmameController.Controller_GetNodeById(fontSizeBox.getChildren(), "fontSize");

                    if (fontSizeText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"fontSize\" node in global properties window \"fontSizeBox\" entry!");

                    String fontSizeStr = fontSizeText.getText();

                    if (EDAmameController.Controller_IsStringNum(fontSizeStr))
                    {
                        double fontSize = Double.parseDouble(fontSizeStr);

                        if (((fontSize >= EDAmameController.Editor_TextFontSizeMin) && (fontSize <= EDAmameController.Editor_TextFontSizeMax)))
                        {
                            ((Text)renderNode.RenderNode_Node).setFont(new Font("Arial", fontSize));
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply text font size because the entered field is is outside the limits! (Font size limits: " + EDAmameController.Editor_TextFontSizeMin + ", " + EDAmameController.Editor_TextFontSizeMax + ")");
                        }
                    }
                    else if (!fontSizeStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply text font size because the entered field is non-numeric!");
                    }
                }
            }
            // Applying circle radius...
            else if (renderNode.RenderNode_Node.getClass() == Group.class)
            {
                Group group = (Group)renderNode.RenderNode_Node;

                if (group.getChildren().size() != 2)
                    throw new java.lang.Error("ERROR: Attempting to apply pin from symbol properties window without 2 children!");
                if (group.getChildren().get(1).getClass() != Text.class)
                    throw new java.lang.Error("ERROR: Attempting to apply pin from symbol properties window without a text node!");

                Integer pinLabelBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "pinLabelBox");

                if (pinLabelBoxIdx != -1)
                {
                    HBox pinLabelBox = (HBox)propsBox.getChildren().get(pinLabelBoxIdx);
                    TextField pinLabelText = (TextField)EDAmameController.Controller_GetNodeById(pinLabelBox.getChildren(), "pinLabels");

                    if (pinLabelText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"pinLabels\" node in global properties window \"pinLabelBox\" entry!");

                    String pinLabel = pinLabelText.getText();

                    if (!pinLabel.isEmpty())
                    {
                        if (!pinLabel.equals("<mixed>"))
                            ((Text)group.getChildren().get(1)).setText(pinLabel);
                    }
                    else
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply pin label because the entered field is empty!");
                    }
                }
            }

            // Applying borders...
            if (renderNode.RenderNode_Node.getClass() != Line.class &&
                renderNode.RenderNode_Node.getClass() != Text.class &&
                renderNode.RenderNode_Node.getClass() != Group.class)
            {
                Integer strokeWidthBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "strokesWidthBox");

                if (strokeWidthBoxIdx != -1)
                {
                    HBox strokeWidthBox = (HBox)propsBox.getChildren().get(strokeWidthBoxIdx);
                    TextField strokeWidthText = (TextField) EDAmameController.Controller_GetNodeById(strokeWidthBox.getChildren(), "strokeWidth");

                    if (strokeWidthText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"strokeWidth\" node in Symbol Editor properties window \"strokesWidthBox\" entry!");

                    String strokeWidthStr = strokeWidthText.getText();

                    if (EDAmameController.Controller_IsStringNum(strokeWidthStr))
                    {
                        Double newStrokeWidth = Double.parseDouble(strokeWidthStr);

                        if ((newStrokeWidth >= EDAmameController.Editor_BorderMin) && (newStrokeWidth <= EDAmameController.Editor_BorderMax))
                        {
                            ((Shape)renderNode.RenderNode_Node).setStrokeWidth(newStrokeWidth);
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply shape border width because the entered field is outside the limits! (Border width limits: " + EDAmameController.Editor_BorderMin + ", " + EDAmameController.Editor_BorderMax + ")");
                        }
                    }
                    else if (!strokeWidthStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply shape border width because the entered field is non-numeric!");
                    }
                }

                Integer strokeBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "strokeColorBox");

                if (strokeBoxIdx != -1)
                {
                    HBox strokeBox = (HBox) propsBox.getChildren().get(strokeBoxIdx);
                    ColorPicker colorPicker = (ColorPicker)EDAmameController.Controller_GetNodeById(strokeBox.getChildren(), "strokeColor");

                    if (colorPicker == null)
                        throw new java.lang.Error("ERROR: Unable to find \"strokeColor\" node in Symbol Editor properties window \"strokeColorBox\" entry!");

                    Color color = colorPicker.getValue();

                    if ((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                    {
                        if (renderNode.RenderNode_Node.getClass() != Line.class)
                            ((Shape)renderNode.RenderNode_Node).setStroke(color);
                    }
                    else
                    {
                        if (color != null)
                            EDAmameController.Controller_SetStatusBar("Unable to apply shape border color because the entered color is transparent!");
                    }
                }
            }
        }
    }

    public static boolean EditorSymbol_checkShapeTransparency(String strokeSize, Paint strokeColor, Color fillColor)
    {
        if (EDAmameController.Controller_IsStringNum(strokeSize))
        {
            double strokeDouble = Double.parseDouble(strokeSize);
            if ((strokeDouble == 0) || (!strokeColor.isOpaque()))
            {
                if (!((fillColor != null) && (fillColor != Color.TRANSPARENT) && (fillColor.hashCode() != 0x00000000)) || (fillColor.getOpacity() != 1.0))
                {
                    EDAmameController.Controller_SetStatusBar("Unable to drop shape because the entered fill and border is transparent!");
                    return false;
                }
            }
        }
        else
        {
            return false;
        }
        return true;
    }

    public static boolean EditorSymbol_checkStringBorderSize(String checkString)
    {
        if (EDAmameController.Controller_IsStringNum(checkString))
        {
            double checkDouble = Double.parseDouble(checkString);
            if (((checkDouble >= EDAmameController.Editor_BorderMin) && (checkDouble <= EDAmameController.Editor_BorderMax)))
            {
                return true;
            }
            else
            {
                EDAmameController.Controller_SetStatusBar("Unable to drop shape because the entered border size field is outside the limits! (Length limits: " + EDAmameController.Editor_BorderMin + ", " + EDAmameController.Editor_BorderMax + ")");
                return false;
            }
        }
        else
        {
            EDAmameController.Controller_SetStatusBar("Unable to drop shape because the entered border size is non-numeric!");
            return false;
        }
    }

    public static boolean EditorSymbol_checkStringCircleBounds(String checkString)
    {
        if (EDAmameController.Controller_IsStringNum(checkString))
        {
            double checkDouble = Double.parseDouble(checkString);
            if (((checkDouble >= EDAmameController.Editor_CircleRadiusMin) && (checkDouble <= EDAmameController.Editor_CircleRadiusMax)))
            {
                return true;
            }
            else
            {
                EDAmameController.Controller_SetStatusBar("Unable to drop circle because the entered radius field is outside the limits! (Radius limits: " + EDAmameController.Editor_CircleRadiusMin + ", " + EDAmameController.Editor_CircleRadiusMax + ")");
                return false;
            }
        }
        else
        {
            EDAmameController.Controller_SetStatusBar("Unable to drop circle because the entered radius field is non-numeric!");
            return false;
        }
    }

    public static boolean EditorSymbol_checkStringRectBounds(String checkWidth, String checkHeight)
    {
        if ((EDAmameController.Controller_IsStringNum(checkWidth)) && (EDAmameController.Controller_IsStringNum(checkHeight)))
        {
            double doubleWidth = Double.parseDouble(checkWidth);
            double doubleHeight = Double.parseDouble(checkHeight);
            if (!((doubleWidth >= EDAmameController.Editor_RectWidthMin) && (doubleWidth <= EDAmameController.Editor_RectWidthMax)))
            {
                EDAmameController.Controller_SetStatusBar("Unable to drop rectangle because the entered width or height field is outside the limits! (Width limits: " + EDAmameController.Editor_RectWidthMin + ", " + EDAmameController.Editor_RectWidthMax + " | Height limits: " + EDAmameController.Editor_RectHeightMin + ", " + EDAmameController.Editor_RectHeightMax + ")");
                return false;
            }
            else if (!((doubleHeight >= EDAmameController.Editor_RectHeightMin) && (doubleHeight <= EDAmameController.Editor_RectHeightMax)))
            {
                EDAmameController.Controller_SetStatusBar("Unable to drop rectangle because the entered width or height field is outside the limits! (Width limits: " + EDAmameController.Editor_RectWidthMin + ", " + EDAmameController.Editor_RectWidthMax + " | Height limits: " + EDAmameController.Editor_RectHeightMin + ", " + EDAmameController.Editor_RectHeightMax + ")");
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            EDAmameController.Controller_SetStatusBar("Unable to drop rectangle because the entered width or height field is non-numeric!");
            return false;
        }
    }

    public static boolean EditorSymbol_checkStringTriBounds(String checkString)
    {
        if (EDAmameController.Controller_IsStringNum(checkString))
        {
            double checkDouble = Double.parseDouble(checkString);
            if (((checkDouble >= EDAmameController.Editor_TriLenMin) && (checkDouble <= EDAmameController.Editor_TriLenMax)))
            {
                return true;
            }
            else
            {
                EDAmameController.Controller_SetStatusBar("Unable to drop triangle because the entered length field is outside the limits! (Length limits: " + EDAmameController.Editor_TriLenMin + ", " + EDAmameController.Editor_TriLenMax + ")");
                return false;
            }
        }
        else
        {
            EDAmameController.Controller_SetStatusBar("Unable to drop triangle because the entered length field is non-numeric!");
            return false;
        }
    }

    //// TESTING FUNCTIONS ////

    /**
     * a test button for checking if controller is working and unique. (hint: it is.) this can be removed.
     */
    /*@FXML
    private void thisButton()
    {
        System.out.println("Button clicked on " + Editor_ID);
    }*/
}
