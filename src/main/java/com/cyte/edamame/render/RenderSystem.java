/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.render;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.util.Utils;

import java.sql.SQLOutput;
import java.util.LinkedList;
import java.util.UUID;

import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.util.Pair;

public class RenderSystem
{
    //// GLOBAL VARIABLES ////

    final UUID id = UUID.randomUUID();

    public Pane paneListener;
    public Pane paneHolder;
    public Pane paneHighlights;
    public Pane paneSelections;
    public Canvas canvas;
    public GraphicsContext gc;
    public Shape crosshair;
    public PairMutable theaterSize;
    public Color backgroundColor;
    public Color gridPointColor;
    public Color gridBoxColor;
    public Integer maxShapes;
    public PairMutable zoomLimits;
    public Double zoomFactor;
    public Double mouseDragFactor;
    public Double mouseDragCheckTimeout;

    // DO NOT EDIT

    public LinkedList<RenderShape> shapes;
    public PairMutable center;
    public Double zoom;
    public PairMutable mouseDragFirstPos;
    public Long mouseDragLastTime;
    public PairMutable mouseDragFirstCenter;
    public boolean mouseDragReachedEdge;
    public PairMutable mouseDragPaneFirstPos;
    public Integer shapesHighlighted;
    public Integer shapesSelected;
    public boolean shapesMoving;

    public Editor editor;

    //// CONSTRUCTORS ////

    public RenderSystem(Editor editorValue, Pane paneListenerValue, Pane paneHolderValue, Pane paneHighlightsValue, Pane paneSelectionsValue, Canvas canvasValue, Shape crosshairValue, PairMutable theaterSizeValue, Color backgroundColorValue, Color gridPointColorValue, Color gridBoxColorValue, Integer maxShapesValue, PairMutable zoomLimitsValue, Double zoomFactorValue, Double mouseDragFactorValue, Double mouseDragCheckTimeoutValue)
    {
        this.paneListener = paneListenerValue;
        this.paneHolder = paneHolderValue;
        this.paneHighlights = paneHighlightsValue;
        this.paneSelections = paneSelectionsValue;
        this.canvas = canvasValue;
        this.gc = this.canvas.getGraphicsContext2D();
        this.crosshair = crosshairValue;
        this.theaterSize = theaterSizeValue;
        this.backgroundColor = backgroundColorValue;
        this.gridPointColor = gridPointColorValue;
        this.gridBoxColor = gridBoxColorValue;
        this.maxShapes = maxShapesValue;
        this.zoomLimits = zoomLimitsValue;
        this.zoomFactor = zoomFactorValue;
        this.mouseDragFactor = mouseDragFactorValue;
        this.mouseDragCheckTimeout = mouseDragCheckTimeoutValue;

        this.shapes = new LinkedList<RenderShape>();
        this.center = new PairMutable(0.0, 0.0);
        this.zoom = 1.0;
        this.mouseDragFirstPos = null;
        this.mouseDragLastTime = System.nanoTime();
        this.mouseDragFirstCenter = null;
        this.mouseDragReachedEdge = false;
        this.mouseDragPaneFirstPos = null;
        this.shapesHighlighted = 0;
        this.shapesSelected = 0;
        this.shapesMoving = false;

        this.editor = editorValue;

        this.RenderSystem_PaneSetTranslate(new PairMutable(0.0, 0.0));
        this.paneHolder.prefWidthProperty().bind(this.canvas.widthProperty());
        this.paneHolder.prefHeightProperty().bind(this.canvas.heightProperty());

        this.RenderSystem_ListenersInit();
    }

    //// RENDERING FUNCTIONS ////

