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
import java.lang.Math;

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
    public Color selectionBoxColor;
    public Double selectionBoxWidth;

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
    public boolean pressedOnShape;
    public Rectangle selectionBox;

    public Editor editor;

    //// CONSTRUCTORS ////

    public RenderSystem(Editor editorValue, Pane paneListenerValue, Pane paneHolderValue, Pane paneHighlightsValue, Pane paneSelectionsValue, Canvas canvasValue, Shape crosshairValue, PairMutable theaterSizeValue, Color backgroundColorValue, Color gridPointColorValue, Color gridBoxColorValue, Integer maxShapesValue, PairMutable zoomLimitsValue, Double zoomFactorValue, Double mouseDragFactorValue, Double mouseDragCheckTimeoutValue, Color selectionBoxColorValue, Double selectionBoxWidthValue)
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
        this.selectionBoxColor = selectionBoxColorValue;
        this.selectionBoxWidth = selectionBoxWidthValue;

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
        this.pressedOnShape = false;
        this.selectionBox = null;

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

    public PairMutable RenderSystem_PaneHolderGetRealPos(PairMutable pos)
    {
        return new PairMutable(pos.GetLeftDouble() - this.paneHolder.getWidth() / 2,
                               pos.GetRightDouble() - this.paneHolder.getHeight() / 2);
    }

    public PairMutable RenderSystem_PanePosListenerToHolder(PairMutable pos)
    {
        Point2D newPos = this.paneHolder.parentToLocal(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public PairMutable RenderSystem_PanePosHolderToListener(PairMutable pos)
    {
        Point2D newPos = this.paneHolder.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
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
        /*for (int i = 0; i < this.paneHolder.getChildren().size(); i++)
        {
            if ((this.paneHolder.getChildren().get(i).getId() != null) && this.paneHolder.getChildren().get(i).getId().equals("TESTMARKER"))
            {
                this.paneHolder.getChildren().remove(this.paneHolder.getChildren().get(i));
                i--;
            }
        }*/

        for (int i = 0; i < this.shapes.size(); i++)
        {
            PairMutable posMouse = this.RenderSystem_PanePosListenerToHolder(new PairMutable(posEvent.GetLeftDouble(), posEvent.GetRightDouble()));
            RenderShape shape = this.shapes.get(i);
            boolean onShape = shape.PosOnShape(posMouse);

            // Checking whether we are highlighting by cursor...
            if (onShape)
            {
                if (EDAmameController.Controller_IsKeyPressed(KeyCode.Q))
                {
                    if ((this.shapesHighlighted > 1) && shape.highlightedMouse)
                        shape.highlightedMouse = false;
                    else if ((this.shapesHighlighted == 0) && !shape.highlightedMouse)
                        shape.highlightedMouse = true;
                }
                else
                {
                    if (!shape.highlightedMouse)
                        shape.highlightedMouse = true;
                }
            }
            else
            {
                if (shape.highlightedMouse)
                    shape.highlightedMouse = false;
            }

            // Checking whether we are highlighting by selection box...
            if (this.selectionBox != null)
            {
                Bounds shapeBounds = shape.shape.getBoundsInParent();
                PairMutable selectionBoxL = this.RenderSystem_PanePosListenerToHolder(new PairMutable(this.selectionBox.getTranslateX(), this.selectionBox.getTranslateY()));
                PairMutable selectionBoxH = this.RenderSystem_PanePosListenerToHolder(new PairMutable(this.selectionBox.getTranslateX() + this.selectionBox.getWidth(), this.selectionBox.getTranslateY() + this.selectionBox.getHeight()));

                /*Circle testShapeL = new Circle(5, Color.RED);
                testShapeL.setId("TESTMARKER");
                testShapeL.setTranslateX(shapeBounds.getMinX());
                testShapeL.setTranslateY(shapeBounds.getMinY());
                this.paneHolder.getChildren().add(testShapeL);

                Circle testShapeH = new Circle(5, Color.RED);
                testShapeH.setId("TESTMARKER");
                testShapeH.setTranslateX(shapeBounds.getMaxX());
                testShapeH.setTranslateY(shapeBounds.getMaxY());
                this.paneHolder.getChildren().add(testShapeH);

                Circle testShapeBL = new Circle(5, Color.BLUE);
                testShapeBL.setId("TESTMARKER");
                PairMutable boxL = this.RenderSystem_PanePosListenerToHolder(new PairMutable(this.selectionBox.getTranslateX(), this.selectionBox.getTranslateY()));
                testShapeBL.setTranslateX(boxL.GetLeftDouble());
                testShapeBL.setTranslateY(boxL.GetRightDouble());
                this.paneHolder.getChildren().add(testShapeBL);

                Circle testShapeBH = new Circle(5, Color.BLUE);
                testShapeBH.setId("TESTMARKER");
                PairMutable boxH = this.RenderSystem_PanePosListenerToHolder(new PairMutable(this.selectionBox.getTranslateX() + this.selectionBox.getWidth(), this.selectionBox.getTranslateY() + this.selectionBox.getHeight()));
                testShapeBH.setTranslateX(boxH.GetLeftDouble());
                testShapeBH.setTranslateY(boxH.GetRightDouble());
                this.paneHolder.getChildren().add(testShapeBH);*/

                if ((selectionBoxL.GetLeftDouble() < shapeBounds.getMaxX()) &&
                    (selectionBoxH.GetLeftDouble() > shapeBounds.getMinX()) &&
                    (selectionBoxL.GetRightDouble() < shapeBounds.getMaxY()) &&
                    (selectionBoxH.GetRightDouble() > shapeBounds.getMinY()))
                {
                    if (!shape.highlightedBox)
                        shape.highlightedBox = true;
                }
                else
                {
                    if (shape.highlightedBox)
                        shape.highlightedBox = false;
                }
            }
            else if (shape.highlightedBox)
            {
                shape.highlightedBox = false;
            }

            // Adjusting highlights accordingly...
            if ((shape.highlightedMouse || shape.highlightedBox) && !shape.highlighted)
            {
                //shape.CalculateShapeHighlighted();
                this.paneHighlights.getChildren().add(shape.shapeHighlighted);
                shape.highlighted = true;
                this.shapesHighlighted++;
            }
            else if ((!shape.highlightedMouse && !shape.highlightedBox) && shape.highlighted)
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
                else if (this.editor.Editor_PressedRMB)
                {}

                this.mouseDragFirstPos = null;
                this.mouseDragPaneFirstPos = null;

                if (this.shapesHighlighted > 0)
                    this.pressedOnShape = true;
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

                            if (!shape.selected)
                            {
                                if (shape.highlightedMouse || shape.highlightedBox)
                                {
                                    shape.selected = true;
                                    //shape.CalculateShapeSelected();
                                    this.paneSelections.getChildren().add(shape.shapeSelected);
                                    this.shapesSelected++;
                                }
                            }
                            else
                            {
                                if ((!shape.highlightedMouse && !shape.highlightedBox) && !EDAmameController.Controller_IsKeyPressed(KeyCode.SHIFT))
                                {
                                    shape.selected = false;
                                    this.RenderSystem_ShapeSelectionRemove(shape.id);
                                    this.shapesSelected--;
                                }
                            }

                            if (shape.highlightedBox && !shape.highlightedMouse)
                                this.RenderSystem_ShapeHighlightRemove(shape.id);

                            shape.mousePressPos = null;

                            this.shapes.set(i, shape);
                        }

                        if (this.selectionBox != null)
                        {
                            this.paneListener.getChildren().remove(this.selectionBox);
                            this.selectionBox = null;
                        }
                    }
                }
                else if (this.editor.Editor_PressedRMB)
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
                this.pressedOnShape = false;
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
                    // Handling moving of the shapes (only if we have some shapes selected & we're not holding the box selection key)
                    if ((this.shapesSelected > 0) && !EDAmameController.Controller_IsKeyPressed(KeyCode.SHIFT))
                    {
                        for (int i = 0; i < this.shapes.size(); i++)
                        {
                            RenderShape shape = this.shapes.get(i);

                            if (!shape.selected)
                                continue;

                            PairMutable posPressReal = this.RenderSystem_PaneHolderGetRealPos(new PairMutable(shape.mousePressPos.GetLeftDouble(), shape.mousePressPos.GetRightDouble()));
                            PairMutable edgeOffset = new PairMutable(0.0, 0.0);

                            if ((posPressReal.GetLeftDouble() + mouseDiffPos.GetLeftDouble()) < -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2)
                                edgeOffset.left = -(posPressReal.GetLeftDouble() + mouseDiffPos.GetLeftDouble() + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2);
                            if ((posPressReal.GetLeftDouble() + mouseDiffPos.GetLeftDouble()) > EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2)
                                edgeOffset.left = -(posPressReal.GetLeftDouble() + mouseDiffPos.GetLeftDouble() - EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2);
                            if ((posPressReal.GetRightDouble() + mouseDiffPos.GetRightDouble()) < -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2)
                                edgeOffset.right = -(posPressReal.GetRightDouble() + mouseDiffPos.GetRightDouble() + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
                            if ((posPressReal.GetRightDouble() + mouseDiffPos.GetRightDouble()) > EDAmameController.Editor_TheaterSize.GetRightDouble() / 2)
                                edgeOffset.right = -(posPressReal.GetRightDouble() + mouseDiffPos.GetRightDouble() - EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);

                            shape.shape.setTranslateX(shape.mousePressPos.GetLeftDouble() + mouseDiffPos.GetLeftDouble() + edgeOffset.GetLeftDouble());
                            shape.shape.setTranslateY(shape.mousePressPos.GetRightDouble() + mouseDiffPos.GetRightDouble() + edgeOffset.GetRightDouble());

                            this.shapes.set(i, shape);
                        }

                        this.shapesMoving = true;
                    }
                    // Handling the box selection (only if we have no shapes selected and we are not moving the viewport)
                    else
                    {
                        if (this.selectionBox == null)
                        {
                            this.selectionBox = new Rectangle(0.0, 0.0);
                            this.selectionBox.setFill(Color.TRANSPARENT);
                            this.selectionBox.setStroke(this.selectionBoxColor);
                            this.selectionBox.setStrokeWidth(this.selectionBoxWidth);

                            this.paneListener.getChildren().add(1, this.selectionBox);
                        }

                        // Adjusting if the width & height are negative...
                        if (mouseDiffPos.GetLeftDouble() < 0)
                            this.selectionBox.setTranslateX(this.mouseDragFirstPos.GetLeftDouble() + mouseDiffPos.GetLeftDouble() * this.zoom / this.mouseDragFactor);
                        else
                            this.selectionBox.setTranslateX(this.mouseDragFirstPos.GetLeftDouble());

                        if (mouseDiffPos.GetRightDouble() < 0)
                            this.selectionBox.setTranslateY(this.mouseDragFirstPos.GetRightDouble() + mouseDiffPos.GetRightDouble() * this.zoom / this.mouseDragFactor);
                        else
                            this.selectionBox.setTranslateY(this.mouseDragFirstPos.GetRightDouble());

                        this.selectionBox.setWidth(Math.abs(mouseDiffPos.GetLeftDouble() * this.zoom / this.mouseDragFactor));
                        this.selectionBox.setHeight(Math.abs(mouseDiffPos.GetRightDouble() * this.zoom / this.mouseDragFactor));
                    }
                }
                else if (this.editor.Editor_PressedRMB)
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

                // Handling shape rotation (only if we have shapes selected and R is pressed)
                if ((this.shapesSelected > 0) && EDAmameController.Controller_IsKeyPressed(KeyCode.R))
                {
                    for (int i = 0; i < this.shapes.size(); i++)
                    {
                        RenderShape shape = this.shapes.get(i);

                        if (!shape.selected)
                            continue;

                        double angle = 10;

                        if (event.getDeltaY() < 0)
                            angle = -10;

                        shape.shape.setRotate(shape.shape.getRotate() + angle);

                        //shape.CalculateShapeSelected();
                        //shape.CalculateShapeHighlighted();

                        this.shapes.set(i, shape);
                    }
                }
                // Handling zoom scaling (only if we're not rotating anything)
                else
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