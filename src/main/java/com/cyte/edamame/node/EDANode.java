/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.node;

import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.misc.PairMutable;
import com.cyte.edamame.misc.SnapPoint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import java.util.LinkedList;

import com.cyte.edamame.misc.Utils;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.scene.text.*;

abstract public class EDANode
{
    //// GLOBAL VARIABLES ////

    final public String id = UUID.randomUUID().toString();

    public String name;
    public Rectangle shapeHighlighted;
    public Rectangle shapeSelected;
    public boolean highlighted;
    public boolean highlightedMouse;
    public boolean highlightedBox;
    public boolean selected;
    public PairMutable mousePressPos;
    public boolean passive;
    public LinkedList<SnapPoint> snapPoints;
    public String[] conns;

    public Editor editor;

    @Override
    public int hashCode() {
        return Objects.hash(id, name, shapeHighlighted, shapeSelected, highlighted, highlightedMouse, highlightedBox, selected, mousePressPos, passive, snapPoints);
    }

    //// SETTERS ////

    abstract public void SetTranslate(PairMutable pos);
    abstract public void SetRotate(double rot);
    abstract public void SetVisible(boolean visible);

    //// GETTERS ////

    public Rectangle GetShapeHighlighted()
    {
        return this.shapeHighlighted;
    }

    public Rectangle GetShapeSelected()
    {
        return this.shapeSelected;
    }

    abstract public Node GetNode();
    abstract public PairMutable GetTranslate();
    abstract public PairMutable GetSnapPos();
    abstract public double GetRotate();
    abstract public Bounds GetBoundsLocal();
    abstract public Bounds GetBoundsParent();

    //// EDITOR FUNCTIONS ////

    public void Heartbeat()
    {
        if (this.passive)
            return;

        this.ShapeHighlightedRefresh();
        this.ShapeSelectedRefresh();
        this.SnapPointsRefresh();
    }

    public void Move()
    {
        if (!this.selected)
            return;

        PairMutable posOffset = new PairMutable(0.0, 0.0);

        // Handling straight-only dragging...
        if (EDAmameController.IsKeyPressed(KeyCode.CONTROL))
        {
            if (Math.abs(this.editor.mouseDragDiffPos.GetLeftDouble()) < Math.abs(this.editor.mouseDragDiffPos.GetRightDouble()))
                posOffset.left = -this.editor.mouseDragDiffPos.GetLeftDouble();
            else
                posOffset.right = -this.editor.mouseDragDiffPos.GetRightDouble();
        }

        PairMutable newPos = new PairMutable(this.mousePressPos.GetLeftDouble() + this.editor.mouseDragDiffPos.GetLeftDouble() + posOffset.GetLeftDouble(),
                                             this.mousePressPos.GetRightDouble() + this.editor.mouseDragDiffPos.GetRightDouble() + posOffset.GetRightDouble());

        this.SetTranslate(this.editor.PaneHolderGetDrawPos(this.editor.PosSnapToGridPoint(this.editor.PaneHolderGetRealPos(newPos))));
    }

    public void MoveReset()
    {
        if (!this.selected)
            return;

        this.mousePressPos = this.GetTranslate();
    }

    public void Add()
    {
        if (this.editor.nodes.size() >= this.editor.maxShapes)
            throw new java.lang.Error("ERROR: Exceeded editors maximum node limit!");

        this.editor.nodes.add(this);
        this.NodeAdd();

        if (!this.passive)
        {
            this.editor.paneHighlights.getChildren().add(this.shapeHighlighted);
            this.editor.paneSelections.getChildren().add(this.shapeSelected);
        }

        for (int i = 0; i < this.snapPoints.size(); i++)
            this.editor.paneSnaps.getChildren().add(this.snapPoints.get(i));
    }

    public boolean Remove()
    {
        int idx = this.editor.NodeFindByID(this.id);

        if (idx == -1)
            return false;

        this.editor.nodes.remove(idx);
        this.NodeRemove();

        if (!this.passive)
        {
            this.editor.paneHighlights.getChildren().remove(this.shapeHighlighted);
            this.editor.paneSelections.getChildren().remove(this.shapeSelected);
        }

        if (this.highlighted)
            this.editor.shapesHighlighted--;
        if (this.selected)
            this.editor.shapesSelected--;

        this.highlighted = false;
        this.selected = false;

        for (int j = 0; j < this.snapPoints.size(); j++)
            this.editor.paneSnaps.getChildren().remove(this.snapPoints.get(j));

        return true;
    }

    abstract public void Rotate(double deltaY);
    abstract public void NodeAdd();
    abstract public void NodeRemove();

    //// POSITION FUNCTIONS ////

    abstract public PairMutable BoundsPosToHolderPane(PairMutable posOffset);
    abstract public PairMutable PosToHolderPane(PairMutable pos);
    abstract public boolean PosOnNode(PairMutable pos);

    //// HIGHLIGHT & SELECTION FUNCTIONS ////

