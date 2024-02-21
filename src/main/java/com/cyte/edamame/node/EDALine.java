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
import javafx.scene.shape.*;

import java.util.LinkedList;

public class EDALine extends EDANode
{
    //// GLOBAL VARIABLES ////

    public Line line;

    //// CONSTRUCTORS ////

    public EDALine(String nameValue, Line nodeValue, boolean createSnapPoints, boolean passiveValue, Editor editorValue)
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

            if (createSnapPoints)
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

    public PairMutable GetSnapPos()
    {
        PairMutable pos = this.GetTranslate();

        return new PairMutable(this.line.getStartX() + pos.GetLeftDouble(), this.line.getStartY() + pos.GetRightDouble());
    }

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

    public void PropsLoadGlobal(LinkedList<String> names, LinkedList<Double> posX, LinkedList<Double> posY, LinkedList<Double> rots)
    {
        if (!this.selected)
            return;

        PairMutable pos = this.GetTranslate();

        names.add(this.name);

        posX.add(pos.GetLeftDouble() - this.editor.paneHolder.getWidth() / 2);
        posY.add(pos.GetRightDouble() - this.editor.paneHolder.getHeight() / 2);
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

                    this.line.setTranslateX(newPosX + this.editor.paneHolder.getWidth() / 2);
                }
                else if (!posXStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(posYStr))
                {
                    Double newPosY = this.editor.PosSnapToGridPoint(Double.parseDouble(posYStr));

                    this.line.setTranslateY(newPosY + this.editor.paneHolder.getHeight() / 2);
                }
                else if (!posYStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element Y position because the entered field is non-numeric!");
                }
            }

        }
    }

    public boolean PropsLoadSymbol(LinkedList<Paint> colors, LinkedList<Double> strokeWidths, LinkedList<Paint> strokes, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<String> pinLabels)
    {
        if (!this.selected)
            return false;

        colors.add(this.line.getStroke());

        lineEndPosX.add(this.line.getEndX());
        lineEndPosY.add(this.line.getEndY());
        lineWidths.add(this.line.getStrokeWidth());

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
                    this.line.setStroke(color);
                }
                else
                {
                    if (color != null)
                        EDAmameController.SetStatusBar("Unable to apply shape colors because the entered color is transparent!");
                }
            }
        }

        // Applying lines...
        {
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

                    if ((newWidth >= EDAmameController.EditorSymbol_LineWidthMin) && (newWidth <= EDAmameController.EditorSymbol_LineWidthMax))
                        this.line.setStrokeWidth(newWidth);
                    else
                        EDAmameController.SetStatusBar("Unable to apply line widths because the entered field is outside the limits! (Width limits: " + EDAmameController.EditorSymbol_LineWidthMin + ", " + EDAmameController.EditorSymbol_LineWidthMax + ")");
                }
                else if (!widthStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply line widths because the entered field is non-numeric!");
                }
            }
        }
    }

    public boolean PropsLoadFootprint(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        layers.add(this.line.getId());

        lineEndPosX.add(this.line.getEndX());
        lineEndPosY.add(this.line.getEndY());
        lineWidths.add(this.line.getStrokeWidth());

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

                    this.line.setId(layer);
                    this.line.setStroke(Editor.GetPCBLayerColor(layer));
                }
                else if (!layerChoiceBox.getValue().equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply layer because the entered field is invalid!");
                }
            }
        }

        // Applying lines...
        {
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

                    if ((newWidth >= EDAmameController.EditorSymbol_LineWidthMin) && (newWidth <= EDAmameController.EditorSymbol_LineWidthMax))
                        this.line.setStrokeWidth(newWidth);
                    else
                        EDAmameController.SetStatusBar("Unable to apply line widths because the entered field is outside the limits! (Width limits: " + EDAmameController.EditorSymbol_LineWidthMin + ", " + EDAmameController.EditorSymbol_LineWidthMax + ")");
                }
                else if (!widthStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply line widths because the entered field is non-numeric!");
                }
            }
        }
    }

    public boolean PropsLoadPCB(LinkedList<String> layers, LinkedList<Boolean> fills, LinkedList<Double> strokeWidths, LinkedList<Double> circlesRadii, LinkedList<Double> rectsWidths, LinkedList<Double> rectsHeights, LinkedList<Double> trisLens, LinkedList<Double> lineEndPosX, LinkedList<Double> lineEndPosY, LinkedList<Double> lineWidths, LinkedList<String> textContents, LinkedList<Double> textFontSizes, LinkedList<Double> holeOuterRadii, LinkedList<Double> holeInnerRadii, LinkedList<Double> viaRadii)
    {
        if (!this.selected)
            return false;

        layers.add(this.line.getId());

        lineEndPosX.add(this.line.getEndX());
        lineEndPosY.add(this.line.getEndY());
        lineWidths.add(this.line.getStrokeWidth());

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

                    this.line.setId(layer);
                    this.line.setStroke(Editor.GetPCBLayerColor(layer));
                }
                else if (!layerChoiceBox.getValue().equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply layer because the entered field is invalid!");
                }
            }
        }

        // Applying lines...
        {
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

                    if ((newWidth >= EDAmameController.EditorSymbol_LineWidthMin) && (newWidth <= EDAmameController.EditorSymbol_LineWidthMax))
                        this.line.setStrokeWidth(newWidth);
                    else
                        EDAmameController.SetStatusBar("Unable to apply line widths because the entered field is outside the limits! (Width limits: " + EDAmameController.EditorSymbol_LineWidthMin + ", " + EDAmameController.EditorSymbol_LineWidthMax + ")");
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
        return new EDALine(this.name, (Line)EDANode.NodeClone(this.line), !this.snapPoints.isEmpty(), this.passive, this.editor);
    }

    public String ToGerberStr(String layerName)
    {
        return EDANode.NodeToGerberStr(this.line, layerName, this.editor);
    }
}
