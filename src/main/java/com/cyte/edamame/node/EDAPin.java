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

public class EDAPin extends EDANode
{
    //// GLOBAL VARIABLES ////

    public Group pin;

    //// CONSTRUCTORS ////

    public EDAPin(String nameValue, Group nodeValue, LinkedList<PairMutable> snapPointPos, boolean passiveValue, Editor editorValue)
    {
        if (editorValue == null)
            throw new java.lang.Error("ERROR: Attempting to create an EDAPin \"" + nameValue + "\" without a supplied editor!");

        this.pin = nodeValue;

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
            this.SnapPointsCreate(snapPointPos);
        }
    }

    //// SETTERS ////

    public void SetTranslate(PairMutable pos)
    {
        this.pin.setTranslateX(pos.GetLeftDouble());
        this.pin.setTranslateY(pos.GetRightDouble());
    };

    public void SetRotate(double rot)
    {
        this.pin.setRotate(rot);
    };

    public void SetVisible(boolean visible)
    {
        this.pin.setVisible(visible);
    };

    //// GETTERS ////

    public Node GetNode()
    {
        return this.pin;
    }

    public PairMutable GetTranslate()
    {
        return new PairMutable(this.pin.getTranslateX(), this.pin.getTranslateY());
    };

    public double GetRotate()
    {
        return this.pin.getRotate();
    };

    public Bounds GetBoundsLocal()
    {
        return this.pin.getBoundsInLocal();
    };

    public Bounds GetBoundsParent()
    {
        return this.pin.getBoundsInParent();
    };

    //// EDITOR FUNCTIONS ////

    public void Rotate(double deltaY) {}

    public void NodeAdd()
    {
        this.editor.paneHolder.getChildren().add(1, this.pin);
    };

    public void NodeRemove()
    {
        this.editor.paneHolder.getChildren().remove(this.pin);
    };

    //// POSITION FUNCTIONS ////

    public PairMutable BoundsPosToHolderPane(PairMutable posOffset)
    {
        Bounds boundsLocal = this.pin.getBoundsInLocal();
        PairMutable boundsRealEdgeL = this.PosToHolderPane(new PairMutable(boundsLocal.getMinX() + posOffset.GetLeftDouble(), boundsLocal.getMinY() + posOffset.GetRightDouble()));
        PairMutable boundsRealEdgeH = this.PosToHolderPane(new PairMutable(boundsLocal.getMaxX() + posOffset.GetLeftDouble(), boundsLocal.getMaxY() + posOffset.GetRightDouble()));

        return new PairMutable((boundsRealEdgeL.GetLeftDouble() + boundsRealEdgeH.GetLeftDouble()) / 2,
                               (boundsRealEdgeL.GetRightDouble() + boundsRealEdgeH.GetRightDouble()) / 2);
    }

    public PairMutable PosToHolderPane(PairMutable pos)
    {
        Point2D newPos = this.pin.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public boolean PosOnNode(PairMutable pos)
    {
        return this.pin.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    //// SNAP POINT FUNCTIONS ////

    public void SnapPointsRefresh()
    {
        for (int i = 0; i < this.snapPoints.size(); i++)
        {
            SnapPoint snapPoint = this.snapPoints.get(i);
            String snapPointId = snapPoint.getId();
            PairMutable posSnapReal = null;

            if (snapPointId.equals("snapManual"))
            {
                posSnapReal = this.PosToHolderPane(new PairMutable(snapPoint.pos.GetLeftDouble(),
                        snapPoint.pos.GetRightDouble()));
            }
            else
            {
                throw new java.lang.Error("ERROR: Encountered unrecognized snap point when refreshing EDAPin snap points!");
            }

            snapPoint.setTranslateX(posSnapReal.GetLeftDouble());
            snapPoint.setTranslateY(posSnapReal.GetRightDouble());

            //this.RenderNode_RenderSystem.RenderSystem_TestShapeAdd(posSnapReal, 5.0, Color.RED, true);
            //System.out.println(posSnapReal.ToStringDouble());
        }
    }

    public void SnapPointsCreate(LinkedList<PairMutable> snapPointPos)
    {
        for (int i = 0; i < snapPointPos.size(); i++)
        {
            PairMutable currPos = snapPointPos.get(i);

            SnapPoint snapPoint = new SnapPoint(currPos, EDAmameController.Editor_SnapPointRadius, EDAmameController.Editor_SnapPointShapeColor, EDAmameController.Editor_SnapPointShapeOpacity, this);
            snapPoint.setId("snapManual");
            snapPoint.setVisible(false);
            this.snapPoints.add(snapPoint);
        }
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

        if (this.pin.getChildren().size() != 2)
            throw new java.lang.Error("ERROR: Attempting to load pin into global properties editor without 2 children!");

        colors.add((Color)((Shape)this.pin.getChildren().get(0)).getStroke());
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

                    this.pin.setTranslateX(newPosX + this.editor.paneHolder.getWidth() / 2);
                }
                else if (!posXStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(posYStr))
                {
                    Double newPosY = Double.parseDouble(posYStr);

                    this.pin.setTranslateY(newPosY + this.editor.paneHolder.getHeight() / 2);
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
                    if (this.pin.getChildren().size() != 2)
                        throw new java.lang.Error("ERROR: Attempting to load pin into global properties window without 2 children!");

                    ((Shape)this.pin.getChildren().get(0)).setFill(color);
                    ((Shape)this.pin.getChildren().get(1)).setFill(color);
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

        if (this.pin.getChildren().size() != 2)
            throw new java.lang.Error("ERROR: Attempting to load pin into symbol properties window without 2 children!");
        if (this.pin.getChildren().get(1).getClass() != Text.class)
            throw new java.lang.Error("ERROR: Attempting to load pin into symbol properties window without a text node!");

        pinLabels.add(((Text)this.pin.getChildren().get(1)).getText());

        return true;
    }

    public void PropsApplySymbol(VBox propsBox)
    {
        if (!this.selected)
            return;

        // Applying pins...
        {
            if (this.pin.getChildren().size() != 2)
                throw new java.lang.Error("ERROR: Attempting to apply pin from symbol properties window without 2 children!");
            if (this.pin.getChildren().get(1).getClass() != Text.class)
                throw new java.lang.Error("ERROR: Attempting to apply pin from symbol properties window without a text node!");

            Integer pinLabelBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "pinLabelBox");

            if (pinLabelBoxIdx != -1)
            {
                HBox pinLabelBox = (HBox)propsBox.getChildren().get(pinLabelBoxIdx);
                TextField pinLabelText = (TextField)EDAmameController.GetNodeById(pinLabelBox.getChildren(), "pinLabels");

                if (pinLabelText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"pinLabels\" node in global properties window \"pinLabelBox\" entry!");

                String pinLabel = pinLabelText.getText();

                if (!pinLabel.isEmpty())
                {
                    if (!pinLabel.equals("<mixed>"))
                        ((Text)this.pin.getChildren().get(1)).setText(pinLabel);
                }
                else
                {
                    EDAmameController.SetStatusBar("Unable to apply pin label because the entered field is empty!");
                }
            }
        }
    }

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        LinkedList<PairMutable> clonedSnapPoints = new LinkedList<PairMutable>();

        for (int i = 0; i < this.snapPoints.size(); i++)
            clonedSnapPoints.add(new PairMutable(this.snapPoints.get(i).getTranslateX(), this.snapPoints.get(i).getTranslateY()));

        return new EDAPin(this.name, (Group)EDANode.NodeClone(this.pin), clonedSnapPoints, this.passive, this.editor);
    }
}
