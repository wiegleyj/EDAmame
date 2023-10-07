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
    public boolean highlighted;
    public boolean selected;
    public PairMutable mousePressPos;

    public RenderShape(String nameValue, Shape shapeValue)
    {
        this.name = nameValue;
        this.shape = shapeValue;
        this.shape.setId(id);
        this.shapeHighlighted = null;
        this.shapeSelected = null;
        this.highlighted = false;
        this.selected = false;
        this.mousePressPos = null;
    }

    public boolean PosOnShape(PairMutable pos)
    {
        return this.shape.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    public void CalculateShapeSelected()
    {
        Bounds bounds = this.shape.getBoundsInParent();

        if (this.shape.getClass() == Rectangle.class)
            this.shapeSelected = new Rectangle(0, 0, bounds.getWidth(), bounds.getHeight());
        else
            this.shapeSelected = new Rectangle(-bounds.getWidth() / 2, -bounds.getHeight() / 2, bounds.getWidth(), bounds.getHeight());

        this.shapeSelected.setFill(Color.GRAY);
        this.shapeSelected.setOpacity(0.5);
        this.shapeSelected.setId(this.id);
        this.shapeSelected.translateXProperty().bind(this.shape.translateXProperty());
        this.shapeSelected.translateYProperty().bind(this.shape.translateYProperty());
    }

    public void CalculateShapeHighlighted()
    {
        Bounds bounds = this.shape.getBoundsInParent();

        if (this.shape.getClass() == Rectangle.class)
            this.shapeHighlighted = new Rectangle(0, 0, bounds.getWidth(), bounds.getHeight());
        else
            this.shapeHighlighted = new Rectangle(-bounds.getWidth() / 2, -bounds.getHeight() / 2, bounds.getWidth(), bounds.getHeight());

        this.shapeHighlighted.setFill(Color.GRAY);
        this.shapeHighlighted.setOpacity(0.5);
        this.shapeHighlighted.setId(this.id);
        this.shapeHighlighted.translateXProperty().bind(this.shape.translateXProperty());
        this.shapeHighlighted.translateYProperty().bind(this.shape.translateYProperty());
    }
}