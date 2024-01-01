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

//import org.apache.commons.lang.StringUtils;

/**
 * Editor for maintaining Symbol libraries.
 */
public class EditorSymbol extends Editor
{
    //// GLOBAL VARIABLES ////

    @FXML
    private Button innerButton;
    @FXML
    public ToggleGroup toggleGroup;
    @FXML
    public ColorPicker circleColor;
    @FXML
    public TextField circleRadius;
    @FXML
    public ColorPicker circleBorderColor;
    @FXML
    public TextField circleBorderSize;
    @FXML
    public ColorPicker rectangleColor;
    @FXML
    public TextField rectangleWidth;
    @FXML
    public TextField rectangleHeight;
    @FXML
    public ColorPicker rectangleBorderColor;
    @FXML
    public TextField rectangleBorderSize;
    @FXML
    public ColorPicker triangleBorderColor;
    @FXML
    public TextField triangleBorderSize;
    @FXML
    public ColorPicker triangleColor;
    @FXML
    public TextField triangleHeight;
    @FXML
    public TextField lineWidth;
    @FXML
    public ColorPicker lineColor;
    @FXML
    public TextField textContent;
    @FXML
    public TextField textSize;
    @FXML
    public ColorPicker textColor;
    @FXML
    public TextField pinLabel;
    @FXML
    public TextField pinRadius;
    @FXML
    public ColorPicker pinColor;

    //// MAIN FUNCTIONS ////

    /**
     * Factory to create a single EditorSymbol and its UI attached to a particular symbol library.
     *
     * @throws IOException if there are problems loading the scene from FXML resources.
     */
    static public Editor Create() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorSymbol.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorSymbol editor = fxmlLoader.getController();
        editor.Init(0, "EditorSymbol");
        editor.Dissect(0, scene);
        editor.renderSystem.CanvasRenderGrid();
        editor.ListenersInit();

        Editor.TextFieldListenerInit(editor.circleRadius);
        Editor.TextFieldListenerInit(editor.circleBorderSize);
        Editor.TextFieldListenerInit(editor.rectangleWidth);
        Editor.TextFieldListenerInit(editor.rectangleHeight);
        Editor.TextFieldListenerInit(editor.rectangleBorderSize);
        Editor.TextFieldListenerInit(editor.triangleBorderSize);
        Editor.TextFieldListenerInit(editor.triangleHeight);
        Editor.TextFieldListenerInit(editor.lineWidth);
        Editor.TextFieldListenerInit(editor.textContent);
        Editor.TextFieldListenerInit(editor.textSize);
        Editor.TextFieldListenerInit(editor.pinLabel);
        Editor.TextFieldListenerInit(editor.pinRadius);



