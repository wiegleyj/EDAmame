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

    final public UUID RenderSystem_ID = UUID.randomUUID();

    public Pane RenderSystem_PaneListener;
    public Pane RenderSystem_PaneHolder;
    public Pane RenderSystem_PaneHighlights;
    public Pane RenderSystem_PaneSelections;
    public Canvas RenderSystem_Canvas;
    public GraphicsContext RenderSystem_GC;
    public Shape RenderSystem_Crosshair;
    public PairMutable RenderSystem_TheaterSize;
    public Color RenderSystem_BackgroundColor;
    public Color RenderSystem_GridPointColor;
    public Color RenderSystem_GridBoxColor;
    public Integer RenderSystem_MaxShapes;

    // DO NOT EDIT

    public LinkedList<RenderNode> RenderSystem_Nodes;
    public PairMutable RenderSystem_Center;

    //// CONSTRUCTORS ////

    public RenderSystem(Pane paneListenerValue, Pane paneHolderValue, Pane paneHighlightsValue, Pane paneSelectionsValue, Canvas canvasValue, Shape crosshairValue, PairMutable theaterSizeValue, Color backgroundColorValue, Color gridPointColorValue, Color gridBoxColorValue, Integer maxShapesValue)
    {
        this.RenderSystem_PaneListener = paneListenerValue;
        this.RenderSystem_PaneHolder = paneHolderValue;
        this.RenderSystem_PaneHighlights = paneHighlightsValue;
        this.RenderSystem_PaneSelections = paneSelectionsValue;
        this.RenderSystem_Canvas = canvasValue;
        this.RenderSystem_GC = this.RenderSystem_Canvas.getGraphicsContext2D();
        this.RenderSystem_Crosshair = crosshairValue;
        this.RenderSystem_TheaterSize = theaterSizeValue;
        this.RenderSystem_BackgroundColor = backgroundColorValue;
        this.RenderSystem_GridPointColor = gridPointColorValue;
        this.RenderSystem_GridBoxColor = gridBoxColorValue;
        this.RenderSystem_MaxShapes = maxShapesValue;

        this.RenderSystem_Nodes = new LinkedList<RenderNode>();
        this.RenderSystem_Center = new PairMutable(0.0, 0.0);

        this.RenderSystem_PaneSetTranslate(new PairMutable(0.0, 0.0));
        this.RenderSystem_PaneHolder.prefWidthProperty().bind(this.RenderSystem_Canvas.widthProperty());
        this.RenderSystem_PaneHolder.prefHeightProperty().bind(this.RenderSystem_Canvas.heightProperty());
    }

    //// RENDERING FUNCTIONS ////

    public void RenderSystem_CanvasRenderGrid()
    {
        // Clearing the canvas
        this.RenderSystem_CanvasClear();

        // Drawing the points
        RenderSystem_GC.setFill(this.RenderSystem_GridPointColor);
        RenderSystem_GC.setGlobalAlpha(1.0);
        Double width = 3.0;

        Double posX = -2500.0;
        Double posY = -2500.0;

        for (int i = 0; i < 70; i++)
        {
            for (int j = 0; j < 70; j++)
            {
                RenderSystem_GC.fillOval(posX - (width / 2), posY - (width / 2), width, width);

                posX += 100.0;
            }

            posX = -2500.0;
            posY += 100.0;
        }

        // Drawing the grid box
        RenderSystem_GC.setStroke(this.RenderSystem_GridBoxColor);
        RenderSystem_GC.setGlobalAlpha(1.0);
        RenderSystem_GC.setLineWidth(2.0);
        RenderSystem_GC.strokeLine(this.RenderSystem_Canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.RenderSystem_Canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                      this.RenderSystem_Canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.RenderSystem_Canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
        RenderSystem_GC.strokeLine(this.RenderSystem_Canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.RenderSystem_Canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                      this.RenderSystem_Canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.RenderSystem_Canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
        RenderSystem_GC.strokeLine(this.RenderSystem_Canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.RenderSystem_Canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                      this.RenderSystem_Canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.RenderSystem_Canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
        RenderSystem_GC.strokeLine(this.RenderSystem_Canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.RenderSystem_Canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                      this.RenderSystem_Canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                      this.RenderSystem_Canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
    }

    public void RenderSystem_CanvasClear()
    {
        this.RenderSystem_GC.clearRect(0, 0, this.RenderSystem_Canvas.getWidth(), this.RenderSystem_Canvas.getHeight());
        this.RenderSystem_GC.setFill(this.RenderSystem_BackgroundColor);
        this.RenderSystem_GC.fillRect(0, 0, this.RenderSystem_Canvas.getWidth(), this.RenderSystem_Canvas.getHeight());
    }

    //// PANE FUNCTIONS ////

    public PairMutable RenderSystem_PaneHolderGetRealPos(PairMutable pos)
    {
        return new PairMutable(pos.GetLeftDouble() - this.RenderSystem_PaneHolder.getWidth() / 2,
                               pos.GetRightDouble() - this.RenderSystem_PaneHolder.getHeight() / 2);
    }

    public PairMutable RenderSystem_PanePosListenerToHolder(PairMutable pos)
    {
        Point2D newPos = this.RenderSystem_PaneHolder.parentToLocal(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public PairMutable RenderSystem_PanePosHolderToListener(PairMutable pos)
    {
        Point2D newPos = this.RenderSystem_PaneHolder.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public void RenderSystem_PaneSetTranslate(PairMutable pos)
    {
        this.RenderSystem_PaneHolder.setTranslateX(pos.GetLeftDouble());
        this.RenderSystem_PaneHolder.setTranslateY(pos.GetRightDouble());
    }

    public void RenderSystem_PaneSetScale(PairMutable scale, boolean compensate)
    {
        PairMutable prevScale = this.RenderSystem_PaneGetScale();

        this.RenderSystem_PaneHolder.setScaleX(scale.GetLeftDouble());
        this.RenderSystem_PaneHolder.setScaleY(scale.GetRightDouble());

        if (compensate)
        {
            PairMutable scaleDelta = new PairMutable(scale.GetLeftDouble() - prevScale.GetLeftDouble(),
                                                     scale.GetRightDouble() - prevScale.GetRightDouble());
            PairMutable newPos = this.RenderSystem_PaneGetTranslate();

            newPos.left = newPos.GetLeftDouble() + this.RenderSystem_Center.GetLeftDouble() * scaleDelta.GetLeftDouble();
            newPos.right = newPos.GetRightDouble() + this.RenderSystem_Center.GetRightDouble() * scaleDelta.GetRightDouble();

            this.RenderSystem_PaneSetTranslate(newPos);
        }
    }

    public PairMutable RenderSystem_PaneGetScale()
    {
        return new PairMutable(this.RenderSystem_PaneHolder.getScaleX(), this.RenderSystem_PaneHolder.getScaleY());
    }

    public PairMutable RenderSystem_PaneGetTranslate()
    {
        return new PairMutable(this.RenderSystem_PaneHolder.getTranslateX(), this.RenderSystem_PaneHolder.getTranslateY());
    }

    //// NODE FUNCTIONS ////

    public void RenderSystem_NodeAdd(RenderNode renderNode)
    {
        if (this.RenderSystem_Nodes.size() >= this.RenderSystem_MaxShapes)
            throw new java.lang.Error("ERROR: Exceeded render system maximum render nodes limit!");

        this.RenderSystem_Nodes.add(renderNode);
        this.RenderSystem_PaneHolder.getChildren().add(1, renderNode.RenderNode_Node);
    }

    public RenderNode RenderSystem_NodeRemove(String name)
    {
        System.out.println(name);

        for (int i = 0; i < this.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.RenderSystem_Nodes.get(i);

            if (renderNode.RenderNode_Name.equals(name))
            {
                this.RenderSystem_Nodes.remove(renderNode);
                this.RenderSystem_PaneHolder.getChildren().remove(renderNode.RenderNode_Node);

                if (renderNode.RenderNode_Highlighted)
                {
                    this.RenderSystem_PaneHighlights.getChildren().remove(renderNode.RenderNode_ShapeHighlighted);
                    renderNode.RenderNode_Highlighted = false;
                }

                if (renderNode.RenderNode_Selected)
                {
                    this.RenderSystem_PaneSelections.getChildren().remove(renderNode.RenderNode_ShapeSelected);
                    renderNode.RenderNode_Selected = false;
                }

                return renderNode;
            }
        }

        return null;
    }

    public void RenderSystem_NodeHighlightsRemove(RenderNode renderNode)
    {
        for (int i = 0; i < this.RenderSystem_PaneHighlights.getChildren().size(); i++)
        {
            Shape highlightShape = (Shape)this.RenderSystem_PaneHighlights.getChildren().get(i);

            if (renderNode.RenderNode_ID.equals(highlightShape.getId()))
            {
                this.RenderSystem_PaneHighlights.getChildren().remove(i);
                i--;
            }
        }
    }

    public void RenderSystem_NodeSelectionsRemove(RenderNode renderNode)
    {
        for (int i = 0; i < this.RenderSystem_PaneSelections.getChildren().size(); i++)
        {
            Shape selectionShape = (Shape)this.RenderSystem_PaneSelections.getChildren().get(i);

            if (renderNode.RenderNode_ID.equals(selectionShape.getId()))
            {
                this.RenderSystem_PaneSelections.getChildren().remove(i);
                i--;
            }
        }
    }
}