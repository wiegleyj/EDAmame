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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;

import java.util.LinkedList;

public class EDARectangle extends EDANode
{
    //// GLOBAL VARIABLES ////

    public Rectangle rectangle;

    //// CONSTRUCTORS ////

    public EDARectangle(String nameValue, Rectangle nodeValue, boolean createSnapPoints, boolean passiveValue, Editor editorValue)
    {
        if (editorValue == null)
            throw new java.lang.Error("ERROR: Attempting to create an EDARectangle \"" + nameValue + "\" without a supplied editor!");

        this.rectangle = nodeValue;

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
        this.rectangle.setTranslateX(pos.GetLeftDouble());
        this.rectangle.setTranslateY(pos.GetRightDouble());
    };

    public void SetRotate(double rot)
    {
        this.rectangle.setRotate(rot);
    };

    public void SetVisible(boolean visible)
    {
        this.rectangle.setVisible(visible);
    };

    //// GETTERS ////

    public Node GetNode()
    {
        return this.rectangle;
    }

    public PairMutable GetTranslate()
    {
        return new PairMutable(this.rectangle.getTranslateX(), this.rectangle.getTranslateY());
    };

    public PairMutable GetSnapPos()
    {
        return this.GetTranslate();
    }

    public double GetRotate()
    {
        return this.rectangle.getRotate();
    };

    public Bounds GetBoundsLocal()
    {
        return this.rectangle.getBoundsInLocal();
    };

    public Bounds GetBoundsParent()
    {
        return this.rectangle.getBoundsInParent();
    };

    //// EDITOR FUNCTIONS ////

    public void Rotate(double deltaY)
    {
        if (!this.selected)
            return;

        double angle = 10;

        if (deltaY < 0)
            angle = -10;

        this.rectangle.setRotate(this.rectangle.getRotate() + angle);
    }

    public void NodeAdd()
    {
        this.editor.paneHolder.getChildren().add(1, this.rectangle);
    };

    public void NodeRemove()
    {
        this.editor.paneHolder.getChildren().remove(this.rectangle);
    };

    //// POSITION FUNCTIONS ////

    public PairMutable BoundsPosToHolderPane(PairMutable posOffset)
    {
        Bounds boundsLocal = this.rectangle.getBoundsInLocal();
        PairMutable boundsRealEdgeL = this.PosToHolderPane(new PairMutable(boundsLocal.getMinX() + posOffset.GetLeftDouble(), boundsLocal.getMinY() + posOffset.GetRightDouble()));
        PairMutable boundsRealEdgeH = this.PosToHolderPane(new PairMutable(boundsLocal.getMaxX() + posOffset.GetLeftDouble(), boundsLocal.getMaxY() + posOffset.GetRightDouble()));

        return new PairMutable((boundsRealEdgeL.GetLeftDouble() + boundsRealEdgeH.GetLeftDouble()) / 2,
                               (boundsRealEdgeL.GetRightDouble() + boundsRealEdgeH.GetRightDouble()) / 2);
    }

