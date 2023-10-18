/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.render;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.util.PairMutable;

import java.util.UUID;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;

public class RenderNode
{
    final public String id = UUID.randomUUID().toString();

    public String name;
    public Node node;
    public Rectangle shapeHighlighted;
    public Rectangle shapeSelected;
    public boolean highlighted;
    public boolean highlightedMouse;
    public boolean highlightedBox;
    public boolean selected;
    public PairMutable mousePressPos;

    public RenderNode(String nameValue, Node nodeValue)
    {
        this.name = nameValue;
        this.node = nodeValue;
        this.node.setId(id);
        this.highlighted = false;
        this.highlightedMouse = false;
        this.highlightedBox = false;
        this.selected = false;
        this.mousePressPos = null;

        // Creating highlighted & selected shapes...
        {
            this.shapeHighlighted = new Rectangle();
            this.shapeHighlighted.setFill(Color.GRAY);
            this.shapeHighlighted.setOpacity(0.5);
            this.shapeHighlighted.setId(this.id);
            this.shapeHighlighted.translateXProperty().bind(this.node.translateXProperty());
            this.shapeHighlighted.translateYProperty().bind(this.node.translateYProperty());
            this.shapeHighlighted.rotateProperty().bind(this.node.rotateProperty());

            this.shapeSelected = new Rectangle();
            this.shapeSelected.setFill(Color.GRAY);
            this.shapeSelected.setOpacity(0.5);
            this.shapeSelected.setId(this.id);
            this.shapeSelected.translateXProperty().bind(this.node.translateXProperty());
            this.shapeSelected.translateYProperty().bind(this.node.translateYProperty());
            this.shapeSelected.rotateProperty().bind(this.node.rotateProperty());
        }

        this.RenderNode_BoundsRefresh();
    }

    public boolean RenderNode_PosOnNode(PairMutable pos)
    {
        return this.node.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    public void RenderNode_BoundsRefresh()
    {
        if (this.node.getClass() == Label.class)
        {
            Bounds bounds = this.node.getBoundsInLocal();
            EDAmameController.Controller_RenderShapesDelayedBoundsRefresh.add(new PairMutable(new PairMutable(this, 0), new PairMutable(bounds.getWidth(), bounds.getHeight())));
        }
        else
        {
            this.RenderNode_ShapeSelectedRefresh();
            this.RenderNode_ShapeHighlightedRefresh();
        }
    }

    public void RenderNode_ShapeSelectedRefresh()
    {
        Bounds bounds = this.node.getBoundsInLocal();

        if ((this.node.getClass() == Rectangle.class) ||
            (this.node.getClass() == Label.class))
        {
            this.shapeHighlighted.setLayoutX(0);
            this.shapeHighlighted.setLayoutY(0);
        }
        else
        {
            this.shapeHighlighted.setLayoutX(-bounds.getWidth() / 2);
            this.shapeHighlighted.setLayoutY(-bounds.getHeight() / 2);
        }

        this.shapeHighlighted.setWidth(bounds.getWidth());
        this.shapeHighlighted.setHeight(bounds.getHeight());
    }

    public void RenderNode_ShapeHighlightedRefresh()
    {
        Bounds bounds = this.node.getBoundsInLocal();

        if ((this.node.getClass() == Rectangle.class) ||
            (this.node.getClass() == Label.class))
        {
            this.shapeSelected.setLayoutX(0);
            this.shapeSelected.setLayoutY(0);
        }
        else
        {
            this.shapeSelected.setLayoutX(-bounds.getWidth() / 2);
            this.shapeSelected.setLayoutY(-bounds.getHeight() / 2);
        }

        this.shapeSelected.setWidth(bounds.getWidth());
        this.shapeSelected.setHeight(bounds.getHeight());
    }
}