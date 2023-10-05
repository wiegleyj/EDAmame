/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.render;
import com.cyte.edamame.util.Utils;
import com.cyte.edamame.util.PairMutable;

import java.util.LinkedList;
import java.util.UUID;

import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.scene.effect.*;
import javafx.scene.shape.*;
import javafx.geometry.*;

public class RenderShape
{
    final String id = UUID.randomUUID().toString();

    public String name;
    public Shape shape;
    public Shape shapeHighlighted;
    public Shape shapeSelected;

    public RenderShape(String nameValue, Shape shapeValue)
    {
        this.name = nameValue;
        this.shape = shapeValue;
        this.shape.setId(id);
    }

    public RenderShape(String nameValue, Shape shapeValue, Shape shapeHighlightedValue, Shape shapeSelectedValue)
    {
        this.name = nameValue;
        this.shape = shapeValue;
        this.shape.setId(id);
        this.shapeHighlighted = shapeHighlightedValue;
        this.shapeHighlighted.setId(id);
        this.shapeSelected = shapeSelectedValue;
        this.shapeSelected.setId(id);
    }

    public boolean PosOnShape(PairMutable pos)
    {
        return this.shape.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }
}