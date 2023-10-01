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

public class RenderSystem
{
    //// GLOBAL VARIABLES ////

    final UUID id = UUID.randomUUID();

    public Pane paneListener;
    public Pane paneHolder;
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

    public RenderSystem(Editor editorValue, Pane paneListenerValue, Pane paneHolderValue, Canvas canvasValue, PairMutable theaterSizeValue, Color backgroundColorValue, Integer maxShapesValue, PairMutable zoomLimitsValue, Double zoomFactorValue, Double mouseDragFactorValue, Double mouseDragCheckTimeoutValue)
    {
        this.paneListener = paneListenerValue;
        this.paneHolder = paneHolderValue;
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

        // Centering the canvas
        //System.out.println(this.stackPane.getWidth());
        //System.out.println(this.stackPane.getHeight());

        //this.canvas.setLayoutX(-this.stackPane.getWidth() / 2);
        //this.canvas.setLayoutY(-this.stackPane.getHeight() / 2);

        // Loading the point grid
        RenderShape gridPointBlueprint = EDAmameController.Controller_BasicShapes.get(Utils.FindCanvasShape(EDAmameController.Controller_BasicShapes, "GridPoint"));

        Double posX = -2500.0;
        Double posY = -2500.0;

        for (int i = 0; i < 50; i++)
        {
            for (int j = 0; j < 50; j++)
            {
                RenderShape gridPoint = new RenderShape(gridPointBlueprint);
                gridPoint.posDraw = new PairMutable(posX, posY);

                this.RenderSystem_CanvasDrawShape(gridPoint);
                posX += 100.0;
            }

            posX = -2500.0;
            posY += 100.0;
            //System.out.println(posY);
        }

        // Loading the grid box
        RenderShape gridBox = new RenderShape(EDAmameController.Controller_BasicShapes.get(Utils.FindCanvasShape(EDAmameController.Controller_BasicShapes, "GridBox")));
        gridBox.posDraw = new PairMutable(this.canvas.getWidth() / 2, this.canvas.getHeight() / 2);
        this.RenderSystem_CanvasDrawShape(gridBox);

        // Loading the center crosshair
        RenderShape crosshair = new RenderShape(EDAmameController.Controller_BasicShapes.get(Utils.FindCanvasShape(EDAmameController.Controller_BasicShapes, "Crosshair")));
        crosshair.posDraw = new PairMutable(editor.Editor_RenderSystem.canvas.getWidth() / 2, editor.Editor_RenderSystem.canvas.getHeight() / 2);
        this.RenderSystem_CanvasDrawShape(crosshair);
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

    public void RenderSystem_PaneSetLayout(PairMutable pos)
    {
        this.paneHolder.setLayoutX(pos.GetLeftDouble());
        this.paneHolder.setLayoutY(pos.GetRightDouble());
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

    public PairMutable RenderSystem_PaneGetLayout()
    {
        return new PairMutable(this.paneHolder.getLayoutX(), this.paneHolder.getLayoutY());
    }

    public PairMutable RenderSystem_PaneGetTranslate()
    {
        return new PairMutable(this.paneHolder.getTranslateX(), this.paneHolder.getTranslateY());
    }

    //// CANVAS FUNCTIONS ////

    /*public void CanvasSetLayout(PairMutable pos)
    {
        this.canvas.setLayoutX(pos.GetLeftDouble());
        this.canvas.setLayoutY(pos.GetRightDouble());
    }

    public void CanvasSetTranslate(PairMutable pos)
    {
        this.canvas.setTranslateX(pos.GetLeftDouble());
        this.canvas.setTranslateY(pos.GetRightDouble());
    }

    public void CanvasSetScale(PairMutable scale)
    {
        PairMutable prevScale = this.CanvasGetScale();

        this.canvas.setScaleX(scale.GetLeftDouble());
        this.canvas.setScaleY(scale.GetRightDouble());

        PairMutable scaleDelta = new PairMutable(scale.GetLeftDouble() - prevScale.GetLeftDouble(),
                                                 scale.GetRightDouble() - prevScale.GetRightDouble());
        PairMutable newPos = this.CanvasGetTranslate();

        newPos.left = newPos.GetLeftDouble() + this.center.GetLeftDouble() * scaleDelta.GetLeftDouble();
        newPos.right = newPos.GetRightDouble() + this.center.GetRightDouble() * scaleDelta.GetRightDouble();

        this.CanvasSetTranslate(newPos);

        //System.out.println(scaleDelta.ToStringDouble());
        //System.out.println(newPos.ToStringDouble());
    }

    public PairMutable CanvasGetScale()
    {
        return new PairMutable(this.canvas.getScaleX(), this.canvas.getScaleY());
    }

    public PairMutable CanvasGetLayout()
    {
        return new PairMutable(this.canvas.getLayoutX(), this.canvas.getLayoutY());
    }

    public PairMutable CanvasGetTranslate()
    {
        return new PairMutable(this.canvas.getTranslateX(), this.canvas.getTranslateY());
    }*/

    /*public void BindSize(Node node)
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
    }*/

    public void RenderSystem_CanvasSetSize(PairMutable sizeValue)
    {
        //this.UnbindSize();

        this.canvas.setWidth(sizeValue.GetLeftDouble());
        this.canvas.setHeight(sizeValue.GetRightDouble());
    }

    public PairMutable RenderSystem_CanvasGetSize()
    {
        return new PairMutable(canvas.getWidth(), canvas.getHeight());
    }

    //// SHAPE FUNCTIONS ////

    public void RenderSystem_ShapeAdd(Integer idx, Shape shape)
    {
        if (idx < 0)
            this.paneHolder.getChildren().add(shape);
        else
            this.paneHolder.getChildren().add(idx, shape);
    }

    public void RenderSystem_ShapeCalculatePosDraw(RenderShape shape)
    {
        shape.posDraw.left = shape.posReal.GetLeftDouble() * this.zoom + this.canvas.getWidth() / 2;
        shape.posDraw.right = shape.posReal.GetRightDouble() * this.zoom + this.canvas.getHeight() / 2;
    }

    public void RenderSystem_CanvasDrawShape(RenderShape shape)
    {
        if (shape.posReal != null)
            this.RenderSystem_ShapeCalculatePosDraw(shape);

        shape.DrawCanvas(this.gc);
    }

    /*public void AddShape(Integer idx, RenderShape shape)
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
    }*/

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
            {}

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
}