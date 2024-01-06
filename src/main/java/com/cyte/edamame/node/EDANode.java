/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.node;

import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.shape.SnapPoint;

import java.util.UUID;
import java.util.LinkedList;

import com.cyte.edamame.util.Utils;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.scene.text.*;

public class EDANode
{
    //// GLOBAL VARIABLES ////

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
    public boolean passive;
    public boolean autoSnapPoints;
    public LinkedList<SnapPoint> manualSnapPoints;
    public boolean isPin;
    public String[] conns;

    public Editor editor;

    //// CONSTRUCTORS ////

    public EDANode(String nameValue, Node nodeValue, boolean edgeSnapPoints, LinkedList<PairMutable> manualSnapPointPos, boolean passiveValue, boolean isPinValue, Editor editorValue)
    {
        this.name = nameValue;
        this.node = nodeValue;
        //this.RenderNode_Node.setId(nodeValue.getId() + "_" + RenderNode_ID);
        this.highlighted = false;
        this.highlightedMouse = false;
        this.highlightedBox = false;
        this.selected = false;
        this.mousePressPos = null;
        this.passive = passiveValue;
        this.autoSnapPoints = edgeSnapPoints;
        this.manualSnapPoints = new LinkedList<SnapPoint>();
        this.isPin = isPinValue;

        this.editor = editorValue;

        if (!this.passive)
        {
            // Creating the highlighted & selected shapes...
            {
                this.shapeHighlighted = new Rectangle();
                this.shapeHighlighted.setFill(Color.GRAY);
                this.shapeHighlighted.setOpacity(0.5);
                this.shapeHighlighted.setId(this.id);
                this.shapeHighlighted.setVisible(false);
                //this.RenderNode_ShapeHighlighted.translateXProperty().bind(this.RenderNode_Node.translateXProperty());
                //this.RenderNode_ShapeHighlighted.translateYProperty().bind(this.RenderNode_Node.translateYProperty());
                //this.RenderNode_ShapeHighlighted.rotateProperty().bind(this.RenderNode_Node.rotateProperty());

                this.shapeSelected = new Rectangle();
                this.shapeSelected.setFill(Color.GRAY);
                this.shapeSelected.setOpacity(0.5);
                this.shapeSelected.setId(this.id);
                this.shapeSelected.setVisible(false);
                //this.RenderNode_ShapeSelected.translateXProperty().bind(this.RenderNode_Node.translateXProperty());
                //this.RenderNode_ShapeSelected.translateYProperty().bind(this.RenderNode_Node.translateYProperty());
                //this.RenderNode_ShapeSelected.rotateProperty().bind(this.RenderNode_Node.rotateProperty());
            }

            // Creating the snap point shapes...
            if (edgeSnapPoints)
                this.EdgeSnapPointsCreate();
            if (manualSnapPointPos != null)
                this.ManualSnapPointsCreate(manualSnapPointPos);
        }
    }

    //// REFRESH FUNCTIONS ////

    public void ShapeHighlightedRefresh()
    {
        Bounds boundsReal = this.node.getBoundsInParent();

        this.shapeHighlighted.setTranslateX(boundsReal.getMinX());
        this.shapeHighlighted.setTranslateY(boundsReal.getMinY());

        this.shapeHighlighted.setWidth(boundsReal.getWidth());
        this.shapeHighlighted.setHeight(boundsReal.getHeight());
    }

    public void ShapeSelectedRefresh()
    {
        Bounds boundsLocal = this.node.getBoundsInLocal();
        PairMutable posBoundsReal = this.BoundsPosToHolderPane(new PairMutable(0.0, 0.0));

        this.shapeSelected.setTranslateX(posBoundsReal.GetLeftDouble() - boundsLocal.getWidth() / 2);
        this.shapeSelected.setTranslateY(posBoundsReal.GetRightDouble() - boundsLocal.getHeight() / 2);

        this.shapeSelected.setRotate(this.node.getRotate());

        this.shapeSelected.setWidth(boundsLocal.getWidth());
        this.shapeSelected.setHeight(boundsLocal.getHeight());
    }

