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
    public Canvas canvas;
    public GraphicsContext gc;
    public PairMutable theaterSize;
    public Color backgroundColor;
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

    public Editor editor;

    //// CONSTRUCTORS ////

    public RenderSystem(Editor editorValue, Pane paneListenerValue, Pane paneHolderValue, Pane paneHighlightsValue, Canvas canvasValue, PairMutable theaterSizeValue, Color backgroundColorValue, Integer maxShapesValue, PairMutable zoomLimitsValue, Double zoomFactorValue, Double mouseDragFactorValue, Double mouseDragCheckTimeoutValue)
    {
        this.paneListener = paneListenerValue;
        this.paneHolder = paneHolderValue;
        this.paneHighlights = paneHighlightsValue;
        this.canvas = canvasValue;
        this.gc = this.canvas.getGraphicsContext2D();
        this.theaterSize = theaterSizeValue;
        this.backgroundColor = backgroundColorValue;
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

        this.editor = editorValue;

        this.RenderSystem_PaneSetTranslate(new PairMutable(0.0, 0.0));
        this.RenderSystem_ListenersInit();
    }

    //// RENDERING FUNCTIONS ////

    public void RenderSystem_CanvasRenderGrid()
    {
        this.RenderSystem_CanvasClear();

        gc.setFill(Color.GRAY);
        gc.setGlobalAlpha(0.5);
        Double width = 5.0;

        Double posX = -2500.0;
        Double posY = -2500.0;

        for (int i = 0; i < 50; i++)
        {
            for (int j = 0; j < 50; j++)
            {
                gc.fillOval(posX - (width / 2), posY - (width / 2), width, width);

                posX += 100.0;
            }

            posX = -2500.0;
            posY += 100.0;
        }
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

    public void RenderSystem_ShapeAdd(RenderShape shape)
    {
        this.shapes.add(shape);
        this.paneHolder.getChildren().add(1, shape.shape);

    }

    //// CALLBACK FUNCTIONS ////

    public void RenderSystem_ListenersInit()
    {
        // When we drag the mouse (from outside the viewport)...
        this.paneListener.setOnDragOver(event -> {
            // Handling global callback actions
            {}

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnDragOver(event);

            event.consume();
        });

        // When we drop something with the cursor (from outside the viewport)...
        this.paneListener.setOnDragDropped(event -> {
            // Handling global callback actions
            {}

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnDragDropped(event);

            event.setDropCompleted(true);
            event.consume();
        });

        // When we move the mouse (without clicking)...
        this.paneListener.setOnMouseMoved(event -> {
            // Handling global callback actions
            {
                for (int i = 0; i < this.shapes.size(); i++)
                {
                    PairMutable posMouse = this.RenderSystem_PaneConvertListenerPos(new PairMutable(event.getX(), event.getY()));
                    RenderShape shape = this.shapes.get(i);
                    boolean onShape = shape.PosOnShape(posMouse);

                    if (onShape && !shape.highlighted)
                    {
                        // Adding highlight for shape
                        shape.CalculateShapeHighlighted();
                        this.paneHighlights.getChildren().add(shape.shapeHighlighted);

                        shape.highlighted = true;
                    }
                    else if (!onShape && shape.highlighted)
                    {
                        // Remove highlight for shape
                        this.RenderSystem_ShapeHighlightRemove(shape.id);

                        shape.highlighted = false;
                    }

                    this.shapes.set(i, shape);
                }
            }

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMouseMoved(event);

            event.consume();
        });

        // When we press down the mouse...
        this.paneListener.setOnMousePressed(event -> {
            // Updating mouse pressed flags
            {
                if (event.isPrimaryButtonDown())
                    this.editor.Editor_PressedLMB = true;
                if (event.isSecondaryButtonDown())
                    this.editor.Editor_PressedRMB = true;
            }

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

            //Circle testShape = new Circle(50, Color.BLUE);
            //testShape.setLayoutX(50.0);
            //testShape.setLayoutY(50.0);
            //this.RenderSystem_ShapeAdd(0, testShape);
            //System.out.println("Hi");

            event.consume();
        });

        // When we release the mouse...
        this.paneListener.setOnMouseReleased(event -> {
            // Handling global callback actions
            {
                if (this.editor.Editor_PressedLMB)
                {}

                if (this.editor.Editor_PressedRMB)
                {
                    // Handling auto-zoom
                    if (EDAmameController.Controller_IsKeyPressed(KeyCode.ALT))
                    {
                        /*for (int i = 0; i < this.shapes.size(); i++)
                        {
                            RenderShape shape = this.shapes.get(i);

                            if (shape.posStatic)
                                continue;

                            shape.posReal = new PairMutable(shape.posReal.GetLeftDouble() - center.GetLeftDouble(),
                                                            shape.posReal.GetRightDouble() - center.GetRightDouble());

                            this.shapes.set(i, shape);
                        }*/

                        this.RenderSystem_PaneSetTranslate(new PairMutable(0.0, 0.0));
                        this.RenderSystem_PaneSetScale(new PairMutable(1.0, 1.0), false);

                        this.center = new PairMutable(0.0, 0.0);
                        this.zoom = 1.0;
                    }

                    for (int i = 0; i < this.shapes.size(); i++)
                    {
                        PairMutable posMouse = this.RenderSystem_PaneConvertListenerPos(new PairMutable(event.getX(), event.getY()));

                        boolean onShape = this.shapes.get(i).PosOnShape(posMouse);
                        System.out.println(onShape);
                    }
                }
            }

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMouseReleased(event);

            // Updating mouse pressed flags
            {
                this.editor.Editor_PressedLMB = false;
                this.editor.Editor_PressedRMB = false;
            }

            event.consume();
        });

        // When we drag the mouse (from inside the viewport)...
        this.paneListener.setOnMouseDragged(event -> {
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
                this.mouseDragPaneFirstPos = new PairMutable(this.RenderSystem_PaneGetTranslate());

                /*for (int i = 0; i < this.shapes.size(); i++)
                {
                    RenderShape shape = this.shapes.get(i);

                    if (shape.posStatic)
                        continue;

                    shape.posMousePress = new PairMutable(shape.posReal);
                    //shape.posEdgeOffset = new PairMutable(new PairMutable(0.0, 0.0));

                    this.shapes.set(i, shape);
                }*/
            }

            PairMutable mouseDiffPos = new PairMutable((posMouse.GetLeftDouble() - this.mouseDragFirstPos.GetLeftDouble()) * this.mouseDragFactor / this.zoom,
                                                       (posMouse.GetRightDouble() - this.mouseDragFirstPos.GetRightDouble()) * this.mouseDragFactor / this.zoom);

            // Handling global callback actions
            {
                if (this.editor.Editor_PressedLMB)
                {}

                if (this.editor.Editor_PressedRMB)
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

                        /*for (int i = 0; i < this.shapes.size(); i++)
                        {
                            RenderShape shape = this.shapes.get(i);

                            if (shape.posStatic)
                                continue;

                            shape.posReal = new PairMutable(shape.posMousePress.GetLeftDouble() + mouseDiffPos.GetLeftDouble(),
                                                            shape.posMousePress.GetRightDouble() + mouseDiffPos.GetRightDouble());

                            this.shapes.set(i, shape);
                        }*/

                        this.RenderSystem_PaneSetTranslate(new PairMutable(this.mouseDragPaneFirstPos.GetLeftDouble() + mouseDiffPos.GetLeftDouble() * this.zoom,
                                                              this.mouseDragPaneFirstPos.GetRightDouble() + mouseDiffPos.GetRightDouble() * this.zoom));

                        //System.out.println(new PairMutable(this.canvas.getLayoutX(), this.canvas.getLayoutY()).ToStringDouble());
                        //System.out.println(new PairMutable(this.canvas.getTranslateX(), this.canvas.getTranslateY()).ToStringDouble());
                        //System.out.println(this.center.ToStringDouble());
                    }
                }
            }

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMouseDragged(event);

            this.mouseDragLastTime = System.nanoTime();

            event.consume();
        });

        // When we scroll the mouse...
        this.paneListener.setOnScroll(event -> {
            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnScroll(event);

            // Handling global callback actions
            {
                // Handling zoom scaling (only if we're not rotating anything)
                PairMutable newPos = this.RenderSystem_PaneGetTranslate();

                if (!this.editor.Editor_Rotating)
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

                        //newPos.left = newPos.GetLeftDouble() + this.center.GetLeftDouble() / this.zoom;
                        //newPos.right = newPos.GetRightDouble() + this.center.GetRightDouble() / this.zoom;
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

                        //newPos.left = newPos.GetLeftDouble() + this.center.GetLeftDouble() * this.zoom;
                        //newPos.right = newPos.GetRightDouble() + this.center.GetRightDouble() * this.zoom;
                    }

                    this.RenderSystem_PaneSetScale(new PairMutable(this.zoom, this.zoom), true);
                    //this.CanvasSetTranslate(newPos);

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

    //// SUPPORT FUNCTIONS ////

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
}