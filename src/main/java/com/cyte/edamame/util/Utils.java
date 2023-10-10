/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.util;

import com.cyte.edamame.render.RenderShape;

import java.util.*;

public class Utils
{
    static public Integer FindCanvasShape(LinkedList<RenderShape> shapes, String name)
    {
        for (int i = 0; i < shapes.size(); i++)
            if (shapes.get(i).name.equals(name))
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