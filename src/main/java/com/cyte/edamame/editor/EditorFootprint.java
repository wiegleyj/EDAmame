/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.cyte.edamame.EDAmame;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.file.File;
import com.cyte.edamame.node.*;
import com.cyte.edamame.misc.PairMutable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Editor for managing footprint libraries.
 */
public class EditorFootprint extends Editor
{
    //// GLOBAL VARIABLES ////

    @FXML
    public Button innerButton;
    @FXML
    public Circle cursorPreview;
    @FXML
    public ToggleGroup toggleGroup;
    @FXML
    public TextField circleRadius;
    @FXML
    public CheckBox circleFill;
    @FXML
    public TextField circleBorderSize;
    @FXML
    public TextField rectangleWidth;
    @FXML
    public TextField rectangleHeight;
    @FXML
    public CheckBox rectangleFill;
    @FXML
    public TextField rectangleBorderSize;
    @FXML
    public TextField triangleHeight;
    @FXML
    public CheckBox triangleFill;
    @FXML
    public TextField triangleBorderSize;
    @FXML
    public TextField lineWidth;
    @FXML
    public TextField textContent;
    @FXML
    public TextField textSize;
    @FXML
    public TextField holeRadiusOuter;
    @FXML
    public TextField holeRadiusInner;
    @FXML
    public TextField viaRadius;
    @FXML
    public ChoiceBox<String> layerBox;

    //// MAIN FUNCTIONS ////

    public static Editor Create() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorFootprint.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorFootprint editor = fxmlLoader.getController();
        editor.Init(2, "EditorFootprint");
        editor.Dissect(2, scene);
        editor.snapGridSpacing = EDAmameController.Editor_SnapGridSpacings[EDAmameController.Editor_SnapGridSpacings.length / 2];
        editor.CanvasDraw();
        editor.ListenersInit();

        Editor.TextFieldListenerInit(editor.circleRadius);
        Editor.TextFieldListenerInit(editor.circleBorderSize);
        Editor.TextFieldListenerInit(editor.rectangleWidth);
        Editor.TextFieldListenerInit(editor.rectangleHeight);
        Editor.TextFieldListenerInit(editor.rectangleBorderSize);
        Editor.TextFieldListenerInit(editor.triangleHeight);
        Editor.TextFieldListenerInit(editor.triangleBorderSize);
        Editor.TextFieldListenerInit(editor.lineWidth);
        Editor.TextFieldListenerInit(editor.textContent);
        Editor.TextFieldListenerInit(editor.textSize);
        Editor.TextFieldListenerInit(editor.holeRadiusOuter);
        Editor.TextFieldListenerInit(editor.holeRadiusInner);
        Editor.TextFieldListenerInit(editor.viaRadius);

        editor.cursorPreview.setRadius(EDAmameController.Editor_CursorPreviewRadius);
        editor.cursorPreview.setStroke(EDAmameController.Editor_GridPointColors[2]);
        editor.cursorPreview.setStrokeWidth(EDAmameController.Editor_CursorPreviewBorderWidth);

        for (int i = 0; i < EDAmameController.Editor_PCBLayers.length; i++)
            editor.layerBox.getItems().add(EDAmameController.Editor_PCBLayers[i]);

        editor.layerBox.setValue(EDAmameController.Editor_PCBLayers[0]);