    public PairMutable PosToHolderPane(PairMutable pos)
    {
        Point2D newPos = this.rectangle.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public boolean PosOnNode(PairMutable pos)
    {
        return this.rectangle.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
    }

    //// SNAP POINT FUNCTIONS ////

    public void SnapPointsRefresh()
    {
        Bounds boundsLocal = this.rectangle.getBoundsInLocal();

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
                throw new java.lang.Error("ERROR: Encountered unrecognized snap point when refreshing EDARectangle snap points!");
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

                    this.rectangle.setTranslateX(newPosX + this.editor.paneHolder.getWidth() / 2);
                }
                else if (!posXStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(posYStr))
                {
                    Double newPosY = this.editor.PosSnapToGridPoint(Double.parseDouble(posYStr));

                    this.rectangle.setTranslateY(newPosY + this.editor.paneHolder.getHeight() / 2);
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

        colors.add(this.rectangle.getFill());

        strokeWidths.add(this.rectangle.getStrokeWidth() / 10);
        strokes.add(this.rectangle.getStroke());

        rectsWidths.add(this.rectangle.getWidth() / 10);
        rectsHeights.add(this.rectangle.getHeight() / 10);

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
                    this.rectangle.setFill(color);
                }
                else
                {
                    if (color != null)
                        EDAmameController.SetStatusBar("Unable to apply shape colors because the entered color is transparent!");
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

                    if ((newStrokeWidth >= EDAmameController.EditorSymbol_BorderMin) && (newStrokeWidth <= EDAmameController.EditorSymbol_BorderMax))
                        this.rectangle.setStrokeWidth(newStrokeWidth * 10);
                    else
                        EDAmameController.SetStatusBar("Unable to apply shape border width because the entered field is outside the limits! (Border width limits: " + EDAmameController.EditorSymbol_BorderMin + ", " + EDAmameController.EditorSymbol_BorderMax + ")");
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
                    this.rectangle.setStroke(color);
                }
                else
                {
                    if (color != null)
                        EDAmameController.SetStatusBar("Unable to apply shape border color because the entered color is transparent!");
                }
            }
        }

        // Applying rectangle width & height...
        {
            Integer rectBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "rectBox");

            if (rectBoxIdx != -1)
            {
                HBox rectBox = (HBox) propsBox.getChildren().get(rectBoxIdx);
                TextField widthText = (TextField) EDAmameController.GetNodeById(rectBox.getChildren(), "rectWidths");
                TextField heightText = (TextField) EDAmameController.GetNodeById(rectBox.getChildren(), "rectHeights");

                if (widthText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"rectWidths\" node in Symbol Editor properties window \"rectBox\" entry!");
                if (heightText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"rectHeights\" node in Symbol Editor properties window \"rectBox\" entry!");

                String widthStr = widthText.getText();
                String heightStr = heightText.getText();

                if (EDAmameController.IsStringNum(widthStr))
                {
                    Double newWidth = Double.parseDouble(widthStr);

                    if ((newWidth >= EDAmameController.EditorSymbol_RectWidthMin) && (newWidth <= EDAmameController.EditorSymbol_RectWidthMax))
                        this.rectangle.setWidth(newWidth * 10);
                    else
                        EDAmameController.SetStatusBar("Unable to apply rectangle widths because the entered field is outside the limits! (Width limits: " + EDAmameController.EditorSymbol_RectWidthMin + ", " + EDAmameController.EditorSymbol_RectWidthMax + ")");
                }
                else if (!widthStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply rectangle widths because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(heightStr))
                {
                    Double newHeight = Double.parseDouble(heightStr);

                    if ((newHeight >= EDAmameController.EditorSymbol_RectHeightMin) && (newHeight <= EDAmameController.EditorSymbol_RectHeightMax))
                        this.rectangle.setHeight(newHeight * 10);
                    else
                        EDAmameController.SetStatusBar("Unable to apply rectangle heights because the entered field is outside the limits! (Height limits: " + EDAmameController.EditorSymbol_RectHeightMin + ", " + EDAmameController.EditorSymbol_RectHeightMax + ")");
                }
                else if (!heightStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply rectangle heights because the entered field is non-numeric!");
                }
            }
        }
    }

    public boolean PropsLoadFootprint(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        layers.add(this.rectangle.getId());

        if (this.rectangle.getFill() == Color.TRANSPARENT)
            fills.add(false);
        else
            fills.add(true);

        strokeWidths.add(this.rectangle.getStrokeWidth() / 10);

        rectsWidths.add(this.rectangle.getWidth() / 10);
        rectsHeights.add(this.rectangle.getHeight() / 10);

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

                    this.rectangle.setId(layer);
                    this.rectangle.setStroke(Editor.GetPCBLayerColor(layer));

                    if (this.rectangle.getFill() != Color.TRANSPARENT)
                        this.rectangle.setFill(Editor.GetPCBLayerColor(layer));
                }
                else if (!layerChoiceBox.getValue().equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply layer because the entered field is invalid!");
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

                    if ((newStrokeWidth >= EDAmameController.EditorSymbol_BorderMin) && (newStrokeWidth <= EDAmameController.EditorSymbol_BorderMax))
                        this.rectangle.setStrokeWidth(newStrokeWidth * 10);
                    else
                        EDAmameController.SetStatusBar("Unable to apply shape border width because the entered field is outside the limits! (Border width limits: " + EDAmameController.EditorSymbol_BorderMin + ", " + EDAmameController.EditorSymbol_BorderMax + ")");
                }
                else if (!strokeWidthStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply shape border width because the entered field is non-numeric!");
                }
            }
        }

        // Applying fills...
        {
            Integer fillBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "fillBox");

            if (fillBoxIdx != -1)
            {
                HBox fillBox = (HBox)propsBox.getChildren().get(fillBoxIdx);
                CheckBox fillCheckBox = (CheckBox)EDAmameController.GetNodeById(fillBox.getChildren(), "fills");

                if (fillCheckBox == null)
                    throw new java.lang.Error("ERROR: Unable to find \"fills\" node in global properties window \"fillBox\" entry!");

                if (fillCheckBox.isSelected())
                    this.rectangle.setFill(Editor.GetPCBLayerColor(this.rectangle.getId()));
                else
                    this.rectangle.setFill(Color.TRANSPARENT);
            }
        }

        // Applying rectangle width & height...
        {
            Integer rectBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "rectBox");

            if (rectBoxIdx != -1)
            {
                HBox rectBox = (HBox) propsBox.getChildren().get(rectBoxIdx);
                TextField widthText = (TextField) EDAmameController.GetNodeById(rectBox.getChildren(), "rectWidths");
                TextField heightText = (TextField) EDAmameController.GetNodeById(rectBox.getChildren(), "rectHeights");

                if (widthText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"rectWidths\" node in Symbol Editor properties window \"rectBox\" entry!");
                if (heightText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"rectHeights\" node in Symbol Editor properties window \"rectBox\" entry!");

                String widthStr = widthText.getText();
                String heightStr = heightText.getText();

                if (EDAmameController.IsStringNum(widthStr))
                {
                    Double newWidth = Double.parseDouble(widthStr);

                    if ((newWidth >= EDAmameController.EditorSymbol_RectWidthMin) && (newWidth <= EDAmameController.EditorSymbol_RectWidthMax))
                        this.rectangle.setWidth(newWidth * 10);
                    else
                        EDAmameController.SetStatusBar("Unable to apply rectangle widths because the entered field is outside the limits! (Width limits: " + EDAmameController.EditorSymbol_RectWidthMin + ", " + EDAmameController.EditorSymbol_RectWidthMax + ")");
                }
                else if (!widthStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply rectangle widths because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(heightStr))
                {
                    Double newHeight = Double.parseDouble(heightStr);

                    if ((newHeight >= EDAmameController.EditorSymbol_RectHeightMin) && (newHeight <= EDAmameController.EditorSymbol_RectHeightMax))
                        this.rectangle.setHeight(newHeight * 10);
                    else
                        EDAmameController.SetStatusBar("Unable to apply rectangle heights because the entered field is outside the limits! (Height limits: " + EDAmameController.EditorSymbol_RectHeightMin + ", " + EDAmameController.EditorSymbol_RectHeightMax + ")");
                }
                else if (!heightStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply rectangle heights because the entered field is non-numeric!");
                }
            }
        }
    }

    public boolean PropsLoadPCB(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        layers.add(this.rectangle.getId());

        if (this.rectangle.getFill() == Color.TRANSPARENT)
            fills.add(false);
        else
            fills.add(true);

        strokeWidths.add(this.rectangle.getStrokeWidth() / 10);

        rectsWidths.add(this.rectangle.getWidth() / 10);
        rectsHeights.add(this.rectangle.getHeight() / 10);

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

                    this.rectangle.setId(layer);
                    this.rectangle.setStroke(Editor.GetPCBLayerColor(layer));

                    if (this.rectangle.getFill() != Color.TRANSPARENT)
                        this.rectangle.setFill(Editor.GetPCBLayerColor(layer));
                }
                else if (!layerChoiceBox.getValue().equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply layer because the entered field is invalid!");
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

                    if ((newStrokeWidth >= EDAmameController.EditorSymbol_BorderMin) && (newStrokeWidth <= EDAmameController.EditorSymbol_BorderMax))
                        this.rectangle.setStrokeWidth(newStrokeWidth * 10);
                    else
                        EDAmameController.SetStatusBar("Unable to apply shape border width because the entered field is outside the limits! (Border width limits: " + EDAmameController.EditorSymbol_BorderMin + ", " + EDAmameController.EditorSymbol_BorderMax + ")");
                }
                else if (!strokeWidthStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply shape border width because the entered field is non-numeric!");
                }
            }
        }

        // Applying fills...
        {
            Integer fillBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "fillBox");

            if (fillBoxIdx != -1)
            {
                HBox fillBox = (HBox)propsBox.getChildren().get(fillBoxIdx);
                CheckBox fillCheckBox = (CheckBox)EDAmameController.GetNodeById(fillBox.getChildren(), "fills");

                if (fillCheckBox == null)
                    throw new java.lang.Error("ERROR: Unable to find \"fills\" node in global properties window \"fillBox\" entry!");

                if (fillCheckBox.isSelected())
                    this.rectangle.setFill(Editor.GetPCBLayerColor(this.rectangle.getId()));
                else
                    this.rectangle.setFill(Color.TRANSPARENT);
            }
        }

        // Applying rectangle width & height...
        {
            Integer rectBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "rectBox");

            if (rectBoxIdx != -1)
            {
                HBox rectBox = (HBox) propsBox.getChildren().get(rectBoxIdx);
                TextField widthText = (TextField) EDAmameController.GetNodeById(rectBox.getChildren(), "rectWidths");
                TextField heightText = (TextField) EDAmameController.GetNodeById(rectBox.getChildren(), "rectHeights");

                if (widthText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"rectWidths\" node in Symbol Editor properties window \"rectBox\" entry!");
                if (heightText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"rectHeights\" node in Symbol Editor properties window \"rectBox\" entry!");

                String widthStr = widthText.getText();
                String heightStr = heightText.getText();

                if (EDAmameController.IsStringNum(widthStr))
                {
                    Double newWidth = Double.parseDouble(widthStr);

                    if ((newWidth >= EDAmameController.EditorSymbol_RectWidthMin) && (newWidth <= EDAmameController.EditorSymbol_RectWidthMax))
                        this.rectangle.setWidth(newWidth * 10);
                    else
                        EDAmameController.SetStatusBar("Unable to apply rectangle widths because the entered field is outside the limits! (Width limits: " + EDAmameController.EditorSymbol_RectWidthMin + ", " + EDAmameController.EditorSymbol_RectWidthMax + ")");
                }
                else if (!widthStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply rectangle widths because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(heightStr))
                {
                    Double newHeight = Double.parseDouble(heightStr);

                    if ((newHeight >= EDAmameController.EditorSymbol_RectHeightMin) && (newHeight <= EDAmameController.EditorSymbol_RectHeightMax))
                        this.rectangle.setHeight(newHeight * 10);
                    else
                        EDAmameController.SetStatusBar("Unable to apply rectangle heights because the entered field is outside the limits! (Height limits: " + EDAmameController.EditorSymbol_RectHeightMin + ", " + EDAmameController.EditorSymbol_RectHeightMax + ")");
                }
                else if (!heightStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply rectangle heights because the entered field is non-numeric!");
                }
            }
        }
    }

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        return new EDARectangle(this.name, (Rectangle)EDANode.NodeClone(this.rectangle), !this.snapPoints.isEmpty(), this.passive, this.editor);
    }

    public String ToGerberStr(String layerName)
    {
        return EDANode.NodeToGerberStr(this.rectangle, layerName, this.editor);
    }
}