    public void HighlightCheck(PairMutable posMouse)
    {
        if (this.passive)
            return;

        boolean onShape = this.PosOnNode(posMouse);

        if (!EDAmameController.IsKeyPressed(KeyCode.CONTROL))
        {
            // Checking whether we are highlighting by cursor...
            if (onShape)
            {
                if (EDAmameController.IsKeyPressed(KeyCode.Q))
                {
                    if ((this.editor.shapesHighlighted > 1) && this.highlightedMouse)
                        this.highlightedMouse = false;
                    else if ((this.editor.shapesHighlighted == 0) && !this.highlightedMouse)
                        this.highlightedMouse = true;
                }
                else
                {
                    if (!this.highlightedMouse)
                        this.highlightedMouse = true;
                }
            }
            else
            {
                if (this.highlightedMouse)
                    this.highlightedMouse = false;
            }

            // Checking whether we are highlighting by selection box...
            if (this.editor.selectionBox != null)
            {
                Bounds shapeBounds = this.GetBoundsParent();
                PairMutable selectionBoxL = this.editor.PanePosListenerToHolder(new PairMutable(this.editor.selectionBox.getTranslateX(), this.editor.selectionBox.getTranslateY()));
                PairMutable selectionBoxH = this.editor.PanePosListenerToHolder(new PairMutable(this.editor.selectionBox.getTranslateX() + this.editor.selectionBox.getWidth(), this.editor.selectionBox.getTranslateY() + this.editor.selectionBox.getHeight()));

                if ((selectionBoxL.GetLeftDouble() < shapeBounds.getMaxX()) &&
                        (selectionBoxH.GetLeftDouble() > shapeBounds.getMinX()) &&
                        (selectionBoxL.GetRightDouble() < shapeBounds.getMaxY()) &&
                        (selectionBoxH.GetRightDouble() > shapeBounds.getMinY()))
                {
                    if (!this.highlightedBox)
                        this.highlightedBox = true;
                }
                else
                {
                    if (this.highlightedBox)
                        this.highlightedBox = false;
                }
            }
            else if (this.highlightedBox)
            {
                this.highlightedBox = false;
            }
        }
        else
        {
            this.highlightedMouse = false;
            this.highlightedBox = false;
        }

        // Adjusting highlights accordingly...
        if ((this.highlightedMouse || this.highlightedBox) && !this.highlighted)
            this.Highlight();
        else if ((!this.highlightedMouse && !this.highlightedBox) && this.highlighted)
            this.Unhighlight();
    }

    public void Highlight()
    {
        this.shapeHighlighted.setVisible(true);
        this.highlighted = true;
        this.editor.shapesHighlighted++;
    }

    public void Unhighlight()
    {
        this.shapeHighlighted.setVisible(false);
        this.highlighted = false;
        this.editor.shapesHighlighted--;
    }

    public void SelectCheck()
    {
        if (!this.selected && (this.highlightedMouse || this.highlightedBox))
            this.Select();
        else if (this.selected && (!this.highlightedMouse && !this.highlightedBox) && !EDAmameController.IsKeyPressed(KeyCode.SHIFT))
            this.Unselect();

        if (this.highlightedBox && !this.highlightedMouse)
            this.Unhighlight();

        this.mousePressPos = null;
    }

    public void Select()
    {
        this.selected = true;
        this.shapeSelected.setVisible(true);
        this.editor.shapesSelected++;
    }

    public void Unselect()
    {
        this.selected = false;
        this.shapeSelected.setVisible(false);
        this.editor.shapesSelected--;
    }

    public void ShapeHighlightedRefresh()
    {
        Bounds boundsParent = this.GetBoundsParent();

        this.shapeHighlighted.setTranslateX(boundsParent.getMinX());
        this.shapeHighlighted.setTranslateY(boundsParent.getMinY());

        this.shapeHighlighted.setWidth(boundsParent.getWidth());
        this.shapeHighlighted.setHeight(boundsParent.getHeight());
    }

    public void ShapeSelectedRefresh()
    {
        Bounds boundsLocal = this.GetBoundsLocal();
        PairMutable posBoundsReal = this.BoundsPosToHolderPane(new PairMutable(0.0, 0.0));

        this.shapeSelected.setTranslateX(posBoundsReal.GetLeftDouble() - boundsLocal.getWidth() / 2);
        this.shapeSelected.setTranslateY(posBoundsReal.GetRightDouble() - boundsLocal.getHeight() / 2);

        this.shapeSelected.setRotate(this.GetRotate());

        this.shapeSelected.setWidth(boundsLocal.getWidth());
        this.shapeSelected.setHeight(boundsLocal.getHeight());
    }

    public void ShapeHighlightedCreate()
    {
        this.shapeHighlighted = new Rectangle();
        this.shapeHighlighted.setFill(Color.GRAY);
        this.shapeHighlighted.setOpacity(0.5);
        this.shapeHighlighted.setId(this.id);
        this.shapeHighlighted.setVisible(false);
    }

    public void ShapeSelectedCreate()
    {
        this.shapeSelected = new Rectangle();
        this.shapeSelected.setFill(Color.GRAY);
        this.shapeSelected.setOpacity(0.5);
        this.shapeSelected.setId(this.id);
        this.shapeSelected.setVisible(false);
    }

    //// SNAP POINT FUNCTIONS ////