    public void RenderSystem_CanvasRenderGrid()
    {
        // Clearing the canvas
        this.RenderSystem_CanvasClear();

        // Drawing the points
        gc.setFill(this.gridPointColor);
        gc.setGlobalAlpha(1.0);
        Double width = 3.0;

        Double posX = -2500.0;
        Double posY = -2500.0;

        for (int i = 0; i < 70; i++)
        {
            for (int j = 0; j < 70; j++)
            {
                gc.fillOval(posX - (width / 2), posY - (width / 2), width, width);

                posX += 100.0;
            }

            posX = -2500.0;
            posY += 100.0;
        }

        // Drawing the grid box
        gc.setStroke(this.gridBoxColor);
        gc.setGlobalAlpha(1.0);
        gc.setLineWidth(2.0);
        gc.strokeLine(this.canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                      this.canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
        gc.strokeLine(this.canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                      this.canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
        gc.strokeLine(this.canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                      this.canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
        gc.strokeLine(this.canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                      this.canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
    }

    public void RenderSystem_CanvasClear()
    {
        this.gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        this.gc.setFill(this.backgroundColor);
        this.gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    //// PANE FUNCTIONS ////

    public PairMutable RenderSystem_PaneConvertListenerPos(PairMutable listenerPos)
    {
        Point2D pos = paneHolder.parentToLocal(listenerPos.GetLeftDouble(), listenerPos.GetRightDouble());

        return new PairMutable(pos.getX(), pos.getY());
    }

    public void RenderSystem_PaneSetTranslate(PairMutable pos)
    {
        this.paneHolder.setTranslateX(pos.GetLeftDouble());
        this.paneHolder.setTranslateY(pos.GetRightDouble());
    }

    public void RenderSystem_PaneSetScale(PairMutable scale, boolean compensate)
    {
        PairMutable prevScale = this.RenderSystem_PaneGetScale();

        this.paneHolder.setScaleX(scale.GetLeftDouble());
        this.paneHolder.setScaleY(scale.GetRightDouble());

        if (compensate)
        {
            PairMutable scaleDelta = new PairMutable(scale.GetLeftDouble() - prevScale.GetLeftDouble(),
                                                     scale.GetRightDouble() - prevScale.GetRightDouble());
            PairMutable newPos = this.RenderSystem_PaneGetTranslate();

            newPos.left = newPos.GetLeftDouble() + this.center.GetLeftDouble() * scaleDelta.GetLeftDouble();
            newPos.right = newPos.GetRightDouble() + this.center.GetRightDouble() * scaleDelta.GetRightDouble();

            this.RenderSystem_PaneSetTranslate(newPos);
        }
    }

    public PairMutable RenderSystem_PaneGetScale()
    {
        return new PairMutable(this.paneHolder.getScaleX(), this.paneHolder.getScaleY());
    }

    public PairMutable RenderSystem_PaneGetTranslate()
    {
        return new PairMutable(this.paneHolder.getTranslateX(), this.paneHolder.getTranslateY());
    }

    //// SHAPE FUNCTIONS ////

    public void RenderSystem_ShapeHighlightsCheck(PairMutable posEvent)
    {
        for (int i = 0; i < this.shapes.size(); i++)
        {
            PairMutable posMouse = this.RenderSystem_PaneConvertListenerPos(new PairMutable(posEvent.GetLeftDouble(), posEvent.GetRightDouble()));
            RenderShape shape = this.shapes.get(i);
            boolean onShape = shape.PosOnShape(posMouse);
            boolean canAdd = false;
            boolean canRemove = false;

            if (onShape)
            {
                if (EDAmameController.Controller_IsKeyPressed(KeyCode.Q))
                {
                    if ((this.shapesHighlighted > 1) && shape.highlighted)
                    {
                        canRemove = true;
                    }
                    else if ((this.shapesHighlighted == 0) && !shape.highlighted)
                    {
                        canAdd = true;
                    }
                }
                else
                {
                    if (!shape.highlighted)
                    {
                        canAdd = true;
                    }
                }
            }
            else
            {
                if (shape.highlighted)
                {
                    canRemove = true;
                }
            }

            if (canAdd)
            {
                shape.CalculateShapeHighlighted();
                this.paneHighlights.getChildren().add(shape.shapeHighlighted);
                shape.highlighted = true;
                this.shapesHighlighted++;
            }
            if (canRemove)
            {
                this.RenderSystem_ShapeHighlightRemove(shape.id);
                shape.highlighted = false;
                this.shapesHighlighted--;
            }

            this.shapes.set(i, shape);
        }
    }

    public void RenderSystem_ShapeHighlightRemove(String id)
    {
        for (int i = 0; i < this.paneHighlights.getChildren().size(); i++)
        {
            Shape shape = (Shape)this.paneHighlights.getChildren().get(i);

            if (shape.getId().equals(id))
            {
                this.paneHighlights.getChildren().remove(shape);
            }
        }
    }

    public void RenderSystem_ShapeSelectionRemove(String id)
    {
        for (int i = 0; i < this.paneSelections.getChildren().size(); i++)
        {
            Shape shape = (Shape)this.paneSelections.getChildren().get(i);

            if (shape.getId().equals(id))
            {
                this.paneSelections.getChildren().remove(shape);
            }
        }
    }

    public void RenderSystem_ShapeAdd(RenderShape shape)
    {
        this.shapes.add(shape);
        this.paneHolder.getChildren().add(1, shape.shape);
    }

    //// CALLBACK FUNCTIONS ////

    // When we press down a key...
    public void RenderSystem_OnKeyPressed(KeyEvent event)
    {
        // Handling editor-specific callback actions
        this.editor.Editor_ViewportOnKeyPressed(event);

        // Handling global callback actions
        {
            // Handling shape deletion
            if (EDAmameController.Controller_IsKeyPressed(KeyCode.BACK_SPACE) || EDAmameController.Controller_IsKeyPressed(KeyCode.DELETE))
            {
                if (this.shapesSelected > 0)
                {
                    for (int i = 0; i < this.shapes.size(); i++)
                    {
                        RenderShape shape = this.shapes.get(i);

                        if (!shape.selected)
                            continue;

                        if (shape.highlighted)
                        {
                            this.RenderSystem_ShapeHighlightRemove(shape.id);
                            this.shapesHighlighted--;
                        }

                        this.RenderSystem_ShapeSelectionRemove(shape.id);
                        this.shapesSelected--;

                        this.paneHolder.getChildren().remove(shape.shape);
                        this.shapes.remove(shape);

                        i--;
                    }
                }
            }
        }
    }

    // When we release a key...
    public void RenderSystem_OnKeyReleased(KeyEvent event)
    {
        // Handling editor-specific callback actions
        this.editor.Editor_ViewportOnKeyReleased(event);

        // Handling global callback actions
        {}
    }

    public void RenderSystem_ListenersInit()
    {
        // When we drag the mouse (from outside the viewport)...
        this.paneListener.setOnDragOver(event -> {
            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnDragOver(event);

            // Handling global callback actions
            {
                // Handling shape highlights
                this.RenderSystem_ShapeHighlightsCheck(new PairMutable(event.getX(), event.getY()));
            }

            event.consume();
        });

        // When we drop something with the cursor (from outside the viewport)...
        this.paneListener.setOnDragDropped(event -> {
            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnDragDropped(event);

            // Handling global callback actions
            {}

            event.setDropCompleted(true);
            event.consume();
        });

        // When we move the mouse...
        this.paneListener.setOnMouseMoved(event -> {
            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMouseMoved(event);

            // Handling global callback actions
            {
                // Handling shape highlights
                this.RenderSystem_ShapeHighlightsCheck(new PairMutable(event.getX(), event.getY()));
            }

            event.consume();
        });

        // When we press down the mouse...
        this.paneListener.setOnMousePressed(event -> {
            // Updating mouse pressed flags
            if (event.isPrimaryButtonDown())
                this.editor.Editor_PressedLMB = true;
            if (event.isSecondaryButtonDown())
                this.editor.Editor_PressedRMB = true;

            // Handling global callback actions
            {
                if (this.editor.Editor_PressedLMB)
                {}

                if (this.editor.Editor_PressedRMB)
                {}

                this.mouseDragFirstPos = null;
                this.mouseDragPaneFirstPos = null;
            }

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMousePressed(event);

            event.consume();
        });

        // When we release the mouse...
        this.paneListener.setOnMouseReleased(event -> {
            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMouseReleased(event);

            // Handling global callback actions
            {
                if (this.editor.Editor_PressedLMB)
                {
                    // Handling shape selection (only if we're not moving any shapes)
                    if (!this.shapesMoving)
                    {
                        for (int i = 0; i < this.shapes.size(); i++)
                        {
                            RenderShape shape = this.shapes.get(i);
                            boolean canAdd = false;
                            boolean canRemove = false;

                            if (shape.highlighted && !shape.selected)
                            {
                                canAdd = true;
                            }
                            else if (!shape.highlighted && shape.selected)
                            {
                                if (!EDAmameController.Controller_IsKeyPressed(KeyCode.SHIFT))
                                {
                                    canRemove = true;
                                }
                            }

                            if (canAdd)
                            {
                                shape.selected = true;
                                shape.CalculateShapeSelected();
                                this.paneSelections.getChildren().add(shape.shapeSelected);
                                this.shapesSelected++;
                            }
                            if (canRemove)
                            {
                                shape.selected = false;
                                this.RenderSystem_ShapeSelectionRemove(shape.id);
                                this.shapesSelected--;
                            }

                            shape.mousePressPos = null;

                            this.shapes.set(i, shape);
                        }
                    }
                }

                if (this.editor.Editor_PressedRMB)
                {
                    // Handling auto-zoom
                    if (EDAmameController.Controller_IsKeyPressed(KeyCode.ALT))
                    {
                        this.RenderSystem_PaneSetTranslate(new PairMutable(0.0, 0.0));
                        this.RenderSystem_PaneSetScale(new PairMutable(1.0, 1.0), false);

                        this.center = new PairMutable(0.0, 0.0);
                        this.zoom = 1.0;
                    }
                }

                this.shapesMoving = false;
            }

            // Updating mouse pressed flags
            this.editor.Editor_PressedLMB = false;
            this.editor.Editor_PressedRMB = false;

            event.consume();
        });

        // When we drag the mouse (from inside the viewport)...
        this.paneListener.setOnMouseDragged(event -> {
            // Handling shape highlights
            this.RenderSystem_ShapeHighlightsCheck(new PairMutable(event.getX(), event.getY()));

            // Only execute callback if we're past the check timeout
            if (((System.nanoTime() - this.mouseDragLastTime) / 1e9) < this.mouseDragCheckTimeout)
                return;

            // Acquiring the current mouse & diff positions
            PairMutable posMouse = new PairMutable(event.getX(), event.getY());

            if ((this.mouseDragFirstPos == null) || this.mouseDragReachedEdge)
            {
                this.mouseDragFirstPos = new PairMutable(posMouse);
                this.mouseDragFirstCenter = new PairMutable(this.center.GetLeftDouble(),
                                                            this.center.GetRightDouble());
                this.mouseDragPaneFirstPos = new PairMutable(this.RenderSystem_PaneGetTranslate());

                if (this.shapesSelected > 0)
                {
                    for (int i = 0; i < this.shapes.size(); i++)
                    {
                        RenderShape shape = this.shapes.get(i);

                        if (!shape.selected)
                            continue;

                        shape.mousePressPos = new PairMutable(new PairMutable(shape.shape.getTranslateX(), shape.shape.getTranslateY()));

                        this.shapes.set(i, shape);
                    }
                }
            }

            PairMutable mouseDiffPos = new PairMutable((posMouse.GetLeftDouble() - this.mouseDragFirstPos.GetLeftDouble()) * this.mouseDragFactor / this.zoom,
                                                       (posMouse.GetRightDouble() - this.mouseDragFirstPos.GetRightDouble()) * this.mouseDragFactor / this.zoom);

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMouseDragged(event);

            // Handling global callback actions
            {
                if (this.editor.Editor_PressedLMB)
                {
                    // Handling moving of the shapes (only if we have some shapes selected)
                    if (this.shapesSelected > 0)
                    {
                        for (int i = 0; i < this.shapes.size(); i++)
                        {
                            RenderShape shape = this.shapes.get(i);

                            if (!shape.selected)
                                continue;

                            shape.shape.setTranslateX(shape.mousePressPos.GetLeftDouble() + mouseDiffPos.GetLeftDouble());
                            shape.shape.setTranslateY(shape.mousePressPos.GetRightDouble() + mouseDiffPos.GetRightDouble());

                            this.shapes.set(i, shape);
                        }

                        this.shapesMoving = true;
                    }
                }

                if (this.editor.Editor_PressedRMB)
                {
                    // Handling moving of the viewport
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

                        this.RenderSystem_PaneSetTranslate(new PairMutable(this.mouseDragPaneFirstPos.GetLeftDouble() + mouseDiffPos.GetLeftDouble() * this.zoom,
                                                                           this.mouseDragPaneFirstPos.GetRightDouble() + mouseDiffPos.GetRightDouble() * this.zoom));
                    }
                }
            }

            this.mouseDragLastTime = System.nanoTime();

            event.consume();
        });

        // When we scroll the mouse...
        this.paneListener.setOnScroll(event -> {
            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnScroll(event);

            // Handling global callback actions
            {
                // Handling shape highlights
                this.RenderSystem_ShapeHighlightsCheck(new PairMutable(event.getX(), event.getY()));

                // Handling zoom scaling (only if we're not rotating anything)
                if (!this.editor.Editor_Rotating)
                {
                    if (event.getDeltaY() < 0)
                        if ((this.zoom / this.zoomFactor) <= this.zoomLimits.GetLeftDouble())
                            this.zoom = this.zoomLimits.GetLeftDouble();
                        else
                            this.zoom /= this.zoomFactor;
                    else
                        if ((this.zoom * this.zoomFactor) >= this.zoomLimits.GetRightDouble())
                            this.zoom = this.zoomLimits.GetRightDouble();
                        else
                            this.zoom *= this.zoomFactor;

                    this.RenderSystem_PaneSetScale(new PairMutable(this.zoom, this.zoom), true);
                }
            }

            event.consume();
        });
    }
}