    //// SNAP POINT FUNCTIONS ////

    public void SnapPointsRefresh()
    {
        for (int i = 0; i < this.manualSnapPoints.size(); i++)
        {
            SnapPoint snapPoint = this.manualSnapPoints.get(i);
            String snapPointId = snapPoint.getId();

            Bounds boundsLocal = this.node.getBoundsInLocal();
            PairMutable posSnapReal = null;

            if (snapPointId.equals("snapTopLeft"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(-boundsLocal.getWidth() / 2,
                        -boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapTopCenter"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(0.0,
                        -boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapTopRight"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(boundsLocal.getWidth() / 2,
                        -boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapLeft"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(-boundsLocal.getWidth() / 2,
                        0.0));
            }
            else if (snapPointId.equals("snapCenter"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(0.0,
                        0.0));
            }
            else if (snapPointId.equals("snapRight"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(boundsLocal.getWidth() / 2,
                        0.0));
            }
            else if (snapPointId.equals("snapBottomLeft"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(-boundsLocal.getWidth() / 2,
                        boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapBottomCenter"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(0.0,
                        boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapBottomRight"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(boundsLocal.getWidth() / 2,
                        boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapStart"))
            {
                posSnapReal = this.PosToHolderPane(new PairMutable(((Line)this.node).getStartX(),
                        ((Line)this.node).getStartY()));
            }
            else if (snapPointId.equals("snapMiddle"))
            {
                PairMutable posStart = new PairMutable(((Line)this.node).getStartX(), ((Line)this.node).getStartY());
                PairMutable posEnd = new PairMutable(((Line)this.node).getEndX(), ((Line)this.node).getEndY());
                PairMutable posMiddle = new PairMutable((posStart.GetLeftDouble() + posEnd.GetLeftDouble()) / 2,
                        (posStart.GetRightDouble() + posEnd.GetRightDouble()) / 2);

                posSnapReal = this.PosToHolderPane(posMiddle);
            }
            else if (snapPointId.equals("snapEnd"))
            {
                posSnapReal = this.PosToHolderPane(new PairMutable(((Line)this.node).getEndX(),
                        ((Line)this.node).getEndY()));
            }
            else if (snapPointId.equals("snapManual"))
            {
                posSnapReal = this.PosToHolderPane(new PairMutable(snapPoint.pos.GetLeftDouble(),
                        snapPoint.pos.GetRightDouble()));
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

    public void BoundsRefresh()
    {
        /*if (this.RenderNode_Node.getClass() == Label.class)
        {
            Bounds bounds = this.RenderNode_Node.getBoundsInLocal();
            EDAmameController.Controller_RenderShapesDelayedBoundsRefresh.add(new PairMutable(new PairMutable(this, 0), new PairMutable(bounds.getWidth(), bounds.getHeight())));
        }
        else
        {*/
        this.ShapeHighlightedRefresh();
        this.ShapeSelectedRefresh();
        //}
    }

    public void ManualSnapPointsCreate(LinkedList<PairMutable> pos)
    {
        for (int i = 0; i < pos.size(); i++)
        {
            PairMutable currPos = pos.get(i);

            SnapPoint snapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            snapPoint.setId("snapManual");
            snapPoint.setVisible(false);
            this.manualSnapPoints.add(snapPoint);
        }
    }

    public void EdgeSnapPointsCreate()
    {
        PairMutable currPos = new PairMutable(0.0, 0.0);

        if (this.node.getClass() == Line.class)
        {
            // Creating the start snap point...
            SnapPoint startSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            startSnapPoint.setId("snapStart");
            startSnapPoint.setVisible(false);
            this.manualSnapPoints.add(startSnapPoint);

            // Creating the middle snap point...
            SnapPoint middleSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            middleSnapPoint.setId("snapMiddle");
            middleSnapPoint.setVisible(false);
            this.manualSnapPoints.add(middleSnapPoint);

            // Creating the end snap point...
            SnapPoint endSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            endSnapPoint.setId("snapEnd");
            endSnapPoint.setVisible(false);
            this.manualSnapPoints.add(endSnapPoint);
        }
        else
        {
            // Creating the top left snap point...
            SnapPoint topLeftSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            topLeftSnapPoint.setId("snapTopLeft");
            topLeftSnapPoint.setVisible(false);
            this.manualSnapPoints.add(topLeftSnapPoint);

            // Creating the top center snap point...
            SnapPoint topCenterSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            topCenterSnapPoint.setId("snapTopCenter");
            topCenterSnapPoint.setVisible(false);
            this.manualSnapPoints.add(topCenterSnapPoint);

            // Creating the top right snap point...
            SnapPoint topRightSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            topRightSnapPoint.setId("snapTopRight");
            topRightSnapPoint.setVisible(false);
            this.manualSnapPoints.add(topRightSnapPoint);

            // Creating the left snap point...
            SnapPoint leftSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            leftSnapPoint.setId("snapLeft");
            leftSnapPoint.setVisible(false);
            this.manualSnapPoints.add(leftSnapPoint);

            // Creating the center snap point...
            SnapPoint centerSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            centerSnapPoint.setId("snapCenter");
            centerSnapPoint.setVisible(false);
            this.manualSnapPoints.add(centerSnapPoint);

            // Creating the right snap point...
            SnapPoint rightSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            rightSnapPoint.setId("snapRight");
            rightSnapPoint.setVisible(false);
            this.manualSnapPoints.add(rightSnapPoint);

            // Creating the bottom left snap point...
            SnapPoint bottomLeftSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            bottomLeftSnapPoint.setId("snapBottomLeft");
            bottomLeftSnapPoint.setVisible(false);
            this.manualSnapPoints.add(bottomLeftSnapPoint);

            // Creating the bottom center snap point...
            SnapPoint bottomCenterSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            bottomCenterSnapPoint.setId("snapBottomCenter");
            bottomCenterSnapPoint.setVisible(false);
            this.manualSnapPoints.add(bottomCenterSnapPoint);

            // Creating the bottom right snap point...
            SnapPoint bottomRightSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            bottomRightSnapPoint.setId("snapBottomRight");
            bottomRightSnapPoint.setVisible(false);
            this.manualSnapPoints.add(bottomRightSnapPoint);
        }
    }

    //// POSITION FUNCTIONS ////

    public PairMutable BoundsPosToHolderPane(PairMutable posOffset)
    {
        Bounds boundsLocal = this.node.getBoundsInLocal();
        PairMutable boundsRealEdgeL = this.PosToHolderPane(new PairMutable(boundsLocal.getMinX() + posOffset.GetLeftDouble(), boundsLocal.getMinY() + posOffset.GetRightDouble()));
        PairMutable boundsRealEdgeH = this.PosToHolderPane(new PairMutable(boundsLocal.getMaxX() + posOffset.GetLeftDouble(), boundsLocal.getMaxY() + posOffset.GetRightDouble()));

        return new PairMutable((boundsRealEdgeL.GetLeftDouble() + boundsRealEdgeH.GetLeftDouble()) / 2,
                               (boundsRealEdgeL.GetRightDouble() + boundsRealEdgeH.GetRightDouble()) / 2);
    }

    public PairMutable PosToHolderPane(PairMutable pos)
    {
        Point2D newPos = this.node.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public boolean PosOnNode(PairMutable pos)
    {
        return this.node.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    public static PairMutable NodesGetMiddlePos(LinkedList<Node> nodes)
    {
        PairMutable nodeBounds = NodesGetRealBounds(nodes);

        return new PairMutable((nodeBounds.GetLeftPair().GetLeftDouble() + nodeBounds.GetLeftPair().GetRightDouble()) / 2,
                (nodeBounds.GetRightPair().GetLeftDouble() + nodeBounds.GetRightPair().GetRightDouble()) / 2);
    }

    public static PairMutable NodesGetRealBounds(LinkedList<Node> nodes)
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

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        LinkedList<PairMutable> clonedSnapPointManualPos = null;

        if (!this.manualSnapPoints.isEmpty())
        {
            clonedSnapPointManualPos = new LinkedList<PairMutable>();

            for (int i = 0; i < this.manualSnapPoints.size(); i++)
                clonedSnapPointManualPos.add(new PairMutable(this.manualSnapPoints.get(i).getTranslateX(),
                                                             this.manualSnapPoints.get(i).getTranslateY()));
        }

        EDANode clonedNode = new EDANode(this.name,
                                               Utils.Utils_NodeClone(this.node),
                                               this.autoSnapPoints,
                                               clonedSnapPointManualPos,
                                               this.passive,
                                               this.isPin,
                                               null);

        return clonedNode;
    }

    public static String ToFXMLString(Node node, PairMutable posOffset, int tabNum)
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
            str += AddLeadingZeros(Integer.toHexString(circle.getFill().hashCode()));

            //if (circle.getStroke() != null)
            //{
                str += " stroke=\"#" + circle.getStroke().toString().replace("0x", "") + "\"";
                //str += " strokeType=\"#" + circle.getStrokeType().toString().replace("#", "") + "\"";
                str += " strokeType=\"INSIDE\"";
                str += " strokeWidth=\"" + circle.getStrokeWidth()+ "\"";
            //}

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
            str += AddLeadingZeros(Integer.toHexString(rectangle.getFill().hashCode()));

            //if (rectangle.getStroke() != null)
            //{
                str += " stroke=\"#" + rectangle.getStroke().toString().replace("0x", "") + "\"";
                //str += " strokeType=\"#" + rectangle.getStrokeType().toString().replace("#", "") + "\"";
                str += " strokeType=\"INSIDE\"";
                str += " strokeWidth=\"" + rectangle.getStrokeWidth() + "\"";
            //}

            str += " translateX=\"" + (rectangle.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (rectangle.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + rectangle.getRotate() + "\" />";
        }
        else if (node.getClass() == Polygon.class)
        {
            Polygon triangle = (Polygon)node;

            str += tabStr + "<Polygon";
            str += " id=\"" + triangle.getId() + "\"";
            str += AddLeadingZeros(Integer.toHexString(triangle.getFill().hashCode()));

            //if (triangle.getStroke() != null)
            //{
                str += " stroke=\"#" + triangle.getStroke().toString().replace("0x", "") + "\"";
                //str += " strokeType=\"#" + triangle.getStrokeType().toString().replace("#", "") + "\"";
                str += " strokeType=\"INSIDE\"";
                str += " strokeWidth=\"" + triangle.getStrokeWidth() + "\"";
            //}

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
            String addZeros = Integer.toHexString(text.getFill().hashCode());
            str += AddLeadingZeros(addZeros);

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
                str += ToFXMLString(group.getChildren().get(i), new PairMutable(0.0, 0.0), tabNum + 2) + "\n";

            str += tabStr + "\t</children>\n";
            str += tabStr + "</Group>";
        }
        else
        {
            throw new java.lang.Error("ERROR: Attempting to convert an unknown node type to FXML string!");
        }

        return str;
    }

    public static String AddLeadingZeros(String addZeros)
    {
        if (addZeros.length() < 8)
        {
            while (addZeros.length() < 8)
                addZeros = "0" + addZeros;
            return " fill=\"#" + addZeros + "\"";
        }
        else
        {
            return " fill=\"#" + addZeros + "\"";
        }
    }
}