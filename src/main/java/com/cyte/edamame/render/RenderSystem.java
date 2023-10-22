/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.render;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.util.PairMutable;

import java.util.LinkedList;
import java.util.UUID;

import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;

public class RenderSystem
{
    //// GLOBAL VARIABLES ////

    final public UUID id = UUID.randomUUID();

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

    // DO NOT EDIT

    public LinkedList<RenderNode> nodes;
    public PairMutable center;

    //// CONSTRUCTORS ////

    public RenderSystem(Pane paneListenerValue, Pane paneHolderValue, Pane paneHighlightsValue, Pane paneSelectionsValue, Canvas canvasValue, Shape crosshairValue, PairMutable theaterSizeValue, Color backgroundColorValue, Color gridPointColorValue, Color gridBoxColorValue, Integer maxShapesValue)
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

        this.nodes = new LinkedList<RenderNode>();
        this.center = new PairMutable(0.0, 0.0);

        this.RenderSystem_PaneSetTranslate(new PairMutable(0.0, 0.0));
        this.paneHolder.prefWidthProperty().bind(this.canvas.widthProperty());
        this.paneHolder.prefHeightProperty().bind(this.canvas.heightProperty());
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

    public void RenderSystem_NodeAdd(RenderNode renderNode)
    {
        if (this.nodes.size() >= this.maxShapes)
            throw new java.lang.Error("ERROR: Exceeded render system maximum render nodes limit!");

        this.nodes.add(renderNode);
        this.paneHolder.getChildren().add(1, renderNode.node);
    }

    public void RenderSystem_NodeHighlightsRemove(RenderNode renderNode)
    {
        for (int i = 0; i < this.paneHighlights.getChildren().size(); i++)
        {
            Shape highlightShape = (Shape)this.paneHighlights.getChildren().get(i);

            if (renderNode.id.equals(highlightShape.getId()))
            {
                this.paneHighlights.getChildren().remove(i);
                i--;
            }
        }
    }

    public void RenderSystem_NodeSelectionsRemove(RenderNode renderNode)
    {
        for (int i = 0; i < this.paneSelections.getChildren().size(); i++)
        {
            Shape selectionShape = (Shape)this.paneSelections.getChildren().get(i);

            if (renderNode.id.equals(selectionShape.getId()))
            {
                this.paneSelections.getChildren().remove(i);
                i--;
            }
        }
    }
}