    public PairMutable MagneticSnapCheck(PairMutable pos, double minDist)
    {
        if (this.passive)
            return new PairMutable(pos, minDist);

        PairMutable posSnapped = new PairMutable(pos);

        for (int j = 0; j < this.snapPoints.size(); j++)
        {
            Shape snapPoint = this.snapPoints.get(j);
            PairMutable snapPos = new PairMutable(snapPoint.getTranslateX(), snapPoint.getTranslateY());

            Double currDist = Utils.GetDist(pos, snapPos);

            if (currDist <= minDist)
            {
                posSnapped = snapPos;
                minDist = currDist;
            }
        }

        return new PairMutable(posSnapped, minDist);
    }

    public void SnapPointsCheck(PairMutable posMouse)
    {
        for (int j = 0; j < this.snapPoints.size(); j++)
        {
            Shape snapPoint = this.snapPoints.get(j);
            PairMutable snapPos = new PairMutable(snapPoint.getTranslateX(), snapPoint.getTranslateY());

            Double dist = Utils.GetDist(posMouse, snapPos);

            if (dist <= EDAmameController.Editor_SnapPointRadius)
                snapPoint.setVisible(true);
            else
                snapPoint.setVisible(false);
        }
    }

    abstract public void SnapPointsRefresh();

    //// PROPERTIES FUNCTIONS ////

    abstract public void PropsLoadGlobal(LinkedList<String> names, LinkedList<Double> posX, LinkedList<Double> posY, LinkedList<Double> rots);
    abstract public void PropsApplyGlobal(VBox propsBox);
    abstract public boolean PropsLoadSymbol(LinkedList<Paint> colors, LinkedList<Double> strokeWidths, LinkedList<Paint> strokes, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<String> pinLabels);
    abstract public void PropsApplySymbol(VBox propsBox);
    abstract public boolean PropsLoadFootprint(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii);
    abstract public void PropsApplyFootprint(VBox propsBox);
    abstract public boolean PropsLoadPCB(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii);
    abstract public void PropsApplyPCB(VBox propsBox);

    //// SUPPORT FUNCTIONS ////

    abstract public EDANode Clone();
    abstract public String ToGerberStr(String layerName);

    static public Node NodeClone(Node oldNode)  // ASK!!!
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
                clonedGroup.getChildren().add(NodeClone(oldGroup.getChildren().get(i)));

