/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.render;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.util.Utils;
import com.cyte.edamame.util.PairMutable;

import java.util.LinkedList;
import java.util.UUID;

import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.event.*;
import javafx.scene.input.*;

public class CanvasRenderSystem
{
    //// GLOBAL VARIABLES ////

    final UUID id = UUID.randomUUID();
    public Canvas canvas;
    public GraphicsContext gc;
    public PairMutable theaterSize;
    public Color backgroundColor;
    public Integer maxShapes;
    public PairMutable zoomLimits;
    public Double zoomFactor;
    public Double mouseDragFactor;
    public Double mouseDragCheckTimeout;

    public LinkedList<CanvasRenderShape> shapes;
    //public LinkedList<PairMutable> shapesPosReal;
    public PairMutable center;
    public Double zoom;
    public boolean mousePressedLeft;
    public boolean mousePressedRight;
    public PairMutable mouseDragFirstPos;
    public Long mouseDragLastTime;
    public PairMutable mouseDragFirstCenter;
    public boolean mouseDragReachedEdge;

    public Editor editor;

    //// CONSTRUCTORS ////

    public CanvasRenderSystem(Editor editorValue, Canvas canvasValue, PairMutable theaterSizeValue, Color backgroundColorValue, Integer maxShapesValue, PairMutable zoomLimitsValue, Double zoomFactorValue, Double mouseDragFactorValue, Double mouseDragCheckTimeoutValue)
    {
        this.canvas = canvasValue;
        this.gc = this.canvas.getGraphicsContext2D();
        this.theaterSize = theaterSizeValue;
        this.backgroundColor = backgroundColorValue;
        this.maxShapes = maxShapesValue;
        this.zoomLimits = zoomLimitsValue;
        this.zoomFactor = zoomFactorValue;
        this.mouseDragFactor = mouseDragFactorValue;
        this.mouseDragCheckTimeout = mouseDragCheckTimeoutValue;
        //this.InitListeners();

        this.shapes = new LinkedList<CanvasRenderShape>();
        //this.shapesPosReal = new LinkedList<PairMutable>();
        this.center = new PairMutable(0.0, 0.0);
        this.zoom = 1.0;
        this.mousePressedLeft = false;
        this.mousePressedRight = false;
        this.mouseDragFirstPos = null;
        this.mouseDragLastTime = System.nanoTime();
        this.mouseDragFirstCenter = null;
        this.mouseDragReachedEdge = false;

        this.editor = editorValue;
    }

    //// RENDERING FUNCTIONS ////

    public void Render()
    {
        this.Clear();

        Integer i = 0;

        while (i < this.shapes.size())
        {
            CanvasRenderShape shape = this.shapes.get(i);
            //PairMutable posReal = this.shapesPosReal.get(i);
            //EditorSchematic_Symbol symbol = shape.symbol;

            if (shape.posReal != null)
                shape.posDraw = this.CalculatePosDraw(shape, shape.posReal);

            if (shape.zoomScaling)
            {
                for (int j = 0; j < shape.points.size(); j++)
                {
                    shape.points.set(j, new PairMutable(shape.points.get(j).GetLeftDouble() * this.zoom, shape.points.get(j).GetRightDouble() * this.zoom));
                    shape.pointWidths.set(j, shape.pointWidths.get(j) * this.zoom);
                }

                //for (int j = 0; j < symbol.wirePoints.size(); j++)
                //    symbol.wirePoints.set(j, new PairMutable(symbol.wirePoints.get(j).GetLeftDouble() * this.zoom, symbol.wirePoints.get(j).GetRightDouble() * this.zoom));
            }

            shape.DrawShape(this.gc);

            if (shape.zoomScaling)
            {
                for (int j = 0; j < shape.points.size(); j++)
                {
                    shape.points.set(j, new PairMutable(shape.points.get(j).GetLeftDouble() / this.zoom, shape.points.get(j).GetRightDouble() / this.zoom));
                    shape.pointWidths.set(j, shape.pointWidths.get(j) / this.zoom);
                }

                //for (int j = 0; j < symbol.wirePoints.size(); j++)
                //    symbol.wirePoints.set(j, new PairMutable(symbol.wirePoints.get(j).GetLeftDouble() / this.zoom, symbol.wirePoints.get(j).GetRightDouble() / this.zoom));
            }

            if (!shape.permanent)
            {
                this.shapes.remove(shape);
                //this.shapesPosReal.remove(posReal);
                i--;
            }

            i++;
        }
    }