        return editor;
    }

    @FXML
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + this.innerButton);
    }

    //// CURSOR FUNCTIONS ////

    public PairMutable CursorPreviewGetPos()
    {
        return new PairMutable(this.cursorPreview.getTranslateX(), this.cursorPreview.getTranslateY());
    }

    public void CursorPreviewUpdate(PairMutable pos)
    {
        PairMutable realPos = this.PaneHolderGetRealPos(this.PanePosListenerToHolder(pos));
        PairMutable snappedPos = this.PanePosHolderToListener(this.PaneHolderGetDrawPos(this.PosSnapToGridPoint(realPos)));

        this.cursorPreview.setTranslateX(snappedPos.GetLeftDouble());
        this.cursorPreview.setTranslateY(snappedPos.GetRightDouble());
    }

    //// CALLBACK FUNCTIONS ////

    @FXML
    public void Save()
    {
        if (this.nodes.isEmpty())
            return;

        LinkedList<Node> nodes = new LinkedList<Node>();

        for (int i = 0; i < this.nodes.size(); i++)
            nodes.add(this.nodes.get(i).GetNode());

        PairMutable groupSnappedPos = this.PaneHolderGetDrawPos(this.PosSnapToGridPoint(this.PaneHolderGetRealPos(EDANode.NodesGetMiddlePos(nodes))));

        System.out.println(groupSnappedPos.ToStringDouble());

        File.NodesSave(nodes, groupSnappedPos);
    }

    @FXML
    public void Load()
    {
        PairMutable groupPos = new PairMutable();
        LinkedList<Node> nodes = File.NodesLoad(groupPos);

        if (nodes == null)
            return;

        for (int i = 0; i < nodes.size(); i++)
        {
            Node node = nodes.get(i);

            node.setTranslateX(node.getTranslateX() + groupPos.GetLeftDouble());
            node.setTranslateY(node.getTranslateY() + groupPos.GetRightDouble());

            if (node.getClass() == Circle.class)
            {
                EDACircle circle = new EDACircle("Circle", (Circle)node, false, false, this);
                circle.Add();
            }
            else if (node.getClass() == Rectangle.class)
            {
                EDARectangle rectangle = new EDARectangle("Rectangle", (Rectangle)node, false, false, this);
                rectangle.Add();
            }
            else if (node.getClass() == Polygon.class)
            {
                EDATriangle triangle = new EDATriangle("Triangle", (Polygon)node, false, false, this);
                triangle.Add();
            }
            else if (node.getClass() == Line.class)
            {
                EDALine line = new EDALine("Line", (Line)node, false, false, this);
                line.Add();
            }
            else if (node.getClass() == Text.class)
            {
                EDAText text = new EDAText("Text", (Text)node, false, false, this);
                text.Add();
            }
            else if (node.getClass() == Group.class)
            {
                Group group = (Group)node;

                if ((group.getId() != null) && group.getId().equals("Through-Hole"))
                {
                    if (group.getChildren().size() != 1)
                        throw new java.lang.Error("ERROR: Attempting to load a hole into footprint editor without 1 child!");

                    EDAHole hole = new EDAHole("Through-Hole", group, false, this);
                    hole.Add();
                }
                else if ((group.getId() != null) && group.getId().equals("Via"))
                {
                    if (group.getChildren().size() != 2)
                        throw new java.lang.Error("ERROR: Attempting to load a via into footprint editor without 2 children!");

                    EDAVia via = new EDAVia("Via", group, false, this);
                    via.Add();
                }
                else
                {
                    throw new java.lang.Error("ERROR: Attempting to load unrecognized Group node into footprint editor!");
                }
            }
            else
            {
                throw new java.lang.Error("ERROR: Attempting to load a shape from a FXML file with unrecognized Node types!");
            }
        }
    }

    @FXML
    public void SettingsKeyPressed()
    {
        // Handling element properties window...
        if (EDAmameController.editorSettingsWindow == null)
        {
            // Attempting to create the properties window...
            EditorSettings settingsWindow = EditorSettings.Create(this);

            if (settingsWindow != null)
            {
                settingsWindow.stage.setOnHidden(e -> {
                    EDAmameController.editorSettingsWindow = null;
                });
                settingsWindow.stage.show();

                EDAmameController.editorSettingsWindow = settingsWindow;
                settingsWindow.SettingsLoad();
            }
        }
    }

    public void OnDragOverSpecific(DragEvent event)
    {}

    public void OnDragDroppedSpecific(DragEvent event)
    {}

    public void OnMouseMovedSpecific(MouseEvent event)
    {}

    public void OnMousePressedSpecific(MouseEvent event)
    {}

    public void OnMouseReleasedSpecific(MouseEvent event)
    {
        PairMutable dropPos = this.PanePosListenerToHolder(this.CursorPreviewGetPos());
        dropPos = this.MagneticSnapCheck(dropPos);
        PairMutable realPos = this.PaneHolderGetRealPos(dropPos);

        if (this.pressedLMB)
        {
            // Handling shape dropping (only if we're not hovering over, selecting, moving any shapes or box selecting)
            if (this.CanDropSomething())
            {
                RadioButton selectedShapeButton = (RadioButton) toggleGroup.getSelectedToggle();

                // Only dropping the shape within the theater limits...
                if (selectedShapeButton != null)
                {
                    if (!selectedShapeButton.getText().equals("Line"))
                        this.linePreview = null;

                    Color layerColor = Editor.GetPCBLayerColor(this.layerBox.getValue());
                    boolean lineStarted = false;

                    if (this.shapesHighlighted == 0)
                    {
                        if (selectedShapeButton.getText().equals("Circle"))
                        {
                            EDAmameController.SetStatusBar("EDAmame Status Area");

                            String stringRadius = this.circleRadius.getText();
                            String stringStrokeSize = this.circleBorderSize.getText();

                            if (CheckStringCircleBounds(stringRadius) && CheckStringBorderSize(stringStrokeSize))
                            {
                                Circle circle = new Circle(Double.parseDouble(stringRadius) * 10);

                                if (this.circleFill.isSelected())
                                    circle.setFill(layerColor);
                                else
                                    circle.setFill(Color.TRANSPARENT);

                                circle.setTranslateX(dropPos.GetLeftDouble());
                                circle.setTranslateY(dropPos.GetRightDouble());

                                circle.setStrokeType(StrokeType.INSIDE);
                                circle.setStroke(layerColor);
                                circle.setStrokeWidth(Double.parseDouble(stringStrokeSize) * 10);

                                circle.setId(this.layerBox.getValue());

                                EDACircle circleNode = new EDACircle("Circle", circle, false, false, this);
                                circleNode.Add();
                            }
                        }
                        else if (selectedShapeButton.getText().equals("Rectangle"))
                        {
                            EDAmameController.SetStatusBar("EDAmame Status Area");

                            String stringWidth = this.rectangleWidth.getText();
                            String stringHeight = this.rectangleHeight.getText();
                            String stringStrokeSize = this.rectangleBorderSize.getText();

                            if (CheckStringRectBounds(stringWidth, stringHeight) && CheckStringBorderSize(stringStrokeSize))
                            {
                                Rectangle rectangle = new Rectangle(Double.parseDouble(stringWidth) * 10, Double.parseDouble(stringHeight) * 10);

                                if (this.rectangleFill.isSelected())
                                    rectangle.setFill(layerColor);
                                else
                                    rectangle.setFill(Color.TRANSPARENT);

                                rectangle.setTranslateX(dropPos.GetLeftDouble());
                                rectangle.setTranslateY(dropPos.GetRightDouble());

                                rectangle.setStrokeType(StrokeType.INSIDE);
                                rectangle.setStroke(layerColor);
                                rectangle.setStrokeWidth(Double.parseDouble(stringStrokeSize) * 10);

                                rectangle.setId(this.layerBox.getValue());

                                EDARectangle rectangleNode = new EDARectangle("Rectangle", rectangle, false, false, this);
                                rectangleNode.Add();
                            }
                        }
                        else if (selectedShapeButton.getText().equals("Triangle"))
                        {
                            EDAmameController.SetStatusBar("EDAmame Status Area");

                            String stringMiddleHeight = this.triangleHeight.getText();
                            String stringStrokeSize = this.triangleBorderSize.getText();

                            if (CheckStringTriBounds(stringMiddleHeight) && CheckStringBorderSize(stringStrokeSize))
                            {
                                Polygon triangle = new Polygon();
                                double middleLength = Double.parseDouble(stringMiddleHeight);
                                triangle.getPoints().setAll(-middleLength / 2 * 10, middleLength / 2 * 10,
                                                            middleLength / 2 * 10, middleLength / 2 * 10,
                                                            0.0, -middleLength / 2 * 10);

                                if (this.triangleFill.isSelected())
                                    triangle.setFill(layerColor);
                                else
                                    triangle.setFill(Color.TRANSPARENT);

                                triangle.setTranslateX(dropPos.GetLeftDouble());
                                triangle.setTranslateY(dropPos.GetRightDouble());

                                triangle.setStrokeType(StrokeType.INSIDE);
                                triangle.setStroke(layerColor);
                                triangle.setStrokeWidth(Double.parseDouble(stringStrokeSize) * 10);

                                triangle.setId(this.layerBox.getValue());

                                EDATriangle triangleNode = new EDATriangle("Triangle", triangle, false, false, this);
                                triangleNode.Add();
                            }
                        }
                        else if (selectedShapeButton.getText().equals("Line"))
                        {
                            EDAmameController.SetStatusBar("EDAmame Status Area");

                            // If we're starting the line drawing...
                            if (this.linePreview == null)
                            {
                                String stringWidth = this.lineWidth.getText();

                                if (stringWidth.isEmpty() || !EDAmameController.IsStringNum(stringWidth))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop line because the entered width field is non-numeric!");
                                }
                                else
                                {
                                    double width = Double.parseDouble(stringWidth);

                                    if (!((width >= EDAmameController.EditorSymbol_LineWidthMin) && (width <= EDAmameController.EditorSymbol_LineWidthMax)))
                                    {
                                        EDAmameController.SetStatusBar("Unable to drop line because the entered width is outside the limits! (Width limits: " + EDAmameController.EditorSymbol_LineWidthMin + ", " + EDAmameController.EditorSymbol_LineWidthMax + ")");
                                    }
                                    else
                                    {
                                        this.linePreview = new Line();

                                        this.linePreview.setStartX(dropPos.GetLeftDouble());
                                        this.linePreview.setStartY(dropPos.GetRightDouble());
                                        this.linePreview.setEndX(dropPos.GetLeftDouble());
                                        this.linePreview.setEndY(dropPos.GetRightDouble());

                                        this.linePreview.setStrokeWidth(width * 10);
                                        this.linePreview.setStroke(layerColor);

                                        EDALine lineNode = new EDALine("linePreview", this.linePreview, false, true, this);
                                        lineNode.Add();

                                        lineStarted = true;
                                    }
                                }
                            }
                        }
                        else if (selectedShapeButton.getText().equals("Text"))
                        {
                            EDAmameController.SetStatusBar("EDAmame Status Area");

                            String stringTextContent = textContent.getText();

                            if (stringTextContent.isEmpty())
                            {
                                EDAmameController.SetStatusBar("Unable to drop text because the entered text field is empty!");
                            }
                            else
                            {
                                String stringFontSize = this.textSize.getText();

                                if (stringFontSize.isEmpty() || !EDAmameController.IsStringNum(stringFontSize))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop text because the entered font size field is non-numeric!");
                                }
                                else
                                {
                                    double fontSize = Double.parseDouble(stringFontSize);

                                    if (!((fontSize >= EDAmameController.EditorSymbol_TextFontSizeMin) && (fontSize <= EDAmameController.EditorSymbol_TextFontSizeMax)))
                                    {
                                        EDAmameController.SetStatusBar("Unable to drop text because the entered font size field is outside the limits! (Font size limits: " + EDAmameController.EditorSymbol_TextFontSizeMin + ", " + EDAmameController.EditorSymbol_TextFontSizeMax + ")");
                                    }
                                    else
                                    {
                                        Text text = new Text(stringTextContent);
                                        text.setFont(new Font("Arial", fontSize * 10));

                                        text.setFill(layerColor);

                                        text.setTranslateX(dropPos.GetLeftDouble());
                                        text.setTranslateY(dropPos.GetRightDouble());

                                        text.setId(this.layerBox.getValue());

                                        EDAText textNode = new EDAText("Text", text, false, false, this);
                                        textNode.Add();
                                    }
                                }
                            }
                        }
                        else if (selectedShapeButton.getText().equals("Through-Hole"))
                        {
                            EDAmameController.SetStatusBar("EDAmame Status Area");

                            String stringRadiusOuter = this.holeRadiusOuter.getText();

                            if (stringRadiusOuter.isEmpty() || !EDAmameController.IsStringNum(stringRadiusOuter))
                            {
                                EDAmameController.SetStatusBar("Unable to drop hole because the entered outer radius field is non-numeric!");
                            }
                            else
                            {
                                double radiusOuter = Double.parseDouble(stringRadiusOuter);

                                if (!((radiusOuter >= EDAmameController.EditorFootprint_HoleRadiusOuterMin) && (radiusOuter <= EDAmameController.EditorFootprint_HoleRadiusOuterMax)))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop hole because the entered outer radius field is outside the limits! (Outer radius limits: " + EDAmameController.EditorFootprint_HoleRadiusOuterMin + ", " + EDAmameController.EditorFootprint_HoleRadiusOuterMax + ")");
                                }
                                else
                                {
                                    String stringRadiusInner = this.holeRadiusInner.getText();

                                    if (stringRadiusInner.isEmpty() || !EDAmameController.IsStringNum(stringRadiusInner))
                                    {
                                        EDAmameController.SetStatusBar("Unable to drop hole because the entered inner radius field is non-numeric!");
                                    }
                                    else
                                    {
                                        double radiusInner = Double.parseDouble(stringRadiusInner);

                                        if (!((radiusInner >= EDAmameController.EditorFootprint_HoleRadiusInnerMin) && (radiusInner <= EDAmameController.EditorFootprint_HoleRadiusInnerMax)))
                                        {
                                            EDAmameController.SetStatusBar("Unable to drop hole because the entered inner radius field is outside the limits! (Inner radius limits: " + EDAmameController.EditorFootprint_HoleRadiusInnerMin + ", " + EDAmameController.EditorFootprint_HoleRadiusInnerMax + ")");
                                        }
                                        else
                                        {
                                            if (radiusOuter <= radiusInner)
                                            {
                                                EDAmameController.SetStatusBar("Unable to drop hole because the entered outer radius field is smaller than or equal to the entered inner radius field!");
                                            }
                                            else
                                            {
                                                Group hole = new Group();

                                                Circle holeCircle = new Circle();
                                                holeCircle.setFill(Color.TRANSPARENT);
                                                holeCircle.setRadius(radiusOuter * 10);
                                                holeCircle.setStrokeType(StrokeType.INSIDE);
                                                holeCircle.setStrokeWidth((radiusOuter - radiusInner) * 10);
                                                holeCircle.setStroke(EDAmameController.Editor_PCBExposedColor);

                                                hole.getChildren().add(holeCircle);

                                                hole.setTranslateX(dropPos.GetLeftDouble());
                                                hole.setTranslateY(dropPos.GetRightDouble());

                                                hole.setId("Through-Hole");

                                                EDAHole holeNode = new EDAHole("Hole", hole, false, this);
                                                holeNode.Add();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if (selectedShapeButton.getText().equals("Via"))
                        {
                            EDAmameController.SetStatusBar("EDAmame Status Area");

                            String stringRadius = this.viaRadius.getText();

                            if (stringRadius.isEmpty() || !EDAmameController.IsStringNum(stringRadius))
                            {
                                EDAmameController.SetStatusBar("Unable to drop via because the entered radius field is non-numeric!");
                            }
                            else
                            {
                                double radius = Double.parseDouble(stringRadius);

                                if (!((radius >= EDAmameController.EditorFootprint_ViaRadiusMin) && (radius <= EDAmameController.EditorFootprint_ViaRadiusMax)))
                                {
                                    EDAmameController.SetStatusBar("Unable to drop via because the entered radius field is outside the limits! (Radius limits: " + EDAmameController.EditorFootprint_ViaRadiusMin + ", " + EDAmameController.EditorFootprint_ViaRadiusMax + ")");
                                }
                                else
                                {
                                    Group via = new Group();

                                    Circle viaOuterCircle = new Circle();
                                    viaOuterCircle.setFill(EDAmameController.Editor_PCBExposedColor);
                                    viaOuterCircle.setRadius(radius * 10);
                                    viaOuterCircle.setStrokeType(StrokeType.INSIDE);
                                    viaOuterCircle.setStrokeWidth(0);
                                    viaOuterCircle.setStroke(Color.TRANSPARENT);

                                    Circle viaInnerCircle = new Circle();
                                    viaInnerCircle.setFill(EDAmameController.Editor_PCBViaColor);
                                    viaInnerCircle.setRadius(radius / 2 * 10);
                                    viaInnerCircle.setStrokeType(StrokeType.INSIDE);
                                    viaInnerCircle.setStrokeWidth(0);
                                    viaInnerCircle.setStroke(Color.TRANSPARENT);

                                    via.getChildren().add(viaOuterCircle);
                                    via.getChildren().add(viaInnerCircle);

                                    via.setTranslateX(dropPos.GetLeftDouble());
                                    via.setTranslateY(dropPos.GetRightDouble());

                                    via.setId("Via");

                                    EDAVia viaNode = new EDAVia("Via", via, false, this);
                                    viaNode.Add();
                                }
                            }
                        }
                        else
                        {
                            throw new java.lang.Error("ERROR: Attempt to drop an unrecognized shape in a Footprint Editor!");
                        }
                    }

                    if (selectedShapeButton.getText().equals("Line"))
                    {
                        // If we're finishing the line drawing...
                        if ((this.linePreview != null) && !lineStarted)
                        {
                            PairMutable posStart = new PairMutable(this.linePreview.getStartX(), this.linePreview.getStartY());
                            PairMutable posEnd = new PairMutable(dropPos.GetLeftDouble(), dropPos.GetRightDouble());

                            if (!posStart.EqualsDouble(posEnd))
                            {
                                Line line = new Line();
                                Editor.LineDropPosCalculate(line, posStart, posEnd);

                                line.setStroke(this.linePreview.getStroke());
                                line.setStrokeWidth(this.linePreview.getStrokeWidth());

                                this.LinePreviewRemove();

                                line.setId(this.layerBox.getValue());

                                EDALine lineNode = new EDALine("Line", line, false, false, this);
                                lineNode.Add();
                            }
                        }
                    }
                }
            }

            this.shapesWereSelected = false;
        }
        else if (this.pressedRMB)
        {}
    }

    public void OnMouseDraggedSpecific(MouseEvent event)
    {}

    public void OnScrollSpecific(ScrollEvent event)
    {}

    public void OnKeyPressedSpecific(KeyEvent event)
    {}

    public void OnKeyReleasedSpecific(KeyEvent event)
    {}

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void PropsLoadSpecific()
    {
        if (this.shapesSelected == 0)
            return;

        boolean needHeader = false;

        // Reading all shape type properties...
        LinkedList<String> layers = new LinkedList<String>();
        LinkedList<Boolean> fills = new LinkedList<Boolean>();
        LinkedList<Double> strokeWidths = new LinkedList<Double>();
        LinkedList<Double> circlesRadii = new LinkedList<Double>();
        LinkedList<Double> rectsWidths = new LinkedList<Double>();
        LinkedList<Double> rectsHeights = new LinkedList<Double>();
        LinkedList<Double> trisLens = new LinkedList<Double>();
        LinkedList<Double> lineEndPosX = new LinkedList<Double>();
        LinkedList<Double> lineEndPosY = new LinkedList<Double>();
        LinkedList<Double> lineWidths = new LinkedList<Double>();
        LinkedList<String> textContents = new LinkedList<String>();
        LinkedList<Double> textFontSizes = new LinkedList<Double>();
        LinkedList<Double> holeOuterRadii = new LinkedList<Double>();
        LinkedList<Double> holeInnerRadii = new LinkedList<Double>();
        LinkedList<Double> viaRadii = new LinkedList<Double>();

        for (int i = 0; i < this.nodes.size(); i++)
        {
            boolean shapeNeedHeader = this.nodes.get(i).PropsLoadFootprint(layers, fills, strokeWidths, circlesRadii, rectsWidths, rectsHeights, trisLens, lineEndPosX, lineEndPosY, lineWidths, textContents, textFontSizes, holeOuterRadii, holeInnerRadii, viaRadii);
            needHeader = needHeader || shapeNeedHeader;
        }

        // Creating header...
        if (needHeader)
        {
            Text shapeHeader = new Text("Footprint Editor Properties:");
            shapeHeader.setStyle("-fx-font-weight: bold;");
            shapeHeader.setStyle("-fx-font-size: 16px;");
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(shapeHeader);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating layer box...
        if (!layers.isEmpty())
        {
            HBox layerHBox = new HBox(10);
            layerHBox.setId("layerBox");
            layerHBox.getChildren().add(new Label("Layer: "));
            ChoiceBox<String> layerBox = new ChoiceBox<String>();

            for (int i = 0; i < EDAmameController.Editor_PCBLayers.length; i++)
                layerBox.getItems().add(EDAmameController.Editor_PCBLayers[i]);

            layerBox.setId("layers");
            layerHBox.getChildren().add(layerBox);

            if (EDAmameController.IsListAllEqual(layers))
                layerBox.setValue(layers.get(0));
            else
                layerBox.setValue("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(layerHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating fill box...
        if (!fills.isEmpty())
        {
            HBox fillHBox = new HBox(10);
            fillHBox.setId("fillBox");
            fillHBox.getChildren().add(new Label("Fill: "));
            CheckBox fillBox = new CheckBox();
            fillBox.setId("fills");
            fillHBox.getChildren().add(fillBox);

            if (EDAmameController.IsListAllEqual(fills))
                fillBox.setSelected(fills.get(0));
            else
                fillBox.setSelected(false);

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(fillHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating border box...
        if (!strokeWidths.isEmpty())
        {
            HBox strokesWidthHBox = new HBox(10);
            strokesWidthHBox.setId("strokesWidthBox");
            strokesWidthHBox.getChildren().add(new Label("Shape Borders: "));
            TextField strokesWidthTextBox = new TextField();
            strokesWidthTextBox.setId("strokeWidth");
            strokesWidthHBox.getChildren().add(strokesWidthTextBox);

            if (EDAmameController.IsListAllEqual(strokeWidths))
                strokesWidthTextBox.setText(Double.toString(strokeWidths.get(0)));
            else
                strokesWidthTextBox.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(strokesWidthHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating circle radius box...
        if (!circlesRadii.isEmpty())
        {
            HBox circleHBox = new HBox(10);
            circleHBox.setId("circleBox");
            circleHBox.getChildren().add(new Label("Circle Radii: "));
            TextField radiusText = new TextField();
            radiusText.setId("circleRadii");
            circleHBox.getChildren().add(radiusText);

            if (EDAmameController.IsListAllEqual(circlesRadii))
                radiusText.setText(Double.toString(circlesRadii.get(0)));
            else
                radiusText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(circleHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating rectangle width & height box...
        if (!rectsWidths.isEmpty() && !rectsHeights.isEmpty())
        {
            HBox rectHBox = new HBox(10);
            rectHBox.setId("rectBox");
            rectHBox.getChildren().add(new Label("Rectangle Widths: "));
            TextField widthText = new TextField();
            widthText.setMinWidth(100);
            widthText.setPrefWidth(100);
            widthText.setMaxWidth(100);
            widthText.setId("rectWidths");
            rectHBox.getChildren().add(widthText);
            rectHBox.getChildren().add(new Label("Heights: "));
            TextField heightText = new TextField();
            heightText.setId("rectHeights");
            heightText.setMinWidth(100);
            heightText.setPrefWidth(100);
            heightText.setMaxWidth(100);
            rectHBox.getChildren().add(heightText);

            if (EDAmameController.IsListAllEqual(rectsWidths))
                widthText.setText(Double.toString(rectsWidths.get(0)));
            else
                widthText.setText("<mixed>");

            if (EDAmameController.IsListAllEqual(rectsHeights))
                heightText.setText(Double.toString(rectsHeights.get(0)));
            else
                heightText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(rectHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating triangle lengths box...
        if (!trisLens.isEmpty())
        {
            HBox triLenHBox = new HBox(10);
            triLenHBox.setId("triBox");
            triLenHBox.getChildren().add(new Label("Triangle Lengths: "));
            TextField triLenText = new TextField();
            triLenText.setId("triLens");
            triLenHBox.getChildren().add(triLenText);

            if (EDAmameController.IsListAllEqual(trisLens))
                triLenText.setText(Double.toString(trisLens.get(0)));
            else
                triLenText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(triLenHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating line box...
        if (!lineEndPosX.isEmpty() && !lineEndPosY.isEmpty() && !lineWidths.isEmpty())
        {
            // End point
            HBox lineEndPointsHBox = new HBox(10);
            lineEndPointsHBox.setId("lineEndPointsBox");
            lineEndPointsHBox.getChildren().add(new Label("Line End Points X: "));
            TextField lineEndPointsXText = new TextField();
            lineEndPointsXText.setMinWidth(100);
            lineEndPointsXText.setPrefWidth(100);
            lineEndPointsXText.setMaxWidth(100);
            lineEndPointsXText.setId("lineEndPointsX");
            lineEndPointsHBox.getChildren().add(lineEndPointsXText);
            lineEndPointsHBox.getChildren().add(new Label("Y: "));
            TextField lineEndPointsYText = new TextField();
            lineEndPointsYText.setId("lineEndPointsY");
            lineEndPointsYText.setMinWidth(100);
            lineEndPointsYText.setPrefWidth(100);
            lineEndPointsYText.setMaxWidth(100);
            lineEndPointsHBox.getChildren().add(lineEndPointsYText);

            if (EDAmameController.IsListAllEqual(lineEndPosX))
                lineEndPointsXText.setText(Double.toString(lineEndPosX.get(0)));
            else
                lineEndPointsXText.setText("<mixed>");

            if (EDAmameController.IsListAllEqual(lineEndPosY))
                lineEndPointsYText.setText(Double.toString(lineEndPosY.get(0)));
            else
                lineEndPointsYText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(lineEndPointsHBox);

            // Width
            HBox lineWidthsHBox = new HBox(10);
            lineWidthsHBox.setId("lineWidthsBox");
            lineWidthsHBox.getChildren().add(new Label("Line Widths: "));
            TextField lineWidthsText = new TextField();
            lineWidthsText.setId("lineWidths");
            lineWidthsHBox.getChildren().add(lineWidthsText);

            if (EDAmameController.IsListAllEqual(lineWidths))
                lineWidthsText.setText(Double.toString(lineWidths.get(0)));
            else
                lineWidthsText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(lineWidthsHBox);

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating text box...
        if (!textFontSizes.isEmpty())
        {
            HBox textContentHBox = new HBox(10);
            textContentHBox.setId("textContentBox");
            textContentHBox.getChildren().add(new Label("Text Contents: "));
            TextField textContentText = new TextField();
            textContentText.setId("textContent");
            textContentHBox.getChildren().add(textContentText);

            HBox textFontSizeHBox = new HBox(10);
            textFontSizeHBox.setId("fontSizeBox");
            textFontSizeHBox.getChildren().add(new Label("Text Font Sizes: "));
            TextField textFontSizeText = new TextField();
            textFontSizeText.setId("fontSize");
            textFontSizeHBox.getChildren().add(textFontSizeText);

            if (EDAmameController.IsListAllEqual(textContents))
                textContentText.setText(textContents.get(0));
            else
                textContentText.setText("<mixed>");

            if (EDAmameController.IsListAllEqual(textFontSizes))
                textFontSizeText.setText(Double.toString(textFontSizes.get(0)));
            else
                textFontSizeText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(textContentHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(textFontSizeHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating hole box...
        if (!holeOuterRadii.isEmpty() && !holeInnerRadii.isEmpty())
        {
            HBox holeHBox = new HBox(10);
            holeHBox.setId("holeBox");
            holeHBox.getChildren().add(new Label("Hole Outer Radii: "));
            TextField outerRadiiText = new TextField();
            outerRadiiText.setMinWidth(100);
            outerRadiiText.setPrefWidth(100);
            outerRadiiText.setMaxWidth(100);
            outerRadiiText.setId("holeOuterRadii");
            holeHBox.getChildren().add(outerRadiiText);
            holeHBox.getChildren().add(new Label("Inner Radii: "));
            TextField innerRadiiText = new TextField();
            innerRadiiText.setId("holeInnerRadii");
            innerRadiiText.setMinWidth(100);
            innerRadiiText.setPrefWidth(100);
            innerRadiiText.setMaxWidth(100);
            holeHBox.getChildren().add(innerRadiiText);

            if (EDAmameController.IsListAllEqual(holeOuterRadii))
                outerRadiiText.setText(Double.toString(holeOuterRadii.get(0)));
            else
                outerRadiiText.setText("<mixed>");

            if (EDAmameController.IsListAllEqual(holeInnerRadii))
                innerRadiiText.setText(Double.toString(holeInnerRadii.get(0)));
            else
                innerRadiiText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(holeHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }

        // Creating via box...
        if (!viaRadii.isEmpty())
        {
            HBox viaHBox = new HBox(10);
            viaHBox.setId("viaBox");
            viaHBox.getChildren().add(new Label("Via Radii: "));
            TextField radiusText = new TextField();
            radiusText.setId("viaRadii");
            viaHBox.getChildren().add(radiusText);

            if (EDAmameController.IsListAllEqual(viaRadii))
                radiusText.setText(Double.toString(viaRadii.get(0)));
            else
                radiusText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(viaHBox);
            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
        }
    }

    public void PropsApplySpecific()
    {
        if (this.shapesSelected == 0)
            return;

        VBox propsBox = EDAmameController.editorPropertiesWindow.propsBox;

        for (int i = 0; i < this.nodes.size(); i++)
            this.nodes.get(i).PropsApplyFootprint(propsBox);
    }

    //// SETTINGS WINDOW FUNCTIONS ////

    public void SettingsLoadSpecific()
    {
        Text globalHeader = new Text("Footprint Editor Settings:");
        globalHeader.setStyle("-fx-font-weight: bold;");
        globalHeader.setStyle("-fx-font-size: 16px;");
        EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(globalHeader);
        EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(new Separator());

        // Creating grid spacing distance box...
        {
            HBox gridSpacingBox = new HBox(10);
            gridSpacingBox.setId("gridSpacingBox");
            gridSpacingBox.getChildren().add(new Label("Grid Spacing Distance (mm): "));
            ChoiceBox<Double> gridSpacing = new ChoiceBox<Double>();

            for (int i = 0; i < EDAmameController.Editor_SnapGridSpacings.length; i++)
                gridSpacing.getItems().add(EDAmameController.Editor_SnapGridSpacings[i]);

            gridSpacing.setId("gridSpacing");
            gridSpacingBox.getChildren().add(gridSpacing);

            gridSpacing.setValue(this.snapGridSpacing);

            EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(gridSpacingBox);
        }
    }

    public void SettingsApplySpecific()
    {
        // Applying grid spacing distance...
        {
            Integer gridSpacingBoxIdx = EDAmameController.FindNodeById(EDAmameController.editorSettingsWindow.settingsBox.getChildren(), "gridSpacingBox");

            if (gridSpacingBoxIdx != -1)
            {
                HBox gridSpacingBox = (HBox)EDAmameController.editorSettingsWindow.settingsBox.getChildren().get(gridSpacingBoxIdx);
                ChoiceBox<Double> choiceBox = (ChoiceBox<Double>)EDAmameController.GetNodeById(gridSpacingBox.getChildren(), "gridSpacing");

                this.snapGridSpacing = choiceBox.getValue();
            }
        }

        this.CanvasRedraw();
    }
}
