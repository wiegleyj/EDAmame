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
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.*;

import java.util.LinkedList;

public class EDAText extends EDANode
{
    //// GLOBAL VARIABLES ////

    public Text text;

    //// CONSTRUCTORS ////

    public EDAText(String nameValue, Text nodeValue, boolean createSnapPoints, boolean passiveValue, Editor editorValue)
    {
        if (editorValue == null)
            throw new java.lang.Error("ERROR: Attempting to create an EDAText \"" + nameValue + "\" without a supplied editor!");

        this.text = nodeValue;

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

            if (createSnapPoints)
                this.SnapPointsCreate();
        }
    }

    //// SETTERS ////

    public void SetTranslate(PairMutable pos)
    {
        this.text.setTranslateX(pos.GetLeftDouble());
        this.text.setTranslateY(pos.GetRightDouble());
    };

    public void SetRotate(double rot)
    {
        this.text.setRotate(rot);
    };

    public void SetVisible(boolean visible)
    {
        this.text.setVisible(visible);
    };

    //// GETTERS ////

    public Node GetNode()
    {
        return this.text;
    }

    public PairMutable GetTranslate()
    {
        return new PairMutable(this.text.getTranslateX(), this.text.getTranslateY());
    };

    public PairMutable GetSnapPos()
    {
        return this.GetTranslate();
    }

    public double GetRotate()
    {
        return this.text.getRotate();
    };

    public Bounds GetBoundsLocal()
    {
        return this.text.getBoundsInLocal();
    };

    public Bounds GetBoundsParent()
    {
        return this.text.getBoundsInParent();
    };

    //// EDITOR FUNCTIONS ////

    public void Rotate(double deltaY)
    {
        if (!this.selected)
            return;

        double angle = 10;

        if (deltaY < 0)
            angle = -10;

        this.text.setRotate(this.text.getRotate() + angle);
    }

    public void NodeAdd()
    {
        this.editor.paneHolder.getChildren().add(1, this.text);
    };

    public void NodeRemove()
    {
        this.editor.paneHolder.getChildren().remove(this.text);
    };

    //// POSITION FUNCTIONS ////

    public PairMutable BoundsPosToHolderPane(PairMutable posOffset)
    {
        Bounds boundsLocal = this.text.getBoundsInLocal();
        PairMutable boundsRealEdgeL = this.PosToHolderPane(new PairMutable(boundsLocal.getMinX() + posOffset.GetLeftDouble(), boundsLocal.getMinY() + posOffset.GetRightDouble()));
        PairMutable boundsRealEdgeH = this.PosToHolderPane(new PairMutable(boundsLocal.getMaxX() + posOffset.GetLeftDouble(), boundsLocal.getMaxY() + posOffset.GetRightDouble()));

        return new PairMutable((boundsRealEdgeL.GetLeftDouble() + boundsRealEdgeH.GetLeftDouble()) / 2,
                               (boundsRealEdgeL.GetRightDouble() + boundsRealEdgeH.GetRightDouble()) / 2);
    }

    public PairMutable PosToHolderPane(PairMutable pos)
    {
        Point2D newPos = this.text.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public boolean PosOnNode(PairMutable pos)
    {
        return this.text.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    //// SNAP POINT FUNCTIONS ////

    public void SnapPointsRefresh()
    {
        Bounds boundsLocal = this.text.getBoundsInLocal();

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
            else if (snapPointId.equals("snapManual"))
            {
                posSnapReal = this.PosToHolderPane(new PairMutable(snapPoint.pos.GetLeftDouble(),
                        snapPoint.pos.GetRightDouble()));
            }
            else
            {
                throw new java.lang.Error("ERROR: Encountered unrecognized snap point when refreshing EDAText snap points!");
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

    public void PropsLoadGlobal(LinkedList<String> names, LinkedList<Double> posX, LinkedList<Double> posY, LinkedList<Double> rots)
    {
        if (!this.selected)
            return;

        PairMutable pos = this.GetTranslate();

        names.add(this.name);

        posX.add(pos.GetLeftDouble() - this.editor.paneHolder.getWidth() / 2);
        posY.add(pos.GetRightDouble() - this.editor.paneHolder.getHeight() / 2);

        rots.add(this.GetRotate());
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

                if (!nameStr.isEmpty() && !nameStr.equals("<mixed>"))
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
                    Double newPosX = this.editor.PosSnapToGridPoint(Double.parseDouble(posXStr));

                    this.text.setTranslateX(newPosX + this.editor.paneHolder.getWidth() / 2);
                }
                else if (!posXStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(posYStr))
                {
                    Double newPosY = this.editor.PosSnapToGridPoint(Double.parseDouble(posYStr));

                    this.text.setTranslateY(newPosY + this.editor.paneHolder.getHeight() / 2);
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
    }

    public boolean PropsLoadSymbol(LinkedList<Paint> colors, LinkedList<Double> strokeWidths, LinkedList<Paint> strokes, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<String> pinLabels)
    {
        if (!this.selected)
            return false;

        colors.add(this.text.getFill());

        textContents.add(this.text.getText());
        textFontSizes.add(this.text.getFont().getSize());

        return true;
    }

    public void PropsApplySymbol(VBox propsBox)
    {
        if (!this.selected)
            return;

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
                    this.text.setFill(color);
                }
                else
                {
                    if (color != null)
                        EDAmameController.SetStatusBar("Unable to apply shape colors because the entered color is transparent!");
                }
            }
        }

        // Applying texts...
        {
            Integer contentBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "textContentBox");

            if (contentBoxIdx != -1)
            {
                HBox contentBox = (HBox)propsBox.getChildren().get(contentBoxIdx);
                TextField contentText = (TextField)EDAmameController.GetNodeById(contentBox.getChildren(), "textContent");

                if (contentText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"textContent\" node in global properties window \"textContentBox\" entry!");

                String content = contentText.getText();

                if (!content.isEmpty())
                {
                    if (!content.equals("<mixed>"))
                        this.text.setText(content);
                }
                else
                {
                    EDAmameController.SetStatusBar("Unable to apply text contents because the entered field is empty!");
                }
            }

            Integer fontSizeBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "fontSizeBox");

            if (fontSizeBoxIdx != -1)
            {
                HBox fontSizeBox = (HBox)propsBox.getChildren().get(fontSizeBoxIdx);
                TextField fontSizeText = (TextField)EDAmameController.GetNodeById(fontSizeBox.getChildren(), "fontSize");

                if (fontSizeText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"fontSize\" node in global properties window \"fontSizeBox\" entry!");

                String fontSizeStr = fontSizeText.getText();

                if (EDAmameController.IsStringNum(fontSizeStr))
                {
                    double fontSize = Double.parseDouble(fontSizeStr);

                    if (((fontSize >= EDAmameController.EditorSymbol_TextFontSizeMin) && (fontSize <= EDAmameController.EditorSymbol_TextFontSizeMax)))
                        this.text.setFont(new Font("Arial", fontSize));
                    else
                        EDAmameController.SetStatusBar("Unable to apply text font size because the entered field is is outside the limits! (Font size limits: " + EDAmameController.EditorSymbol_TextFontSizeMin + ", " + EDAmameController.EditorSymbol_TextFontSizeMax + ")");
                }
                else if (!fontSizeStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply text font size because the entered field is non-numeric!");
                }
            }
        }
    }

    public boolean PropsLoadFootprint(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        layers.add(this.text.getId());

        textContents.add(this.text.getText());
        textFontSizes.add(this.text.getFont().getSize());

        return true;
    }

    public void PropsApplyFootprint(VBox propsBox)
    {
        if (!this.selected)
            return;

        // Applying layers...
        {
            Integer layerBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "layerBox");

            if (layerBoxIdx != -1)
            {
                HBox layerBox = (HBox)propsBox.getChildren().get(layerBoxIdx);
                ChoiceBox<String> layerChoiceBox = (ChoiceBox<String>)EDAmameController.GetNodeById(layerBox.getChildren(), "layers");

                if (layerChoiceBox == null)
                    throw new java.lang.Error("ERROR: Unable to find \"layers\" node in global properties window \"layerBox\" entry!");

                int layerIdx = Editor.FindPCBLayer(layerChoiceBox.getValue());

                if (layerIdx != -1)
                {
                    String layer = EDAmameController.Editor_PCBLayers[layerIdx];

                    this.text.setId(layer);
                    this.text.setFill(Editor.GetPCBLayerColor(layer));
                }
                else if (!layerChoiceBox.getValue().equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply layer because the entered field is invalid!");
                }
            }
        }

        // Applying texts...
        {
            Integer contentBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "textContentBox");

            if (contentBoxIdx != -1)
            {
                HBox contentBox = (HBox)propsBox.getChildren().get(contentBoxIdx);
                TextField contentText = (TextField)EDAmameController.GetNodeById(contentBox.getChildren(), "textContent");

                if (contentText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"textContent\" node in global properties window \"textContentBox\" entry!");

                String content = contentText.getText();

                if (!content.isEmpty())
                {
                    if (!content.equals("<mixed>"))
                        this.text.setText(content);
                }
                else
                {
                    EDAmameController.SetStatusBar("Unable to apply text contents because the entered field is empty!");
                }
            }

            Integer fontSizeBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "fontSizeBox");

            if (fontSizeBoxIdx != -1)
            {
                HBox fontSizeBox = (HBox)propsBox.getChildren().get(fontSizeBoxIdx);
                TextField fontSizeText = (TextField)EDAmameController.GetNodeById(fontSizeBox.getChildren(), "fontSize");

                if (fontSizeText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"fontSize\" node in global properties window \"fontSizeBox\" entry!");

                String fontSizeStr = fontSizeText.getText();

                if (EDAmameController.IsStringNum(fontSizeStr))
                {
                    double fontSize = Double.parseDouble(fontSizeStr);

                    if (((fontSize >= EDAmameController.EditorSymbol_TextFontSizeMin) && (fontSize <= EDAmameController.EditorSymbol_TextFontSizeMax)))
                        this.text.setFont(new Font("Arial", fontSize));
                    else
                        EDAmameController.SetStatusBar("Unable to apply text font size because the entered field is is outside the limits! (Font size limits: " + EDAmameController.EditorSymbol_TextFontSizeMin + ", " + EDAmameController.EditorSymbol_TextFontSizeMax + ")");
                }
                else if (!fontSizeStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply text font size because the entered field is non-numeric!");
                }
            }
        }
    }

    public boolean PropsLoadPCB(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        layers.add(this.text.getId());

        textContents.add(this.text.getText());
        textFontSizes.add(this.text.getFont().getSize());

        return true;
    }

    public void PropsApplyPCB(VBox propsBox)
    {
        if (!this.selected)
            return;

        // Applying layers...
        {
            Integer layerBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "layerBox");

            if (layerBoxIdx != -1)
            {
                HBox layerBox = (HBox)propsBox.getChildren().get(layerBoxIdx);
                ChoiceBox<String> layerChoiceBox = (ChoiceBox<String>)EDAmameController.GetNodeById(layerBox.getChildren(), "layers");

                if (layerChoiceBox == null)
                    throw new java.lang.Error("ERROR: Unable to find \"layers\" node in global properties window \"layerBox\" entry!");

                int layerIdx = Editor.FindPCBLayer(layerChoiceBox.getValue());

                if (layerIdx != -1)
                {
                    String layer = EDAmameController.Editor_PCBLayers[layerIdx];

                    this.text.setId(layer);
                    this.text.setFill(Editor.GetPCBLayerColor(layer));
                }
                else if (!layerChoiceBox.getValue().equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply layer because the entered field is invalid!");
                }
            }
        }

        // Applying texts...
        {
            Integer contentBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "textContentBox");

            if (contentBoxIdx != -1)
            {
                HBox contentBox = (HBox)propsBox.getChildren().get(contentBoxIdx);
                TextField contentText = (TextField)EDAmameController.GetNodeById(contentBox.getChildren(), "textContent");

                if (contentText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"textContent\" node in global properties window \"textContentBox\" entry!");

                String content = contentText.getText();

                if (!content.isEmpty())
                {
                    if (!content.equals("<mixed>"))
                        this.text.setText(content);
                }
                else
                {
                    EDAmameController.SetStatusBar("Unable to apply text contents because the entered field is empty!");
                }
            }

            Integer fontSizeBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "fontSizeBox");

            if (fontSizeBoxIdx != -1)
            {
                HBox fontSizeBox = (HBox)propsBox.getChildren().get(fontSizeBoxIdx);
                TextField fontSizeText = (TextField)EDAmameController.GetNodeById(fontSizeBox.getChildren(), "fontSize");

                if (fontSizeText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"fontSize\" node in global properties window \"fontSizeBox\" entry!");

                String fontSizeStr = fontSizeText.getText();

                if (EDAmameController.IsStringNum(fontSizeStr))
                {
                    double fontSize = Double.parseDouble(fontSizeStr);

                    if (((fontSize >= EDAmameController.EditorSymbol_TextFontSizeMin) && (fontSize <= EDAmameController.EditorSymbol_TextFontSizeMax)))
                        this.text.setFont(new Font("Arial", fontSize));
                    else
                        EDAmameController.SetStatusBar("Unable to apply text font size because the entered field is is outside the limits! (Font size limits: " + EDAmameController.EditorSymbol_TextFontSizeMin + ", " + EDAmameController.EditorSymbol_TextFontSizeMax + ")");
                }
                else if (!fontSizeStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply text font size because the entered field is non-numeric!");
                }
            }
        }
    }

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        return new EDAText(this.name, (Text)EDANode.NodeClone(this.text), !this.snapPoints.isEmpty(), this.passive, this.editor);
    }

    public String ToGerberStr(String layerName)
    {
        return EDANode.NodeToGerberStr(this.text, layerName);
    }
}
