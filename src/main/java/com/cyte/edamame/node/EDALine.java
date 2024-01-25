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
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.LinkedList;

public class EDALine extends EDANode
{
    //// GLOBAL VARIABLES ////

    public Line line;

    //// CONSTRUCTORS ////

    public EDALine(String nameValue, Line nodeValue, boolean passiveValue, Editor editorValue)
    {
        if (editorValue == null)
            throw new java.lang.Error("ERROR: Attempting to create an EDALine \"" + nameValue + "\" without a supplied editor!");

        this.line = nodeValue;

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
        this.line.setTranslateX(pos.GetLeftDouble());
        this.line.setTranslateY(pos.GetRightDouble());
    };

    public void SetRotate(double rot)
    {
        this.line.setRotate(rot);
    };

    public void SetVisible(boolean visible)
    {
        this.line.setVisible(visible);
    };

    //// GETTERS ////

    public Node GetNode()
    {
        return this.line;
    }

    public PairMutable GetTranslate()
    {
        return new PairMutable(this.line.getTranslateX(), this.line.getTranslateY());
    };

    public double GetRotate()
    {
        return this.line.getRotate();
    };

    public Bounds GetBoundsLocal()
    {
        return this.line.getBoundsInLocal();
    };

    public Bounds GetBoundsParent()
    {
        return this.line.getBoundsInParent();
    };

    //// EDITOR FUNCTIONS ////

    public void Rotate(double deltaY) {}

    public void NodeAdd()
    {
        this.editor.paneHolder.getChildren().add(1, this.line);
    };

    public void NodeRemove()
    {
        this.editor.paneHolder.getChildren().remove(this.line);
    };

    //// POSITION FUNCTIONS ////

    public PairMutable BoundsPosToHolderPane(PairMutable posOffset)
    {
        Bounds boundsLocal = this.line.getBoundsInLocal();
        PairMutable boundsRealEdgeL = this.PosToHolderPane(new PairMutable(boundsLocal.getMinX() + posOffset.GetLeftDouble(), boundsLocal.getMinY() + posOffset.GetRightDouble()));
        PairMutable boundsRealEdgeH = this.PosToHolderPane(new PairMutable(boundsLocal.getMaxX() + posOffset.GetLeftDouble(), boundsLocal.getMaxY() + posOffset.GetRightDouble()));

        return new PairMutable((boundsRealEdgeL.GetLeftDouble() + boundsRealEdgeH.GetLeftDouble()) / 2,
                               (boundsRealEdgeL.GetRightDouble() + boundsRealEdgeH.GetRightDouble()) / 2);
    }

    public PairMutable PosToHolderPane(PairMutable pos)
    {
        Point2D newPos = this.line.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public boolean PosOnNode(PairMutable pos)
    {
        return this.line.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    //// SNAP POINT FUNCTIONS ////

    public void SnapPointsRefresh()
    {
        for (int i = 0; i < this.snapPoints.size(); i++)
        {
            SnapPoint snapPoint = this.snapPoints.get(i);
            String snapPointId = snapPoint.getId();
            PairMutable posSnapReal = null;

            if (snapPointId.equals("snapStart"))
            {
                posSnapReal = this.PosToHolderPane(new PairMutable(this.line.getStartX(),
                        this.line.getStartY()));
            }
            else if (snapPointId.equals("snapMiddle"))
            {
                PairMutable posStart = new PairMutable(this.line.getStartX(), this.line.getStartY());
                PairMutable posEnd = new PairMutable(this.line.getEndX(), this.line.getEndY());
                PairMutable posMiddle = new PairMutable((posStart.GetLeftDouble() + posEnd.GetLeftDouble()) / 2,
                        (posStart.GetRightDouble() + posEnd.GetRightDouble()) / 2);

                posSnapReal = this.PosToHolderPane(posMiddle);
            }
            else if (snapPointId.equals("snapEnd"))
            {
                posSnapReal = this.PosToHolderPane(new PairMutable(this.line.getEndX(),
                        this.line.getEndY()));
            }
            else
            {
                throw new java.lang.Error("ERROR: Encountered unrecognized snap point when refreshing EDALine snap points!");
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

        // Creating the start snap point...
        SnapPoint startSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        startSnapPoint.setId("snapStart");
        startSnapPoint.setVisible(false);
        this.snapPoints.add(startSnapPoint);

        // Creating the middle snap point...
        SnapPoint middleSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        middleSnapPoint.setId("snapMiddle");
        middleSnapPoint.setVisible(false);
        this.snapPoints.add(middleSnapPoint);

        // Creating the end snap point...
        SnapPoint endSnapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
        endSnapPoint.setId("snapEnd");
        endSnapPoint.setVisible(false);
        this.snapPoints.add(endSnapPoint);
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

        colors.add((Color)this.line.getStroke());
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

                    this.line.setTranslateX(newPosX + this.editor.paneHolder.getWidth() / 2);
                }
                else if (!posXStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(posYStr))
                {
                    Double newPosY = Double.parseDouble(posYStr);

                    this.line.setTranslateY(newPosY + this.editor.paneHolder.getHeight() / 2);
                }
                else if (!posYStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element Y position because the entered field is non-numeric!");
                }
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
                    this.line.setStroke(color);
                }
                else
                {
                    if (color != null)
                        EDAmameController.SetStatusBar("Unable to apply shape colors because the entered color is transparent!");
                }
            }
        }
    }

