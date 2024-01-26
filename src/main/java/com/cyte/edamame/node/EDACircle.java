/*
 * Copyright (c) 2024. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.node;

import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.shape.SnapPoint;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.util.Utils;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.LinkedList;

public class EDACircle extends EDANode
{
    //// GLOBAL VARIABLES ////

    public Circle circle;

    //// CONSTRUCTORS ////

    public EDACircle(String nameValue, Circle nodeValue, boolean passiveValue, Editor editorValue)
    {
        if (editorValue == null)
            throw new java.lang.Error("ERROR: Attempting to create an EDACircle \"" + nameValue + "\" without a supplied editor!");

        this.circle = nodeValue;

        this.name = nameValue;
        this.highlighted = false;
        this.highlightedMouse = false;
        this.highlightedBox = false;
        this.selected = false;
        this.mousePressPos = null;
        this.passive = passiveValue;
        this.snapPoints = new LinkedList<SnapPoint>();

        this.editor = editorValue;

        if (!this.passive)
        {
            this.ShapeHighlightedCreate();
            this.ShapeSelectedCreate();
            this.SnapPointsCreate();
        }
    }

    //// SETTERS ////

    public void SetTranslate(PairMutable pos)
    {
        this.circle.setTranslateX(pos.GetLeftDouble());
        this.circle.setTranslateY(pos.GetRightDouble());
    };

    public void SetRotate(double rot)
    {
        this.circle.setRotate(rot);
    };

    public void SetVisible(boolean visible)
    {
        this.circle.setVisible(visible);
    };

    //// GETTERS ////

    public Node GetNode()
    {
        return this.circle;
    }

    public PairMutable GetTranslate()
    {
        return new PairMutable(this.circle.getTranslateX(), this.circle.getTranslateY());
    };

    public double GetRotate()
    {
        return this.circle.getRotate();
    };

    public Bounds GetBoundsLocal()
    {
        return this.circle.getBoundsInLocal();
    };

    public Bounds GetBoundsParent()
    {
        return this.circle.getBoundsInParent();
    };

    //// EDITOR FUNCTIONS ////

    public void Rotate(double deltaY)
    {
        if (!this.selected)
            return;

        double angle = 10;

        if (deltaY < 0)
            angle = -10;

        this.circle.setRotate(this.circle.getRotate() + angle);
    }

    public void NodeAdd()
    {
        this.editor.paneHolder.getChildren().add(1, this.circle);
    };

    public void NodeRemove()
    {
        this.editor.paneHolder.getChildren().remove(this.circle);
    };

    //// POSITION FUNCTIONS ////

    public PairMutable BoundsPosToHolderPane(PairMutable posOffset)
    {
        Bounds boundsLocal = this.circle.getBoundsInLocal();
        PairMutable boundsRealEdgeL = this.PosToHolderPane(new PairMutable(boundsLocal.getMinX() + posOffset.GetLeftDouble(), boundsLocal.getMinY() + posOffset.GetRightDouble()));
        PairMutable boundsRealEdgeH = this.PosToHolderPane(new PairMutable(boundsLocal.getMaxX() + posOffset.GetLeftDouble(), boundsLocal.getMaxY() + posOffset.GetRightDouble()));

        return new PairMutable((boundsRealEdgeL.GetLeftDouble() + boundsRealEdgeH.GetLeftDouble()) / 2,
                (boundsRealEdgeL.GetRightDouble() + boundsRealEdgeH.GetRightDouble()) / 2);
    }

    public PairMutable PosToHolderPane(PairMutable pos)
    {
        Point2D newPos = this.circle.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public boolean PosOnNode(PairMutable pos)
    {
        return this.circle.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    //// SNAP POINT FUNCTIONS ////

    public void SnapPointsRefresh()
    {
        Bounds boundsLocal = this.circle.getBoundsInLocal();

        for (int i = 0; i < this.snapPoints.size(); i++)
        {
            SnapPoint snapPoint = this.snapPoints.get(i);
            String snapPointId = snapPoint.getId();
            PairMutable posSnapReal = null;

            if (snapPointId.equals("snapTopLeft"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(-boundsLocal.getWidth() / 2,
                        -boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapTopCenter"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(0.0,
                        -boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapTopRight"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(boundsLocal.getWidth() / 2,
                        -boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapLeft"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(-boundsLocal.getWidth() / 2,
                        0.0));
            }
            else if (snapPointId.equals("snapCenter"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(0.0,
                        0.0));
            }
            else if (snapPointId.equals("snapRight"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(boundsLocal.getWidth() / 2,
                        0.0));
            }
            else if (snapPointId.equals("snapBottomLeft"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(-boundsLocal.getWidth() / 2,
                        boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapBottomCenter"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(0.0,
                        boundsLocal.getHeight() / 2));
            }
            else if (snapPointId.equals("snapBottomRight"))
            {
                posSnapReal = this.BoundsPosToHolderPane(new PairMutable(boundsLocal.getWidth() / 2,
                        boundsLocal.getHeight() / 2));
            }
            else
            {
                throw new java.lang.Error("ERROR: Encountered unrecognized snap point when refreshing EDACircle snap points!");
            }

            snapPoint.setTranslateX(posSnapReal.GetLeftDouble());
            snapPoint.setTranslateY(posSnapReal.GetRightDouble());

            //this.RenderNode_RenderSystem.RenderSystem_TestShapeAdd(posSnapReal, 5.0, Color.RED, true);
            //System.out.println(posSnapReal.ToStringDouble());
        }
    }

    public void SnapPointsCreate()
    {
        PairMutable currPos = new PairMutable(0.0, 0.0);

        // Creating the top left snap point...
        SnapPoint topLeftSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        topLeftSnapPoint.setId("snapTopLeft");
        topLeftSnapPoint.setVisible(false);
        this.snapPoints.add(topLeftSnapPoint);

        // Creating the top center snap point...
        SnapPoint topCenterSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        topCenterSnapPoint.setId("snapTopCenter");
        topCenterSnapPoint.setVisible(false);
        this.snapPoints.add(topCenterSnapPoint);

        // Creating the top right snap point...
        SnapPoint topRightSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        topRightSnapPoint.setId("snapTopRight");
        topRightSnapPoint.setVisible(false);
        this.snapPoints.add(topRightSnapPoint);

        // Creating the left snap point...
        SnapPoint leftSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        leftSnapPoint.setId("snapLeft");
        leftSnapPoint.setVisible(false);
        this.snapPoints.add(leftSnapPoint);

        // Creating the center snap point...
        SnapPoint centerSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        centerSnapPoint.setId("snapCenter");
        centerSnapPoint.setVisible(false);
        this.snapPoints.add(centerSnapPoint);

        // Creating the right snap point...
        SnapPoint rightSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        rightSnapPoint.setId("snapRight");
        rightSnapPoint.setVisible(false);
        this.snapPoints.add(rightSnapPoint);

        // Creating the bottom left snap point...
        SnapPoint bottomLeftSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        bottomLeftSnapPoint.setId("snapBottomLeft");
        bottomLeftSnapPoint.setVisible(false);
        this.snapPoints.add(bottomLeftSnapPoint);

        // Creating the bottom center snap point...
        SnapPoint bottomCenterSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        bottomCenterSnapPoint.setId("snapBottomCenter");
        bottomCenterSnapPoint.setVisible(false);
        this.snapPoints.add(bottomCenterSnapPoint);

        // Creating the bottom right snap point...
        SnapPoint bottomRightSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        bottomRightSnapPoint.setId("snapBottomRight");
        bottomRightSnapPoint.setVisible(false);
        this.snapPoints.add(bottomRightSnapPoint);
    }

    //// PROPERTIES FUNCTIONS ////

    public void PropsLoadGlobal(LinkedList<String> names, LinkedList<Double> posX, LinkedList<Double> posY, LinkedList<Double> rots, LinkedList<Color> colors)
    {
        if (!this.selected)
            return;

        PairMutable pos = this.GetTranslate();

        names.add(this.name);
        posX.add(pos.GetLeftDouble() - this.editor.paneHolder.getWidth() / 2);
        posY.add(pos.GetRightDouble() - this.editor.paneHolder.getHeight() / 2);

        rots.add(this.GetRotate());

        colors.add((Color)this.circle.getFill());
    }

    public void PropsApplyGlobal(VBox propsBox)
    {
        if (!this.selected)
            return;

        // Applying name...
        {
            Integer nameBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "nameBox");

            if (nameBoxIdx != -1)
            {
                HBox nameBox = (HBox)propsBox.getChildren().get(nameBoxIdx);
                TextField nameText = (TextField)EDAmameController.GetNodeById(nameBox.getChildren(), "name");

                if (nameText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"name\" node in global properties window \"nameBox\" entry!");

                String nameStr = nameText.getText();

                if (!nameStr.isEmpty())
                {
                    this.name = nameStr;
                }
                else if (!nameStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element name because the entered field is empty!");
                }
            }
        }

        // Applying position...
        {
            Integer posBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "posBox");

            if (posBoxIdx != -1)
            {
                HBox posBox = (HBox)propsBox.getChildren().get(posBoxIdx);
                TextField posXText = (TextField)EDAmameController.GetNodeById(posBox.getChildren(), "posX");
                TextField posYText = (TextField)EDAmameController.GetNodeById(posBox.getChildren(), "posY");

                if (posXText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"posX\" node in global properties window \"posBox\" entry!");
                if (posYText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"posY\" node in global properties window \"posBox\" entry!");

                String posXStr = posXText.getText();
                String posYStr = posYText.getText();

                if (EDAmameController.IsStringNum(posXStr))
                {
                    Double newPosX = Double.parseDouble(posXStr);

                    this.circle.setTranslateX(newPosX + this.editor.paneHolder.getWidth() / 2);
                }
                else if (!posXStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(posYStr))
                {
                    Double newPosY = Double.parseDouble(posYStr);

                    this.circle.setTranslateY(newPosY + this.editor.paneHolder.getHeight() / 2);
                }
                else if (!posYStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element Y position because the entered field is non-numeric!");
                }
            }
        }

        // Applying rotation...
        {
            Integer rotBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "rotBox");

            if (rotBoxIdx != -1)
            {
                HBox rotBox = (HBox) propsBox.getChildren().get(rotBoxIdx);
                TextField rotText = (TextField) EDAmameController.GetNodeById(rotBox.getChildren(), "rot");

                if (rotText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"rot\" node in global properties window \"rotBox\" entry!");

                String rotStr = rotText.getText();

                if (EDAmameController.IsStringNum(rotStr))
                    this.SetRotate(Double.parseDouble(rotStr));
                else if (!rotStr.equals("<mixed>"))
                    EDAmameController.SetStatusBar("Unable to apply element rotation because the entered field is non-numeric!");
            }
        }

        // Applying color...
        {
            Integer colorBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "colorBox");

            if (colorBoxIdx != -1)
            {
                HBox colorBox = (HBox) propsBox.getChildren().get(colorBoxIdx);
                ColorPicker colorPicker = (ColorPicker) EDAmameController.GetNodeById(colorBox.getChildren(), "color");

                if (colorPicker == null)
                    throw new java.lang.Error("ERROR: Unable to find \"color\" node in Symbol Editor properties window \"colorBox\" entry!");

                Color color = colorPicker.getValue();

                if ((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                {
                    this.circle.setFill(color);
                }
                else
                {
                    if (color != null)
                        EDAmameController.SetStatusBar("Unable to apply shape colors because the entered color is transparent!");
                }
            }
        }
    }

    public boolean PropsLoadSymbol(LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineStartPosX, LinkedList<Double> lineStartPosY, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<Double> strokeWidths, LinkedList<Paint> strokes, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<String> pinLabels)
    {
        if (!this.selected)
            return false;

        circlesRadii.add(this.circle.getRadius());
        strokeWidths.add(this.circle.getStrokeWidth());
        strokes.add(this.circle.getStroke());

        return true;
    }

    public void PropsApplySymbol(VBox propsBox)
    {
        if (!this.selected)
            return;
        // Applying circle radius...
        {
            Integer circleBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "circleBox");

            if (circleBoxIdx != -1)
            {
                HBox circleBox = (HBox) propsBox.getChildren().get(circleBoxIdx);
                TextField radiiText = (TextField) EDAmameController.GetNodeById(circleBox.getChildren(), "circleRadii");

                if (radiiText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"circleRadii\" node in Symbol Editor properties window \"circleBox\" entry!");

                String radiusStr = radiiText.getText();

                if (EDAmameController.IsStringNum(radiusStr))
                {
                    Double newRadius = Double.parseDouble(radiusStr);

                    if ((newRadius >= EDAmameController.Editor_CircleRadiusMin) && (newRadius <= EDAmameController.Editor_CircleRadiusMax))
                        this.circle.setRadius(newRadius);
                    else
                        EDAmameController.SetStatusBar("Unable to apply circle radii because the entered field is outside the limits! (Radius limits: " + EDAmameController.Editor_CircleRadiusMin + ", " + EDAmameController.Editor_CircleRadiusMax + ")");
                }
                else if (!radiusStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply circle radii because the entered field is non-numeric!");
                }
            }
        }

        // Applying borders...
        {
            Integer strokeWidthBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "strokesWidthBox");

            if (strokeWidthBoxIdx != -1)
            {
                HBox strokeWidthBox = (HBox)propsBox.getChildren().get(strokeWidthBoxIdx);
                TextField strokeWidthText = (TextField) EDAmameController.GetNodeById(strokeWidthBox.getChildren(), "strokeWidth");

                if (strokeWidthText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"strokeWidth\" node in Symbol Editor properties window \"strokesWidthBox\" entry!");

                String strokeWidthStr = strokeWidthText.getText();

                if (EDAmameController.IsStringNum(strokeWidthStr))
                {
                    Double newStrokeWidth = Double.parseDouble(strokeWidthStr);

                    if ((newStrokeWidth >= EDAmameController.Editor_BorderMin) && (newStrokeWidth <= EDAmameController.Editor_BorderMax))
                        this.circle.setStrokeWidth(newStrokeWidth);
                    else
                        EDAmameController.SetStatusBar("Unable to apply shape border width because the entered field is outside the limits! (Border width limits: " + EDAmameController.Editor_BorderMin + ", " + EDAmameController.Editor_BorderMax + ")");
                }
                else if (!strokeWidthStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply shape border width because the entered field is non-numeric!");
                }
            }

            Integer strokeBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "strokeColorBox");

            if (strokeBoxIdx != -1)
            {
                HBox strokeBox = (HBox) propsBox.getChildren().get(strokeBoxIdx);
                ColorPicker colorPicker = (ColorPicker)EDAmameController.GetNodeById(strokeBox.getChildren(), "strokeColor");

                if (colorPicker == null)
                    throw new java.lang.Error("ERROR: Unable to find \"strokeColor\" node in Symbol Editor properties window \"strokeColorBox\" entry!");

                Color color = colorPicker.getValue();

                if ((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                {
                    this.circle.setStroke(color);
                }
                else
                {
                    if (color != null)
                        EDAmameController.SetStatusBar("Unable to apply shape border color because the entered color is transparent!");
                }
            }
        }
    }

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        return new EDACircle(this.name, (Circle)EDANode.NodeClone(this.circle), this.passive, this.editor);
    }
}
