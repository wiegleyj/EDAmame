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

public class SnapPoint extends Circle
{
    public PairMutable SnapPoint_Pos;
    public RenderNode SnapPoint_RenderNode;

    public SnapPoint(PairMutable posValue, Double radiusValue, Color colorValue, Double opacityValue, RenderNode renderNodeValue)
    {
        this.SnapPoint_Pos = posValue;
        this.SnapPoint_RenderNode = renderNodeValue;
        this.setRadius(radiusValue);
        this.setFill(colorValue);
        this.setOpacity(opacityValue);
        this.setTranslateX(posValue.GetLeftDouble());
        this.setTranslateY(posValue.GetRightDouble());
    }
}
