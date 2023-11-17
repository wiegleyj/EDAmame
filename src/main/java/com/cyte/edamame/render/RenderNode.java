/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.render;

import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.shape.SnapPoint;

import java.util.UUID;
import java.util.LinkedList;

import com.cyte.edamame.util.Utils;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.scene.text.*;

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
    public boolean RenderNode_AutoSnapPoints;
    public LinkedList<SnapPoint> RenderNode_SnapPoints;
    public RenderSystem RenderNode_RenderSystem;

    public RenderNode(String nameValue, Node nodeValue, boolean edgeSnapPoints, LinkedList<PairMutable> manualSnapPointPos, boolean passiveValue, RenderSystem renderSystemValue)
    {
        this.RenderNode_Name = nameValue;
        this.RenderNode_Node = nodeValue;
        //this.RenderNode_Node.setId(nodeValue.getId() + "_" + RenderNode_ID);
        this.RenderNode_Highlighted = false;
        this.RenderNode_HighlightedMouse = false;
        this.RenderNode_HighlightedBox = false;
        this.RenderNode_Selected = false;
        this.RenderNode_MousePressPos = null;
        this.RenderNode_Passive = passiveValue;
        this.RenderNode_AutoSnapPoints = edgeSnapPoints;
        this.RenderNode_SnapPoints = new LinkedList<SnapPoint>();
        this.RenderNode_RenderSystem = renderSystemValue;

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
            if (edgeSnapPoints)
                this.RenderNode_EdgeSnapPointsCreate();
            if (manualSnapPointPos != null)
                this.RenderNode_ManualSnapPointsCreate(manualSnapPointPos);
        }
    }

    public void RenderNode_ManualSnapPointsCreate(LinkedList<PairMutable> pos)
    {
        for (int i = 0; i < pos.size(); i++)
        {
            PairMutable currPos = pos.get(i);

            SnapPoint snapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            snapPoint.setId("snapManual");
            snapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(snapPoint);
        }
    }

    public void RenderNode_EdgeSnapPointsCreate()
    {
        PairMutable currPos = new PairMutable(0.0, 0.0);

        if (this.RenderNode_Node.getClass() == Line.class)
        {
            // Creating the start snap point...
            SnapPoint startSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            startSnapPoint.setId("snapStart");
            startSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(startSnapPoint);

            // Creating the middle snap point...
            SnapPoint middleSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            middleSnapPoint.setId("snapMiddle");
            middleSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(middleSnapPoint);

            // Creating the end snap point...
            SnapPoint endSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            endSnapPoint.setId("snapEnd");
            endSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(endSnapPoint);
        }
        else
        {
            // Creating the top left snap point...
            SnapPoint topLeftSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            topLeftSnapPoint.setId("snapTopLeft");
            topLeftSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(topLeftSnapPoint);

            // Creating the top center snap point...
            SnapPoint topCenterSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            topCenterSnapPoint.setId("snapTopCenter");
            topCenterSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(topCenterSnapPoint);

            // Creating the top right snap point...
            SnapPoint topRightSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            topRightSnapPoint.setId("snapTopRight");
            topRightSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(topRightSnapPoint);

            // Creating the left snap point...
            SnapPoint leftSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            leftSnapPoint.setId("snapLeft");
            leftSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(leftSnapPoint);

            // Creating the center snap point...
            SnapPoint centerSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            centerSnapPoint.setId("snapCenter");
            centerSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(centerSnapPoint);

            // Creating the right snap point...
            SnapPoint rightSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            rightSnapPoint.setId("snapRight");
            rightSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(rightSnapPoint);

            // Creating the bottom left snap point...
            SnapPoint bottomLeftSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            bottomLeftSnapPoint.setId("snapBottomLeft");
            bottomLeftSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(bottomLeftSnapPoint);

            // Creating the bottom center snap point...
            SnapPoint bottomCenterSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            bottomCenterSnapPoint.setId("snapBottomCenter");
            bottomCenterSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(bottomCenterSnapPoint);

            // Creating the bottom right snap point...
            SnapPoint bottomRightSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            bottomRightSnapPoint.setId("snapBottomRight");
            bottomRightSnapPoint.setVisible(false);
            this.RenderNode_SnapPoints.add(bottomRightSnapPoint);
        }
    }

    public PairMutable RenderNode_BoundsPosToHolderPane(PairMutable posOffset)
    {
        Bounds boundsLocal = this.RenderNode_Node.getBoundsInLocal();
        PairMutable boundsRealEdgeL = this.RenderNode_PosToHolderPane(new PairMutable(boundsLocal.getMinX() + posOffset.GetLeftDouble(), boundsLocal.getMinY() + posOffset.GetRightDouble()));
        PairMutable boundsRealEdgeH = this.RenderNode_PosToHolderPane(new PairMutable(boundsLocal.getMaxX() + posOffset.GetLeftDouble(), boundsLocal.getMaxY() + posOffset.GetRightDouble()));

        return new PairMutable((boundsRealEdgeL.GetLeftDouble() + boundsRealEdgeH.GetLeftDouble()) / 2,
                               (boundsRealEdgeL.GetRightDouble() + boundsRealEdgeH.GetRightDouble()) / 2);
    }

    public PairMutable RenderNode_PosToHolderPane(PairMutable pos)
    {
        Point2D newPos = this.RenderNode_Node.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public boolean RenderNode_PosOnNode(PairMutable pos)
    {
        return this.RenderNode_Node.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    public void RenderNode_SnapPointsRefresh()
    {
        for (int i = 0; i < this.RenderNode_SnapPoints.size(); i++)
        {
            SnapPoint snapPoint = this.RenderNode_SnapPoints.get(i);
            String snapPointId = snapPoint.getId();

            Bounds boundsLocal = this.RenderNode_Node.getBoundsInLocal();
            PairMutable posSnapReal = null;

            if (snapPointId.equals("snapTopLeft"))
            {
                posSnapReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(-boundsLocal.getWidth() / 2,
                                                                                    -boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapTopCenter"))
            {
                posSnapReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(0.0,
                                                                                    -boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapTopRight"))
            {
                posSnapReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(boundsLocal.getWidth() / 2,
                                                                                    -boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapLeft"))
            {
                posSnapReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(-boundsLocal.getWidth() / 2,
                                                                                    0.0));
            }
            else if (snapPointId.equals("snapCenter"))
            {
                posSnapReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(0.0,
                                                                                    0.0));
            }
            else if (snapPointId.equals("snapRight"))
            {
                posSnapReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(boundsLocal.getWidth() / 2,
                                                                                    0.0));
            }
            else if (snapPointId.equals("snapBottomLeft"))
            {
                posSnapReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(-boundsLocal.getWidth() / 2,
                                                                                    boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapBottomCenter"))
            {
                posSnapReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(0.0,
                                                                                    boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapBottomRight"))
            {
                posSnapReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(boundsLocal.getWidth() / 2,
                                                                                    boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapStart"))
            {
                posSnapReal = this.RenderNode_PosToHolderPane(new PairMutable(((Line)this.RenderNode_Node).getStartX(),
                                                                              ((Line)this.RenderNode_Node).getStartY()));
            }
            else if (snapPointId.equals("snapMiddle"))
            {
                PairMutable posStart = new PairMutable(((Line)this.RenderNode_Node).getStartX(), ((Line)this.RenderNode_Node).getStartY());
                PairMutable posEnd = new PairMutable(((Line)this.RenderNode_Node).getEndX(), ((Line)this.RenderNode_Node).getEndY());
                PairMutable posMiddle = new PairMutable((posStart.GetLeftDouble() + posEnd.GetLeftDouble()) / 2,
                                                        (posStart.GetRightDouble() + posEnd.GetRightDouble()) / 2);

                posSnapReal = this.RenderNode_PosToHolderPane(posMiddle);
            }
            else if (snapPointId.equals("snapEnd"))
            {
                posSnapReal = this.RenderNode_PosToHolderPane(new PairMutable(((Line)this.RenderNode_Node).getEndX(),
                                                                              ((Line)this.RenderNode_Node).getEndY()));
            }
            else if (snapPointId.equals("snapManual"))
            {
                posSnapReal = this.RenderNode_PosToHolderPane(new PairMutable(snapPoint.SnapPoint_Pos.GetLeftDouble(),
                                                                              snapPoint.SnapPoint_Pos.GetRightDouble()));
            }
            else
            {
                throw new java.lang.Error("ERROR: Encountered unrecognized snap point when refreshing RenderNode snap points!");
            }

            snapPoint.setTranslateX(posSnapReal.GetLeftDouble());
            snapPoint.setTranslateY(posSnapReal.GetRightDouble());

            //this.RenderNode_RenderSystem.RenderSystem_TestShapeAdd(posSnapReal, 5.0, Color.RED, true);
            //System.out.println(posSnapReal.ToStringDouble());
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
        PairMutable posBoundsReal = this.RenderNode_BoundsPosToHolderPane(new PairMutable(0.0, 0.0));

        this.RenderNode_ShapeSelected.setTranslateX(posBoundsReal.GetLeftDouble() - boundsLocal.getWidth() / 2);
        this.RenderNode_ShapeSelected.setTranslateY(posBoundsReal.GetRightDouble() - boundsLocal.getHeight() / 2);

        this.RenderNode_ShapeSelected.setRotate(this.RenderNode_Node.getRotate());

        this.RenderNode_ShapeSelected.setWidth(boundsLocal.getWidth());
        this.RenderNode_ShapeSelected.setHeight(boundsLocal.getHeight());
    }

    public RenderNode RenderNode_Clone()
    {
        LinkedList<PairMutable> clonedSnapPointManualPos = null;

        if (!this.RenderNode_SnapPoints.isEmpty())
        {
            clonedSnapPointManualPos = new LinkedList<PairMutable>();

            for (int i = 0; i < this.RenderNode_SnapPoints.size(); i++)
                clonedSnapPointManualPos.add(new PairMutable(this.RenderNode_SnapPoints.get(i).getTranslateX(),
                                                             this.RenderNode_SnapPoints.get(i).getTranslateY()));
        }

        RenderNode clonedNode = new RenderNode(this.RenderNode_Name,
                                               Utils.Utils_NodeClone(this.RenderNode_Node),
                                               this.RenderNode_AutoSnapPoints,
                                               clonedSnapPointManualPos,
                                               this.RenderNode_Passive,
                                               null);

        return clonedNode;
    }

    public static PairMutable RenderNode_NodesGetMiddlePos(LinkedList<Node> nodes)
    {
        PairMutable nodeBounds = RenderNode_NodesGetRealBounds(nodes);

        return new PairMutable((nodeBounds.GetLeftPair().GetLeftDouble() + nodeBounds.GetLeftPair().GetRightDouble()) / 2,
                               (nodeBounds.GetRightPair().GetLeftDouble() + nodeBounds.GetRightPair().GetRightDouble()) / 2);
    }

    public static PairMutable RenderNode_NodesGetRealBounds(LinkedList<Node> nodes)
    {
        PairMutable childTotalBoundsX = new PairMutable(0.0, 0.0);
        PairMutable childTotalBoundsY = new PairMutable(0.0, 0.0);

        for (int i = 0; i < nodes.size(); i++)
        {
            Node node = nodes.get(i);
            Bounds nodeBoundsReal = node.getBoundsInParent();

            if (i == 0)
            {
                childTotalBoundsX.left = nodeBoundsReal.getMinX();
                childTotalBoundsX.right = nodeBoundsReal.getMaxX();
                childTotalBoundsY.left = nodeBoundsReal.getMinY();
                childTotalBoundsY.right = nodeBoundsReal.getMaxY();
            }
            else
            {
                if (nodeBoundsReal.getMinX() < childTotalBoundsX.GetLeftDouble())
                    childTotalBoundsX.left = nodeBoundsReal.getMinX();
                if (nodeBoundsReal.getMaxX() > childTotalBoundsX.GetRightDouble())
                    childTotalBoundsX.right = nodeBoundsReal.getMaxX();
                if (nodeBoundsReal.getMinY() < childTotalBoundsY.GetLeftDouble())
                    childTotalBoundsY.left = nodeBoundsReal.getMinY();
                if (nodeBoundsReal.getMaxY() > childTotalBoundsY.GetRightDouble())
                    childTotalBoundsY.right = nodeBoundsReal.getMaxY();
            }
        }

        return new PairMutable(childTotalBoundsX, childTotalBoundsY);
    }

    public static String RenderNode_ToFXMLString(Node node, PairMutable posOffset, int tabNum)
    {
        String str = "";
        String tabStr = "";

        for (int i = 0; i < tabNum; i++)
            tabStr += "\t";

        if (node.getClass() == Circle.class)
        {
            Circle circle = (Circle)node;

            str += tabStr + "<Circle";
            str += " id=\"" + circle.getId() + "\"";
            str += " radius=\"" + circle.getRadius() + "\"";
            //str += " fill=\"#" + Integer.toHexString(circle.getFill().hashCode()) + "\"";

            if (Integer.toHexString(circle.getFill().hashCode()).length() < 8)
            {
                String addZeros = Integer.toHexString(circle.getFill().hashCode());

                while (addZeros.length() < 8)
                    addZeros = "0" + addZeros;

                str += " fill=\"#" + addZeros + "\"";
            }
            else
            {
                str += " fill=\"#" + Integer.toHexString(circle.getFill().hashCode()) + "\"";
            }

            if (circle.getStroke() != null)
            {
                str += " stroke=\"#" + circle.getStroke().toString().replace("0x", "") + "\"";
                //str += " strokeType=\"#" + circle.getStrokeType().toString().replace("#", "") + "\"";
                str += " strokeType=\"INSIDE\"";
                str += " strokeWidth=\"" + circle.getStrokeWidth()+ "\"";
            }

            str += " translateX=\"" + (circle.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (circle.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + circle.getRotate() + "\" />";
        }
        else if  (node.getClass() == Rectangle.class)
        {
            Rectangle rectangle = (Rectangle)node;

            str += tabStr + "<Rectangle";
            str += " id=\"" + rectangle.getId() + "\"";
            str += " width=\"" + rectangle.getWidth() + "\"";
            str += " height=\"" + rectangle.getHeight() + "\"";
            //str += " fill=\"#" + Integer.toHexString(rectangle.getFill().hashCode()) + "\"";

            if (Integer.toHexString(rectangle.getFill().hashCode()).length() < 8)
            {
                String addZeros = Integer.toHexString(rectangle.getFill().hashCode());

                while (addZeros.length() < 8)
                    addZeros = "0" + addZeros;

                str += " fill=\"#" + addZeros + "\"";
            }
            else
            {
                str += " fill=\"#" + Integer.toHexString(rectangle.getFill().hashCode()) + "\"";
            }

            if (rectangle.getStroke() != null)
            {
                str += " stroke=\"#" + rectangle.getStroke().toString().replace("0x", "") + "\"";
                //str += " strokeType=\"#" + rectangle.getStrokeType().toString().replace("#", "") + "\"";
                str += " strokeType=\"INSIDE\"";
                str += " strokeWidth=\"" + rectangle.getStrokeWidth() + "\"";
            }

            str += " translateX=\"" + (rectangle.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (rectangle.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + rectangle.getRotate() + "\" />";
        }
        else if (node.getClass() == Polygon.class)
        {
            Polygon triangle = (Polygon)node;

            str += tabStr + "<Polygon";
            str += " id=\"" + triangle.getId() + "\"";

            if (Integer.toHexString(triangle.getFill().hashCode()).length() < 8)
            {
                String addZeros = Integer.toHexString(triangle.getFill().hashCode());

                while (addZeros.length() < 8)
                    addZeros = "0" + addZeros;

                str += " fill=\"#" + addZeros + "\"";
            }
            else
            {
                str += " fill=\"#" + Integer.toHexString(triangle.getFill().hashCode()) + "\"";
            }

            if (triangle.getStroke() != null)
            {
                str += " stroke=\"#" + triangle.getStroke().toString().replace("0x", "") + "\"";
                //str += " strokeType=\"#" + triangle.getStrokeType().toString().replace("#", "") + "\"";
                str += " strokeType=\"INSIDE\"";
                str += " strokeWidth=\"" + triangle.getStrokeWidth() + "\"";
            }

            str += " translateX=\"" + (triangle.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (triangle.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + triangle.getRotate() + "\" >\n";
            str += tabStr + "\t<points>\n";

            for (int i = 0; i < triangle.getPoints().size(); i++)
                str += tabStr + "\t\t<Double fx:value=\"" + triangle.getPoints().get(i) + "\"/>\n";

            str += tabStr + "\t</points>\n";
            str += tabStr + "</Polygon>";
        }
        else if (node.getClass() == Line.class)
        {
            Line line = (Line)node;

            str += tabStr + "<Line";
            str += " id=\"" + line.getId() + "\"";
            str += " startX=\"" + line.getStartX() + "\"";
            str += " startY=\"" + line.getStartY() + "\"";
            str += " endX=\"" + line.getEndX() + "\"";
            str += " endY=\"" + line.getEndY() + "\"";
            str += " strokeWidth=\"" + line.getStrokeWidth() + "\"";
            str += " translateX=\"" + (line.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (line.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " stroke=\"" + line.getStroke().toString().replace("0x", "#") + "\"";
            //str += " stroke=\"#" + line.getStroke().toString() + "\"";
            //str += " strokeType=\"#" + line.getStrokeType().toString() + "\"";
            str += " />";
        }
        else if (node.getClass() == Text.class)
        {
            Text text = (Text)node;

            str += tabStr + "<Text";
            str += " id=\"" + text.getId() + "\"";
            str += " text=\"" + text.getText() + "\"";

            if (Integer.toHexString(text.getFill().hashCode()).length() < 8)
            {
                String addZeros = Integer.toHexString(text.getFill().hashCode());

                while (addZeros.length() < 8)
                    addZeros = "0" + addZeros;

                str += " fill=\"#" + addZeros + "\"";
            }
            else
            {
                str += " fill=\"#" + Integer.toHexString(text.getFill().hashCode()) + "\"";
            }

            str += " translateX=\"" + (text.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (text.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + text.getRotate() + "\">\n";
            str += tabStr + "\t<font> \n";
            str += tabStr + "\t\t<Font";
            //str += " font=\"" + text.getFont().getName() + "\"";
            str += " name=\"" + text.getFont().getFamily() + "\"";
            str += " size=\"" + text.getFont().getSize() + "\" />\n";

            str += tabStr + "\t</font>\n";
            str += tabStr + "</Text>";
        }
        else if (node.getClass() == Group.class)
        {
            Group group = (Group)node;

            str += tabStr + "<Group";
            str += " id=\"" + group.getId() + "\"";
            str += " translateX=\"" + (group.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (group.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + group.getRotate() + "\">\n";
            str += tabStr + "\t<children>\n";

            for (int i = 0; i < group.getChildren().size(); i++)
                str += RenderNode_ToFXMLString(group.getChildren().get(i), new PairMutable(0.0, 0.0), tabNum + 2) + "\n";

            str += tabStr + "\t</children>\n";
            str += tabStr + "</Group>";
        }
        else
        {
            throw new java.lang.Error("ERROR: Attempting to convert an unknown node type to FXML string!");
        }

        return str;
    }


}