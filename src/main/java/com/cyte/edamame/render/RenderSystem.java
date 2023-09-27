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

public class RenderSystem
{
    //// GLOBAL VARIABLES ////

    final UUID id = UUID.randomUUID();

    public StackPane stackPane;
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
    public PairMutable mouseDragCanvasFirstPos;

    public Editor editor;

    //// CONSTRUCTORS ////

    public RenderSystem(Editor editorValue, StackPane stackPaneValue, Canvas canvasValue, PairMutable theaterSizeValue, Color backgroundColorValue, Integer maxShapesValue, PairMutable zoomLimitsValue, Double zoomFactorValue, Double mouseDragFactorValue, Double mouseDragCheckTimeoutValue)
    {
        this.stackPane = stackPaneValue;
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
        this.mouseDragCanvasFirstPos = null;

        this.editor = editorValue;
        //this.canvas.widthProperty().bind(this.stackPane.widthProperty());
        //this.canvas.heightProperty().bind(this.stackPane.heightProperty());
        this.ListenersInit();
    }

    //// RENDERING FUNCTIONS ////

    public void CanvasRenderGrid()
    {
        this.CanvasClear();

        // Centering the canvas
        //System.out.println(this.stackPane.getWidth());
        //System.out.println(this.stackPane.getHeight());

        //this.canvas.setLayoutX(-this.stackPane.getWidth() / 2);
        //this.canvas.setLayoutY(-this.stackPane.getHeight() / 2);

        // Loading the point grid
        RenderShape gridPointBlueprint = EDAmameController.Global_BasicShapes.get(Utils.FindCanvasShape(EDAmameController.Global_BasicShapes, "GridPoint"));

        Double posX = -2500.0;
        Double posY = -2500.0;

        for (int i = 0; i < 50; i++)
        {
            for (int j = 0; j < 50; j++)
            {
                RenderShape gridPoint = new RenderShape(gridPointBlueprint);
                gridPoint.posDraw = new PairMutable(posX, posY);

                this.CanvasDrawShape(gridPoint);
                posX += 100.0;
            }

            posX = -2500.0;
            posY += 100.0;
            //System.out.println(posY);
        }

        // Loading the grid box
        RenderShape gridBox = new RenderShape(EDAmameController.Global_BasicShapes.get(Utils.FindCanvasShape(EDAmameController.Global_BasicShapes, "GridBox")));
        gridBox.posDraw = new PairMutable(this.canvas.getWidth() / 2, this.canvas.getHeight() / 2);
        this.CanvasDrawShape(gridBox);

        // Loading the center crosshair
        RenderShape crosshair = new RenderShape(EDAmameController.Global_BasicShapes.get(Utils.FindCanvasShape(EDAmameController.Global_BasicShapes, "Crosshair")));
        crosshair.posDraw = new PairMutable(editor.Editor_RenderSystem.canvas.getWidth() / 2, editor.Editor_RenderSystem.canvas.getHeight() / 2);
        this.CanvasDrawShape(crosshair);
    }

    public void CanvasClear()
    {
        this.gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        this.gc.setFill(this.backgroundColor);
        this.gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    //// CANVAS FUNCTIONS ////

    public void CanvasSetPos(PairMutable pos)
    {
        this.canvas.setLayoutX(pos.GetLeftDouble());
        this.canvas.setLayoutY(pos.GetRightDouble());
    }

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

    /*public void SetSize(PairMutable sizeValue)
    {
        //this.UnbindSize();

        this.canvas.setWidth(sizeValue.GetLeftDouble());
        this.canvas.setHeight(sizeValue.GetRightDouble());
    }

    public PairMutable GetSize()
    {
        return new PairMutable(canvas.getWidth(), canvas.getHeight());
    }*/

    //// SHAPE FUNCTIONS ////

    public PairMutable CalculatePosDraw(RenderShape shape)
    {
        PairMutable posDraw = new PairMutable(shape.posReal);

        posDraw.left = posDraw.GetLeftDouble() * this.zoom + this.canvas.getWidth() / 2;
        posDraw.right = posDraw.GetRightDouble() * this.zoom + this.canvas.getHeight() / 2;

        return posDraw;
    }

    public void CanvasDrawShape(RenderShape shape)
    {
        if (shape.posReal != null)
            this.CalculatePosDraw(shape);

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

    public void ListenersInit()
    {
        // When we drag the mouse (from outside the viewport)...
        this.stackPane.setOnDragOver(event -> {
            // Handling global callback actions
            {}

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnDragOver();

            event.consume();
        });

        // When we drop something with the cursor (from outside the viewport)...
        this.stackPane.setOnDragDropped(event -> {
            // Handling global callback actions
            {}

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnDragDropped();

            event.setDropCompleted(true);
            event.consume();
        });

        // When we move the mouse (without clicking)...
        this.stackPane.setOnMouseMoved(event -> {
            // Handling global callback actions
            {}

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMouseMoved();

            event.consume();
        });

        // When we press down the mouse...
        this.stackPane.setOnMousePressed(event -> {
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
                this.mouseDragCanvasFirstPos = null;
            }

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMousePressed();

            event.consume();
        });

        // When we release the mouse...
        this.stackPane.setOnMouseReleased(event -> {
            // Handling global callback actions
            {
                if (this.editor.Editor_PressedLMB)
                {}

                if (this.editor.Editor_PressedRMB)
                {
                    // Handling auto-zoom
                    if (EDAmameController.isGlobalKeyPressed(KeyCode.ALT))
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

                        this.center = new PairMutable(0.0, 0.0);
                        this.zoom = 1.0;
                    }
                }
            }

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMouseReleased();

            // Updating mouse pressed flags
            {
                this.editor.Editor_PressedLMB = false;
                this.editor.Editor_PressedRMB = false;
            }

            event.consume();
        });

        // When we drag the mouse (from inside the viewport)...
        this.stackPane.setOnMouseDragged(event -> {
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
                this.mouseDragCanvasFirstPos = new PairMutable(this.canvas.getLayoutX(),
                                                               this.canvas.getLayoutY());

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

                        this.canvas.setLayoutX(this.mouseDragCanvasFirstPos.GetLeftDouble() + mouseDiffPos.GetLeftDouble());
                        this.canvas.setLayoutY(this.mouseDragCanvasFirstPos.GetRightDouble() + mouseDiffPos.GetRightDouble());

                        //System.out.println(this.stackPane.getWidth());
                        //System.out.println(this.stackPane.getHeight());
                    }
                }
            }

            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnMouseDragged(mouseDiffPos);

            this.mouseDragLastTime = System.nanoTime();

            event.consume();
        });

        // When we scroll the mouse...
        this.stackPane.setOnScroll(event -> {
            // Handling editor-specific callback actions
            this.editor.Editor_ViewportOnScroll();

            // Handling global callback actions
            {
                // Handling zoom scaling (only if we're not rotating anything)
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