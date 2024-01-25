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

public class EDAGroup extends EDANode
{
    //// GLOBAL VARIABLES ////

    public Group group;

    //// CONSTRUCTORS ////

    public EDAGroup(String nameValue, Group nodeValue, LinkedList<PairMutable> snapPointPos, boolean passiveValue, Editor editorValue)
    {
        if (editorValue == null)
            throw new java.lang.Error("ERROR: Attempting to create an EDAGroup \"" + nameValue + "\" without a supplied editor!");

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

    //// SETTERS ////

    public void SetTranslate(PairMutable pos)
    {
        this.group.setTranslateX(pos.GetLeftDouble());
        this.group.setTranslateY(pos.GetRightDouble());
    };

    public void SetRotate(double rot)
    {
        this.group.setRotate(rot);
    };

    public void SetVisible(boolean visible)
    {
        this.group.setVisible(visible);
    };

    //// GETTERS ////

    public Node GetNode()
    {
        return this.group;
    }

    public PairMutable GetTranslate()
    {
        return new PairMutable(this.group.getTranslateX(), this.group.getTranslateY());
    };

    public double GetRotate()
    {
        return this.group.getRotate();
    };

    public Bounds GetBoundsLocal()
    {
        return this.group.getBoundsInLocal();
    };

    public Bounds GetBoundsParent()
    {
        return this.group.getBoundsInParent();
    };

    //// EDITOR FUNCTIONS ////

    public void Rotate(double deltaY)
    {
        if (!this.selected)
            return;

        double angle = 10;

        if (deltaY < 0)
            angle = -10;

        this.group.setRotate(this.group.getRotate() + angle);
    }

    public void NodeAdd()
    {
        this.editor.paneHolder.getChildren().add(1, this.group);
    };

    public void NodeRemove()
    {
        this.editor.paneHolder.getChildren().remove(this.group);
    };

    //// POSITION FUNCTIONS ////

    public PairMutable BoundsPosToHolderPane(PairMutable posOffset)
    {
        Bounds boundsLocal = this.group.getBoundsInLocal();
        PairMutable boundsRealEdgeL = this.PosToHolderPane(new PairMutable(boundsLocal.getMinX() + posOffset.GetLeftDouble(), boundsLocal.getMinY() + posOffset.GetRightDouble()));
        PairMutable boundsRealEdgeH = this.PosToHolderPane(new PairMutable(boundsLocal.getMaxX() + posOffset.GetLeftDouble(), boundsLocal.getMaxY() + posOffset.GetRightDouble()));

        return new PairMutable((boundsRealEdgeL.GetLeftDouble() + boundsRealEdgeH.GetLeftDouble()) / 2,
                               (boundsRealEdgeL.GetRightDouble() + boundsRealEdgeH.GetRightDouble()) / 2);
    }

    public PairMutable PosToHolderPane(PairMutable pos)
    {
        Point2D newPos = this.group.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public boolean PosOnNode(PairMutable pos)
    {
        return this.group.getBoundsInParent().contains(new Point2D(pos.GetLeftDouble(), pos.GetRightDouble()));
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
                throw new java.lang.Error("ERROR: Encountered unrecognized snap point when refreshing EDAGroup snap points!");
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
        Bounds boundsLocal = this.GetBoundsLocal();

        names.add(this.name);
        posX.add(pos.GetLeftDouble() - boundsLocal.getWidth() / 2);
        posY.add(pos.GetRightDouble() - boundsLocal.getHeight() / 2);

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

                    this.group.setTranslateX(newPosX + this.editor.paneHolder.getWidth() / 2);
                }
                else if (!posXStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                }

                if (EDAmameController.IsStringNum(posYStr))
                {
                    Double newPosY = Double.parseDouble(posYStr);

                    this.group.setTranslateY(newPosY + this.editor.paneHolder.getHeight() / 2);
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
                HBox rotBox = (HBox)propsBox.getChildren().get(rotBoxIdx);
                TextField rotText = (TextField)EDAmameController.GetNodeById(rotBox.getChildren(), "rot");

                if (rotText == null)
                    throw new java.lang.Error("ERROR: Unable to find \"rot\" node in global properties window \"rotBox\" entry!");

                String rotStr = rotText.getText();

                if (EDAmameController.IsStringNum(rotStr))
                {
                    this.SetRotate(Double.parseDouble(rotStr));
                }
                else if (!rotStr.equals("<mixed>"))
                {
                    EDAmameController.SetStatusBar("Unable to apply element rotation because the entered field is non-numeric!");
                }
            }
        }
    }

    public void PropsApplySymbol(VBox propsBox) {}

    //// SUPPORT FUNCTIONS ////

    public EDANode Clone()
    {
        LinkedList<PairMutable> clonedSnapPoints = new LinkedList<PairMutable>();

        for (int i = 0; i < this.snapPoints.size(); i++)
            clonedSnapPoints.add(new PairMutable(this.snapPoints.get(i).getTranslateX(), this.snapPoints.get(i).getTranslateY()));

        return new EDAGroup(this.name, (Group)EDANode.NodeClone(this.group), clonedSnapPoints, this.passive, this.editor);
    }
}
