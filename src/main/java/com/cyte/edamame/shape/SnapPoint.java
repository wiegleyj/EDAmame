/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.shape;
import com.cyte.edamame.render.RenderNode;
import com.cyte.edamame.util.PairMutable;

import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;

import java.util.LinkedList;

public class SnapPoint extends Circle
{
    public PairMutable SnapPoint_Pos;
    public RenderNode SnapPoint_RenderNode;

    public SnapPoint(PairMutable posValue, Double radiusValue, Paint colorValue, Double opacityValue, RenderNode renderNodeValue)
    {
        this.SnapPoint_Pos = posValue;
        this.SnapPoint_RenderNode = renderNodeValue;
        this.setRadius(radiusValue);
        this.setFill(colorValue);
        this.setOpacity(opacityValue);
        this.setTranslateX(posValue.GetLeftDouble());
        this.setTranslateY(posValue.GetRightDouble());
    }

    public SnapPoint SnapPoint_Clone(RenderNode renderNodeValue)
    {
        return new SnapPoint(new PairMutable(this.SnapPoint_Pos),
                             this.getRadius(),
                             this.getFill(),
                             this.getOpacity(),
                             renderNodeValue);
    }

    /*public static LinkedList<SnapPoint> SnapPoint_CloneList(LinkedList<SnapPoint> oldSnapPoints)
    {
        LinkedList<SnapPoint> clonedSnapPoints = new LinkedList<SnapPoint>();

        for (int i = 0; i < oldSnapPoints.size(); i++)
            clonedSnapPoints.add(oldSnapPoints.get(i).SnapPoint_Clone(oldSnapPoints.get(i).SnapPoint_RenderNode));

        return clonedSnapPoints;
    }*/
}
