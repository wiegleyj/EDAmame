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

        this.shapes = new LinkedList<CanvasRenderShape>();
        //this.shapesPosReal = new LinkedList<PairMutable>();
        this.center = new PairMutable(0.0, 0.0);
        this.zoom = 1.0;
        this.mouseDragFirstPos = null;
        this.mouseDragLastTime = System.nanoTime();
        this.mouseDragFirstCenter = null;
        this.mouseDragReachedEdge = false;

        this.editor = editorValue;
        this.InitListeners();
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
            // Handling global callback actions
            {}

            // Handling editor-specific callback actions
            this.editor.ViewportOnDragOver();

            event.consume();
        });

        this.canvas.setOnDragDropped(event -> {
            // Handling global callback actions
            {}

            // Handling editor-specific callback actions
            this.editor.ViewportOnDragDropped();

            event.setDropCompleted(true);
            event.consume();
        });

        this.canvas.setOnMouseMoved(event -> {
            // Handling global callback actions
            {}

            // Handling editor-specific callback actions
            this.editor.ViewportOnMouseMoved();

            event.consume();
        });

        this.canvas.setOnMousePressed(event -> {
            // Updating mouse pressed flags
            {
                if (event.isPrimaryButtonDown())
                    this.editor.pressedLMB = true;
                if (event.isSecondaryButtonDown())
                    this.editor.pressedRMB = true;
            }

            // Handling global callback actions
            {
                if (this.editor.pressedLMB)
                {}

                if (this.editor.pressedRMB)
                {}

                this.mouseDragFirstPos = null;
            }

            // Handling editor-specific callback actions
            this.editor.ViewportOnMousePressed();

            event.consume();
        });

        this.canvas.setOnMouseReleased(event -> {
            // Handling global callback actions
            {
                if (this.editor.pressedLMB)
                {}

                if (this.editor.pressedRMB)
                {
                    // Handling auto-zoom
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
                }
            }

            // Handling editor-specific callback actions
            this.editor.ViewportOnMouseReleased();

            // Updating mouse pressed flags
            {
                this.editor.pressedLMB = false;
                this.editor.pressedRMB = false;
            }

            event.consume();
        });

        this.canvas.setOnMouseDragged(event -> {
            // Only callback if we're past the check timeout
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

                    if (shape.posReal == null)
                        continue;

                    shape.posMousePress = new PairMutable(shape.posReal);
                    //shape.posEdgeOffset = new PairMutable(new PairMutable(0.0, 0.0));

                    this.shapes.set(i, shape);
                }
            }

            PairMutable mouseDiffPos = new PairMutable((posMouse.GetLeftDouble() - this.mouseDragFirstPos.GetLeftDouble()) * this.mouseDragFactor / this.zoom,
                                                       (posMouse.GetRightDouble() - this.mouseDragFirstPos.GetRightDouble()) * this.mouseDragFactor / this.zoom);

            // Handling global callback actions
            {
                if (this.editor.pressedLMB)
                {}

                if (this.editor.pressedRMB)
                {
                    // Handling the moving of the viewport
                    {
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

                            if (shape.posReal == null)
                                continue;

                            shape.posReal = new PairMutable(shape.posMousePress.GetLeftDouble() + mouseDiffPos.GetLeftDouble(),
                                                            shape.posMousePress.GetRightDouble() + mouseDiffPos.GetRightDouble());

                            this.shapes.set(i, shape);
                        }
                    }
                }
            }

            // Handling editor-specific callback actions
            this.editor.ViewportOnMouseDragged(mouseDiffPos);

            this.mouseDragLastTime = System.nanoTime();

            event.consume();
        });

        this.canvas.setOnScroll(event -> {
            // Handling editor-specific callback actions
            this.editor.ViewportOnScroll();

            // Handling global callback actions
            {
                // Handling zoom scaling (only if we're not rotating anything)
                if (!this.editor.rotating)
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
            }

            event.consume();
        });
    }
}