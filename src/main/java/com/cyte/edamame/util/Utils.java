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
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;

public class Utils
{
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
            if (nodes.get(i).RenderNode_Name.equals(name))
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