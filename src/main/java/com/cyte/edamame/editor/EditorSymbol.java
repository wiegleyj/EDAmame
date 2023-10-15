/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.EDAmame;
import com.cyte.edamame.render.RenderShape;

import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;

/**
 * Editor for maintaining Symbol libraries.
 */
public class EditorSymbol extends Editor
{
    //// GLOBAL VARIABLES ////

    @FXML
    private Button EditorSymbol_InnerButton;
    @FXML
    public ToggleGroup EditorSymbol_ShapeToggleGroup;
    @FXML
    public ColorPicker EditorSymbol_CircleColor;
    @FXML
    public TextField EditorSymbol_CircleRadius;
    @FXML
    public ColorPicker EditorSymbol_RectangleColor;
    @FXML
    public TextField EditorSymbol_RectangleWidth;
    @FXML
    public TextField EditorSymbol_RectangleHeight;
    @FXML
    public ColorPicker EditorSymbol_TriangleColor;
    @FXML
    public TextField EditorSymbol_TriangleHeight;

    //// MAIN FUNCTIONS ////

    /**
     * Factory to create a single EditorSymbol and its UI attached to a particular symbol library.
     *
     * @throws IOException if there are problems loading the scene from FXML resources.
     */
    static public Editor create() throws IOException
    {
        // Loading FXML file for the symbol editor
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorSymbol.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorSymbol editor = fxmlLoader.getController();
        editor.Editor_Name = "EditorSymbol";
        editor.Editor_Dissect(0, scene);
        editor.Editor_RenderSystem.RenderSystem_CanvasRenderGrid();

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

    public void Editor_ViewportOnDragOver(DragEvent event)
    {
        //System.out.println("Symbol dragged over!");

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

    public void Editor_ViewportOnDragDropped(DragEvent event)
    {
        //System.out.println("Symbol drag dropped!");

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

    public void Editor_ViewportOnMouseMoved(MouseEvent event)
    {
        //System.out.println("Symbol mouse moved!");

        //this.EditorSchematic_CheckSymbolsDroppedMouseHighlights(new PairMutable(event.getX(), event.getY()));
    }

    public void Editor_ViewportOnMousePressed(MouseEvent event)
    {
        //System.out.println("Symbol mouse pressed!");

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

    public void Editor_ViewportOnMouseReleased(MouseEvent event)
    {
        //System.out.println("Symbol mouse released!");

        if (this.Editor_PressedLMB)
        {
            // Handling shape dropping (only if we're not hovering over, selecting, moving any shapes or box selecting)
            if ((this.Editor_RenderSystem.shapesHighlighted == 0) &&
                (this.Editor_RenderSystem.shapesSelected == 0) &&
                !this.Editor_RenderSystem.shapesMoving &&
                this.Editor_RenderSystem.selectionBox == null)
            {
                PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
                PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

                // Only dropping the shape within the theater limits...
                if ((realPos.GetLeftDouble() > -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2) &&
                    (realPos.GetLeftDouble() < EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2) &&
                    (realPos.GetRightDouble() > -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2) &&
                    (realPos.GetRightDouble() < EDAmameController.Editor_TheaterSize.GetRightDouble() / 2))
                {
                    RadioButton selectedShapeButton = (RadioButton) EditorSymbol_ShapeToggleGroup.getSelectedToggle();

                    if ((selectedShapeButton != null) && (!this.Editor_RenderSystem.pressedOnShape))
                    {
                        if (selectedShapeButton.getText().equals("Circle"))
                        {
                            String stringRadius = this.EditorSymbol_CircleRadius.getText();

                            if (EDAmameController.Controller_IsStringNum(stringRadius))
                            {
                                double width = Double.parseDouble(stringRadius) * 2;
                                Color color = this.EditorSymbol_CircleColor.getValue();

                                if (width >= 1.0)
                                {
                                    Circle circle = new Circle(width, color);

                                    circle.setTranslateX(dropPos.GetLeftDouble());
                                    circle.setTranslateY(dropPos.GetRightDouble());

                                    RenderShape shape = new RenderShape("Circle", circle);
                                    this.Editor_RenderSystem.RenderSystem_ShapeAdd(shape);
                                }
                            }
                        }
                        else if (selectedShapeButton.getText().equals("Rectangle"))
                        {
                            String stringWidth = this.EditorSymbol_RectangleWidth.getText();
                            String stringHeight = this.EditorSymbol_RectangleHeight.getText();

                            if (EDAmameController.Controller_IsStringNum(stringWidth) && EDAmameController.Controller_IsStringNum(stringHeight))
                            {
                                double width = Double.parseDouble(stringWidth);
                                double height = Double.parseDouble(stringHeight);
                                Color color = this.EditorSymbol_RectangleColor.getValue();

                                if ((width >= 1.0) && (height >= 1.0))
                                {
                                    Rectangle rectangle = new Rectangle(width, height, color);

                                    rectangle.setTranslateX(dropPos.GetLeftDouble() - width / 2);
                                    rectangle.setTranslateY(dropPos.GetRightDouble() - height / 2);

                                    RenderShape shape = new RenderShape("Rectangle", rectangle);
                                    this.Editor_RenderSystem.RenderSystem_ShapeAdd(shape);
                                }
                            }

                        }
                        else if (selectedShapeButton.getText().equals("Triangle"))
                        {
                            String stringMiddleHeight = this.EditorSymbol_TriangleHeight.getText();

                            if (EDAmameController.Controller_IsStringNum(stringMiddleHeight))
                            {
                                double middleLength = Double.parseDouble(stringMiddleHeight);
                                Color color = this.EditorSymbol_TriangleColor.getValue();

                                if (middleLength >= 1.0)
                                {
                                    Polygon triangle = new Polygon();
                                    triangle.getPoints().setAll(-middleLength / 2, middleLength / 2,
                                            middleLength / 2, middleLength / 2,
                                            0.0,
                                            -middleLength / 2);
                                    triangle.setFill(color);

                                    triangle.setTranslateX(dropPos.GetLeftDouble());
                                    triangle.setTranslateY(dropPos.GetRightDouble());

                                    RenderShape shape = new RenderShape("Triangle", triangle);
                                    this.Editor_RenderSystem.RenderSystem_ShapeAdd(shape);
                                }
                            }
                        }
                        else
                        {
                            throw new java.lang.Error("ERROR: Attempt to drop an unrecognized shape in a Symbol Editor!");
                        }
                    }
                }
            }

            /*// Handling wire drawing
            this.EditorSchematic_WireDrawingType = this.EditorSchematic_GetSelectedWire();

            if (this.EditorSchematic_WireDrawingType != -1)
            {
                // TODO
            }
            // Handling symbol moving (only if we're not drawing wires)
            else if (this.EditorSchematic_SymbolsMoving)
            {
                this.EditorSchematic_SymbolsMoving = false;
            }
            // Handling symbol selection & deselection (only if we're not drawing wires & not moving any symbols)
            else
            {
                for (int i = 0; i < this.EditorSchematic_ViewportSymbolsDropped.size(); i++)
                {
                    EditorSchematic_Symbol symbol = this.EditorSchematic_ViewportSymbolsDropped.get(i);

                    if (symbol.highlightedMouse)
                    {
                        if (this.EditorSchematic_SymbolPressedIDs.contains(symbol.id))
                            symbol.selected = true;
                    }
                    else if (symbol.highlightedBox)
                    {
                        symbol.selected = true;
                        symbol.highlightedBox = false;
                    }
                    else
                    {
                        if (symbol.selected)
                            if (!this.EditorSchematic_IsKeyPressed(KeyCode.SHIFT))
                                symbol.selected = false;
                    }

                    this.EditorSchematic_ViewportSymbolsDropped.set(i, symbol);
                }

                this.EditorSchematic_SymbolPressedIDs.clear();
            }

            // Removing the selection box from shape queue
            if (this.EditorSchematic_RenderSystem.shapes.contains(this.EditorSchematic_SymbolSelectionBoxShape))
            {
                Integer idx = this.EditorSchematic_RenderSystem.shapes.indexOf(this.EditorSchematic_SymbolSelectionBoxShape);

                //this.EditorSchematic_RenderSystem.shapesPosReal.remove(this.EditorSchematic_RenderSystem.shapesPosReal.get(idx));
                //this.EditorSchematic_RenderSystem.shapes.remove(this.EditorSchematic_SymbolSelectionBoxShape);
                this.EditorSchematic_RenderSystem.RemoveShape(idx);
            }*/
        }
        else if (this.Editor_PressedRMB)
        {}
    }

    public void Editor_ViewportOnMouseDragged(MouseEvent event)
    {
        //System.out.println("Symbol mouse dragged!");

        if (this.Editor_PressedLMB)
        {
            /*// Handling the symbol moving (should only trigger if we got symbols selected)
            this.EditorSchematic_SymbolsMoving = false;

            for (int i = 0; i < this.EditorSchematic_ViewportSymbolsDropped.size(); i++)
            {
                EditorSchematic_Symbol symbol = this.EditorSchematic_ViewportSymbolsDropped.get(i);

                if (!symbol.selected)
                    continue;

                PairMutable theaterEdges = new PairMutable(new PairMutable(-this.EditorSchematic_TheaterSize.GetLeftDouble() / 2 + this.EditorSchematic_ViewportCenter.GetLeftDouble(),
                        this.EditorSchematic_TheaterSize.GetLeftDouble() / 2 + this.EditorSchematic_ViewportCenter.GetLeftDouble()),
                        new PairMutable(-this.EditorSchematic_TheaterSize.GetLeftDouble() / 2 + this.EditorSchematic_ViewportCenter.GetRightDouble(),
                                this.EditorSchematic_TheaterSize.GetLeftDouble() / 2 + this.EditorSchematic_ViewportCenter.GetRightDouble()));
                PairMutable symbolNewPos = new PairMutable(symbol.posMousePress.GetLeftDouble() + mouseDiffPos.GetLeftDouble(),
                        symbol.posMousePress.GetRightDouble() + mouseDiffPos.GetRightDouble());
                PairMutable symbolNewEdges = new PairMutable(new PairMutable(symbolNewPos.GetLeftDouble() - symbol.shape.boundingBox.GetLeftDouble() / 2,
                        symbolNewPos.GetLeftDouble() + symbol.shape.boundingBox.GetLeftDouble() / 2),
                        new PairMutable(symbolNewPos.GetRightDouble() - symbol.shape.boundingBox.GetRightDouble() / 2,
                                symbolNewPos.GetRightDouble() + symbol.shape.boundingBox.GetRightDouble() / 2));
                PairMutable symbolNewPosOffset = new PairMutable(0.0, 0.0);

                if (symbolNewEdges.GetLeftPair().GetLeftDouble() < theaterEdges.GetLeftPair().GetLeftDouble())
                    symbolNewPosOffset.left = theaterEdges.GetLeftPair().GetLeftDouble() - symbolNewEdges.GetLeftPair().GetLeftDouble();
                if (symbolNewEdges.GetLeftPair().GetRightDouble() > theaterEdges.GetLeftPair().GetRightDouble())
                    symbolNewPosOffset.left = theaterEdges.GetLeftPair().GetRightDouble() - symbolNewEdges.GetLeftPair().GetRightDouble();
                if (symbolNewEdges.GetRightPair().GetLeftDouble() < theaterEdges.GetRightPair().GetLeftDouble())
                    symbolNewPosOffset.right = theaterEdges.GetRightPair().GetLeftDouble() - symbolNewEdges.GetRightPair().GetLeftDouble();
                if (symbolNewEdges.GetRightPair().GetRightDouble() > theaterEdges.GetRightPair().GetRightDouble())
                    symbolNewPosOffset.right = theaterEdges.GetRightPair().GetRightDouble() - symbolNewEdges.GetRightPair().GetRightDouble();

                symbol.posReal = new PairMutable(symbolNewPos.GetLeftDouble() + symbolNewPosOffset.GetLeftDouble(),
                        symbolNewPos.GetRightDouble() + symbolNewPosOffset.GetRightDouble());

                this.EditorSchematic_ViewportSymbolsDropped.set(i, symbol);
                this.EditorSchematic_SymbolsMoving = true;
            }

            // Handling the wire drawing
            this.EditorSchematic_WireDrawingType = this.EditorSchematic_GetSelectedWire();

            if (this.EditorSchematic_WireDrawingType != -1)
            {
                // TODO
            }
            // Handling the symbol box selection (only if we're not drawing wires and not moving symbols)
            else if (!this.EditorSchematic_SymbolsMoving)
            {
                PairMutable rawMouseDiffPos = new PairMutable(mouseDiffPos);
                rawMouseDiffPos.left = rawMouseDiffPos.GetLeftDouble() * this.EditorSchematic_RenderSystem.zoom;
                rawMouseDiffPos.right = rawMouseDiffPos.GetRightDouble() * this.EditorSchematic_RenderSystem.zoom;

                this.EditorSchematic_SymbolSelectionBoxShape.points.set(0, new PairMutable(posMouse.GetLeftDouble(), posMouse.GetRightDouble()));
                this.EditorSchematic_SymbolSelectionBoxShape.points.set(1, new PairMutable(posMouse.GetLeftDouble() - rawMouseDiffPos.GetLeftDouble(), posMouse.GetRightDouble()));
                this.EditorSchematic_SymbolSelectionBoxShape.points.set(2, new PairMutable(posMouse.GetLeftDouble() - rawMouseDiffPos.GetLeftDouble(), posMouse.GetRightDouble() - rawMouseDiffPos.GetRightDouble()));
                this.EditorSchematic_SymbolSelectionBoxShape.points.set(3, new PairMutable(posMouse.GetLeftDouble(), posMouse.GetRightDouble() - rawMouseDiffPos.GetRightDouble()));

                if (!this.EditorSchematic_RenderSystem.shapes.contains(this.EditorSchematic_SymbolSelectionBoxShape))
                    this.EditorSchematic_RenderSystem.AddShape(-1, this.EditorSchematic_SymbolSelectionBoxShape, null);

                PairMutable selectionBoxExtents = new PairMutable(Math.abs(this.EditorSchematic_SymbolSelectionBoxShape.points.get(1).GetLeftDouble() - this.EditorSchematic_SymbolSelectionBoxShape.points.get(0).GetLeftDouble()),
                        Math.abs(this.EditorSchematic_SymbolSelectionBoxShape.points.get(2).GetRightDouble() - this.EditorSchematic_SymbolSelectionBoxShape.points.get(1).GetRightDouble()));
                PairMutable selectionBoxCenter = new PairMutable((this.EditorSchematic_SymbolSelectionBoxShape.points.get(1).GetLeftDouble() + this.EditorSchematic_SymbolSelectionBoxShape.points.get(0).GetLeftDouble()) / 2,
                        (this.EditorSchematic_SymbolSelectionBoxShape.points.get(2).GetRightDouble() + this.EditorSchematic_SymbolSelectionBoxShape.points.get(1).GetRightDouble()) / 2);
                PairMutable selectionBoxEdges = new PairMutable(new PairMutable(selectionBoxCenter.GetLeftDouble() - selectionBoxExtents.GetLeftDouble() / 2,
                        selectionBoxCenter.GetLeftDouble() + selectionBoxExtents.GetLeftDouble() / 2),
                        new PairMutable(selectionBoxCenter.GetRightDouble() - selectionBoxExtents.GetRightDouble() / 2,
                                selectionBoxCenter.GetRightDouble() + selectionBoxExtents.GetRightDouble() / 2));

                for (int i = 0; i < this.EditorSchematic_ViewportSymbolsDropped.size(); i++)
                {
                    EditorSchematic_Symbol symbol = this.EditorSchematic_ViewportSymbolsDropped.get(i);

                    if (symbol.passive)
                        continue;

                    PairMutable symbolBoxEdges = new PairMutable(new PairMutable(symbol.shape.posDraw.GetLeftDouble() - symbol.shape.boundingBox.GetLeftDouble() / 2,
                            symbol.shape.posDraw.GetLeftDouble() + symbol.shape.boundingBox.GetLeftDouble() / 2),
                            new PairMutable(symbol.shape.posDraw.GetRightDouble() - symbol.shape.boundingBox.GetRightDouble() / 2,
                                    symbol.shape.posDraw.GetRightDouble() + symbol.shape.boundingBox.GetRightDouble() / 2));

                    if ((selectionBoxEdges.GetLeftPair().GetRightDouble() > symbolBoxEdges.GetLeftPair().GetLeftDouble()) &&
                            (selectionBoxEdges.GetLeftPair().GetLeftDouble() < symbolBoxEdges.GetLeftPair().GetRightDouble()) &&
                            (selectionBoxEdges.GetRightPair().GetRightDouble() > symbolBoxEdges.GetRightPair().GetLeftDouble()) &&
                            (selectionBoxEdges.GetRightPair().GetLeftDouble() < symbolBoxEdges.GetRightPair().GetRightDouble()))
                    {
                        symbol.highlightedBox = true;
                    }
                    else
                    {
                        if (!this.EditorSchematic_IsKeyPressed(KeyCode.SHIFT))
                            symbol.highlightedBox = false;
                    }

                    this.EditorSchematic_ViewportSymbolsDropped.set(i, symbol);
                }
            }*/
        }
        else if (this.Editor_PressedRMB)
        {}

        // Handling symbol highlighting
        //this.EditorSchematic_CheckSymbolsDroppedMouseHighlights(new PairMutable(event.getX(), event.getY()));
    }

    public void Editor_ViewportOnScroll(ScrollEvent event)
    {

    }

    public void Editor_ViewportOnKeyPressed(KeyEvent event)
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
                propsWindow.EditorProps_Stage.setTitle("Symbol Properties");
                propsWindow.EditorProps_Stage.show();

                EDAmameController.Controller_EditorPropertiesWindow = propsWindow;
            }
        }
    }

    public void Editor_ViewportOnKeyReleased(KeyEvent event)
    {
        //System.out.println("Symbol key released!");

        //KeyCode releasedKey = event.getCode();
    }

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void Editor_ElemPropsLoad()
    {
        // Populating properties window with all selected shape properties...
        EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().clear();

        for (int i = 0; i < this.Editor_RenderSystem.shapes.size(); i++)
        {
            RenderShape shape = this.Editor_RenderSystem.shapes.get(i);

            if (!shape.selected)
                continue;

            // Creating shape section boxes...
            HBox shapeHBox = new HBox();
            shapeHBox.setId("shape_" + i + "_box");
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(shapeHBox);
            VBox shapeVBox = new VBox();
            shapeHBox.getChildren().add(shapeVBox);

            // Creating position box...
            HBox posHBox = new HBox();
            posHBox.getChildren().add(new Label("Position X: "));
            TextField posXText = new TextField(Double.toString(shape.shape.getTranslateX()));
            posXText.setMinWidth(100);
            posXText.setPrefWidth(100);
            posXText.setMaxWidth(100);
            posXText.setId("shape_" + i + "_posX");
            posHBox.getChildren().add(posXText);
            posHBox.getChildren().add(new Label("Position Y: "));
            TextField posYText = new TextField(Double.toString(shape.shape.getTranslateY()));
            posYText.setId("shape_" + i + "_posY");
            posYText.setMinWidth(100);
            posYText.setPrefWidth(100);
            posYText.setMaxWidth(100);
            posHBox.getChildren().add(posYText);
            shapeVBox.getChildren().add(posHBox);

            // Creating rotation box...
            HBox rotHBox = new HBox();
            rotHBox.getChildren().add(new Label("Rotation: "));
            TextField rotText = new TextField(Double.toString(shape.shape.getRotate()));
            rotText.setMinWidth(100);
            rotText.setPrefWidth(100);
            rotText.setMaxWidth(100);
            rotText.setId("shape_" + i + "_rotX");
            rotHBox.getChildren().add(rotText);
            shapeVBox.getChildren().add(rotHBox);

            // Creating color box...
            HBox colorHBox = new HBox();
            colorHBox.getChildren().add(new Label("Color: "));
            ColorPicker colorPicker = new ColorPicker();
            colorPicker.setId("shape_" + i + "_color");
            colorPicker.setValue((Color)shape.shape.getFill());
            colorHBox.getChildren().add(colorPicker);
            shapeVBox.getChildren().add(colorHBox);

            if (shape.shape.getClass() == Circle.class)
            {
                // Creating radius box...
                HBox radiusHBox = new HBox();
                radiusHBox.getChildren().add(new Label("Radius: "));
                TextField radiusText = new TextField(Double.toString(((Circle)shape.shape).getRadius()));
                radiusText.setMinWidth(100);
                radiusText.setPrefWidth(100);
                radiusText.setMaxWidth(100);
                radiusText.setId("shape_" + i + "_radius");
                radiusHBox.getChildren().add(radiusText);
                shapeVBox.getChildren().add(radiusHBox);
            }
            else if (shape.shape.getClass() == Rectangle.class)
            {
                // Creating width box...
                HBox widthHBox = new HBox();
                widthHBox.getChildren().add(new Label("Width: "));
                widthHBox.getChildren().add(new Label("Rectangle!"));
                shapeVBox.getChildren().add(widthHBox);

                // Creating height box...
                HBox heightHBox = new HBox();
                heightHBox.getChildren().add(new Label("Height: "));
                heightHBox.getChildren().add(new Label("Rectangle!"));
                shapeVBox.getChildren().add(heightHBox);
            }
            else if (shape.shape.getClass() == Polygon.class)
            {
                // Creating height box...
                HBox heightHBox = new HBox();
                heightHBox.getChildren().add(new Label("Height: "));
                heightHBox.getChildren().add(new Label("Triangle!"));
                shapeVBox.getChildren().add(heightHBox);
            }

            shapeVBox.getChildren().add(new Separator());
        }
    }

    public void Editor_ElemPropsApply()
    {
        // Reading all data from each shape's property entry & applying them...
        for (int i = 0; i < this.Editor_RenderSystem.shapes.size(); i++)
        {
            RenderShape shape = this.Editor_RenderSystem.shapes.get(i);

            if (!shape.selected)
                continue;

            // Acquiring the current shape's properties box...
            HBox shapeBox = (HBox)EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().get(i);

            // TODO
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