    public void PropsApplySymbol(VBox propsBox)
    {
        if (!this.selected)
            return;

        // Applying lines...
        {
            Integer lineStartPointsBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "lineStartPointsBox");

            if (lineStartPointsBoxIdx != -1)
            {
                HBox lineStartPointsBox = (HBox) propsBox.getChildren().get(lineStartPointsBoxIdx);
                TextField startPointXText = (TextField) EDAmameController.GetNodeById(lineStartPointsBox.getChildren(), "lineStartPointsX");
                TextField startPointYText = (TextField) EDAmameController.GetNodeById(lineStartPointsBox.getChildren(), "lineStartPointsY");

                if (startPointXText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"lineStartPointsX\" node in Symbol Editor properties window \"lineStartPointsBox\" entry!");
                if (startPointYText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"lineStartPointsY\" node in Symbol Editor properties window \"lineStartPointsBox\" entry!");

                String startPointXStr = startPointXText.getText();
                String startPointYStr = startPointYText.getText();

                if (EDAmameController.IsStringNum(startPointXStr))
                    this.line.setStartX(Double.parseDouble(startPointXStr));
                else if (!startPointXStr.equals("<mixed>"))
                    EDAmameController.SetStatusBar("Unable to apply line start point X because the entered field is non-numeric!");

                if (EDAmameController.IsStringNum(startPointYStr))
                    this.line.setStartY(Double.parseDouble(startPointYStr));
                else if (!startPointYStr.equals("<mixed>"))
                    EDAmameController.SetStatusBar("Unable to apply line start point Y because the entered field is non-numeric!");
            }

            Integer lineEndPointsBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "lineEndPointsBox");

            if (lineEndPointsBoxIdx != -1)
            {
                HBox lineEndPointsBox = (HBox) propsBox.getChildren().get(lineEndPointsBoxIdx);
                TextField endPointXText = (TextField) EDAmameController.GetNodeById(lineEndPointsBox.getChildren(), "lineEndPointsX");
                TextField endPointYText = (TextField) EDAmameController.GetNodeById(lineEndPointsBox.getChildren(), "lineEndPointsY");

                if (endPointXText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"lineEndPointsX\" node in Symbol Editor properties window \"lineEndPointsBox\" entry!");
                if (endPointYText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"lineEndPointsY\" node in Symbol Editor properties window \"lineEndPointsBox\" entry!");

                String endPointXStr = endPointXText.getText();
                String endPointYStr = endPointYText.getText();

                if (EDAmameController.IsStringNum(endPointXStr))
                    this.line.setEndX(Double.parseDouble(endPointXStr));
                else if (!endPointXStr.equals("<mixed>"))
                    EDAmameController.SetStatusBar("Unable to apply line end point X because the entered field is non-numeric!");

                if (EDAmameController.IsStringNum(endPointYStr))
                    this.line.setEndY(Double.parseDouble(endPointYStr));
                else if (!endPointYStr.equals("<mixed>"))
                    EDAmameController.SetStatusBar("Unable to apply line end point Y because the entered field is non-numeric!");
            }

            Integer lineWidthsBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "lineWidthsBox");

            if (lineWidthsBoxIdx != -1)
            {
                HBox lineWidthsBox = (HBox) propsBox.getChildren().get(lineWidthsBoxIdx);
                TextField lineWidthsText = (TextField) EDAmameController.GetNodeById(lineWidthsBox.getChildren(), "lineWidths");

                if (lineWidthsText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"lineWidths\" node in Symbol Editor properties window \"lineWidthsBox\" entry!");

                String widthStr = lineWidthsText.getText();

                if (EDAmameController.IsStringNum(widthStr))
                {
                    Double newWidth = Double.parseDouble(widthStr);

                    if ((newWidth >= EDAmameController.Editor_LineWidthMin) && (newWidth <= EDAmameController.Editor_LineWidthMax))
                        this.line.setStrokeWidth(newWidth);
                    else
                        EDAmameController.SetStatusBar("Unable to apply line widths because the entered field is outside the limits! (Width limits: " + EDAmameController.Editor_LineWidthMin + ", " + EDAmameController.Editor_LineWidthMax + ")");
                }
                else if (!widthStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply line widths because the entered field is non-numeric!");
                }
            }
        }
    }

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        return new EDALine(this.name, (Line)EDANode.NodeClone(this.line), this.passive, this.editor);
    }
}