    public void Clear()
    {
        this.gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        this.gc.setFill(this.backgroundColor);
        this.gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    //// CANVAS FUNCTIONS ////

    public void BindSize(Node node)
    {
        if (node.getClass() == TabPane.class)
        {
            this.canvas.widthProperty().bind(((TabPane)node).widthProperty());
            this.canvas.heightProperty().bind(((TabPane)node).heightProperty());
        }
        else if (node.getClass() == TextArea.class)
        {
            this.canvas.widthProperty().bind(((TextArea)node).widthProperty());
            this.canvas.heightProperty().bind(((TextArea)node).heightProperty());
        }
        else if (node.getClass() == VBox.class)
        {
            this.canvas.widthProperty().bind(((VBox)node).widthProperty());
            this.canvas.heightProperty().bind(((VBox)node).heightProperty());
        }
        else
        {
            throw new java.lang.Error("ERROR: Unsupported node type supplied to size binding function of canvas!");
        }
    }

    public void UnbindSize()
    {
        this.canvas.widthProperty().unbind();
        this.canvas.heightProperty().unbind();
    }

    public void SetSize(PairMutable sizeValue)
    {
        this.UnbindSize();

        this.canvas.setWidth(sizeValue.GetLeftDouble());
        this.canvas.setHeight(sizeValue.GetRightDouble());
    }

    public PairMutable GetSize()
    {
        return new PairMutable(canvas.getWidth(), canvas.getHeight());
    }

    //// SHAPE FUNCTIONS ////

    public PairMutable CalculatePosDraw(CanvasRenderShape shape, PairMutable posReal)
    {
        PairMutable posDraw = new PairMutable(posReal);

        posDraw.left = posDraw.GetLeftDouble() * this.zoom + this.canvas.getWidth() / 2;
        posDraw.right = posDraw.GetRightDouble() * this.zoom + this.canvas.getHeight() / 2;

        return posDraw;
    }

    public void AddShape(Integer idx, CanvasRenderShape shape)
    {
        if (this.shapes.size() >= this.maxShapes)
            return;

        if (idx < 0)
        {
            this.shapes.add(shape);
            //this.shapesPosReal.add(posReal);
        }
        else
        {
            this.shapes.add(idx, shape);
            //this.shapesPosReal.add(idx, posReal);
        }
    }

    public void RemoveShape(Integer idx)
    {
        this.shapes.remove(this.shapes.get(idx));
        //this.shapesPosReal.remove(this.shapesPosReal.get(idx));
    }

    //// CALLBACK FUNCTIONS ////

    public void InitListeners()
    {
        this.canvas.setOnDragOver(event -> {
            //System.out.println("Dragging over viewport!");
            this.editor.ViewportOnDragOver();

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

            event.consume();
        });

        this.canvas.setOnDragDropped(event -> {
            //System.out.println("Drag dropped over viewport!");
            this.editor.ViewportOnDragDropped();

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

            event.setDropCompleted(true);
            event.consume();
        });

        this.canvas.setOnMouseMoved(event -> {
            //System.out.println("Mouse moving over viewport!");
            this.editor.ViewportOnMouseMoved();

            //this.EditorSchematic_CheckSymbolsDroppedMouseHighlights(new PairMutable(event.getX(), event.getY()));

            event.consume();
        });

        this.canvas.setOnMousePressed(event -> {
            //System.out.println("Mouse pressed over viewport!");
            this.editor.ViewportOnMousePressed();

            /*// Checking whether the left mouse button is pressed
            if (event.isPrimaryButtonDown())
            {
                // Handling the wire drawing
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
                this.EditorSchematic_PressedMouseLeft = true;
            }
            else
            {
                // Updating left mouse button flag
                this.EditorSchematic_PressedMouseLeft = false;
            }*/

            // Checking whether the right mouse button is pressed
            if (event.isSecondaryButtonDown())
                this.mousePressedLeft = true;
            else
                this.mousePressedRight = false;

            this.mouseDragFirstPos = null;

            event.consume();
        });

        this.canvas.setOnMouseReleased(event -> {
            //System.out.println("Mouse released over viewport!");
            this.editor.ViewportOnMouseReleased();

            // Acquiring the current mouse position
            //PairMutable posMouse = new PairMutable(event.getX(), event.getY());

            // Checking whether the right mouse button is pressed
            if (this.mousePressedRight)
            {
                // Checking whether the auto-zoom is triggered
                if (EDAmameController.isGlobalKeyPressed(KeyCode.ALT))
                {
                    for (int i = 0; i < this.shapes.size(); i++)
                    {
                        CanvasRenderShape shape = this.shapes.get(i);

                        shape.posReal = new PairMutable(shape.posReal.GetLeftDouble() - center.GetLeftDouble(),
                                                        shape.posReal.GetRightDouble() - center.GetRightDouble());

                        this.shapes.set(i, shape);
                    }

                    this.center = new PairMutable(0.0, 0.0);
                    this.zoom = 1.0;
                }

                // Setting mouse pressed flag
                this.mousePressedRight = false;
            }

            // Checking whether the left mouse button is pressed
            if (this.mousePressedLeft)
            {
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

                // Setting mouse pressed flag
                this.mousePressedLeft = false;
            }

            event.consume();
        });

        this.canvas.setOnMouseDragged(event -> {
            //System.out.println("Mouse dragged over viewport!");
            this.editor.ViewportOnMouseDragged();

            // Only check if we're past the check timeout
            if (((System.nanoTime() - this.mouseDragLastTime) / 1e9) < this.mouseDragCheckTimeout)
                return;

            // Acquiring the current mouse & diff positions
            PairMutable posMouse = new PairMutable(event.getX(), event.getY());

            if ((this.mouseDragFirstPos == null) || this.mouseDragReachedEdge)
            {
                this.mouseDragFirstPos = new PairMutable(posMouse);
                this.mouseDragFirstCenter = new PairMutable(this.center.GetLeftDouble(),
                                                            this.center.GetRightDouble());

                for (int i = 0; i < this.shapes.size(); i++)
                {
                    CanvasRenderShape shape = this.shapes.get(i);

                    shape.posMousePress = new PairMutable(shape.posReal);
                    //shape.posEdgeOffset = new PairMutable(new PairMutable(0.0, 0.0));

                    this.shapes.set(i, shape);
                }
            }

            PairMutable mouseDiffPos = new PairMutable((posMouse.GetLeftDouble() - this.mouseDragFirstPos.GetLeftDouble()) * this.mouseDragFactor / this.zoom,
                                                       (posMouse.GetRightDouble() - this.mouseDragFirstPos.GetRightDouble()) * this.mouseDragFactor / this.zoom);

            // Checking whether the right mouse button is pressed
            if (event.isSecondaryButtonDown())
            {
                // Handling the moving of the viewport
                this.mouseDragReachedEdge = false;

                if ((this.center.GetLeftDouble() <= -this.theaterSize.GetLeftDouble() / 2) && (mouseDiffPos.GetLeftDouble() < 0))
                {
                    mouseDiffPos.left = mouseDiffPos.GetLeftDouble() + (-this.theaterSize.GetLeftDouble() / 2 - (this.mouseDragFirstCenter.GetLeftDouble() + mouseDiffPos.GetLeftDouble()));
                    this.mouseDragReachedEdge = true;
                }
                if ((this.center.GetLeftDouble() >= this.theaterSize.GetLeftDouble() / 2) && (mouseDiffPos.GetLeftDouble() > 0))
                {
                    mouseDiffPos.left = mouseDiffPos.GetLeftDouble() + (this.theaterSize.GetLeftDouble() / 2 - (this.mouseDragFirstCenter.GetLeftDouble() + mouseDiffPos.GetLeftDouble()));
                    this.mouseDragReachedEdge = true;
                }
                if ((this.center.GetRightDouble() <= -this.theaterSize.GetRightDouble() / 2) && (mouseDiffPos.GetRightDouble() < 0))
                {
                    mouseDiffPos.right = mouseDiffPos.GetRightDouble() + (-this.theaterSize.GetRightDouble() / 2 - (this.mouseDragFirstCenter.GetRightDouble() + mouseDiffPos.GetRightDouble()));
                    this.mouseDragReachedEdge = true;
                }
                if ((this.center.GetRightDouble() >= this.theaterSize.GetRightDouble() / 2) && (mouseDiffPos.GetRightDouble() > 0))
                {
                    mouseDiffPos.right = mouseDiffPos.GetRightDouble() + (this.theaterSize.GetRightDouble() / 2 - (this.mouseDragFirstCenter.GetRightDouble() + mouseDiffPos.GetRightDouble()));
                    this.mouseDragReachedEdge = true;
                }

                this.center.left = this.mouseDragFirstCenter.GetLeftDouble() + mouseDiffPos.GetLeftDouble();
                this.center.right = this.mouseDragFirstCenter.GetRightDouble() + mouseDiffPos.GetRightDouble();

                for (int i = 0; i < this.shapes.size(); i++)
                {
                    CanvasRenderShape shape = this.shapes.get(i);

                    shape.posReal = new PairMutable(shape.posMousePress.GetLeftDouble() + mouseDiffPos.GetLeftDouble(),
                                                    shape.posMousePress.GetRightDouble() + mouseDiffPos.GetRightDouble());

                    this.shapes.set(i, shape);
                }
            }

            // Checking whether the left mouse button is pressed
            /*if (event.isPrimaryButtonDown())
            {
                // Handling the symbol moving (should only trigger if we got symbols selected)
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
                }
            }*/

            this.mouseDragLastTime = System.nanoTime();

            // Checking whether we are hovering over any symbols
            //this.EditorSchematic_CheckSymbolsDroppedMouseHighlights(new PairMutable(event.getX(), event.getY()));

            event.consume();
        });

        this.canvas.setOnScroll(event -> {
            //System.out.println("Mouse scrolled over viewport!");
            this.editor.ViewportOnScroll();

            // Handling symbol rotation
            boolean rotating = false;

            /*if (this.EditorSchematic_IsKeyPressed(KeyCode.R))
            {
                for (int i = 0; i < this.EditorSchematic_ViewportSymbolsDropped.size(); i++)
                {
                    EditorSchematic_Symbol symbol = this.EditorSchematic_ViewportSymbolsDropped.get(i);

                    if (!symbol.selected)
                        continue;

                    Double angleInc = this.EditorSchematic_SymbolRotationStep;

                    if (event.getDeltaY() > 0)
                        angleInc = -this.EditorSchematic_SymbolRotationStep;

                    symbol.angle += angleInc;

                    // Rotating shape points
                    for (int j = 0; j < symbol.shape.points.size(); j++)
                    {
                        PairMutable point = symbol.shape.points.get(j);

                        Double newX = point.GetLeftDouble() * Math.cos(Math.toRadians(angleInc)) - point.GetRightDouble() * Math.sin(Math.toRadians(angleInc));
                        Double newY = point.GetLeftDouble() * Math.sin(Math.toRadians(angleInc)) + point.GetRightDouble() * Math.cos(Math.toRadians(angleInc));

                        symbol.shape.points.set(j, new PairMutable(newX, newY));
                    }

                    // Rotating wire points
                    for (int j = 0; j < symbol.wirePoints.size(); j++)
                    {
                        PairMutable point = symbol.wirePoints.get(j);

                        Double newX = point.GetLeftDouble() * Math.cos(Math.toRadians(angleInc)) - point.GetRightDouble() * Math.sin(Math.toRadians(angleInc));
                        Double newY = point.GetLeftDouble() * Math.sin(Math.toRadians(angleInc)) + point.GetRightDouble() * Math.cos(Math.toRadians(angleInc));

                        symbol.wirePoints.set(j, new PairMutable(newX, newY));
                    }

                    this.EditorSchematic_ViewportSymbolsDropped.set(i, symbol);
                    rotating = true;
                }
            }*/

            // Handling symbol zoom scaling (only if we're not rotating symbols)
            if (!rotating)
            {
                //boolean canMove = true;

                if (event.getDeltaY() < 0)
                {
                    if ((this.zoom / this.zoomFactor) <= this.zoomLimits.GetLeftDouble())
                    {
                        this.zoom = this.zoomLimits.GetLeftDouble();
                        //canMove = false;
                    }
                    else
                    {
                        this.zoom /= this.zoomFactor;
                    }
                }
                else
                {
                    if ((this.zoom * this.zoomFactor) >= this.zoomLimits.GetRightDouble())
                    {
                        this.zoom = this.zoomLimits.GetRightDouble();
                        //canMove = false;
                    }
                    else
                    {
                        this.zoom *= this.zoomFactor;
                    }
                }

                //if (canMove)
                //    this.EditorSchematic_ViewportCenter = new PairMutable(this.EditorSchematic_ViewportCenter.GetLeftDouble() + (event.getX() - this.EditorSchematic_ViewportCenter.GetLeftDouble()) / 2,
                //                                                          this.EditorSchematic_ViewportCenter.GetRightDouble() + (event.getY() - this.EditorSchematic_ViewportCenter.GetRightDouble()) / 2);

                //this.EditorSchematic_ViewportZoomOffset = new PairMutable(event.getX() - this.EditorSchematic_ViewportCenter.GetLeftDouble(),
                //                                                          event.getY() - this.EditorSchematic_ViewportCenter.GetRightDouble());
            }

            // Checking for updated symbol highlights
            //this.EditorSchematic_CheckSymbolsDroppedMouseHighlights(new PairMutable(event.getX(), event.getY()));

            event.consume();
        });
    }
}