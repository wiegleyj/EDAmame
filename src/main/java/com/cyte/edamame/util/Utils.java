/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.util;

import com.cyte.edamame.render.RenderNode;

import java.util.*;

import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Utils
{
    public static Node Utils_NodeClone(Node oldNode)
    {
        Node clonedNode = null;

        if (oldNode.getClass() == Circle.class)
        {
            Circle oldCircle = (Circle)oldNode;

            Circle clonedCircle = new Circle();
            clonedCircle.setRadius(oldCircle.getRadius());
            Color colorFill = (Color)oldCircle.getFill();
            clonedCircle.setFill(Color.rgb((int)(colorFill.getRed() * 255), (int)(colorFill.getGreen() * 255), (int)(colorFill.getBlue() * 255), colorFill.getOpacity()));
            Color colorStroke = (Color)oldCircle.getStroke();
            clonedCircle.setStroke(Color.rgb((int)(colorStroke.getRed() * 255), (int)(colorStroke.getGreen() * 255), (int)(colorStroke.getBlue() * 255), colorStroke.getOpacity()));
            clonedCircle.setStrokeWidth(oldCircle.getStrokeWidth());

            clonedNode = clonedCircle;
        }
        else if (oldNode.getClass() == Rectangle.class)
        {
            Rectangle oldRectangle = (Rectangle)oldNode;

            Rectangle clonedRectangle = new Rectangle();
            clonedRectangle.setWidth(oldRectangle.getWidth());
            clonedRectangle.setHeight(oldRectangle.getHeight());
            Color colorFill = (Color)oldRectangle.getFill();
            clonedRectangle.setFill(Color.rgb((int)(colorFill.getRed() * 255), (int)(colorFill.getGreen() * 255), (int)(colorFill.getBlue() * 255), colorFill.getOpacity()));
            Color colorStroke = (Color)oldRectangle.getStroke();
            clonedRectangle.setStroke(Color.rgb((int)(colorStroke.getRed() * 255), (int)(colorStroke.getGreen() * 255), (int)(colorStroke.getBlue() * 255), colorStroke.getOpacity()));
            clonedRectangle.setStrokeWidth(oldRectangle.getStrokeWidth());

            clonedNode = clonedRectangle;
        }
        else if (oldNode.getClass() == Polygon.class)
        {
            Polygon oldTriangle = (Polygon)oldNode;

            Polygon clonedTriangle = new Polygon();
            LinkedList<Double> clonedPoints = new LinkedList<Double>(oldTriangle.getPoints());
            clonedTriangle.getPoints().setAll(clonedPoints);
            Color colorFill = (Color)oldTriangle.getFill();
            clonedTriangle.setFill(Color.rgb((int)(colorFill.getRed() * 255), (int)(colorFill.getGreen() * 255), (int)(colorFill.getBlue() * 255), colorFill.getOpacity()));
            Color colorStroke = (Color)oldTriangle.getStroke();
            clonedTriangle.setStroke(Color.rgb((int)(colorStroke.getRed() * 255), (int)(colorStroke.getGreen() * 255), (int)(colorStroke.getBlue() * 255), colorStroke.getOpacity()));
            clonedTriangle.setStrokeWidth(oldTriangle.getStrokeWidth());

            clonedNode = clonedTriangle;
        }
        else if (oldNode.getClass() == Line.class)
        {
            Line oldLine = (Line)oldNode;

            Line clonedLine = new Line();
            clonedLine.setStartX(oldLine.getStartX());
            clonedLine.setStartY(oldLine.getStartY());
            clonedLine.setEndX(oldLine.getEndX());
            clonedLine.setEndY(oldLine.getEndY());
            Color colorStroke = (Color)oldLine.getStroke();
            clonedLine.setStroke(Color.rgb((int)(colorStroke.getRed() * 255), (int)(colorStroke.getGreen() * 255), (int)(colorStroke.getBlue() * 255), colorStroke.getOpacity()));
            clonedLine.setStrokeWidth(oldLine.getStrokeWidth());

            clonedNode = clonedLine;
        }
        else if (oldNode.getClass() == Text.class)
        {
            Text oldText = (Text)oldNode;

            Text clonedText = new Text();
            clonedText.setFont(new Font("Arial", oldText.getFont().getSize()));
            clonedText.setText(new String(oldText.getText()));
            Color colorFill = (Color)oldText.getFill();
            clonedText.setFill(Color.rgb((int)(colorFill.getRed() * 255), (int)(colorFill.getGreen() * 255), (int)(colorFill.getBlue() * 255), colorFill.getOpacity()));

            clonedNode = clonedText;
        }
        else if (oldNode.getClass() == Group.class)
        {
            Group oldGroup = (Group)oldNode;

            Group clonedGroup = new Group();

            for (int i = 0; i < oldGroup.getChildren().size(); i++)
                clonedGroup.getChildren().add(Utils.Utils_NodeClone(oldGroup.getChildren().get(i)));

            clonedNode = clonedGroup;
        }
        else
        {
            throw new java.lang.Error("ERROR: Attempting to clone an unrecognized JavaFX oldNode type!");
        }

        clonedNode.setTranslateX(oldNode.getTranslateX());
        clonedNode.setTranslateY(oldNode.getTranslateY());
        clonedNode.setLayoutX(oldNode.getLayoutX());
        clonedNode.setLayoutY(oldNode.getLayoutY());
        clonedNode.setRotate(oldNode.getRotate());

        return clonedNode;
    }

    static public PairMutable GetPosInNodeParent(Node node, PairMutable pos)
    {
        Point2D newPos = node.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    static public Double GetDist(PairMutable pointA, PairMutable pointB)
    {
        return Math.sqrt(Math.pow(pointB.GetLeftDouble() - pointA.GetLeftDouble(), 2) + Math.pow(pointB.GetRightDouble() - pointA.GetRightDouble(), 2));
    }

    static public Double GetDist(LinkedList<Double> pointA, LinkedList<Double> pointB)
    {
        if (pointA.size() != pointB.size())
            throw new java.lang.Error("ERROR: Attempting to get distance between points of different dimensions!");

        Double squaredSum = 0.0;

        for (int i = 0; i < pointA.size(); i++)
            squaredSum += Math.pow(pointB.get(i) - pointA.get(i), 2);

        return Math.sqrt(squaredSum);
    }

    static public Integer FindCanvasShape(LinkedList<RenderNode> nodes, String name)
    {
        for (int i = 0; i < nodes.size(); i++)
            if (nodes.get(i).name.equals(name))
                return i;

        return -1;
    }

    static public Integer ListFindMaxIdx(LinkedList<Double> list)
    {
        int maxIdx = -1;
        Double max = -Double.MAX_VALUE;

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) > max)
            {
                maxIdx = i;
                max = list.get(i);
            }
        }

        return maxIdx;
    }

    static public Integer ListFindMinIdx(LinkedList<Double> list)
    {
        int minIdx = -1;
        Double min = Double.MAX_VALUE;

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) < min)
            {
                minIdx = i;
                min = list.get(i);
            }
        }

        return minIdx;
    }

    static public Double ListFindMax(LinkedList<Double> list)
    {
        int maxIdx = -1;
        Double max = -Double.MAX_VALUE;

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) > max)
            {
                maxIdx = i;
                max = list.get(i);
            }
        }

        return list.get(maxIdx);
    }

    static public Double ListFindMin(LinkedList<Double> list)
    {
        int minIdx = -1;
        Double min = Double.MAX_VALUE;

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) < min)
            {
                minIdx = i;
                min = list.get(i);
            }
        }

        return list.get(minIdx);
    }
}