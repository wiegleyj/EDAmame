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
    public Pane paneSnaps;
    public Canvas canvas;
    public GraphicsContext gc;
    public Shape crosshair;
    public PairMutable theaterSize;
    public Color backgroundColor;
    public Color gridPointColor;
    public Color gridBoxColor;
    public Integer maxShapes;

    public LinkedList<RenderNode> nodes;
    public PairMutable center;

    //// CONSTRUCTORS ////

    public RenderSystem(Pane paneListenerValue, Pane paneHolderValue, Pane paneHighlightsValue, Pane paneSelectionsValue, Pane paneSnapsValue, Canvas canvasValue, Shape crosshairValue, PairMutable theaterSizeValue, Color backgroundColorValue, Color gridPointColorValue, Color gridBoxColorValue, Integer maxShapesValue)
    {
        this.paneListener = paneListenerValue;
        this.paneHolder = paneHolderValue;
        this.paneHighlights = paneHighlightsValue;
        this.paneSelections = paneSelectionsValue;
        this.paneSnaps = paneSnapsValue;
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

        this.PaneHolderSetTranslate(new PairMutable(0.0, 0.0));
        this.paneHolder.prefWidthProperty().bind(this.canvas.widthProperty());
        this.paneHolder.prefHeightProperty().bind(this.canvas.heightProperty());
    }

    //// RENDERING FUNCTIONS ////

    public void CanvasRenderGrid()
    {
        // Clearing the canvas
        this.CanvasClear();

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

    public void CanvasClear()
    {
        this.gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        this.gc.setFill(this.backgroundColor);
        this.gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    //// PANE FUNCTIONS ////

    public PairMutable PaneHolderGetDrawPos(PairMutable pos)
    {
        return new PairMutable(pos.GetLeftDouble() + this.paneHolder.getWidth() / 2,
                              pos.GetRightDouble() + this.paneHolder.getHeight() / 2);
    }

    public PairMutable PaneHolderGetRealPos(PairMutable pos)
    {
        return new PairMutable(pos.GetLeftDouble() - this.paneHolder.getWidth() / 2,
                               pos.GetRightDouble() - this.paneHolder.getHeight() / 2);
    }

    public PairMutable PaneHolderGetRealCenter()
    {
        return new PairMutable(-this.center.GetLeftDouble() + this.paneHolder.getWidth() / 2,
                               -this.center.GetRightDouble() + this.paneHolder.getHeight() / 2);
    }

    public PairMutable PanePosListenerToHolder(PairMutable pos)
    {
        Point2D newPos = this.paneHolder.parentToLocal(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public PairMutable PanePosHolderToListener(PairMutable pos)
    {
        Point2D newPos = this.paneHolder.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public void PaneHolderSetTranslate(PairMutable pos)
    {
        this.paneHolder.setTranslateX(pos.GetLeftDouble());
        this.paneHolder.setTranslateY(pos.GetRightDouble());
    }

    public void PaneHolderSetScale(PairMutable scale, boolean compensate)
    {
        PairMutable prevScale = this.PaneHolderGetScale();

        this.paneHolder.setScaleX(scale.GetLeftDouble());
        this.paneHolder.setScaleY(scale.GetRightDouble());

        if (compensate)
        {
            PairMutable scaleDelta = new PairMutable(scale.GetLeftDouble() - prevScale.GetLeftDouble(),
                                                     scale.GetRightDouble() - prevScale.GetRightDouble());
            PairMutable newPos = this.PaneHolderGetTranslate();

            newPos.left = newPos.GetLeftDouble() + this.center.GetLeftDouble() * scaleDelta.GetLeftDouble();
            newPos.right = newPos.GetRightDouble() + this.center.GetRightDouble() * scaleDelta.GetRightDouble();

            this.PaneHolderSetTranslate(newPos);
        }
    }

    public PairMutable PaneHolderGetScale()
    {
        return new PairMutable(this.paneHolder.getScaleX(), this.paneHolder.getScaleY());
    }

    public PairMutable PaneHolderGetTranslate()
    {
        return new PairMutable(this.paneHolder.getTranslateX(), this.paneHolder.getTranslateY());
    }

    //// NODE FUNCTIONS ////

    public int NodeFind(String id)
    {
        for (int i = 0; i < this.nodes.size(); i++)
            if (this.nodes.get(i).id.equals(id))
                return i;

        return -1;
    }

    public void NodesAdd(LinkedList<RenderNode> renderNodes)
    {
        for (int i = 0; i < renderNodes.size(); i++)
            this.NodeAdd(renderNodes.get(i));
    }

    public void NodeAdd(RenderNode renderNode)
    {
        if (this.nodes.size() >= this.maxShapes)
            throw new java.lang.Error("ERROR: Exceeded render system maximum render nodes limit!");

        this.nodes.add(renderNode);
        this.paneHolder.getChildren().add(1, renderNode.node);

        if (!renderNode.passive)
        {
            this.paneHighlights.getChildren().add(renderNode.shapeHighlighted);
            this.paneSelections.getChildren().add(renderNode.shapeSelected);
        }

        for (int i = 0; i < renderNode.manualSnapPoints.size(); i++)
            this.paneSnaps.getChildren().add(renderNode.manualSnapPoints.get(i));
    }

    public void NodesClear()
    {
        while (!this.nodes.isEmpty())
            NodeRemove(this.nodes.get(0).name);
    }

    public RenderNode NodeRemove(String name)
    {
        for (int i = 0; i < this.nodes.size(); i++)
        {
            RenderNode renderNode = this.nodes.get(i);

            if (renderNode.name.equals(name))
            {
                this.nodes.remove(renderNode);
                this.paneHolder.getChildren().remove(renderNode.node);

                if (!renderNode.passive)
                {
                    this.paneHighlights.getChildren().remove(renderNode.shapeHighlighted);
                    this.paneSelections.getChildren().remove(renderNode.shapeSelected);
                }

                renderNode.highlighted = false;
                renderNode.selected = false;

                for (int j = 0; j < renderNode.manualSnapPoints.size(); j++)
                    this.paneSnaps.getChildren().remove(renderNode.manualSnapPoints.get(j));

                return renderNode;
            }
        }

        return null;
    }

    public LinkedList<RenderNode> NodesClone()
    {
        LinkedList<RenderNode> clonedNodes = new LinkedList<RenderNode>();

        for (int i = 0; i < this.nodes.size(); i++)
            clonedNodes.add(this.nodes.get(i).Clone());

        return clonedNodes;
    }

    //// TESTING FUNCTIONS ////

    public void TestShapeAdd(PairMutable pos, Double radius, Color color, double opacity, boolean passive)
    {
        Circle testShape = new Circle(radius, color);

        if (passive)
            testShape.setId("testShapePassive");
        else
            testShape.setId("testShapeActive");

        testShape.setTranslateX(pos.GetLeftDouble());
        testShape.setTranslateY(pos.GetRightDouble());
        testShape.setOpacity(opacity);

        this.paneHolder.getChildren().add(testShape);
    }
}