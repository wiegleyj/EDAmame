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

import javafx.scene.Node;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;

public class CanvasRenderSystem
{
    final UUID id = UUID.randomUUID();
    public Canvas canvas;
    public GraphicsContext gc;
    public Color backgroundColor;
    public LinkedList<CanvasRenderShape> shapes;
    public LinkedList<PairMutable> shapesPosReal;
    public Integer maxShapes;
    public Double zoom;

    public CanvasRenderSystem(Canvas canvasValue, Color backgroundColorValue, Integer maxShapesValue)
    {
        this.canvas = canvasValue;
        this.gc = this.canvas.getGraphicsContext2D();
        this.backgroundColor = backgroundColorValue;
        this.shapes = new LinkedList<CanvasRenderShape>();
        this.shapesPosReal = new LinkedList<PairMutable>();
        this.maxShapes = maxShapesValue;
        this.zoom = 1.0;
    }

    public void Render()
    {
        this.Clear();

        Integer i = 0;

        while (i < this.shapes.size())
        {
            CanvasRenderShape shape = this.shapes.get(i);
            PairMutable posReal = this.shapesPosReal.get(i);
            //EditorSchematic_Symbol symbol = shape.symbol;

            if (posReal != null)
                shape.posDraw = this.CalculatePosDraw(shape, posReal);

            if (shape.zoomScaling)
            {
                for (int j = 0; j < shape.points.size(); j++)
                {
                    shape.points.set(j, new PairMutable(shape.points.get(j).GetLeftDouble() * this.zoom, shape.points.get(j).GetRightDouble() * this.zoom));
                    shape.pointWidths.set(j, shape.pointWidths.get(j) * this.zoom);
                }

                //for (int j = 0; j < symbol.wirePoints.size(); j++)
                //    symbol.wirePoints.set(j, new PairMutable(symbol.wirePoints.get(j).GetLeftDouble() * this.zoom, symbol.wirePoints.get(j).GetRightDouble() * this.zoom));
            }

            shape.DrawShape(this.gc);

            if (shape.zoomScaling)
            {
                for (int j = 0; j < shape.points.size(); j++)
                {
                    shape.points.set(j, new PairMutable(shape.points.get(j).GetLeftDouble() / this.zoom, shape.points.get(j).GetRightDouble() / this.zoom));
                    shape.pointWidths.set(j, shape.pointWidths.get(j) / this.zoom);
                }

                //for (int j = 0; j < symbol.wirePoints.size(); j++)
                //    symbol.wirePoints.set(j, new PairMutable(symbol.wirePoints.get(j).GetLeftDouble() / this.zoom, symbol.wirePoints.get(j).GetRightDouble() / this.zoom));
            }

            if (!shape.permanent)
            {
                this.shapes.remove(shape);
                this.shapesPosReal.remove(posReal);
                i--;
            }

            i++;
        }
    }

    public void Clear()
    {
        this.gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        this.gc.setFill(this.backgroundColor);
        this.gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    public PairMutable CalculatePosDraw(CanvasRenderShape shape, PairMutable posReal)
    {
        PairMutable posDraw = new PairMutable(posReal);

        posDraw.left = posDraw.GetLeftDouble() * this.zoom + this.canvas.getWidth() / 2;
        posDraw.right = posDraw.GetRightDouble() * this.zoom + this.canvas.getHeight() / 2;

        return posDraw;
    }

    public void AddShape(Integer idx, CanvasRenderShape shape, PairMutable posReal)
    {
        if (this.shapes.size() >= this.maxShapes)
            return;

        if (idx < 0)
        {
            this.shapes.add(shape);
            this.shapesPosReal.add(posReal);
        }
        else
        {
            this.shapes.add(idx, shape);
            this.shapesPosReal.add(idx, posReal);
        }
    }

    public void RemoveShape(Integer idx)
    {
        this.shapes.remove(this.shapes.get(idx));
        this.shapesPosReal.remove(this.shapesPosReal.get(idx));
    }

    public void BindSize(Node node)
    {
        if (node.getClass() == TabPane.class)
        {
            this.canvas.widthProperty().bind(((TabPane)node).widthProperty());
            this.canvas.heightProperty().bind(((TabPane)node).heightProperty());
        }
        else if (node.getClass() == TextArea.class)
        {
            this.canvas.widthProperty().bind(((TextArea)node).widthProperty());
            this.canvas.heightProperty().bind(((TextArea)node).heightProperty());
        }
        else if (node.getClass() == VBox.class)
        {
            this.canvas.widthProperty().bind(((VBox)node).widthProperty());
            this.canvas.heightProperty().bind(((VBox)node).heightProperty());
        }
        else
        {
            throw new java.lang.Error("ERROR: Unsupported node type supplied to size binding function of canvas!");
        }
    }

    public void UnbindSize()
    {
        this.canvas.widthProperty().unbind();
        this.canvas.heightProperty().unbind();
    }

    public void SetSize(PairMutable sizeValue)
    {
        this.UnbindSize();

        this.canvas.setWidth(sizeValue.GetLeftDouble());
        this.canvas.setHeight(sizeValue.GetRightDouble());
    }

    public PairMutable GetSize()
    {
        return new PairMutable(canvas.getWidth(), canvas.getHeight());
    }
}