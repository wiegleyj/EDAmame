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

public class EDAPin extends EDAGroup
{
    //// CONSTRUCTORS ////

    public EDAPin(String nameValue, Group nodeValue, LinkedList<PairMutable> snapPointPos, boolean passiveValue, Editor editorValue)
    {
        if (editorValue == null)
            throw new java.lang.Error("ERROR: Attempting to create an EDAPin \"" + nameValue + "\" without a supplied editor!");

        this.group = nodeValue;

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

    //// PROPERTIES FUNCTIONS ////

    public boolean PropsLoadSymbol(LinkedList<Paint> colors, LinkedList<Double> strokeWidths, LinkedList<Paint> strokes, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineStartPosX, LinkedList<Double> lineStartPosY, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<String> pinLabels)
    {
        if (!this.selected)
            return false;

        if (group.getChildren().size() != 2)
            throw new java.lang.Error("ERROR: Attempting to load pin into symbol properties window without 2 children!");
        if (group.getChildren().get(1).getClass() != Text.class)
            throw new java.lang.Error("ERROR: Attempting to load pin into symbol properties window without a text node!");

        colors.add(((Circle)this.group.getChildren().get(0)).getFill());

        pinLabels.add(((Text)this.group.getChildren().get(1)).getText());

        return true;
    }

    public void PropsApplySymbol(VBox propsBox)
    {
        if (!this.selected)
            return;

        if (this.group.getChildren().size() != 2)
            throw new java.lang.Error("ERROR: Attempting to apply pin from symbol properties window without 2 children!");
        if (this.group.getChildren().get(1).getClass() != Text.class)
            throw new java.lang.Error("ERROR: Attempting to apply pin from symbol properties window without a text node!");

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
                    ((Shape)this.group.getChildren().get(0)).setFill(color);
                    ((Shape)this.group.getChildren().get(1)).setFill(color);
                }
                else
                {
                    if (color != null)
                        EDAmameController.SetStatusBar("Unable to apply shape colors because the entered color is transparent!");
                }
            }
        }

        // Applying pin labels...
        {
            Integer pinLabelBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "pinLabelBox");

            if (pinLabelBoxIdx != -1) {
                HBox pinLabelBox = (HBox) propsBox.getChildren().get(pinLabelBoxIdx);
                TextField pinLabelText = (TextField) EDAmameController.GetNodeById(pinLabelBox.getChildren(), "pinLabels");

                if (pinLabelText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"pinLabels\" node in global properties window \"pinLabelBox\" entry!");

                String pinLabel = pinLabelText.getText();

                if (!pinLabel.isEmpty())
                {
                    if (!pinLabel.equals("<mixed>"))
                        ((Text)this.group.getChildren().get(1)).setText(pinLabel);
                }
                else
                {
                    EDAmameController.SetStatusBar("Unable to apply pin label because the entered field is empty!");
                }
            }
        }
    }

    public boolean PropsLoadFootprint(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineStartPosX, LinkedList<Double> lineStartPosY, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        return true;
    }

    public void PropsApplyFootprint(VBox propsBox)
    {}

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        LinkedList<PairMutable> clonedSnapPoints = new LinkedList<PairMutable>();

        for (int i = 0; i < this.snapPoints.size(); i++)
            clonedSnapPoints.add(new PairMutable(this.snapPoints.get(i).getTranslateX(), this.snapPoints.get(i).getTranslateY()));

        return new EDAPin(this.name, (Group)EDANode.NodeClone(this.group), clonedSnapPoints, this.passive, this.editor);
    }
}