        return editor;
    }

    /**
     * Provides initialization of the Controller
     */
    @FXML
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + this.innerButton);
    }

    //// CALLBACK FUNCTIONS ////

    @FXML
    public void Save()
    {
        LinkedList<Node> nodes = new LinkedList<Node>();

        for (int i = 0; i < this.renderSystem.nodes.size(); i++)
            nodes.add(this.renderSystem.nodes.get(i).node);

        File.NodesSave(nodes, true);
    }

    @FXML
    public void Load()
    {
        LinkedList<Node> nodes = File.NodesLoad(true);

        if (nodes == null)
            return;

        for (int i = 0; i < nodes.size(); i++)
        {
            Node node = nodes.get(i);
            PairMutable realPos = this.renderSystem.PaneHolderGetDrawPos(new PairMutable(node.getTranslateX(), node.getTranslateY()));
            node.setTranslateX(realPos.GetLeftDouble());
            node.setTranslateY(realPos.GetRightDouble());

            boolean edgeSnaps = true;
            LinkedList<PairMutable> snapManualPos = null;
            boolean isPin = false;

            if (node.getClass() == Group.class)
            {
                edgeSnaps = false;
                snapManualPos = new LinkedList<PairMutable>();
                isPin = true;
                Group group = (Group)node;

                for (int j = 0; j < group.getChildren().size(); j++)
                {
                    Node currChild = group.getChildren().get(j);

                    if (currChild.getClass() == Circle.class)
                        snapManualPos.add(new PairMutable(currChild.getTranslateX(), currChild.getTranslateY()));
                }
            }

            RenderNode renderNode = new RenderNode("LoadedNode", node, edgeSnaps, snapManualPos, false, isPin, this.renderSystem);
            this.renderSystem.NodeAdd(renderNode);
        }
    }

    public void OnDragOverSpecific(DragEvent event)
    {
        PairMutable dropPos = this.renderSystem.PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.renderSystem.PaneHolderGetRealPos(dropPos);

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

    public void OnDragDroppedSpecific(DragEvent event)
    {
        PairMutable dropPos = this.renderSystem.PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.renderSystem.PaneHolderGetRealPos(dropPos);

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

    public void OnMouseMovedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.renderSystem.PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.renderSystem.PaneHolderGetRealPos(dropPos);
    }

    public void OnMousePressedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.renderSystem.PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.renderSystem.PaneHolderGetRealPos(dropPos);

        if (this.pressedLMB)
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
        else if (this.pressedRMB)
        {}
    }

    public void OnMouseReleasedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.renderSystem.PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        dropPos = this.MagneticSnapCheck(dropPos);
        PairMutable realPos = this.renderSystem.PaneHolderGetRealPos(dropPos);

        if (this.pressedLMB)
        {
            // Handling shape dropping (only if we're not hovering over, selecting, moving any shapes or box selecting)
            if ((this.shapesSelected == 0) &&
                !this.shapesMoving &&
                (this.selectionBox == null) &&
                !this.shapesWereSelected &&
                !this.wasSelectionBox)
            {
                RadioButton selectedShapeButton = (RadioButton) toggleGroup.getSelectedToggle();

                // Only dropping the shape within the theater limits...
                if (selectedShapeButton != null)
                {
                    /*if ((realPos.GetLeftDouble() > -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2) &&
                        (realPos.GetLeftDouble() < EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2) &&
                        (realPos.GetRightDouble() > -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2) &&
                        (realPos.GetRightDouble() < EDAmameController.Editor_TheaterSize.GetRightDouble() / 2))
                    {*/
                        if (!selectedShapeButton.getText().equals("Line"))
                            this.linePreview = null;

                        boolean lineStarted = false;

                        if (this.shapesHighlighted == 0)
                        {
                            if (selectedShapeButton.getText().equals("Circle"))
                            {
                                EDAmameController.SetStatusBar("EDAmame Status Area");

                                String stringRadius = this.circleRadius.getText();
                                String stringStrokeSize = this.circleBorderSize.getText();
                                Color fillColor = this.circleColor.getValue();
                                Paint strokeColor = this.circleBorderColor.getValue();

                                if (CheckStringCircleBounds(stringRadius) && CheckStringBorderSize(stringStrokeSize) && CheckShapeTransparency(stringStrokeSize, strokeColor, fillColor))
                                {
                                    Circle circle = new Circle(Double.parseDouble(stringRadius), fillColor);

                                    circle.setTranslateX(dropPos.GetLeftDouble());
                                    circle.setTranslateY(dropPos.GetRightDouble());

                                    circle.setStroke(strokeColor);
                                    circle.setStrokeWidth(Double.parseDouble(stringStrokeSize));

                                    RenderNode renderNode = new RenderNode("Circle", circle, true, null, false, false, this.renderSystem);
                                    this.renderSystem.NodeAdd(renderNode);
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Rectangle"))
                            {
                                EDAmameController.SetStatusBar("EDAmame Status Area");

                                String stringWidth = this.rectangleWidth.getText();
                                String stringHeight = this.rectangleHeight.getText();
                                String stringStrokeSize = this.rectangleBorderSize.getText();
                                Color fillColor = this.rectangleColor.getValue();
                                Paint strokeColor = this.rectangleBorderColor.getValue();

                                if (CheckStringRectBounds(stringWidth, stringHeight) && CheckStringBorderSize(stringStrokeSize) && CheckShapeTransparency(stringStrokeSize, strokeColor, fillColor))
                                {
                                    Rectangle rectangle = new Rectangle(Double.parseDouble(stringWidth), Double.parseDouble(stringHeight), fillColor);

                                    rectangle.setTranslateX(dropPos.GetLeftDouble());
                                    rectangle.setTranslateY(dropPos.GetRightDouble());

                                    rectangle.setStroke(strokeColor);
                                    rectangle.setStrokeWidth(Double.parseDouble(stringStrokeSize));

                                    RenderNode renderNode = new RenderNode("Rectangle", rectangle, true, null, false, false, this.renderSystem);
                                    this.renderSystem.NodeAdd(renderNode);
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Triangle"))
                            {
                                EDAmameController.SetStatusBar("EDAmame Status Area");

                                String stringMiddleHeight = this.triangleHeight.getText();
                                String stringStrokeSize = this.triangleBorderSize.getText();
                                Color fillColor = this.triangleColor.getValue();
                                Paint strokeColor = this.triangleBorderColor.getValue();

                                if (CheckStringTriBounds(stringMiddleHeight) && CheckStringBorderSize(stringStrokeSize) && CheckShapeTransparency(stringStrokeSize, strokeColor, fillColor))
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

                                    RenderNode renderNode = new RenderNode("Triangle", triangle, true, null, false, false, this.renderSystem);
                                    this.renderSystem.NodeAdd(renderNode);
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Line"))
                            {
                                EDAmameController.SetStatusBar("EDAmame Status Area");

                                // If we're starting the line drawing...
                                if (this.linePreview == null)
                                {
                                    String stringWidth = this.lineWidth.getText();
                                    Color color = this.lineColor.getValue();
                                    boolean canDrop = true;

                                    if (stringWidth.isEmpty() || !EDAmameController.IsStringNum(stringWidth))
                                    {
                                        EDAmameController.SetStatusBar("Unable to drop line because the entered width field is non-numeric!");
                                        canDrop = false;
                                    }
                                    double width = Double.parseDouble(stringWidth);
                                    if (!((width >= EDAmameController.Editor_LineWidthMin) && (width <= EDAmameController.Editor_LineWidthMax)))
                                    {
                                        EDAmameController.SetStatusBar("Unable to drop line because the entered width is outside the limits! (Width limits: " + EDAmameController.Editor_LineWidthMin + ", " + EDAmameController.Editor_LineWidthMax + ")");
                                        canDrop = false;
                                    }

                                    if (!((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000)))
                                    {
                                        EDAmameController.SetStatusBar("Unable to drop line because the entered color field is transparent!");
                                        canDrop = false;
                                    }

                                    if (canDrop)
                                    {
                                        this.linePreview = new Line();
                                        //this.EditorSymbol_LinePreview.setId("linePreview");

                                        this.linePreview.setStartX(dropPos.GetLeftDouble());
                                        this.linePreview.setStartY(dropPos.GetRightDouble());
                                        this.linePreview.setEndX(dropPos.GetLeftDouble());
                                        this.linePreview.setEndY(dropPos.GetRightDouble());

                                        this.linePreview.setStrokeWidth(width);
                                        this.linePreview.setStroke(color);

                                        RenderNode renderNode = new RenderNode("linePreview", this.linePreview, true, null, true, false, this.renderSystem);
                                        this.renderSystem.NodeAdd(renderNode);

                                        lineStarted = true;
                                    }
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Text"))
                            {
                                EDAmameController.SetStatusBar("EDAmame Status Area");

                                String stringTextContent = textContent.getText();
                                boolean canDrop = true;

                                if (stringTextContent.isEmpty())
                                {
                                    EDAmameController.SetStatusBar("Unable to drop text because the entered text field is empty!");
                                    canDrop = false;
                                }
                                String stringFontSize = this.textSize.getText();
                                if (stringFontSize.isEmpty() || !EDAmameController.IsStringNum(stringFontSize))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop text because the entered font size field is non-numeric!");
                                    canDrop = false;
                                }
                                double fontSize = Double.parseDouble(stringFontSize);
                                Color color = this.textColor.getValue();

                                if (!((fontSize >= EDAmameController.Editor_TextFontSizeMin) && (fontSize <= EDAmameController.Editor_TextFontSizeMax)))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop text because the entered font size field is outside the limits! (Font size limits: " + EDAmameController.Editor_TextFontSizeMin + ", " + EDAmameController.Editor_TextFontSizeMax + ")");
                                    canDrop = false;
                                }

                                if (!((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop text because the entered font color field is transparent!");
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

                                    RenderNode renderNode = new RenderNode("Text", text, true, null, false, false, this.renderSystem);
                                    this.renderSystem.NodeAdd(renderNode);
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Pin"))
                            {
                                EDAmameController.SetStatusBar("EDAmame Status Area");

                                String stringPinLabel = this.pinLabel.getText();
                                boolean canDrop = true;

                                if (stringPinLabel.isEmpty())
                                {
                                    EDAmameController.SetStatusBar("Unable to drop pin because the entered label field is empty!");
                                    canDrop = false;
                                }
                                String stringPinRadius = this.pinRadius.getText();

                                if (stringPinRadius.isEmpty() || !EDAmameController.IsStringNum(stringPinRadius))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop pin because the entered radius field is non-numeric!");
                                    canDrop = false;
                                }

                                double pinRadius = Double.parseDouble(stringPinRadius);
                                Color pinColor = this.pinColor.getValue();

                                if (!((pinRadius >= EDAmameController.Editor_PinRadiusMin) && (pinRadius <= EDAmameController.Editor_PinRadiusMax)))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop pin because the entered radius field is outside the limits! (Font size limits: " + EDAmameController.Editor_PinRadiusMin + ", " + EDAmameController.Editor_PinRadiusMax + ")");
                                    canDrop = false;
                                }
                                if (!((pinColor != null) && (pinColor != Color.TRANSPARENT) && (pinColor.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop pin because the entered font color field is transparent!");
                                    canDrop = false;
                                }

                                if (canDrop)
                                {
                                    Group pin = new Group();
                                    pin.setId("PIN_" + stringPinLabel);

                                    Circle pinCircle = new Circle(pinRadius, pinColor);
                                    pinCircle.setStroke(Color.TRANSPARENT);
                                    pinCircle.setStrokeWidth(0);

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

                                    RenderNode renderNode = new RenderNode("Pin", pin, false, snap, false, true, this.renderSystem);
                                    this.renderSystem.NodeAdd(renderNode);
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
                            if ((this.linePreview != null) && !lineStarted)
                            {
                                PairMutable posStart = new PairMutable(this.linePreview.getStartX(), this.linePreview.getStartY());
                                PairMutable posEnd = new PairMutable(dropPos.GetLeftDouble(), dropPos.GetRightDouble());

                                Line line = new Line();
                                Editor.LineDropPosCalculate(line, posStart, posEnd);

                                line.setStroke(this.linePreview.getStroke());
                                line.setStrokeWidth(this.linePreview.getStrokeWidth());

                                this.LinePreviewRemove();

                                RenderNode renderNode = new RenderNode("Line", line, true, null, false, false, this.renderSystem);
                                this.renderSystem.NodeAdd(renderNode);
                            }
                        }
                    /*}
                    else
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to drop element because the dropping position is outside the theater limits!");
                    }*/
                }
            }

            this.shapesWereSelected = false;
        }
        else if (this.pressedRMB)
        {}
    }

    public void OnMouseDraggedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.renderSystem.PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        PairMutable realPos = this.renderSystem.PaneHolderGetRealPos(dropPos);

        if (this.pressedLMB)
        {}
        else if (this.pressedRMB)
        {}

        // Handling symbol highlighting
        //this.EditorSchematic_CheckSymbolsDroppedMouseHighlights(new PairMutable(event.getX(), event.getY()));
    }

    public void OnScrollSpecific(ScrollEvent event)
    {}

    public void OnKeyPressedSpecific(KeyEvent event)
    {
        //System.out.println("Symbol key pressed!");

        // Handling the shape properties window (only if there's not another properties window already open)...
    }

    public void OnKeyReleasedSpecific(KeyEvent event)
    {}

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void PropsLoadSpecific()
    {
        if (this.shapesSelected == 0)
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

        for (int i = 0; i < this.renderSystem.nodes.size(); i++)
        {
            RenderNode renderNode = this.renderSystem.nodes.get(i);

            if (!renderNode.selected)
                continue;

            if (renderNode.node.getClass() == Circle.class)
            {
                circlesRadii.add(((Circle)renderNode.node).getRadius());
                strokeWidths.add(((Circle)renderNode.node).getStrokeWidth());
                strokes.add(((Circle)renderNode.node).getStroke());

                needHeader = true;
            }
            else if (renderNode.node.getClass() == Rectangle.class)
            {
                rectsWidths.add(((Rectangle)renderNode.node).getWidth());
                rectsHeights.add(((Rectangle)renderNode.node).getHeight());
                strokeWidths.add(((Rectangle)renderNode.node).getStrokeWidth());
                strokes.add(((Rectangle)renderNode.node).getStroke());

                needHeader = true;
            }
            else if (renderNode.node.getClass() == Polygon.class)
            {
                trisLens.add(((Polygon)renderNode.node).getPoints().get(2) - ((Polygon) renderNode.node).getPoints().get(0));
                strokeWidths.add(((Polygon)renderNode.node).getStrokeWidth());
                strokes.add(((Polygon)renderNode.node).getStroke());

                needHeader = true;
            }
            else if (renderNode.node.getClass() == Line.class)
            {
                Line line = (Line)renderNode.node;
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
            else if (renderNode.node.getClass() == Text.class)
            {
                textContents.add(((Text)renderNode.node).getText());
                textFontSizes.add(((Text)renderNode.node).getFont().getSize());

                needHeader = true;
            }
            else if (renderNode.isPin)
            {
                Group group = (Group)renderNode.node;

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
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(shapeHeader);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
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

            if (EDAmameController.IsListAllEqual(circlesRadii))
                radiusText.setText(Double.toString(circlesRadii.get(0)));
            else
                radiusText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(circleHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
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

            if (EDAmameController.IsListAllEqual(rectsWidths))
                widthText.setText(Double.toString(rectsWidths.get(0)));
            else
                widthText.setText("<mixed>");

            if (EDAmameController.IsListAllEqual(rectsHeights))
                heightText.setText(Double.toString(rectsHeights.get(0)));
            else
                heightText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(rectHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
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

            if (EDAmameController.IsListAllEqual(trisLens))
                triLenText.setText(Double.toString(trisLens.get(0)));
            else
                triLenText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(triLenHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
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

            if (EDAmameController.IsListAllEqual(lineStartPosX))
                lineStartPointsXText.setText(Double.toString(lineStartPosX.get(0)));
            else
                lineStartPointsXText.setText("<mixed>");

            if (EDAmameController.IsListAllEqual(lineStartPosY))
                lineStartPointsYText.setText(Double.toString(lineStartPosY.get(0)));
            else
                lineStartPointsYText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(lineStartPointsHBox);

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

            if (EDAmameController.IsListAllEqual(lineEndPosX))
                lineEndPointsXText.setText(Double.toString(lineEndPosX.get(0)));
            else
                lineEndPointsXText.setText("<mixed>");

            if (EDAmameController.IsListAllEqual(lineEndPosY))
                lineEndPointsYText.setText(Double.toString(lineEndPosY.get(0)));
            else
                lineEndPointsYText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(lineEndPointsHBox);

            // Width
            HBox lineWidthsHBox = new HBox(10);
            lineWidthsHBox.setId("lineWidthsBox");
            lineWidthsHBox.getChildren().add(new Label("Line Widths: "));
            TextField lineWidthsText = new TextField();
            lineWidthsText.setId("lineWidths");
            lineWidthsHBox.getChildren().add(lineWidthsText);

            if (EDAmameController.IsListAllEqual(lineWidths))
                lineWidthsText.setText(Double.toString(lineWidths.get(0)));
            else
                lineWidthsText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(lineWidthsHBox);

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
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

            if (EDAmameController.IsListAllEqual(strokeWidths))
                strokesWidthTextBox.setText(Double.toString(strokeWidths.get(0)));
            else
                strokesWidthTextBox.setText("<mixed>");

            HBox strokeColorHBox = new HBox(10);
            strokeColorHBox.setId("strokeColorBox");
            strokeColorHBox.getChildren().add(new Label("Shape Border Colors: "));
            ColorPicker strokeColorPicker = new ColorPicker();
            strokeColorPicker.setId("strokeColor");
            strokeColorHBox.getChildren().add(strokeColorPicker);

            if (EDAmameController.IsListAllEqual(strokes))
                strokeColorPicker.setValue((Color)strokes.get(0));
            else
                strokeColorPicker.setValue(null);

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(strokesWidthHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(strokeColorHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
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

            if (EDAmameController.IsListAllEqual(textContents))
                textContentText.setText(textContents.get(0));
            else
                textContentText.setText("<mixed>");

            if (EDAmameController.IsListAllEqual(textFontSizes))
                textFontSizeText.setText(Double.toString(textFontSizes.get(0)));
            else
                textFontSizeText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(textContentHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(textFontSizeHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
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

            if (EDAmameController.IsListAllEqual(pinLabels))
                pinLabelText.setText(pinLabels.get(0));
            else
                pinLabelText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(pinLabelHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }
    }

    public void PropsApplySpecific()
    {
        if (this.shapesSelected == 0)
            return;

        VBox propsBox = EDAmameController.editorPropertiesWindow.propsBox;

        // Iterating over all the shapes & attempting to apply shape properties if selected...
        for (int i = 0; i < this.renderSystem.nodes.size(); i++)
        {
            RenderNode renderNode = this.renderSystem.nodes.get(i);

            if (!renderNode.selected)
                continue;

            // Applying circle radius...
            if (renderNode.node.getClass() == Circle.class)
            {
                Integer circleBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "circleBox");

                if (circleBoxIdx != -1)
                {
                    HBox circleBox = (HBox) propsBox.getChildren().get(circleBoxIdx);
                    TextField radiiText = (TextField) EDAmameController.GetNodeById(circleBox.getChildren(), "circleRadii");

                    if (radiiText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"circleRadii\" node in Symbol Editor properties window \"circleBox\" entry!");

                    String radiusStr = radiiText.getText();

                    if (EDAmameController.IsStringNum(radiusStr))
                    {
                        Double newRadius = Double.parseDouble(radiusStr);

                        if ((newRadius >= EDAmameController.Editor_CircleRadiusMin) && (newRadius <= EDAmameController.Editor_CircleRadiusMax))
                        {
                            ((Circle) renderNode.node).setRadius(newRadius);
                        }
                        else
                        {
                            EDAmameController.SetStatusBar("Unable to apply circle radii because the entered field is outside the limits! (Radius limits: " + EDAmameController.Editor_CircleRadiusMin + ", " + EDAmameController.Editor_CircleRadiusMax + ")");
                        }
                    }
                    else if (!radiusStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply circle radii because the entered field is non-numeric!");
                    }
                }
            }
            // Applying rectangle width & height...
            else if (renderNode.node.getClass() == Rectangle.class)
            {
                Integer rectBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "rectBox");

                if (rectBoxIdx != -1)
                {
                    HBox rectBox = (HBox) propsBox.getChildren().get(rectBoxIdx);
                    TextField widthText = (TextField) EDAmameController.GetNodeById(rectBox.getChildren(), "rectWidths");
                    TextField heightText = (TextField) EDAmameController.GetNodeById(rectBox.getChildren(), "rectHeights");

                    if (widthText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"rectWidths\" node in Symbol Editor properties window \"rectBox\" entry!");
                    if (heightText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"rectHeights\" node in Symbol Editor properties window \"rectBox\" entry!");

                    String widthStr = widthText.getText();
                    String heightStr = heightText.getText();

                    if (EDAmameController.IsStringNum(widthStr))
                    {
                        Double newWidth = Double.parseDouble(widthStr);

                        if ((newWidth >= EDAmameController.Editor_RectWidthMin) && (newWidth <= EDAmameController.Editor_RectWidthMax))
                        {
                            ((Rectangle) renderNode.node).setWidth(newWidth);
                        }
                        else
                        {
                            EDAmameController.SetStatusBar("Unable to apply rectangle widths because the entered field is outside the limits! (Width limits: " + EDAmameController.Editor_RectWidthMin + ", " + EDAmameController.Editor_RectWidthMax + ")");
                        }
                    }
                    else if (!widthStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply rectangle widths because the entered field is non-numeric!");
                    }

                    if (EDAmameController.IsStringNum(heightStr))
                    {
                        Double newHeight = Double.parseDouble(heightStr);

                        if ((newHeight >= EDAmameController.Editor_RectHeightMin) && (newHeight <= EDAmameController.Editor_RectHeightMax))
                        {
                            ((Rectangle) renderNode.node).setHeight(newHeight);
                        }
                        else
                        {
                            EDAmameController.SetStatusBar("Unable to apply rectangle heights because the entered field is outside the limits! (Height limits: " + EDAmameController.Editor_RectHeightMin + ", " + EDAmameController.Editor_RectHeightMax + ")");
                        }
                    }
                    else if (!heightStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply rectangle heights because the entered field is non-numeric!");
                    }
                }
            }
            // Applying triangle length...
            else if (renderNode.node.getClass() == Polygon.class)
            {
                Integer triBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "triBox");

                if (triBoxIdx != -1)
                {
                    HBox triBox = (HBox) propsBox.getChildren().get(triBoxIdx);
                    TextField lensText = (TextField) EDAmameController.GetNodeById(triBox.getChildren(), "triLens");

                    if (lensText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"triLens\" node in Symbol Editor properties window \"triBox\" entry!");

                    String lenStr = lensText.getText();

                    if (EDAmameController.IsStringNum(lenStr))
                    {
                        Double newLen = Double.parseDouble(lenStr);

                        if ((newLen >= EDAmameController.Editor_TriLenMin) && (newLen <= EDAmameController.Editor_TriLenMax))
                        {
                            ((Polygon) renderNode.node).getPoints().setAll(-newLen / 2, newLen / 2,
                                    newLen / 2, newLen / 2,
                                    0.0, -newLen / 2);
                        }
                        else
                        {
                            EDAmameController.SetStatusBar("Unable to apply triangle lengths because the entered field is outside the limits! (Length limits: " + EDAmameController.Editor_TriLenMin + ", " + EDAmameController.Editor_TriLenMax + ")");
                        }
                    }
                    else if (!lenStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply triangle lengths because the entered field is non-numeric!");
                    }
                }
            }
            // Applying lines...
            else if (renderNode.node.getClass() == Line.class)
            {
                Integer lineStartPointsBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "lineStartPointsBox");

                if (lineStartPointsBoxIdx != -1)
                {
                    HBox lineStartPointsBox = (HBox) propsBox.getChildren().get(lineStartPointsBoxIdx);
                    TextField startPointXText = (TextField) EDAmameController.GetNodeById(lineStartPointsBox.getChildren(), "lineStartPointsX");
                    TextField startPointYText = (TextField) EDAmameController.GetNodeById(lineStartPointsBox.getChildren(), "lineStartPointsY");

                    if (startPointXText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineStartPointsX\" node in Symbol Editor properties window \"lineStartPointsBox\" entry!");
                    if (startPointYText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineStartPointsY\" node in Symbol Editor properties window \"lineStartPointsBox\" entry!");

                    String startPointXStr = startPointXText.getText();
                    String startPointYStr = startPointYText.getText();

                    if (EDAmameController.IsStringNum(startPointXStr))
                        ((Line)renderNode.node).setStartX(Double.parseDouble(startPointXStr));
                    else if (!startPointXStr.equals("<mixed>"))
                        EDAmameController.SetStatusBar("Unable to apply line start point X because the entered field is non-numeric!");

                    if (EDAmameController.IsStringNum(startPointYStr))
                        ((Line)renderNode.node).setStartY(Double.parseDouble(startPointYStr));
                    else if (!startPointYStr.equals("<mixed>"))
                        EDAmameController.SetStatusBar("Unable to apply line start point Y because the entered field is non-numeric!");
                }

                Integer lineEndPointsBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "lineEndPointsBox");

                if (lineEndPointsBoxIdx != -1)
                {
                    HBox lineEndPointsBox = (HBox) propsBox.getChildren().get(lineEndPointsBoxIdx);
                    TextField endPointXText = (TextField) EDAmameController.GetNodeById(lineEndPointsBox.getChildren(), "lineEndPointsX");
                    TextField endPointYText = (TextField) EDAmameController.GetNodeById(lineEndPointsBox.getChildren(), "lineEndPointsY");

                    if (endPointXText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineEndPointsX\" node in Symbol Editor properties window \"lineEndPointsBox\" entry!");
                    if (endPointYText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineEndPointsY\" node in Symbol Editor properties window \"lineEndPointsBox\" entry!");

                    String endPointXStr = endPointXText.getText();
                    String endPointYStr = endPointYText.getText();

                    if (EDAmameController.IsStringNum(endPointXStr))
                        ((Line)renderNode.node).setEndX(Double.parseDouble(endPointXStr));
                    else if (!endPointXStr.equals("<mixed>"))
                        EDAmameController.SetStatusBar("Unable to apply line end point X because the entered field is non-numeric!");

                    if (EDAmameController.IsStringNum(endPointYStr))
                        ((Line)renderNode.node).setEndY(Double.parseDouble(endPointYStr));
                    else if (!endPointYStr.equals("<mixed>"))
                        EDAmameController.SetStatusBar("Unable to apply line end point Y because the entered field is non-numeric!");
                }

                Integer lineWidthsBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "lineWidthsBox");

                if (lineWidthsBoxIdx != -1)
                {
                    HBox lineWidthsBox = (HBox) propsBox.getChildren().get(lineWidthsBoxIdx);
                    TextField lineWidthsText = (TextField) EDAmameController.GetNodeById(lineWidthsBox.getChildren(), "lineWidths");

                    if (lineWidthsText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"lineWidths\" node in Symbol Editor properties window \"lineWidthsBox\" entry!");

                    String widthStr = lineWidthsText.getText();

                    if (EDAmameController.IsStringNum(widthStr))
                    {
                        Double newWidth = Double.parseDouble(widthStr);

                        if ((newWidth >= EDAmameController.Editor_LineWidthMin) && (newWidth <= EDAmameController.Editor_LineWidthMax))
                        {
                            ((Line)renderNode.node).setStrokeWidth(newWidth);
                        }
                        else
                        {
                            EDAmameController.SetStatusBar("Unable to apply line widths because the entered field is outside the limits! (Width limits: " + EDAmameController.Editor_LineWidthMin + ", " + EDAmameController.Editor_LineWidthMax + ")");
                        }
                    }
                    else if (!widthStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply line widths because the entered field is non-numeric!");
                    }
                }
            }
            // Applying texts...
            else if (renderNode.node.getClass() == Text.class)
            {
                Integer contentBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "textContentBox");

                if (contentBoxIdx != -1)
                {
                    HBox contentBox = (HBox)propsBox.getChildren().get(contentBoxIdx);
                    TextField contentText = (TextField)EDAmameController.GetNodeById(contentBox.getChildren(), "textContent");

                    if (contentText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"textContent\" node in global properties window \"textContentBox\" entry!");

                    String content = contentText.getText();

                    if (!content.isEmpty())
                    {
                        if (!content.equals("<mixed>"))
                            ((Text)renderNode.node).setText(content);
                    }
                    else
                    {
                        EDAmameController.SetStatusBar("Unable to apply text contents because the entered field is empty!");
                    }
                }

                Integer fontSizeBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "fontSizeBox");

                if (fontSizeBoxIdx != -1)
                {
                    HBox fontSizeBox = (HBox)propsBox.getChildren().get(fontSizeBoxIdx);
                    TextField fontSizeText = (TextField)EDAmameController.GetNodeById(fontSizeBox.getChildren(), "fontSize");

                    if (fontSizeText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"fontSize\" node in global properties window \"fontSizeBox\" entry!");

                    String fontSizeStr = fontSizeText.getText();

                    if (EDAmameController.IsStringNum(fontSizeStr))
                    {
                        double fontSize = Double.parseDouble(fontSizeStr);

                        if (((fontSize >= EDAmameController.Editor_TextFontSizeMin) && (fontSize <= EDAmameController.Editor_TextFontSizeMax)))
                        {
                            ((Text)renderNode.node).setFont(new Font("Arial", fontSize));
                        }
                        else
                        {
                            EDAmameController.SetStatusBar("Unable to apply text font size because the entered field is is outside the limits! (Font size limits: " + EDAmameController.Editor_TextFontSizeMin + ", " + EDAmameController.Editor_TextFontSizeMax + ")");
                        }
                    }
                    else if (!fontSizeStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply text font size because the entered field is non-numeric!");
                    }
                }
            }
            // Applying pins...
            else if (renderNode.isPin)
            {
                Group group = (Group)renderNode.node;

                if (group.getChildren().size() != 2)
                    throw new java.lang.Error("ERROR: Attempting to apply pin from symbol properties window without 2 children!");
                if (group.getChildren().get(1).getClass() != Text.class)
                    throw new java.lang.Error("ERROR: Attempting to apply pin from symbol properties window without a text node!");

                Integer pinLabelBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "pinLabelBox");

                if (pinLabelBoxIdx != -1)
                {
                    HBox pinLabelBox = (HBox)propsBox.getChildren().get(pinLabelBoxIdx);
                    TextField pinLabelText = (TextField)EDAmameController.GetNodeById(pinLabelBox.getChildren(), "pinLabels");

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
                        EDAmameController.SetStatusBar("Unable to apply pin label because the entered field is empty!");
                    }
                }
            }

            // Applying borders...
            if (renderNode.node.getClass() != Line.class &&
                renderNode.node.getClass() != Text.class &&
                !renderNode.isPin)
            {
                Integer strokeWidthBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "strokesWidthBox");

                if (strokeWidthBoxIdx != -1)
                {
                    HBox strokeWidthBox = (HBox)propsBox.getChildren().get(strokeWidthBoxIdx);
                    TextField strokeWidthText = (TextField) EDAmameController.GetNodeById(strokeWidthBox.getChildren(), "strokeWidth");

                    if (strokeWidthText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"strokeWidth\" node in Symbol Editor properties window \"strokesWidthBox\" entry!");

                    String strokeWidthStr = strokeWidthText.getText();

                    if (EDAmameController.IsStringNum(strokeWidthStr))
                    {
                        Double newStrokeWidth = Double.parseDouble(strokeWidthStr);

                        if ((newStrokeWidth >= EDAmameController.Editor_BorderMin) && (newStrokeWidth <= EDAmameController.Editor_BorderMax))
                        {
                            ((Shape)renderNode.node).setStrokeWidth(newStrokeWidth);
                        }
                        else
                        {
                            EDAmameController.SetStatusBar("Unable to apply shape border width because the entered field is outside the limits! (Border width limits: " + EDAmameController.Editor_BorderMin + ", " + EDAmameController.Editor_BorderMax + ")");
                        }
                    }
                    else if (!strokeWidthStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply shape border width because the entered field is non-numeric!");
                    }
                }

                Integer strokeBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "strokeColorBox");

                if (strokeBoxIdx != -1)
                {
                    HBox strokeBox = (HBox) propsBox.getChildren().get(strokeBoxIdx);
                    ColorPicker colorPicker = (ColorPicker)EDAmameController.GetNodeById(strokeBox.getChildren(), "strokeColor");

                    if (colorPicker == null)
                        throw new java.lang.Error("ERROR: Unable to find \"strokeColor\" node in Symbol Editor properties window \"strokeColorBox\" entry!");

                    Color color = colorPicker.getValue();

                    if ((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                    {
                        if (renderNode.node.getClass() != Line.class)
                            ((Shape)renderNode.node).setStroke(color);
                    }
                    else
                    {
                        if (color != null)
                            EDAmameController.SetStatusBar("Unable to apply shape border color because the entered color is transparent!");
                    }
                }
            }
        }
    }

    //// SUPPORT FUNCTIONS ////

    public static boolean CheckShapeTransparency(String strokeSize, Paint strokeColor, Color fillColor)
    {
        if (EDAmameController.IsStringNum(strokeSize))
        {
            double strokeDouble = Double.parseDouble(strokeSize);
            if ((strokeDouble == 0) || (!strokeColor.isOpaque()))
            {
                if (!((fillColor != null) && (fillColor != Color.TRANSPARENT) && (fillColor.hashCode() != 0x00000000)) || (fillColor.getOpacity() != 1.0))
                {
                    EDAmameController.SetStatusBar("Unable to drop shape because the entered fill and border is transparent!");
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

    public static boolean CheckStringBorderSize(String checkString)
    {
        if (EDAmameController.IsStringNum(checkString))
        {
            double checkDouble = Double.parseDouble(checkString);
            if (((checkDouble >= EDAmameController.Editor_BorderMin) && (checkDouble <= EDAmameController.Editor_BorderMax)))
            {
                return true;
            }
            else
            {
                EDAmameController.SetStatusBar("Unable to drop shape because the entered border size field is outside the limits! (Length limits: " + EDAmameController.Editor_BorderMin + ", " + EDAmameController.Editor_BorderMax + ")");
                return false;
            }
        }
        else
        {
            EDAmameController.SetStatusBar("Unable to drop shape because the entered border size is non-numeric!");
            return false;
        }
    }

    public static boolean CheckStringCircleBounds(String checkString)
    {
        if (EDAmameController.IsStringNum(checkString))
        {
            double checkDouble = Double.parseDouble(checkString);
            if (((checkDouble >= EDAmameController.Editor_CircleRadiusMin) && (checkDouble <= EDAmameController.Editor_CircleRadiusMax)))
            {
                return true;
            }
            else
            {
                EDAmameController.SetStatusBar("Unable to drop circle because the entered radius field is outside the limits! (Radius limits: " + EDAmameController.Editor_CircleRadiusMin + ", " + EDAmameController.Editor_CircleRadiusMax + ")");
                return false;
            }
        }
        else
        {
            EDAmameController.SetStatusBar("Unable to drop circle because the entered radius field is non-numeric!");
            return false;
        }
    }

    public static boolean CheckStringRectBounds(String checkWidth, String checkHeight)
    {
        if ((EDAmameController.IsStringNum(checkWidth)) && (EDAmameController.IsStringNum(checkHeight)))
        {
            double doubleWidth = Double.parseDouble(checkWidth);
            double doubleHeight = Double.parseDouble(checkHeight);
            if (!((doubleWidth >= EDAmameController.Editor_RectWidthMin) && (doubleWidth <= EDAmameController.Editor_RectWidthMax)))
            {
                EDAmameController.SetStatusBar("Unable to drop rectangle because the entered width or height field is outside the limits! (Width limits: " + EDAmameController.Editor_RectWidthMin + ", " + EDAmameController.Editor_RectWidthMax + " | Height limits: " + EDAmameController.Editor_RectHeightMin + ", " + EDAmameController.Editor_RectHeightMax + ")");
                return false;
            }
            else if (!((doubleHeight >= EDAmameController.Editor_RectHeightMin) && (doubleHeight <= EDAmameController.Editor_RectHeightMax)))
            {
                EDAmameController.SetStatusBar("Unable to drop rectangle because the entered width or height field is outside the limits! (Width limits: " + EDAmameController.Editor_RectWidthMin + ", " + EDAmameController.Editor_RectWidthMax + " | Height limits: " + EDAmameController.Editor_RectHeightMin + ", " + EDAmameController.Editor_RectHeightMax + ")");
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            EDAmameController.SetStatusBar("Unable to drop rectangle because the entered width or height field is non-numeric!");
            return false;
        }
    }

    public static boolean CheckStringTriBounds(String checkString)
    {
        if (EDAmameController.IsStringNum(checkString))
        {
            double checkDouble = Double.parseDouble(checkString);
            if (((checkDouble >= EDAmameController.Editor_TriLenMin) && (checkDouble <= EDAmameController.Editor_TriLenMax)))
            {
                return true;
            }
            else
            {
                EDAmameController.SetStatusBar("Unable to drop triangle because the entered length field is outside the limits! (Length limits: " + EDAmameController.Editor_TriLenMin + ", " + EDAmameController.Editor_TriLenMax + ")");
                return false;
            }
        }
        else
        {
            EDAmameController.SetStatusBar("Unable to drop triangle because the entered length field is non-numeric!");
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
