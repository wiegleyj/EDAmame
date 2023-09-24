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

public class CanvasRenderShape
{
    final UUID id = UUID.randomUUID();
    public String name;
    public LinkedList<PairMutable> points;
    public LinkedList<Double> pointWidths;
    public LinkedList<Color> pointColors;
    public LinkedList<Double> pointOpacities;
    public LinkedList<PairMutable> lineIndices;
    public LinkedList<Double> lineWidths;
    public LinkedList<Color> lineColors;
    public LinkedList<Double> lineOpacities;
    public Double globalOpacity;
    public BlendMode blendMode;
    public PairMutable boundingBox;
    public PairMutable posDraw;
    public boolean zoomScaling;
    public boolean permanent;
    //public EditorSchematic_Symbol symbol;

    public CanvasRenderShape()
    {
        this.name = "";
        this.points = new LinkedList<PairMutable>();
        this.pointWidths = new LinkedList<Double>();
        this.pointColors = new LinkedList<Color>();
        this.pointOpacities = new LinkedList<Double>();
        this.lineIndices = new LinkedList<PairMutable>();
        this.lineWidths = new LinkedList<Double>();
        this.lineColors = new LinkedList<Color>();
        this.lineOpacities = new LinkedList<Double>();
        this.globalOpacity = 1.0;
        this.blendMode = BlendMode.SRC_OVER;
        this.boundingBox = new PairMutable(0.0, 0.0);
        this.posDraw = new PairMutable(0.0, 0.0);
        this.zoomScaling = true;
        this.permanent = false;
        //this.symbol = null;
    }

    public CanvasRenderShape(CanvasRenderShape otherShape)
    {
        this.name = otherShape.name;
        this.points = new LinkedList<PairMutable>();
        this.pointWidths = new LinkedList<Double>();
        this.pointColors = new LinkedList<Color>();
        this.pointOpacities = new LinkedList<Double>();
        this.lineIndices = new LinkedList<PairMutable>();
        this.lineWidths = new LinkedList<Double>();
        this.lineColors = new LinkedList<Color>();
        this.lineOpacities = new LinkedList<Double>();
        this.globalOpacity = otherShape.globalOpacity;
        this.blendMode = otherShape.blendMode;

        for (int i = 0; i < otherShape.points.size(); i++)
            this.AddPoint(otherShape.points.get(i).GetLeftDouble(), otherShape.points.get(i).GetRightDouble(), otherShape.pointWidths.get(i), otherShape.pointColors.get(i), otherShape.pointOpacities.get(i));
        for (int i = 0; i < otherShape.lineIndices.size(); i++)
            this.AddLine(otherShape.lineIndices.get(i).GetLeftInteger(), otherShape.lineIndices.get(i).GetRightInteger(), otherShape.lineWidths.get(i), otherShape.lineColors.get(i), otherShape.lineOpacities.get(i));

        this.boundingBox = new PairMutable(otherShape.boundingBox);
        this.posDraw = new PairMutable(otherShape.posDraw);
        this.zoomScaling = otherShape.zoomScaling;
        this.permanent = otherShape.permanent;
        //this.symbol = otherShape.symbol;
    }

