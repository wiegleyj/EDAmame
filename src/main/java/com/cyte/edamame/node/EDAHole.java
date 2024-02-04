/*
 * Copyright (c) 2024. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.node;

import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.misc.SnapPoint;
import com.cyte.edamame.misc.PairMutable;
import javafx.scene.*;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;

import java.util.LinkedList;

public class EDAHole extends EDAGroup
{
    //// CONSTRUCTORS ////

    public EDAHole(String nameValue, Group nodeValue, boolean passiveValue, Editor editorValue)
    {
        if (editorValue == null)
            throw new java.lang.Error("ERROR: Attempting to create an EDAHole \"" + nameValue + "\" without a supplied editor!");

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

    public boolean PropsLoadSymbol(LinkedList<Paint> colors, LinkedList<Double> strokeWidths, LinkedList<Paint> strokes, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<String> pinLabels)
    {
        if (!this.selected)
            return false;

        return true;
    }

    public void PropsApplySymbol(VBox propsBox)
    {}

    public boolean PropsLoadFootprint(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        if (this.group.getChildren().size() != 1)
            throw new java.lang.Error("ERROR: Attempting to load a hole into the footprint editor properties window that doesn't have 1 child!");

        holeOuterRadii.add(((Circle)this.group.getChildren().get(0)).getRadius() + ((Circle)this.group.getChildren().get(0)).getStrokeWidth() / 2);
        holeInnerRadii.add(((Circle)this.group.getChildren().get(0)).getRadius() - ((Circle)this.group.getChildren().get(0)).getStrokeWidth() / 2);

        return true;
    }

    public void PropsApplyFootprint(VBox propsBox)
    {
        if (!this.selected)
            return;

        if (this.group.getChildren().size() != 1)
            throw new java.lang.Error("ERROR: Attempting to apply a hole from the footprint editor properties window that doesn't have 1 child!");

        // Applying hole radii...
        {
            Integer holeBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "holeBox");

            if (holeBoxIdx != -1)
            {
                HBox holeBox = (HBox) propsBox.getChildren().get(holeBoxIdx);
                TextField outerRadiiText = (TextField) EDAmameController.GetNodeById(holeBox.getChildren(), "holeOuterRadii");
                TextField innerRadiiText = (TextField) EDAmameController.GetNodeById(holeBox.getChildren(), "holeInnerRadii");

                if (outerRadiiText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"holeOuterRadii\" node in Footprint Editor properties window \"holeBox\" entry!");
                if (innerRadiiText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"holeInnerRadii\" node in Footprint Editor properties window \"holeBox\" entry!");

                String outerRadiiStr = outerRadiiText.getText();
                String innerRadiiStr = innerRadiiText.getText();

                if (EDAmameController.IsStringNum(outerRadiiStr))
                {
                    if (EDAmameController.IsStringNum(innerRadiiStr))
                    {
                        Double outerRadii = Double.parseDouble(outerRadiiStr);
                        Double innerRadii = Double.parseDouble(innerRadiiStr);

                        if ((outerRadii >= EDAmameController.EditorFootprint_HoleRadiusOuterMin) && (outerRadii <= EDAmameController.EditorFootprint_HoleRadiusOuterMax))
                        {
                            if ((innerRadii >= EDAmameController.EditorFootprint_HoleRadiusInnerMin) && (innerRadii <= EDAmameController.EditorFootprint_HoleRadiusInnerMax))
                            {
                                if (outerRadii <= innerRadii)
                                {
                                    EDAmameController.SetStatusBar("Unable to apply hole radii because the entered outer radius field is smaller than or equal to the entered inner radius field!");
                                }
                                else
                                {
                                    ((Circle)this.group.getChildren().get(0)).setRadius((outerRadii + innerRadii) / 2);
                                    ((Circle)this.group.getChildren().get(0)).setStrokeWidth(outerRadii - innerRadii);
                                }
                            }
                            else
                            {
                                EDAmameController.SetStatusBar("Unable to apply hole inner radii because the entered field is outside the limits! (Inner radius limits: " + EDAmameController.EditorFootprint_HoleRadiusInnerMin + ", " + EDAmameController.EditorFootprint_HoleRadiusInnerMax + ")");
                            }
                        }
                        else
                        {
                            EDAmameController.SetStatusBar("Unable to apply hole outer radii because the entered field is outside the limits! (Outer radius limits: " + EDAmameController.EditorFootprint_HoleRadiusOuterMin + ", " + EDAmameController.EditorFootprint_HoleRadiusOuterMax + ")");
                        }
                    }
                    else if (!innerRadiiStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply hole inner radii because the entered field is non-numeric!");
                    }
                }
                else if (!outerRadiiStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply hole outer radii because the entered field is non-numeric!");
                }
            }
        }
    }

    public boolean PropsLoadPCB(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        if (this.group.getChildren().size() != 1)
            throw new java.lang.Error("ERROR: Attempting to load a hole into the footprint editor properties window that doesn't have 1 child!");

        holeOuterRadii.add(((Circle)this.group.getChildren().get(0)).getRadius() + ((Circle)this.group.getChildren().get(0)).getStrokeWidth() / 2);
        holeInnerRadii.add(((Circle)this.group.getChildren().get(0)).getRadius() - ((Circle)this.group.getChildren().get(0)).getStrokeWidth() / 2);

        return true;
    }

    public void PropsApplyPCB(VBox propsBox)
    {
        if (!this.selected)
            return;

        if (this.group.getChildren().size() != 1)
            throw new java.lang.Error("ERROR: Attempting to apply a hole from the footprint editor properties window that doesn't have 1 child!");

        // Applying hole radii...
        {
            Integer holeBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "holeBox");

            if (holeBoxIdx != -1)
            {
                HBox holeBox = (HBox) propsBox.getChildren().get(holeBoxIdx);
                TextField outerRadiiText = (TextField) EDAmameController.GetNodeById(holeBox.getChildren(), "holeOuterRadii");
                TextField innerRadiiText = (TextField) EDAmameController.GetNodeById(holeBox.getChildren(), "holeInnerRadii");

                if (outerRadiiText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"holeOuterRadii\" node in Footprint Editor properties window \"holeBox\" entry!");
                if (innerRadiiText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"holeInnerRadii\" node in Footprint Editor properties window \"holeBox\" entry!");

                String outerRadiiStr = outerRadiiText.getText();
                String innerRadiiStr = innerRadiiText.getText();

                if (EDAmameController.IsStringNum(outerRadiiStr))
                {
                    if (EDAmameController.IsStringNum(innerRadiiStr))
                    {
                        Double outerRadii = Double.parseDouble(outerRadiiStr);
                        Double innerRadii = Double.parseDouble(innerRadiiStr);

                        if ((outerRadii >= EDAmameController.EditorFootprint_HoleRadiusOuterMin) && (outerRadii <= EDAmameController.EditorFootprint_HoleRadiusOuterMax))
                        {
                            if ((innerRadii >= EDAmameController.EditorFootprint_HoleRadiusInnerMin) && (innerRadii <= EDAmameController.EditorFootprint_HoleRadiusInnerMax))
                            {
                                if (outerRadii <= innerRadii)
                                {
                                    EDAmameController.SetStatusBar("Unable to apply hole radii because the entered outer radius field is smaller than or equal to the entered inner radius field!");
                                }
                                else
                                {
                                    ((Circle)this.group.getChildren().get(0)).setRadius((outerRadii + innerRadii) / 2);
                                    ((Circle)this.group.getChildren().get(0)).setStrokeWidth(outerRadii - innerRadii);
                                }
                            }
                            else
                            {
                                EDAmameController.SetStatusBar("Unable to apply hole inner radii because the entered field is outside the limits! (Inner radius limits: " + EDAmameController.EditorFootprint_HoleRadiusInnerMin + ", " + EDAmameController.EditorFootprint_HoleRadiusInnerMax + ")");
                            }
                        }
                        else
                        {
                            EDAmameController.SetStatusBar("Unable to apply hole outer radii because the entered field is outside the limits! (Outer radius limits: " + EDAmameController.EditorFootprint_HoleRadiusOuterMin + ", " + EDAmameController.EditorFootprint_HoleRadiusOuterMax + ")");
                        }
                    }
                    else if (!innerRadiiStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply hole inner radii because the entered field is non-numeric!");
                    }
                }
                else if (!outerRadiiStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply hole outer radii because the entered field is non-numeric!");
                }
            }
        }
    }

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        return new EDAHole(this.name, (Group)EDANode.NodeClone(this.group), this.passive, this.editor);
    }
}
