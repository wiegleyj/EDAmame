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
    final public String RenderNode_ID = UUID.randomUUID().toString();

    public String RenderNode_Name;
    public Node RenderNode_Node;
    public Rectangle RenderNode_ShapeHighlighted;
    public Rectangle RenderNode_ShapeSelected;
    public boolean RenderNode_Highlighted;
    public boolean RenderNode_HighlightedMouse;
    public boolean RenderNode_HighlightedBox;
    public boolean RenderNode_Selected;
    public PairMutable RenderNode_MousePressPos;
    public boolean RenderNode_Passive;

    public RenderNode(String nameValue, Node nodeValue, boolean passiveValue)
    {
        this.RenderNode_Name = nameValue;
        this.RenderNode_Node = nodeValue;
        this.RenderNode_Node.setId(RenderNode_ID);
        this.RenderNode_Highlighted = false;
        this.RenderNode_HighlightedMouse = false;
        this.RenderNode_HighlightedBox = false;
        this.RenderNode_Selected = false;
        this.RenderNode_MousePressPos = null;
        this.RenderNode_Passive = passiveValue;

        // Creating highlighted & selected shapes...
        {
            this.RenderNode_ShapeHighlighted = new Rectangle();
            this.RenderNode_ShapeHighlighted.setFill(Color.GRAY);
            this.RenderNode_ShapeHighlighted.setOpacity(0.5);
            this.RenderNode_ShapeHighlighted.setId(this.RenderNode_ID);
            //this.RenderNode_ShapeHighlighted.translateXProperty().bind(this.RenderNode_Node.translateXProperty());
            //this.RenderNode_ShapeHighlighted.translateYProperty().bind(this.RenderNode_Node.translateYProperty());
            //this.RenderNode_ShapeHighlighted.rotateProperty().bind(this.RenderNode_Node.rotateProperty());

            this.RenderNode_ShapeSelected = new Rectangle();
            this.RenderNode_ShapeSelected.setFill(Color.GRAY);
            this.RenderNode_ShapeSelected.setOpacity(0.5);
            this.RenderNode_ShapeSelected.setId(this.RenderNode_ID);
            //this.RenderNode_ShapeSelected.translateXProperty().bind(this.RenderNode_Node.translateXProperty());
            //this.RenderNode_ShapeSelected.translateYProperty().bind(this.RenderNode_Node.translateYProperty());
            //this.RenderNode_ShapeSelected.rotateProperty().bind(this.RenderNode_Node.rotateProperty());
        }

        //RenderNode_ShapeHighlightedRefresh();
        //RenderNode_ShapeSelectedRefresh();
    }

    public boolean RenderNode_PosOnNode(PairMutable pos)
    {
        return this.RenderNode_Node.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    public void RenderNode_BoundsRefresh()
    {
        /*if (this.RenderNode_Node.getClass() == Label.class)
        {
            Bounds bounds = this.RenderNode_Node.getBoundsInLocal();
            EDAmameController.Controller_RenderShapesDelayedBoundsRefresh.add(new PairMutable(new PairMutable(this, 0), new PairMutable(bounds.getWidth(), bounds.getHeight())));
        }
        else
        {*/
            this.RenderNode_ShapeHighlightedRefresh();
            this.RenderNode_ShapeSelectedRefresh();
        //}
    }

    public void RenderNode_ShapeHighlightedRefresh()
    {
        Bounds boundsReal = this.RenderNode_Node.getBoundsInParent();

        this.RenderNode_ShapeHighlighted.setTranslateX(boundsReal.getMinX());
        this.RenderNode_ShapeHighlighted.setTranslateY(boundsReal.getMinY());

        this.RenderNode_ShapeHighlighted.setWidth(boundsReal.getWidth());
        this.RenderNode_ShapeHighlighted.setHeight(boundsReal.getHeight());
    }

    public void RenderNode_ShapeSelectedRefresh()
    {
        Bounds boundsLocal = this.RenderNode_Node.getBoundsInLocal();
        Bounds boundsReal = this.RenderNode_Node.getBoundsInParent();
        PairMutable posReal = new PairMutable((boundsReal.getMinX() + boundsReal.getMaxX()) / 2,
                                              (boundsReal.getMinY() + boundsReal.getMaxY()) / 2);

        if ((this.RenderNode_Node.getClass() == Rectangle.class) ||
            (this.RenderNode_Node.getClass() == Label.class))
        {
            this.RenderNode_ShapeSelected.setTranslateX(posReal.GetLeftDouble());
            this.RenderNode_ShapeSelected.setTranslateY(posReal.GetRightDouble());
        }
        else
        {
            this.RenderNode_ShapeSelected.setTranslateX(posReal.GetLeftDouble() - boundsLocal.getWidth() / 2);
            this.RenderNode_ShapeSelected.setTranslateY(posReal.GetRightDouble() - boundsLocal.getHeight() / 2);
        }

        this.RenderNode_ShapeSelected.setRotate(this.RenderNode_Node.getRotate());

        this.RenderNode_ShapeSelected.setWidth(boundsLocal.getWidth());
        this.RenderNode_ShapeSelected.setHeight(boundsLocal.getHeight());

        /*Bounds boundsLocal = this.RenderNode_Node.getBoundsInLocal();

        if ((this.RenderNode_Node.getClass() == Rectangle.class) ||
                (this.RenderNode_Node.getClass() == Label.class))
        {
            this.RenderNode_ShapeSelected.setLayoutX(0);
            this.RenderNode_ShapeSelected.setLayoutY(0);
        }
        else
        {
            this.RenderNode_ShapeSelected.setLayoutX(-boundsLocal.getWidth() / 2);
            this.RenderNode_ShapeSelected.setLayoutY(-boundsLocal.getHeight() / 2);
        }

        this.RenderNode_ShapeSelected.setWidth(boundsLocal.getWidth());
        this.RenderNode_ShapeSelected.setHeight(boundsLocal.getHeight());*/
    }
}