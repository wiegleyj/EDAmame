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

import java.util.UUID;
import java.util.LinkedList;

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
    public LinkedList<Shape> RenderNode_SnapPoints;
    public LinkedList<PairMutable> RenderNode_SnapPointOffsets;

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
        this.RenderNode_SnapPoints = new LinkedList<Shape>();
        this.RenderNode_SnapPointOffsets = new LinkedList<PairMutable>();

        if (!this.RenderNode_Passive)
        {
            // Creating the highlighted & selected shapes...
            {
                this.RenderNode_ShapeHighlighted = new Rectangle();
                this.RenderNode_ShapeHighlighted.setFill(Color.GRAY);
                this.RenderNode_ShapeHighlighted.setOpacity(0.5);
                this.RenderNode_ShapeHighlighted.setId(this.RenderNode_ID);
                this.RenderNode_ShapeHighlighted.setVisible(false);
                //this.RenderNode_ShapeHighlighted.translateXProperty().bind(this.RenderNode_Node.translateXProperty());
                //this.RenderNode_ShapeHighlighted.translateYProperty().bind(this.RenderNode_Node.translateYProperty());
                //this.RenderNode_ShapeHighlighted.rotateProperty().bind(this.RenderNode_Node.rotateProperty());

                this.RenderNode_ShapeSelected = new Rectangle();
                this.RenderNode_ShapeSelected.setFill(Color.GRAY);
                this.RenderNode_ShapeSelected.setOpacity(0.5);
                this.RenderNode_ShapeSelected.setId(this.RenderNode_ID);
                this.RenderNode_ShapeSelected.setVisible(false);
                //this.RenderNode_ShapeSelected.translateXProperty().bind(this.RenderNode_Node.translateXProperty());
                //this.RenderNode_ShapeSelected.translateYProperty().bind(this.RenderNode_Node.translateYProperty());
                //this.RenderNode_ShapeSelected.rotateProperty().bind(this.RenderNode_Node.rotateProperty());
            }

            // Creating the snap point shapes...
            {
                // Creating the center snap point...
                Circle centerSnapPoint = new Circle(EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor);
                centerSnapPoint.setOpacity(EDAmameController.Editor_SnapPointShapeOpacity);
                centerSnapPoint.setVisible(false);
                this.RenderNode_SnapPoints.add(centerSnapPoint);
                this.RenderNode_SnapPointOffsets.add(new PairMutable(0.0, 0.0));

                // Creating the line endpoint snap points...
                if (this.RenderNode_Node.getClass() == Line.class)
                {
                    PairMutable startPoint = new PairMutable(((Line)this.RenderNode_Node).getStartX(), ((Line)this.RenderNode_Node).getStartY());
                    PairMutable endPoint = new PairMutable(((Line)this.RenderNode_Node).getEndX(), ((Line)this.RenderNode_Node).getEndY());

                    Circle startSnapPoint = new Circle(EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor);
                    startSnapPoint.setOpacity(EDAmameController.Editor_SnapPointShapeOpacity);
                    startSnapPoint.setVisible(false);
                    this.RenderNode_SnapPoints.add(startSnapPoint);
                    this.RenderNode_SnapPointOffsets.add(startPoint);

                    Circle endSnapPoint = new Circle(EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor);
                    endSnapPoint.setOpacity(EDAmameController.Editor_SnapPointShapeOpacity);
                    endSnapPoint.setVisible(false);
                    this.RenderNode_SnapPoints.add(endSnapPoint);
                    this.RenderNode_SnapPointOffsets.add(endPoint);
                }
            }
        }

        //RenderNode_ShapeHighlightedRefresh();
        //RenderNode_ShapeSelectedRefresh();
    }

    public boolean RenderNode_PosOnNode(PairMutable pos)
    {
        return this.RenderNode_Node.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    public void RenderNode_SnapPointsRefresh()
    {
        for (int i = 0; i < this.RenderNode_SnapPoints.size(); i++)
        {
            Shape snapPointShape = this.RenderNode_SnapPoints.get(i);
            PairMutable snapPointOffset = this.RenderNode_SnapPointOffsets.get(i);

            snapPointShape.setTranslateX(snapPointOffset.GetLeftDouble() + this.RenderNode_Node.getTranslateX());
            snapPointShape.setTranslateY(snapPointOffset.GetRightDouble() + this.RenderNode_Node.getTranslateY());
        }
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
            //this.RenderNode_ShapeSelected.setTranslateX(posReal.GetLeftDouble());
            //this.RenderNode_ShapeSelected.setTranslateY(posReal.GetRightDouble());
            this.RenderNode_ShapeSelected.setTranslateX(this.RenderNode_Node.getTranslateX());
            this.RenderNode_ShapeSelected.setTranslateY(this.RenderNode_Node.getTranslateY());
        }
        else
        {
            //this.RenderNode_ShapeSelected.setTranslateX(posReal.GetLeftDouble() - boundsLocal.getWidth() / 2);
            //this.RenderNode_ShapeSelected.setTranslateY(posReal.GetRightDouble() - boundsLocal.getHeight() / 2);
            this.RenderNode_ShapeSelected.setTranslateX(this.RenderNode_Node.getTranslateX() - boundsLocal.getWidth() / 2);
            this.RenderNode_ShapeSelected.setTranslateY(this.RenderNode_Node.getTranslateY() - boundsLocal.getHeight() / 2);
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

    public String RenderNode_ToFXMLString()
    {
        String str = "";

        if (this.RenderNode_Node.getClass() == Circle.class)
        {
            Circle circle = (Circle)this.RenderNode_Node;

            str += "<Circle";
            str += " radius=\"" + circle.getRadius() + "\"";
            //str += " fill=\"#" + Integer.toHexString(circle.getFill().hashCode()) + "\"";
            if (Integer.toHexString(circle.getFill().hashCode()).length() < 8)
            {
                String addZeros = Integer.toHexString(circle.getFill().hashCode());
                while (addZeros.length() < 8)
                {
                    addZeros = "0" + addZeros;
                }
                str += " fill=\"#" + addZeros + "\"";
            } else {
                str += " fill=\"#" + Integer.toHexString(circle.getFill().hashCode()) + "\"";
            }
            //str += " stroke=\"#" + circle.getStroke().toString() + "\"";
            //str += " strokeType=\"#" + circle.getStrokeType().toString() + "\"";
            str += " translateX=\"" + circle.getTranslateX() + "\"";
            str += " translateY=\"" + circle.getTranslateY() + "\"";
            str += " />";
        }
        else if  (this.RenderNode_Node.getClass() == Rectangle.class)
        {
            Rectangle rectangle = (Rectangle)this.RenderNode_Node;

            str += "<Rectangle";
            str += " width=\"" + rectangle.getWidth() + "\"";
            str += " height=\"" + rectangle.getHeight() + "\"";
            //str += " fill=\"#" + Integer.toHexString(rectangle.getFill().hashCode()) + "\"";
            if (Integer.toHexString(rectangle.getFill().hashCode()).length() < 8)
            {
                String addZeros = Integer.toHexString(rectangle.getFill().hashCode());
                while (addZeros.length() < 8)
                {
                    addZeros = "0" + addZeros;
                }
                str += " fill=\"#" + addZeros + "\"";
            } else {
                str += " fill=\"#" + Integer.toHexString(rectangle.getFill().hashCode()) + "\"";
            }
            //str += " stroke=\"#" + rectangle.getStroke().toString() + "\"";
            //str += " strokeType=\"#" + rectangle.getStrokeType().toString() + "\"";
            str += " translateX=\"" + rectangle.getTranslateX() + "\"";
            str += " translateY=\"" + rectangle.getTranslateY() + "\"";
            str += " />";
        }
        else if (this.RenderNode_Node.getClass() == Polygon.class)
        {
            Polygon triangle = (Polygon)this.RenderNode_Node;

            str += "<Polygon";
            //str += " points=\"" + triangle.getPoints();
            //str += " fill=\"#" + Integer.toHexString(triangle.getFill().hashCode()) + "\"";
            if (Integer.toHexString(triangle.getFill().hashCode()).length() < 8)
            {
                String addZeros = Integer.toHexString(triangle.getFill().hashCode());
                while (addZeros.length() < 8)
                {
                    addZeros = "0" + addZeros;
                }
                str += " fill=\"#" + addZeros + "\"";
            } else {
                str += " fill=\"#" + Integer.toHexString(triangle.getFill().hashCode()) + "\"";
            }
            //str += " stroke=\"#" + polygon.getStroke().toString() + "\"";
            //str += " strokeType=\"#" + polygon.getStrokeType().toString() + "\"";
            str += " translateX=\"" + triangle.getTranslateX() + "\"";
            str += " translateY=\"" + triangle.getTranslateY() + "\">";
            str += "\n\t\t\t<points>";
            for (int i = 0; i < triangle.getPoints().size() - 1; i++)
            {
                str += "\n\t\t\t\t<Double fx:value=\"" + triangle.getPoints().get(i) + "\"/>";
            }
            str += "\n\t\t\t</points>\n\t\t</Polygon>";
        }
        else if (this.RenderNode_Node.getClass() == Line.class)
        {
            Line line = (Line)this.RenderNode_Node;

            str += "<Line";
            str += " startX=\"" + line.getStartX() + "\"";
            str += " startY=\"" + line.getStartY() + "\"";
            str += " endX=\"" + line.getEndX() + "\"";
            str += " endY=\"" + line.getEndY() + "\"";
            //str += " fill=\"#" + Integer.toHexString(line.getStroke().hashCode()) + "\"";
            str += " stroke=\"" + line.getStroke() + "\"";

            //str += " stroke=\"#" + line.getStroke().toString() + "\"";
            //str += " strokeType=\"#" + line.getStrokeType().toString() + "\"";
            str += " />";
        }
        else if (this.RenderNode_Node.getClass() == Label.class)
        {
            Label text = (Label)this.RenderNode_Node;

            str += "<Label";
            str += " text=\"" + text.getText() + "\"";
            if (Integer.toHexString(text.getTextFill().hashCode()).length() < 8)
            {
                String addZeros = Integer.toHexString(text.getTextFill().hashCode());
                while (addZeros.length() < 8)
                {
                    addZeros = "0" + addZeros;
                }
                str += " textFill=\"#" + addZeros + "\">\n";
            } else {
                str += " textFill=\"#" + Integer.toHexString(text.getTextFill().hashCode()) + "\">\n";
            }
            str += "\t\t\t<font> \n\t\t\t\t<Font";
            //str += " font=\"" + text.getFont().getName() + "\"";
            str += " size=\"" + text.getFont().getSize() + "\"";
            str += "/>\n" + "\t\t\t</font>\n" + "\t\t</Label>";

        }
        else
        {
            throw new java.lang.Error("ERROR: Attempting to convert an unknown node type to FXML string!");
        }

        return str;
    }
}