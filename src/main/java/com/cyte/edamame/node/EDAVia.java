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
import javafx.scene.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;

import java.util.LinkedList;

public class EDAVia extends EDAGroup
{
    //// CONSTRUCTORS ////

    public EDAVia(String nameValue, Group nodeValue, boolean passiveValue, Editor editorValue)
    {
        if (editorValue == null)
            throw new java.lang.Error("ERROR: Attempting to create an EDAVia \"" + nameValue + "\" without a supplied editor!");

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
        }
    }

    //// PROPERTIES FUNCTIONS ////

    public boolean PropsLoadSymbol(LinkedList<Paint> colors, LinkedList<Double> strokeWidths, LinkedList<Paint> strokes, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineStartPosX, LinkedList<Double> lineStartPosY, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<String> pinLabels)
    {
        if (!this.selected)
            return false;

        return true;
    }

    public void PropsApplySymbol(VBox propsBox)
    {}

    public boolean PropsLoadFootprint(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineStartPosX, LinkedList<Double> lineStartPosY, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        if (this.group.getChildren().size() != 2)
            throw new java.lang.Error("ERROR: Attempting to load a via into the footprint editor properties window that doesn't have 2 children!");

        viaRadii.add(Math.max(((Circle)this.group.getChildren().get(0)).getRadius(), ((Circle)this.group.getChildren().get(1)).getRadius()));

        return true;
    }

    public void PropsApplyFootprint(VBox propsBox)
    {
        if (!this.selected)
            return;

        if (this.group.getChildren().size() != 2)
            throw new java.lang.Error("ERROR: Attempting to apply a via from the footprint editor properties window that doesn't have 2 children!");

        // Applying via radii...
        {
            Integer viaBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "viaBox");

            if (viaBoxIdx != -1)
            {
                HBox viaBox = (HBox) propsBox.getChildren().get(viaBoxIdx);
                TextField radiiText = (TextField) EDAmameController.GetNodeById(viaBox.getChildren(), "viaRadii");

                if (radiiText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"viaRadii\" node in Footprint Editor properties window \"viaBox\" entry!");

                String radiusStr = radiiText.getText();

                if (EDAmameController.IsStringNum(radiusStr))
                {
                    Double radius = Double.parseDouble(radiusStr);

                    if ((radius >= EDAmameController.EditorFootprint_ViaRadiusMin) && (radius <= EDAmameController.EditorFootprint_ViaRadiusMax))
                    {
                        ((Circle)this.group.getChildren().get(0)).setRadius(radius);
                        ((Circle)this.group.getChildren().get(1)).setRadius(radius / 2);
                    }
                    else
                    {
                        EDAmameController.SetStatusBar("Unable to apply via radii because the entered field is outside the limits! (Radius limits: " + EDAmameController.EditorFootprint_ViaRadiusMin + ", " + EDAmameController.EditorFootprint_ViaRadiusMax + ")");
                    }
                }
                else if (!radiusStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply via radii because the entered field is non-numeric!");
                }
            }
        }
    }

    public boolean PropsLoadPCB(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineStartPosX, LinkedList<Double> lineStartPosY, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        if (this.group.getChildren().size() != 2)
            throw new java.lang.Error("ERROR: Attempting to load a via into the footprint editor properties window that doesn't have 2 children!");

        viaRadii.add(Math.max(((Circle)this.group.getChildren().get(0)).getRadius(), ((Circle)this.group.getChildren().get(1)).getRadius()));

        return true;
    }

    public void PropsApplyPCB(VBox propsBox)
    {
        if (!this.selected)
            return;

        if (this.group.getChildren().size() != 2)
            throw new java.lang.Error("ERROR: Attempting to apply a via from the footprint editor properties window that doesn't have 2 children!");

        // Applying via radii...
        {
            Integer viaBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "viaBox");

            if (viaBoxIdx != -1)
            {
                HBox viaBox = (HBox) propsBox.getChildren().get(viaBoxIdx);
                TextField radiiText = (TextField) EDAmameController.GetNodeById(viaBox.getChildren(), "viaRadii");

                if (radiiText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"viaRadii\" node in Footprint Editor properties window \"viaBox\" entry!");

                String radiusStr = radiiText.getText();

                if (EDAmameController.IsStringNum(radiusStr))
                {
                    Double radius = Double.parseDouble(radiusStr);

                    if ((radius >= EDAmameController.EditorFootprint_ViaRadiusMin) && (radius <= EDAmameController.EditorFootprint_ViaRadiusMax))
                    {
                        ((Circle)this.group.getChildren().get(0)).setRadius(radius);
                        ((Circle)this.group.getChildren().get(1)).setRadius(radius / 2);
                    }
                    else
                    {
                        EDAmameController.SetStatusBar("Unable to apply via radii because the entered field is outside the limits! (Radius limits: " + EDAmameController.EditorFootprint_ViaRadiusMin + ", " + EDAmameController.EditorFootprint_ViaRadiusMax + ")");
                    }
                }
                else if (!radiusStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply via radii because the entered field is non-numeric!");
                }
            }
        }
    }

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        return new EDAVia(this.name, (Group)EDANode.NodeClone(this.group), this.passive, this.editor);
    }
}
