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
import com.cyte.edamame.util.PairMutable;
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
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.collections.*;

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
        editor.CanvasRenderGrid();
        editor.ListenersInit();

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
        LinkedList<Node> nodes = new LinkedList<Node>();

        for (int i = 0; i < this.nodes.size(); i++)
            nodes.add(this.nodes.get(i).GetNode());

        File.NodesSave(nodes, true);
    }

    @FXML
    public void Load()
    {
        LinkedList<Node> nodes = File.NodesLoad(true);

        if (nodes == null)
            return;

        for (int i = 0; i < nodes.size(); i++)
        {
            Node node = nodes.get(i);
            PairMutable realPos = this.PaneHolderGetDrawPos(new PairMutable(node.getTranslateX(), node.getTranslateY()));
            node.setTranslateX(realPos.GetLeftDouble());
            node.setTranslateY(realPos.GetRightDouble());

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
                LinkedList<PairMutable> snapPointPos = new LinkedList<PairMutable>();

                for (int j = 0; j < group.getChildren().size(); j++)
                {
                    Node currChild = group.getChildren().get(j);

                    if (currChild.getClass() == Circle.class)
                        snapPointPos.add(new PairMutable(currChild.getTranslateX(), currChild.getTranslateY()));
                }

                System.out.println("loadin hole or via!");
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
                                Circle circle = new Circle(Double.parseDouble(stringRadius));

                                if (this.circleFill.isSelected())
                                    circle.setFill(layerColor);
                                else
                                    circle.setFill(Color.TRANSPARENT);

                                circle.setTranslateX(dropPos.GetLeftDouble());
                                circle.setTranslateY(dropPos.GetRightDouble());

                                circle.setStroke(layerColor);
                                circle.setStrokeWidth(Double.parseDouble(stringStrokeSize));

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
                                Rectangle rectangle = new Rectangle(Double.parseDouble(stringWidth), Double.parseDouble(stringHeight));

                                if (this.rectangleFill.isSelected())
                                    rectangle.setFill(layerColor);
                                else
                                    rectangle.setFill(Color.TRANSPARENT);

                                rectangle.setTranslateX(dropPos.GetLeftDouble());
                                rectangle.setTranslateY(dropPos.GetRightDouble());

                                rectangle.setStroke(layerColor);
                                rectangle.setStrokeWidth(Double.parseDouble(stringStrokeSize));

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
                                triangle.getPoints().setAll(-middleLength / 2, middleLength / 2,
                                                            middleLength / 2, middleLength / 2,
                                                            0.0, -middleLength / 2);

                                if (this.triangleFill.isSelected())
                                    triangle.setFill(layerColor);
                                else
                                    triangle.setFill(Color.TRANSPARENT);

                                triangle.setTranslateX(dropPos.GetLeftDouble());
                                triangle.setTranslateY(dropPos.GetRightDouble());

                                triangle.setStroke(layerColor);
                                triangle.setStrokeWidth(Double.parseDouble(stringStrokeSize));

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

                                        this.linePreview.setStrokeWidth(width);
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
                                        text.setFont(new Font("Arial", fontSize));

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

                            System.out.println("droppin hole!");
                        }
                        else if (selectedShapeButton.getText().equals("Via"))
                        {
                            EDAmameController.SetStatusBar("EDAmame Status Area");

                            System.out.println("droppin via!");
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
    {}

    public void PropsApplySpecific()
    {}

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
    }
}
