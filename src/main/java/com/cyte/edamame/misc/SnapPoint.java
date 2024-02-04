/*
 * Copyright (c) 2023-2024. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.misc;
import com.cyte.edamame.node.EDANode;

import javafx.scene.paint.*;
import javafx.scene.shape.*;

public class SnapPoint extends Circle
{
    public PairMutable pos;

    public EDANode renderNode;

    public SnapPoint(PairMutable posValue, Double radiusValue, Paint colorValue, Double opacityValue, EDANode renderNodeValue)
    {
        this.pos = posValue;
        this.renderNode = renderNodeValue;
        this.setRadius(radiusValue);
        this.setFill(colorValue);
        this.setOpacity(opacityValue);
        this.setTranslateX(posValue.GetLeftDouble());
        this.setTranslateY(posValue.GetRightDouble());
    }

    public SnapPoint Clone(EDANode renderNodeValue)
    {
        return new SnapPoint(new PairMutable(this.pos),
                             this.getRadius(),
                             this.getFill(),
                             this.getOpacity(),
                             renderNodeValue);
    }
}