            clonedNode = clonedGroup;
        }
        else
        {
            throw new java.lang.Error("ERROR: Attempting to clone an unrecognized JavaFX node type!");
        }

        clonedNode.setTranslateX(oldNode.getTranslateX());
        clonedNode.setTranslateY(oldNode.getTranslateY());
        clonedNode.setLayoutX(oldNode.getLayoutX());
        clonedNode.setLayoutY(oldNode.getLayoutY());
        clonedNode.setRotate(oldNode.getRotate());

        return clonedNode;
    }

    static public String NodeToFXMLString(Node node, PairMutable posOffset, int tabNum) // ASK!!!
    {
        String str = "";
        String tabStr = "";

        for (int i = 0; i < tabNum; i++)
            tabStr += "\t";

        if (node.getClass() == Circle.class)
        {
            Circle circle = (Circle)node;

            str += tabStr + "<Circle";
            str += " id=\"" + circle.getId() + "\"";
            str += " radius=\"" + circle.getRadius() + "\"";
            str += FillAddLeadingZeros(Integer.toHexString(circle.getFill().hashCode()));

            //if (circle.getStroke() != null)
            //{
            str += " stroke=\"#" + circle.getStroke().toString().replace("0x", "") + "\"";
            //str += " strokeType=\"#" + circle.getStrokeType().toString().replace("#", "") + "\"";
            str += " strokeType=\"INSIDE\"";
            str += " strokeWidth=\"" + circle.getStrokeWidth()+ "\"";
            //}

            str += " translateX=\"" + (circle.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (circle.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + circle.getRotate() + "\" />";
        }
        else if  (node.getClass() == Rectangle.class)
        {
            Rectangle rectangle = (Rectangle)node;

            str += tabStr + "<Rectangle";
            str += " id=\"" + rectangle.getId() + "\"";
            str += " width=\"" + rectangle.getWidth() + "\"";
            str += " height=\"" + rectangle.getHeight() + "\"";
            str += FillAddLeadingZeros(Integer.toHexString(rectangle.getFill().hashCode()));

            //if (rectangle.getStroke() != null)
            //{
            str += " stroke=\"#" + rectangle.getStroke().toString().replace("0x", "") + "\"";
            //str += " strokeType=\"#" + rectangle.getStrokeType().toString().replace("#", "") + "\"";
            str += " strokeType=\"INSIDE\"";
            str += " strokeWidth=\"" + rectangle.getStrokeWidth() + "\"";
            //}

            str += " translateX=\"" + (rectangle.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (rectangle.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + rectangle.getRotate() + "\" />";
        }
        else if (node.getClass() == Polygon.class)
        {
            Polygon triangle = (Polygon)node;

            str += tabStr + "<Polygon";
            str += " id=\"" + triangle.getId() + "\"";
            str += FillAddLeadingZeros(Integer.toHexString(triangle.getFill().hashCode()));

            //if (triangle.getStroke() != null)
            //{
            str += " stroke=\"#" + triangle.getStroke().toString().replace("0x", "") + "\"";
            //str += " strokeType=\"#" + triangle.getStrokeType().toString().replace("#", "") + "\"";
            str += " strokeType=\"INSIDE\"";
            str += " strokeWidth=\"" + triangle.getStrokeWidth() + "\"";
            //}

            str += " translateX=\"" + (triangle.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (triangle.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + triangle.getRotate() + "\" >\n";
            str += tabStr + "\t<points>\n";

            for (int i = 0; i < triangle.getPoints().size(); i++)
                str += tabStr + "\t\t<Double fx:value=\"" + triangle.getPoints().get(i) + "\"/>\n";

            str += tabStr + "\t</points>\n";
            str += tabStr + "</Polygon>";
        }
        else if (node.getClass() == Line.class)
        {
            Line line = (Line)node;

            str += tabStr + "<Line";
            str += " id=\"" + line.getId() + "\"";
            str += " startX=\"" + line.getStartX() + "\"";
            str += " startY=\"" + line.getStartY() + "\"";
            str += " endX=\"" + line.getEndX() + "\"";
            str += " endY=\"" + line.getEndY() + "\"";
            str += " strokeWidth=\"" + line.getStrokeWidth() + "\"";
            str += " translateX=\"" + (line.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (line.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " stroke=\"" + line.getStroke().toString().replace("0x", "#") + "\"";
            //str += " stroke=\"#" + line.getStroke().toString() + "\"";
            //str += " strokeType=\"#" + line.getStrokeType().toString() + "\"";
            str += " />";
        }
        else if (node.getClass() == Text.class)
        {
            Text text = (Text)node;

            str += tabStr + "<Text";
            str += " id=\"" + text.getId() + "\"";
            str += " text=\"" + text.getText() + "\"";
            String addZeros = Integer.toHexString(text.getFill().hashCode());
            str += FillAddLeadingZeros(addZeros);

            str += " translateX=\"" + (text.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (text.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + text.getRotate() + "\">\n";
            str += tabStr + "\t<font> \n";
            str += tabStr + "\t\t<Font";
            //str += " font=\"" + text.getFont().getName() + "\"";
            str += " name=\"" + text.getFont().getFamily() + "\"";
            str += " size=\"" + text.getFont().getSize() + "\" />\n";

            str += tabStr + "\t</font>\n";
            str += tabStr + "</Text>";
        }
        else if (node.getClass() == Group.class)
        {
            Group group = (Group)node;

            str += tabStr + "<Group";
            str += " id=\"" + group.getId() + "\"";
            str += " translateX=\"" + (group.getTranslateX() + posOffset.GetLeftDouble()) + "\"";
            str += " translateY=\"" + (group.getTranslateY() + posOffset.GetRightDouble()) + "\"";
            str += " rotate=\"" + group.getRotate() + "\">\n";
            str += tabStr + "\t<children>\n";

            for (int i = 0; i < group.getChildren().size(); i++)
                str += NodeToFXMLString(group.getChildren().get(i), new PairMutable(0.0, 0.0), tabNum + 2) + "\n";

            str += tabStr + "\t</children>\n";
            str += tabStr + "</Group>";
        }
        else
        {
            throw new java.lang.Error("ERROR: Attempting to convert an unknown node type to FXML string!");
        }

        return str;
    }

    static public String NodeToGerberStr(Node node, String layer, Editor editor)
    {
        String str = "";

        if (node.getClass() == Circle.class)
        {
            Circle circle = (Circle)node;

            if (!circle.getId().equals(layer))
                return "";

            PairMutable point = GetPosInNodeParent(circle, new PairMutable(0.0, 0.0));

            if (((Node)circle.getParent()).getClass() == Group.class)
                point = GetPosInNodeParent(circle.getParent(), point);

            point = editor.PaneHolderGetRealPos(point);
            point = new PairMutable(point.GetLeftDouble(), -point.GetRightDouble());

            //editor.TestShapeAdd(point, 10.0, Color.BLUE, 1, false);

            double radius = circle.getRadius();

            String newStr = "";

            newStr += "G01*\n";

            if (circle.getFill() != Color.TRANSPARENT) //FILLED IN CIRCLE
            {
                newStr += "%ADD" + Editor.GerberApertureCounter + "C," + PositionFormattingDouble(radius) + "*%\n";
                newStr += "D" + (Editor.GerberApertureCounter++) + "*\n";

                newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(radius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                newStr += "G75*\nG03*\n";

                newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(radius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                        + "I" + (((PositionFormatting(radius)) / 2) * -1) + "J0D01*\n";
                newStr += "G01*\n";
                newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(radius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                newStr += "G75*\nG03*\n";
                newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(radius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                        + "I" + ((PositionFormatting(radius)) / 2) + "J0D01*\n";
                newStr += "G01*\n";
            }
            else //BORDER ONLY CIRCLE
            {
                Double apertureValue = (circle.getStrokeWidth() / 10);
                newStr += "%ADD" + Editor.GerberApertureCounter + "C," + apertureValue + "*%\n";
                newStr += "D" + (Editor.GerberApertureCounter++) + "*\n";

                newStr += "X" + (PositionFormatting(point.GetLeftDouble())+PositionFormatting(radius)) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                newStr += "G75*\nG03*\n";

                newStr += "X" + (PositionFormatting(point.GetLeftDouble())-PositionFormatting(radius)) + "Y" + PositionFormatting(point.GetRightDouble())
                        + "I" + (PositionFormatting(radius) * -1) + "J0D01*\n";
                newStr += "G01*\n";
                newStr += "X" + (PositionFormatting(point.GetLeftDouble())-PositionFormatting(radius)) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                newStr += "G75*\nG03*\n";
                newStr += "X" + (PositionFormatting(point.GetLeftDouble())+PositionFormatting(radius)) + "Y" + PositionFormatting(point.GetRightDouble())
                        + "I" + PositionFormatting(radius) + "J0D01*\n";
                newStr += "G01*\n";
            }

            return newStr;
        }
        else if (node.getClass() == Rectangle.class)
        {
            Rectangle rectangle = (Rectangle)node;

            if (!rectangle.getId().equals(layer))
                return "";

            LinkedList<PairMutable> points = new LinkedList<PairMutable>();
            points.add(GetPosInNodeParent(rectangle, new PairMutable(0.0, 0.0)));
            points.add(GetPosInNodeParent(rectangle, new PairMutable(rectangle.getWidth(), 0.0)));
            points.add(GetPosInNodeParent(rectangle, new PairMutable(rectangle.getWidth(), rectangle.getHeight())));
            points.add(GetPosInNodeParent(rectangle, new PairMutable(0.0, rectangle.getHeight())));

            if (((Node)rectangle.getParent()).getClass() == Group.class)
            {
                points.set(0, GetPosInNodeParent(rectangle.getParent(), points.get(0)));
                points.set(1, GetPosInNodeParent(rectangle.getParent(), points.get(1)));
                points.set(2, GetPosInNodeParent(rectangle.getParent(), points.get(2)));
                points.set(3, GetPosInNodeParent(rectangle.getParent(), points.get(3)));
            }

            points.set(0, editor.PaneHolderGetRealPos(points.get(0)));
            points.set(1, editor.PaneHolderGetRealPos(points.get(1)));
            points.set(2, editor.PaneHolderGetRealPos(points.get(2)));
            points.set(3, editor.PaneHolderGetRealPos(points.get(3)));
            points.set(0, new PairMutable(points.get(0).GetLeftDouble(), -points.get(0).GetRightDouble()));
            points.set(1, new PairMutable(points.get(1).GetLeftDouble(), -points.get(1).GetRightDouble()));
            points.set(2, new PairMutable(points.get(2).GetLeftDouble(), -points.get(2).GetRightDouble()));
            points.set(3, new PairMutable(points.get(3).GetLeftDouble(), -points.get(3).GetRightDouble()));


            //editor.TestShapeAdd(points.get(0), 10.0, Color.BLUE, 1, false);
            //editor.TestShapeAdd(points.get(1), 10.0, Color.BLUE, 1, false);
            //editor.TestShapeAdd(points.get(2), 10.0, Color.BLUE, 1, false);
            //editor.TestShapeAdd(points.get(3), 10.0, Color.BLUE, 1, false);

            String newStr = "";

            newStr += "G01*\n";

            if (rectangle.getFill() != Color.TRANSPARENT)
                newStr += "G36*\n";
            else {
                Double apertureValue = (rectangle.getStrokeWidth() / 10);
                newStr += "%ADD" + Editor.GerberApertureCounter + "C," + apertureValue + "*%\n";
                newStr += "D" + Editor.GerberApertureCounter++ + "*\n";
            }
            for (int i = 0; i < points.size(); i++)
            {
                if (i == 0) {
                    newStr += "X" + PositionFormatting(points.get(i).GetLeftDouble()) + "Y" + PositionFormatting(points.get(i).GetRightDouble()) + "D02*\n";
                    if (rectangle.getFill() != Color.TRANSPARENT)
                        newStr += "G01*\n";
                } else
                    newStr += "X" + PositionFormatting(points.get(i).GetLeftDouble()) + "Y" + PositionFormatting(points.get(i).GetRightDouble()) + "D01*\n";
            }
            newStr += "X" + PositionFormatting(points.get(0).GetLeftDouble()) + "Y" + PositionFormatting(points.get(0).GetRightDouble()) + "D01*\n";

            if (rectangle.getFill() != Color.TRANSPARENT)
                newStr += "G37*\n";

            return newStr;
        }
        else if (node.getClass() == Polygon.class)
        {
            Polygon triangle = (Polygon)node;

            if (!triangle.getId().equals(layer))
                return "";

            LinkedList<PairMutable> points = new LinkedList<PairMutable>();
            points.add(GetPosInNodeParent(triangle, new PairMutable(triangle.getPoints().get(0), triangle.getPoints().get(1))));
            points.add(GetPosInNodeParent(triangle, new PairMutable(triangle.getPoints().get(2), triangle.getPoints().get(3))));
            points.add(GetPosInNodeParent(triangle, new PairMutable(triangle.getPoints().get(4), triangle.getPoints().get(5))));

            if (((Node)triangle.getParent()).getClass() == Group.class)
            {
                points.set(0, GetPosInNodeParent(triangle.getParent(), points.get(0)));
                points.set(1, GetPosInNodeParent(triangle.getParent(), points.get(1)));
                points.set(2, GetPosInNodeParent(triangle.getParent(), points.get(2)));
            }

            points.set(0, editor.PaneHolderGetRealPos(points.get(0)));
            points.set(1, editor.PaneHolderGetRealPos(points.get(1)));
            points.set(2, editor.PaneHolderGetRealPos(points.get(2)));
            points.set(0, new PairMutable(points.get(0).GetLeftDouble(), -points.get(0).GetRightDouble()));
            points.set(1, new PairMutable(points.get(1).GetLeftDouble(), -points.get(1).GetRightDouble()));
            points.set(2, new PairMutable(points.get(2).GetLeftDouble(), -points.get(2).GetRightDouble()));

            //editor.TestShapeAdd(points.get(0), 10.0, Color.BLUE, 1, false);
            //editor.TestShapeAdd(points.get(1), 10.0, Color.BLUE, 1, false);
            //editor.TestShapeAdd(points.get(2), 10.0, Color.BLUE, 1, false);

            String newStr = "";

            newStr += "G01*\n";

            if (triangle.getFill() != Color.TRANSPARENT)
                newStr += "G36*\n";
            else {
                Double apertureValue = (triangle.getStrokeWidth() / 10);
                newStr += "%ADD" + Editor.GerberApertureCounter + "C," + apertureValue + "*%\n";
                newStr += "D" + Editor.GerberApertureCounter++ + "*\n";
            }
            for (int i = 0; i < points.size(); i++)
            {
                if (i == 0) {
                    newStr += "X" + PositionFormatting(points.get(i).GetLeftDouble()) + "Y" + PositionFormatting(points.get(i).GetRightDouble()) + "D02*\n";
                    if (triangle.getFill() != Color.TRANSPARENT)
                        newStr += "G01*\n";
                } else
                    newStr += "X" + PositionFormatting(points.get(i).GetLeftDouble()) + "Y" + PositionFormatting(points.get(i).GetRightDouble()) + "D01*\n";
            }
            newStr += "X" + PositionFormatting(points.get(0).GetLeftDouble()) + "Y" + PositionFormatting(points.get(0).GetRightDouble()) + "D01*\n";

            if (triangle.getFill() != Color.TRANSPARENT)
                newStr += "G37*\n";

            return newStr;
        }
        else if (node.getClass() == Line.class)
        {
            Line line = (Line)node;

            if (!line.getId().equals(layer))
                return "";

            LinkedList<PairMutable> points = new LinkedList<PairMutable>();
            points.add(GetPosInNodeParent(line, new PairMutable(line.getStartX(), line.getStartY())));
            points.add(GetPosInNodeParent(line, new PairMutable(line.getEndX(), line.getEndY())));

            if (((Node)line.getParent()).getClass() == Group.class)
            {
                points.set(0, GetPosInNodeParent(line.getParent(), points.get(0)));
                points.set(1, GetPosInNodeParent(line.getParent(), points.get(1)));
            }

            points.set(0, editor.PaneHolderGetRealPos(points.get(0)));
            points.set(1, editor.PaneHolderGetRealPos(points.get(1)));
            points.set(0, new PairMutable(points.get(0).GetLeftDouble(), -points.get(0).GetRightDouble()));
            points.set(1, new PairMutable(points.get(1).GetLeftDouble(), -points.get(1).GetRightDouble()));

            //editor.TestShapeAdd(points.get(0), 10.0, Color.BLUE, 1, false);
            //editor.TestShapeAdd(points.get(1), 10.0, Color.BLUE, 1, false);

            String newStr = "";

            newStr += "G01*\n";
            Double apertureValue = (line.getStrokeWidth() / 10);
            newStr += "%ADD" + Editor.GerberApertureCounter + "C," + apertureValue + "*%\n";
            newStr += "D" + Editor.GerberApertureCounter++ + "*\n";

            newStr += "X" + PositionFormatting(points.get(0).GetLeftDouble()) +
                    "Y" + PositionFormatting(points.get(0).GetRightDouble()) + "D02*\n";
            newStr += "X" + PositionFormatting(points.get(1).GetLeftDouble()) +
                    "Y" + PositionFormatting(points.get(1).GetRightDouble()) + "D01*\n";

            return newStr;
        }
        else if (node.getClass() == Text.class)
        {
            Text text = (Text)node;

            // TODO
        }
        else if (node.getClass() == Group.class)
        {
            Group group = (Group)node;

            if ((node.getId() != null) && node.getId().equals("Through-Hole"))
            {
                if (group.getChildren().size() != 1)
                    throw new java.lang.Error("ERROR: Attempting to convert a hole without 1 child to a Gerber string!");

                PairMutable point = GetPosInNodeParent(group, GetPosInNodeParent(group.getChildren().get(0), new PairMutable(0.0, 0.0)));

                if (((Node)group.getParent()).getClass() == Group.class)
                    point = GetPosInNodeParent(group.getParent(), point);

                point = editor.PaneHolderGetRealPos(point);
                point = new PairMutable(point.GetLeftDouble(), -point.GetRightDouble());

                //editor.TestShapeAdd(point, 10.0, Color.BLUE, 1, false);

                Circle ThroughHoleCircle = (Circle)group.getChildren().get(0);
                double outerRadius = ThroughHoleCircle.getRadius();
                double innerRadius = ThroughHoleCircle.getRadius() - ThroughHoleCircle.getStrokeWidth();

                String newStr = "";

                if (layer.equals("Edge Cuts"))
                {
                    // creating the hole (inner radius)

                    newStr += "G01*\n";

                    newStr += "%ADD" + Editor.GerberApertureCounter + "C," + PositionFormattingDouble(innerRadius * 2) + "*%\n";
                    newStr += "D" + Editor.GerberApertureCounter++ + "*\n";

                    newStr += "%TO.P,REF\\u002A\\u002A,1*%\n";
                    newStr += "%TO.N,N/C*%\n";

                    newStr += "X" + PositionFormatting(point.GetLeftDouble()) + "Y" + PositionFormatting(point.GetRightDouble()) + "D03*\n";
                    newStr += "%TD*%\n";
                }
                else if (layer.equals("Mask Front"))
                {
                    // creating the front exposed copper (outer radius)

                    newStr += "%ADD" + Editor.GerberApertureCounter + "C," + PositionFormattingDouble(outerRadius) + "*%\n";
                    newStr += "D" + (Editor.GerberApertureCounter++) + "*\n";

                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                    newStr += "G75*\nG03*\n";

                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                            + "I" + (((PositionFormatting(outerRadius)) / 2) * -1) + "J0D01*\n";
                    newStr += "G01*\n";
                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                    newStr += "G75*\nG03*\n";
                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                            + "I" + ((PositionFormatting(outerRadius)) / 2) + "J0D01*\n";
                    newStr += "G01*\n";
                }
                else if (layer.equals("Mask Rear"))
                {
                    // creating the rear exposed copper (outer radius)

                    newStr += "%ADD" + Editor.GerberApertureCounter + "C," + PositionFormattingDouble(outerRadius) + "*%\n";
                    newStr += "D" + (Editor.GerberApertureCounter++) + "*\n";

                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                    newStr += "G75*\nG03*\n";

                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                            + "I" + (((PositionFormatting(outerRadius)) / 2) * -1) + "J0D01*\n";
                    newStr += "G01*\n";
                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                    newStr += "G75*\nG03*\n";
                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                            + "I" + ((PositionFormatting(outerRadius)) / 2) + "J0D01*\n";
                    newStr += "G01*\n";
                }

                return newStr;
            }
            else if ((node.getId() != null) && node.getId().equals("Via"))
            {
                if (group.getChildren().size() != 2)
                    throw new java.lang.Error("ERROR: Attempting to convert a via without 2 children to a Gerber string!");

                PairMutable point = GetPosInNodeParent(group, GetPosInNodeParent(group.getChildren().get(0), new PairMutable(0.0, 0.0)));

                if (((Node)group.getParent()).getClass() == Group.class)
                    point = GetPosInNodeParent(group.getParent(), point);

                point = editor.PaneHolderGetRealPos(point);
                point = new PairMutable(point.GetLeftDouble(), -point.GetRightDouble());

                //editor.TestShapeAdd(point, 10.0, Color.BLUE, 1, false);

                Circle viaCircle1 = (Circle)group.getChildren().get(0);
                Circle viaCircle2 = (Circle)group.getChildren().get(1);
                double outerRadius = Math.max(viaCircle1.getRadius(), viaCircle2.getRadius());
                double innerRadius = Math.max(viaCircle1.getRadius(), viaCircle2.getRadius()) - Math.min(viaCircle1.getRadius(), viaCircle2.getRadius());

                String newStr = "";

                if (layer.equals("Edge Cuts"))
                {
                    // creating the hole (inner radius)

                    newStr += "G01*\n";

                    newStr += "%ADD" + Editor.GerberApertureCounter + "C," + PositionFormattingDouble(innerRadius * 2) + "*%\n";
                    newStr += "D" + Editor.GerberApertureCounter++ + "*\n";

                    newStr += "%TO.P,REF\\u002A\\u002A,1*%\n";
                    newStr += "%TO.N,N/C*%\n";

                    newStr += "X" + PositionFormatting(point.GetLeftDouble()) + "Y" + PositionFormatting(point.GetRightDouble()) + "D03*\n";
                    newStr += "%TD*%\n";
                }
                else if (layer.equals("Copper Front"))
                {
                    // creating the front copper (outer radius)

                    newStr += "%ADD" + Editor.GerberApertureCounter + "C," + PositionFormattingDouble(outerRadius) + "*%\n";
                    newStr += "D" + (Editor.GerberApertureCounter++) + "*\n";

                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                    newStr += "G75*\nG03*\n";

                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                            + "I" + (((PositionFormatting(outerRadius)) / 2) * -1) + "J0D01*\n";
                    newStr += "G01*\n";
                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                    newStr += "G75*\nG03*\n";
                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                            + "I" + ((PositionFormatting(outerRadius)) / 2) + "J0D01*\n";
                    newStr += "G01*\n";
                }
                else if (layer.equals("Copper Rear"))
                {
                    // creating the rear copper (outer radius)

                    newStr += "%ADD" + Editor.GerberApertureCounter + "C," + PositionFormattingDouble(outerRadius) + "*%\n";
                    newStr += "D" + (Editor.GerberApertureCounter++) + "*\n";

                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                    newStr += "G75*\nG03*\n";

                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                            + "I" + (((PositionFormatting(outerRadius)) / 2) * -1) + "J0D01*\n";
                    newStr += "G01*\n";
                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) - (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble()) + "D02*\n";
                    newStr += "G75*\nG03*\n";
                    newStr += "X" + (PositionFormatting(point.GetLeftDouble()) + (PositionFormatting(outerRadius)) / 2) + "Y" + PositionFormatting(point.GetRightDouble())
                            + "I" + ((PositionFormatting(outerRadius)) / 2) + "J0D01*\n";
                    newStr += "G01*\n";
                }

                return newStr;
            }
            else
            {
                for (int i = 0; i < group.getChildren().size(); i++)
                    NodeToGerberStr(group.getChildren().get(i), layer, editor);
            }
        }
        else
        {
            throw new java.lang.Error("ERROR: Attempting to convert an unknown node type to Gerber string!");
        }

        return str;
    }

    static public PairMutable GetPosInNodeParent(Node node, PairMutable pos)
    {
        Point2D newPos = node.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public static PairMutable NodesGetMiddlePos(LinkedList<Node> nodes)
    {
        PairMutable nodeBounds = NodesGetRealBounds(nodes);

        return new PairMutable((nodeBounds.GetLeftPair().GetLeftDouble() + nodeBounds.GetLeftPair().GetRightDouble()) / 2,
                (nodeBounds.GetRightPair().GetLeftDouble() + nodeBounds.GetRightPair().GetRightDouble()) / 2);
    }

    public static PairMutable NodesGetRealBounds(LinkedList<Node> nodes)
    {
        PairMutable childTotalBoundsX = new PairMutable(0.0, 0.0);
        PairMutable childTotalBoundsY = new PairMutable(0.0, 0.0);

        for (int i = 0; i < nodes.size(); i++)
        {
            Node node = nodes.get(i);
            Bounds nodeBoundsReal = node.getBoundsInParent();

            if (i == 0)
            {
                childTotalBoundsX.left = nodeBoundsReal.getMinX();
                childTotalBoundsX.right = nodeBoundsReal.getMaxX();
                childTotalBoundsY.left = nodeBoundsReal.getMinY();
                childTotalBoundsY.right = nodeBoundsReal.getMaxY();
            }
            else
            {
                if (nodeBoundsReal.getMinX() < childTotalBoundsX.GetLeftDouble())
                    childTotalBoundsX.left = nodeBoundsReal.getMinX();
                if (nodeBoundsReal.getMaxX() > childTotalBoundsX.GetRightDouble())
                    childTotalBoundsX.right = nodeBoundsReal.getMaxX();
                if (nodeBoundsReal.getMinY() < childTotalBoundsY.GetLeftDouble())
                    childTotalBoundsY.left = nodeBoundsReal.getMinY();
                if (nodeBoundsReal.getMaxY() > childTotalBoundsY.GetRightDouble())
                    childTotalBoundsY.right = nodeBoundsReal.getMaxY();
            }
        }

        return new PairMutable(childTotalBoundsX, childTotalBoundsY);
    }

    static public String FillAddLeadingZeros(String addZeros)
    {
        if (addZeros.length() < 8)
        {
            while (addZeros.length() < 8)
                addZeros = "0" + addZeros;
            return " fill=\"#" + addZeros + "\"";
        }
        else
        {
            return " fill=\"#" + addZeros + "\"";
        }
    }

    static public int PositionFormatting(Double OriginalValue)
    {
        Double dividedByTen = OriginalValue / 10;
        Double roundedValue = EDANode.round(dividedByTen, 6);
        String roundedAsString = String.valueOf(roundedValue);
        int decimalIndex = roundedAsString.indexOf(".");
        String integerValue = roundedAsString.substring(0, decimalIndex);
        String fractionalValue = roundedAsString.substring(decimalIndex+1);
        while (fractionalValue.length() < 6)
        {
            fractionalValue += "0";
        }
        return Integer.parseInt(integerValue + fractionalValue);
    }

    static public String PositionFormattingDouble(Double OriginalValue)
    {
        Double dividedByTen = OriginalValue / 10;
        Double roundedValue = EDANode.round(dividedByTen, 6);
        String roundedAsString = String.valueOf(roundedValue);
        int decimalIndex = roundedAsString.indexOf(".");
        String integerValue = roundedAsString.substring(0, decimalIndex);
        String fractionalValue = roundedAsString.substring(decimalIndex+1);
        while (fractionalValue.length() < 6)
        {
            fractionalValue += "0";
        }
        return integerValue + "." + fractionalValue;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}