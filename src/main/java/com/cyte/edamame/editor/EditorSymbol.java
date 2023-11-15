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

import com.cyte.edamame.util.ShapeMemento;
import com.cyte.edamame.util.ShapeOriginator;
import com.cyte.edamame.util.ShapeRecorder;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
    @FXML
    private Pane drawingPane;

    private ShapeOriginator originator;
    private ShapeRecorder recorder;

    //// MAIN FUNCTIONS ////

    /**
     * Factory to create a single EditorSymbol and its UI attached to a particular symbol library.
     *
     * @throws IOException if there are problems loading the scene from FXML resources.
     */
    @FXML
        public void initialize() {
                System.out.println("I was initialized, the button was " + this.EditorSymbol_InnerButton);

            originator = new ShapeOriginator();
            recorder = new ShapeRecorder();

            // Add an initial shape to the drawingPane
            Circle initialCircle = createCircle(50, 50, 30, Color.BLUE);
            drawingPane.getChildren().add(initialCircle);

            // Save the initial state
            originator.setState(initialCircle);
            recorder.addMemento(originator.saveStateToMemento());
        }

        @FXML
        private void handleKeyPress(KeyEvent event) {
            if (event.isControlDown() && event.getCode().equals(KeyCode.Z)) {
                undo();
            } else if (event.isControlDown() && event.getCode().equals(KeyCode.Y)) {
                redo();
            }
        }

        // Add your shape manipulation methods here...

        private void undo() {
            if (recorder.getMementoList().size() > 1) {
                ShapeMemento memento = (ShapeMemento) recorder.getMementoList().get(recorder.getMementoList().size() - 2);
                recorder.getMementoList().remove(recorder.getMementoList().size() - 1);
                originator.restoreStateFromMemento(memento);

                // Update the drawingPane with the restored state
                drawingPane.getChildren().setAll(originator.getState());
            }
        }

        private void redo() {
            if (recorder.getMementoList().size() < 2) {
                return; // No redo available
            }

            ShapeMemento memento = (ShapeMemento) recorder.getMementoList().get(recorder.getMementoList().size() - 1);
            originator.restoreStateFromMemento(memento);

            // Update the drawingPane with the restored state
            drawingPane.getChildren().setAll(originator.getState());
        }

        private Circle createCircle(double centerX, double centerY, double radius, Color color) {
            Circle circle = new Circle(centerX, centerY, radius, color);
            // Add event handlers for shape manipulation as needed
            return circle;
        }
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
                                if (!EDAmameController.Controller_IsStringNum(stringRadius))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop circle because the entered radius field is non-numeric!");
                                    return;
                                }
                                double radius = Double.parseDouble(stringRadius);
                                if (!((radius >= EDAmameController.Editor_CircleRadiusMin) && (radius <= EDAmameController.Editor_CircleRadiusMax)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop circle because the entered radius field is outside the limits! (Radius limits: " + EDAmameController.Editor_CircleRadiusMin + ", " + EDAmameController.Editor_CircleRadiusMax + ")");
                                    return;
                                }

                                Color fillColor = this.EditorSymbol_CircleColor.getValue();
                                if (!((fillColor != Color.TRANSPARENT) && (fillColor.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop circle because the entered color field is transparent!");
                                    return;
                                }

                                String stringStrokeSize = this.EditorSymbol_CircleBorderSize.getText();
                                if (!EDAmameController.Controller_IsStringNum(stringStrokeSize))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop circle because the entered border size is non-numeric!");
                                    return;
                                }
                                double strokeSize = Double.parseDouble(stringStrokeSize);
                                if (!((strokeSize >= EDAmameController.Editor_BorderMin) && (strokeSize <= EDAmameController.Editor_BorderMax)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop circle because the entered border size field is outside the limits! (Length limits: " + EDAmameController.Editor_BorderMin + ", " + EDAmameController.Editor_BorderMax + ")");
                                    return;
                                }

                                Paint strokeColor = this.EditorSymbol_CircleBorderColor.getValue();
                                if (!((strokeColor != Color.TRANSPARENT) && (strokeColor.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop circle because the entered border color field is transparent!");
                                    return;
                                }

                                Circle circle = new Circle(radius, fillColor);

                                circle.setTranslateX(dropPos.GetLeftDouble());
                                circle.setTranslateY(dropPos.GetRightDouble());

                                circle.setStroke(strokeColor);
                                circle.setStrokeWidth(strokeSize);

                                RenderNode renderNode = new RenderNode("Circle", circle, true, null, false, this.Editor_RenderSystem);
                                this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);


                            }
                            else if (selectedShapeButton.getText().equals("Rectangle"))
                            {
                                EDAmameController.Controller_SetStatusBar("EDAmame Status Area");

                                String stringWidth = this.EditorSymbol_RectangleWidth.getText();
                                String stringHeight = this.EditorSymbol_RectangleHeight.getText();
                                if (!(EDAmameController.Controller_IsStringNum(stringWidth) && EDAmameController.Controller_IsStringNum(stringHeight)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop rectangle because the entered width or height field is non-numeric!");
                                    return;
                                }
                                double width = Double.parseDouble(stringWidth);
                                double height = Double.parseDouble(stringHeight);
                                if (!(((width >= EDAmameController.Editor_RectWidthMin) && (width <= EDAmameController.Editor_RectWidthMax)) &&
                                        ((height >= EDAmameController.Editor_RectHeightMin) && (height <= EDAmameController.Editor_RectHeightMax))))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop rectangle because the entered width or height field is outside the limits! (Width limits: " + EDAmameController.Editor_RectWidthMin + ", " + EDAmameController.Editor_RectWidthMax + " | Height limits: " + EDAmameController.Editor_RectHeightMin + ", " + EDAmameController.Editor_RectHeightMax + ")");
                                    return;
                                }

                                Color fillColor = this.EditorSymbol_RectangleColor.getValue();
                                if (!((fillColor != Color.TRANSPARENT) && (fillColor.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop rectangle because the entered color field is transparent!");
                                    return;
                                }

                                String stringStrokeSize = this.EditorSymbol_RectangleBorderSize.getText();
                                if (!EDAmameController.Controller_IsStringNum(stringStrokeSize))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop rectangle because the entered border size is non-numeric!");
                                    return;
                                }
                                double strokeSize = Double.parseDouble(stringStrokeSize);
                                if (!((strokeSize >= EDAmameController.Editor_BorderMin) && (strokeSize <= EDAmameController.Editor_BorderMax)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop rectangle because the entered border size field is outside the limits! (Length limits: " + EDAmameController.Editor_BorderMin + ", " + EDAmameController.Editor_BorderMax + ")");
                                    return;
                                }

                                Paint strokeColor = this.EditorSymbol_RectangleBorderColor.getValue();
                                if (!((strokeColor != Color.TRANSPARENT) && (strokeColor.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop rectangle because the entered border color field is transparent!");
                                    return;
                                }

                                Rectangle rectangle = new Rectangle(width, height, fillColor);

                                rectangle.setTranslateX(dropPos.GetLeftDouble());
                                rectangle.setTranslateY(dropPos.GetRightDouble());

                                rectangle.setStroke(strokeColor);
                                rectangle.setStrokeWidth(strokeSize);

                                RenderNode renderNode = new RenderNode("Rectangle", rectangle, true, null, false, this.Editor_RenderSystem);
                                this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);

                            }
                            else if (selectedShapeButton.getText().equals("Triangle"))
                            {
                                EDAmameController.Controller_SetStatusBar("EDAmame Status Area");

                                String stringMiddleHeight = this.EditorSymbol_TriangleHeight.getText();
                                if (!EDAmameController.Controller_IsStringNum(stringMiddleHeight))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop triangle because the entered length field is non-numeric!");
                                    return;
                                }
                                double middleLength = Double.parseDouble(stringMiddleHeight);
                                if (!((middleLength >= EDAmameController.Editor_TriLenMin) && (middleLength <= EDAmameController.Editor_TriLenMax)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop triangle because the entered length field is outside the limits! (Length limits: " + EDAmameController.Editor_TriLenMin + ", " + EDAmameController.Editor_TriLenMax + ")");
                                    return;
                                }

                                Color fillColor = this.EditorSymbol_TriangleColor.getValue();
                                if (!((fillColor != Color.TRANSPARENT) && (fillColor.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop triangle because the entered fill color field is transparent!");
                                    return;
                                }

                                String stringStrokeSize = this.EditorSymbol_TriangleBorderSize.getText();
                                if (!EDAmameController.Controller_IsStringNum(stringStrokeSize))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop triangle because the entered border size is non-numeric!");
                                    return;
                                }
                                double strokeSize = Double.parseDouble(stringStrokeSize);
                                if (!((strokeSize >= EDAmameController.Editor_BorderMin) && (strokeSize <= EDAmameController.Editor_BorderMax)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop triangle because the entered border size field is outside the limits! (Length limits: " + EDAmameController.Editor_BorderMin + ", " + EDAmameController.Editor_BorderMax + ")");
                                    return;
                                }

                                Paint strokeColor = this.EditorSymbol_TriangleBorderColor.getValue();
                                if (!((strokeColor != Color.TRANSPARENT) && (strokeColor.hashCode() != 0x00000000)))
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop triangle because the entered border color field is transparent!");
                                    return;
                                }

                                Polygon triangle = new Polygon();
                                triangle.getPoints().setAll(-middleLength / 2, middleLength / 2,
                                        middleLength / 2, middleLength / 2,
                                        0.0, -middleLength / 2);
                                triangle.setFill(fillColor);

                                triangle.setTranslateX(dropPos.GetLeftDouble());
                                triangle.setTranslateY(dropPos.GetRightDouble());

                                triangle.setStroke(strokeColor);
                                triangle.setStrokeWidth(strokeSize);

                                RenderNode renderNode = new RenderNode("Triangle", triangle, true, null, false, this.Editor_RenderSystem);
                                this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
                            }
                            else if (selectedShapeButton.getText().equals("Line"))
                            {
                                // If we're starting the line drawing...
                                if (this.EditorSymbol_LinePreview == null)
                                {
                                    String stringWidth = this.EditorSymbol_LineWidth.getText();
                                    Color color = this.EditorSymbol_LineColor.getValue();

                                    if (EDAmameController.Controller_IsStringNum(stringWidth))
                                    {
                                        double width = Double.parseDouble(stringWidth);

                                        if (((width >= EDAmameController.Editor_LineWidthMin) && (width <= EDAmameController.Editor_LineWidthMax)))
                                        {
                                            if ((color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
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
                                            else
                                            {
                                                EDAmameController.Controller_SetStatusBar("Unable to drop line because the entered color field is transparent!");
                                            }
                                        }
                                        else
                                        {
                                            EDAmameController.Controller_SetStatusBar("Unable to drop line because the entered width is outside the limits! (Width limits: " + EDAmameController.Editor_LineWidthMin + ", " + EDAmameController.Editor_LineWidthMax + ")");
                                        }
                                    }
                                    else
                                    {
                                        EDAmameController.Controller_SetStatusBar("Unable to drop line because the entered width field is non-numeric!");
                                    }
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Text"))
                            {
                                String stringTextContent = EditorSymbol_TextContent.getText();

                                if (!stringTextContent.isEmpty())
                                {
                                    String stringFontSize = this.EditorSymbol_TextSize.getText();

                                    if (EDAmameController.Controller_IsStringNum(stringFontSize))
                                    {
                                        double fontSize = Double.parseDouble(stringFontSize);
                                        Color color = this.EditorSymbol_TextColor.getValue();

                                        if (((fontSize >= EDAmameController.Editor_TextFontSizeMin) && (fontSize <= EDAmameController.Editor_TextFontSizeMax)))
                                        {
                                            if ((color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                                            {
                                                Label text = new Label(stringTextContent);
                                                text.setFont(new Font("Arial", fontSize));
                                                text.setTextFill(color);

                                                text.setTranslateX(dropPos.GetLeftDouble());
                                                text.setTranslateY(dropPos.GetRightDouble());

                                                RenderNode renderNode = new RenderNode("Text", text, true, null, false, this.Editor_RenderSystem);
                                                this.Editor_RenderSystem.RenderSystem_NodeAdd(renderNode);
                                            }
                                            else
                                            {
                                                EDAmameController.Controller_SetStatusBar("Unable to drop text because the entered font color field is transparent!");
                                            }
                                        }
                                        else
                                        {
                                            EDAmameController.Controller_SetStatusBar("Unable to drop text because the entered font size field is outside the limits! (Font size limits: " + EDAmameController.Editor_TextFontSizeMin + ", " + EDAmameController.Editor_TextFontSizeMax + ")");
                                        }
                                    }
                                    else
                                    {
                                        EDAmameController.Controller_SetStatusBar("Unable to drop text because the entered font size field is non-numeric!");
                                    }
                                }
                                else
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop text because the entered text field is empty!");
                                }
                            }
                            else if (selectedShapeButton.getText().equals("Pin"))
                            {
                                String stringPinLabel = this.EditorSymbol_PinLabel.getText();

                                if (!stringPinLabel.isEmpty())
                                {
                                    String stringPinRadius = this.EditorSymbol_PinRadius.getText();

                                    if (EDAmameController.Controller_IsStringNum(stringPinRadius))
                                    {
                                        double pinRadius = Double.parseDouble(stringPinRadius);
                                        Color pinColor = this.EditorSymbol_PinColor.getValue();

                                        if (((pinRadius >= EDAmameController.Editor_PinRadiusMin) && (pinRadius <= EDAmameController.Editor_PinRadiusMax)))
                                        {
                                            if ((pinColor != Color.TRANSPARENT) && (pinColor.hashCode() != 0x00000000))
                                            {
                                                Group pin = new Group();
                                                pin.setId("PIN_" + stringPinLabel);

                                                Circle pinCircle = new Circle(pinRadius, pinColor);

                                                Label pinLabel = new Label(stringPinLabel);
                                                pinLabel.setFont(new Font("Arial", EDAmameController.Editor_PinLabelFontSize));
                                                pinLabel.setTextFill(pinColor);

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
                                            else
                                            {
                                                EDAmameController.Controller_SetStatusBar("Unable to drop pin because the entered font color field is transparent!");
                                            }
                                        }
                                        else
                                        {
                                            EDAmameController.Controller_SetStatusBar("Unable to drop pin because the entered radius field is outside the limits! (Font size limits: " + EDAmameController.Editor_PinRadiusMin + ", " + EDAmameController.Editor_PinRadiusMax + ")");
                                        }
                                    }
                                    else
                                    {
                                        EDAmameController.Controller_SetStatusBar("Unable to drop pin because the entered radius field is non-numeric!");
                                    }
                                }
                                else
                                {
                                    EDAmameController.Controller_SetStatusBar("Unable to drop pin because the entered label field is empty!");
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
        LinkedList<Color> shapesColor = new LinkedList<Color>();
        LinkedList<Double> circlesRadii = new LinkedList<Double>();
        LinkedList<Double> rectsWidths = new LinkedList<Double>();
        LinkedList<Double> rectsHeights = new LinkedList<Double>();
        LinkedList<Double> trisLens = new LinkedList<Double>();
        LinkedList<Double> lineStartPosX = new LinkedList<Double>();
        LinkedList<Double> lineStartPosY = new LinkedList<Double>();
        LinkedList<Double> lineEndPosX = new LinkedList<Double>();
        LinkedList<Double> lineEndPosY = new LinkedList<Double>();
        LinkedList<Double> lineWidths = new LinkedList<Double>();

        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            if (!renderNode.RenderNode_Selected)
                continue;

            if (renderNode.RenderNode_Node.getClass() != Label.class)
            {
                if (renderNode.RenderNode_Node.getClass() == Line.class)
                {
                    shapesColor.add((Color)((Line)renderNode.RenderNode_Node).getStroke());
                }
                else
                {
                    shapesColor.add((Color)((Shape)renderNode.RenderNode_Node).getFill());
                }

                needHeader = true;
            }

            if (renderNode.RenderNode_Node.getClass() == Circle.class)
            {
                circlesRadii.add(((Circle)renderNode.RenderNode_Node).getRadius());
            }
            else if (renderNode.RenderNode_Node.getClass() == Rectangle.class)
            {
                rectsWidths.add(((Rectangle)renderNode.RenderNode_Node).getWidth());
                rectsHeights.add(((Rectangle)renderNode.RenderNode_Node).getHeight());
            }
            else if (renderNode.RenderNode_Node.getClass() == Polygon.class)
            {
                trisLens.add(((Polygon)renderNode.RenderNode_Node).getPoints().get(2) - ((Polygon) renderNode.RenderNode_Node).getPoints().get(0));
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

        // Creating color box...
        if (!shapesColor.isEmpty())
        {
            HBox colorHBox = new HBox(10);
            colorHBox.setId("colorBox");
            colorHBox.getChildren().add(new Label("Shape Colors: "));
            ColorPicker colorPicker = new ColorPicker();
            colorPicker.setId("color");
            colorHBox.getChildren().add(colorPicker);

            if (EDAmameController.Controller_IsListAllEqual(shapesColor))
                colorPicker.setValue(shapesColor.get(0));
            else
                colorPicker.setValue(Color.TRANSPARENT);

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(colorHBox);
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

            // Applying color...
            if (renderNode.RenderNode_Node.getClass() != Label.class)
            {
                Integer colorBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "colorBox");

                if (colorBoxIdx != -1)
                {
                    HBox colorBox = (HBox) propsBox.getChildren().get(colorBoxIdx);
                    ColorPicker colorPicker = (ColorPicker) EDAmameController.Controller_GetNodeById(colorBox.getChildren(), "color");

                    if (colorPicker == null)
                        throw new java.lang.Error("ERROR: Unable to find \"color\" node in Symbol Editor properties window \"colorBox\" entry!");

                    Color color = colorPicker.getValue();

                    if ((color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                    {
                        if (renderNode.RenderNode_Node.getClass() == Line.class)
                        {
                            ((Shape)renderNode.RenderNode_Node).setStroke(color);
                        }
                        else
                        {
                            ((Shape)renderNode.RenderNode_Node).setFill(color);
                        }
                    }
                    else
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply shape colors because the entered color is transparent!");
                    }
                }
            }

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