    public void DrawShape(GraphicsContext gc)
    {
        // Checking whether our drawing elements are valid
        if (this.id == null)
            throw new java.lang.Error("ERROR: Attempting to draw canvas shape with no id!");
        if (this.name == null)
            throw new java.lang.Error("ERROR: Attempting to draw canvas shape with no name!");
        if ((this.points.size() != this.pointWidths.size()) || (this.pointWidths.size() != this.pointColors.size()) || (points.size() != this.pointColors.size()))
            throw new java.lang.Error("ERROR: Attempting to draw canvas shape \"" + this.name + "\" with this.unequal amount of points, point widths or point colors!");
        if ((this.lineIndices.size() != this.lineWidths.size()) || (this.lineWidths.size() != this.lineColors.size()) || (this.lineIndices.size() != this.lineColors.size()))
            throw new java.lang.Error("ERROR: Attempting to draw canvas shape \"" + this.name + "\" with unequal amount of lines, line widths or line colors!");

        //Blend blend = new Blend();
        //blend.setMode(this.blendMode);

        // Drawing the points
        for (int i = 0; i < points.size(); i++)
        {
            if (this.globalOpacity >= 1.0)
                gc.setGlobalAlpha(this.pointOpacities.get(i));
            else
                gc.setGlobalAlpha(this.globalOpacity);

            gc.setFill(this.pointColors.get(i));

            //blend.setTopInput(this.pointColors.get(i));
            //gc.setEffect(blend);

            gc.fillOval(this.points.get(i).GetLeftDouble() - (this.pointWidths.get(i) / 2.0) + this.posDraw.GetLeftDouble(),
                        this.points.get(i).GetRightDouble() - (this.pointWidths.get(i) / 2.0) + this.posDraw.GetRightDouble(),
                        this.pointWidths.get(i),
                        this.pointWidths.get(i));
        }

        // Drawing the lines
        for (int i = 0; i < this.lineIndices.size(); i++)
        {
            if ((this.lineIndices.get(i).GetLeftInteger() < 0) || (this.lineIndices.get(i).GetLeftInteger() >= this.points.size()))
                throw new java.lang.Error("ERROR: Drawing line with incorrect starting point index!");
            if ((this.lineIndices.get(i).GetRightInteger() < 0) || (this.lineIndices.get(i).GetRightInteger() >= this.points.size()))
                throw new java.lang.Error("ERROR: Drawing line with incorrect ending point index!");

            if (this.globalOpacity >= 1.0)
                gc.setGlobalAlpha(this.lineOpacities.get(i));
            else
                gc.setGlobalAlpha(this.globalOpacity);

            gc.setStroke(this.lineColors.get(i));

            //blend.setTopInput(this.lineColors.get(i));
            //gc.setEffect(blend);

            gc.setLineWidth(this.lineWidths.get(i));
            gc.strokeLine(this.points.get(this.lineIndices.get(i).GetLeftInteger()).GetLeftDouble() + this.posDraw.GetLeftDouble(),
                    this.points.get(this.lineIndices.get(i).GetLeftInteger()).GetRightDouble() + this.posDraw.GetRightDouble(),
                    this.points.get(this.lineIndices.get(i).GetRightInteger()).GetLeftDouble() + this.posDraw.GetLeftDouble(),
                    this.points.get(this.lineIndices.get(i).GetRightInteger()).GetRightDouble() + this.posDraw.GetRightDouble());
        }

        // Drawing the wire connection points
        /*if (this.symbol != null)
        {
            if (!this.symbol.wirePoints.isEmpty())
            {
                gc.setGlobalAlpha(this.symbol.wirePointsOpacity);
                gc.setFill(this.symbol.wirePointsColor);
                gc.setLineWidth(this.symbol.wirePointsWidth);

                for (int i = 0; i < this.symbol.wirePoints.size(); i++)
                    gc.fillOval(this.symbol.wirePoints.get(i).GetLeftDouble() - (this.symbol.wirePointsWidth / 2.0) + this.posDraw.GetLeftDouble(),
                            this.symbol.wirePoints.get(i).GetRightDouble() - (this.symbol.wirePointsWidth / 2.0) + this.posDraw.GetRightDouble(),
                            this.symbol.wirePointsWidth,
                            this.symbol.wirePointsWidth);
            }
        }*/

        // Drawing the bounding boxes
        this.CalculateBoundingBox();

        /*if (this.symbol != null)
        {
            if (this.symbol.highlightedMouse || this.symbol.highlightedBox)
                this.DrawBoundingBox(gc, this.symbol.highlightColor, this.symbol.highlightOpacity);
            if (this.symbol.selected)
                this.DrawBoundingBox(gc, this.symbol.selectColor, this.symbol.selectOpacity);
        }*/
    }

    public void CalculateBoundingBox()
    {
        LinkedList<Double> pointsX = new LinkedList<Double>();
        LinkedList<Double> pointsY = new LinkedList<Double>();
        LinkedList<Double> pointsWidthsX = new LinkedList<Double>();
        LinkedList<Double> pointsWidthsY = new LinkedList<Double>();

        for (int i = 0; i < this.points.size(); i++)
        {
            pointsX.add(this.points.get(i).GetLeftDouble());
            pointsY.add(this.points.get(i).GetRightDouble());
            pointsWidthsX.add(this.pointWidths.get(i));
            pointsWidthsY.add(this.pointWidths.get(i));
        }

        Integer maxXIdx = Utils.ListFindMaxIdx(pointsX);
        Integer minXIdx = Utils.ListFindMinIdx(pointsX);
        Integer maxYIdx = Utils.ListFindMaxIdx(pointsY);
        Integer minYIdx = Utils.ListFindMinIdx(pointsY);

        this.boundingBox.left = pointsWidthsX.get(maxXIdx) / 2 + pointsX.get(maxXIdx) - pointsX.get(minXIdx) + pointsWidthsX.get(minXIdx) / 2;
        this.boundingBox.right = pointsWidthsY.get(maxYIdx) / 2 + pointsY.get(maxYIdx) - pointsY.get(minYIdx) + pointsWidthsY.get(minYIdx) / 2;
    }

    public void DrawBoundingBox(GraphicsContext gc, Color color, Double opacity)
    {
        if (opacity > 0.0)
        {
            gc.setGlobalAlpha(opacity);
            gc.setFill(color);
            gc.fillRect(this.posDraw.GetLeftDouble() - this.boundingBox.GetLeftDouble() / 2,
                        this.posDraw.GetRightDouble() - this.boundingBox.GetRightDouble() / 2,
                        this.boundingBox.GetLeftDouble(),
                        this.boundingBox.GetRightDouble());
        }
    }

    public void AddPoint(Double posX, Double posY, Double width, Color color, Double opacity)
    {
        this.points.add(new PairMutable(posX, posY));
        this.pointWidths.add(width);
        this.pointColors.add(color);
        this.pointOpacities.add(opacity);
    }

    public void AddLine(Integer idx1, Integer idx2, Double width, Color color, Double opacity)
    {
        this.lineIndices.add(new PairMutable(idx1, idx2));
        this.lineWidths.add(width);
        this.lineColors.add(color);
        this.lineOpacities.add(opacity);
    }
}