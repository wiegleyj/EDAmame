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
import com.cyte.edamame.util.Utils;
import com.cyte.edamame.netlist.NetListExperimental;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Editor for developing schematics
 */
public class EditorSchematic extends Editor
{
    //// GLOBAL VARIABLES ////

    @FXML
    private Button innerButton;
    @FXML
    public ToggleGroup toggleGroup;
    @FXML
    public TextField wireWidth;
    @FXML
    public ColorPicker wireColor;

    //// MAIN FUNCTIONS ////

    public static Editor Create() throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(EDAmame.class.getResource("fxml/EditorSchematic.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        EditorSchematic editor = fxmlLoader.getController();
        editor.Init(1, "EditorSchematic");
        editor.Dissect(1, scene);
        editor.CanvasRenderGrid();
        editor.ListenersInit();

        Editor.TextFieldListenerInit(editor.wireWidth);

        return editor;
    }

    @FXML
    public void initialize()
    {
        System.out.println("I was initialized, the button was " + this.innerButton);
    }

    //// CALLBACK FUNCTIONS ////

    @FXML
    public void Save()
    {
        NetListExperimental<String> netList = this.ToNetList();

        File.Write("C:\\Users\\SVARUN\\Downloads\\netlist.txt", netList.ToString(), true);
    }

    @FXML
    public void Load()
    {
        System.out.println("Loading schematic!");
    }

    @FXML
    public void LoadSymbol()
    {
        LinkedList<Node> nodes = File.NodesLoad(true);

        if ((nodes == null) || nodes.isEmpty())
            return;

        Group symbol = new Group();
        PairMutable dropPos = this.PaneHolderGetRealCenter();

        symbol.setTranslateX(dropPos.GetLeftDouble());
        symbol.setTranslateY(dropPos.GetRightDouble());

        symbol.getChildren().addAll(nodes);

        LinkedList<PairMutable> snapPointPos = null;

        for (int i = 0; i < symbol.getChildren().size(); i++)
        {
            Node node = symbol.getChildren().get(i);

            if (node.getClass() == Group.class)
            {
                if (snapPointPos == null)
                    snapPointPos = new LinkedList<PairMutable>();

                Group group = (Group)node;

                for (int j = 0; j < group.getChildren().size(); j++)
                {
                    Node currChild = group.getChildren().get(j);

                    if (currChild.getClass() == Circle.class)
                        snapPointPos.add(EDANode.GetPosInNodeParent(group, new PairMutable(currChild.getTranslateX(), currChild.getTranslateY())));
                }
            }
        }

        EDAGroup symbolNode = new EDAGroup("Symbol", symbol, snapPointPos, false, this);
        symbolNode.Add();
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
        PairMutable dropPos = this.PanePosListenerToHolder(new PairMutable(event.getX(), event.getY()));
        dropPos = this.MagneticSnapCheck(dropPos);
        PairMutable realPos = this.PaneHolderGetRealPos(dropPos);

        // Handling element dropping (only if we're not hovering over, selecting, moving any shapes or box selecting)
        if ((this.shapesSelected == 0) &&
                !this.shapesMoving &&
                (this.selectionBox == null) &&
                !this.shapesWereSelected &&
                !this.wasSelectionBox)
        {
            RadioButton selectedShapeButton = (RadioButton) toggleGroup.getSelectedToggle();

            // Only dropping the element within the theater limits...
            if (selectedShapeButton != null)
            {
                if (!selectedShapeButton.getText().equals("Wire"))
                    this.linePreview = null;

                boolean lineStarted = false;

                if (this.shapesHighlighted == 0)
                {
                    if (selectedShapeButton.getText().equals("Wire"))
                    {
                        // If we're starting the line drawing...
                        if (this.linePreview == null)
                        {
                            String stringWidth = this.wireWidth.getText();
                            Color color = this.wireColor.getValue();

                            if (EDAmameController.IsStringNum(stringWidth))
                            {
                                double width = Double.parseDouble(stringWidth);

                                if (((width >= EDAmameController.Editor_WireWidthMin) && (width <= EDAmameController.Editor_WireWidthMax)))
                                {
                                    if ((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                                    {
                                        this.linePreview = new Line();

                                        this.linePreview.setStartX(dropPos.GetLeftDouble());
                                        this.linePreview.setStartY(dropPos.GetRightDouble());
                                        this.linePreview.setEndX(dropPos.GetLeftDouble());
                                        this.linePreview.setEndY(dropPos.GetRightDouble());

                                        this.linePreview.setStrokeWidth(width);
                                        this.linePreview.setStroke(color);

                                        EDALine lineNode = new EDALine("linePreview", this.linePreview, true, this);
                                        lineNode.Add();

                                        lineStarted = true;
                                    }
                                    else
                                    {
                                        EDAmameController.SetStatusBar("Unable to drop wire because the entered color field is transparent!");
                                    }
                                }
                                else
                                {
                                    EDAmameController.SetStatusBar("Unable to drop wire because the entered width is outside the limits! (Width limits: " + EDAmameController.Editor_WireWidthMin + ", " + EDAmameController.Editor_WireWidthMax + ")");
                                }
                            }
                            else
                            {
                                EDAmameController.SetStatusBar("Unable to drop wire because the entered width field is non-numeric!");
                            }
                        }
                    }
                    else
                    {
                        throw new java.lang.Error("ERROR: Attempt to drop an element shape in a Schematic Editor!");
                    }
                }

                if (selectedShapeButton.getText().equals("Wire"))
                {
                    // If we're finishing the line drawing...
                    if ((this.linePreview != null) && !lineStarted)
                    {
                        PairMutable posStart = new PairMutable(this.linePreview.getStartX(), this.linePreview.getStartY());
                        PairMutable posEnd = new PairMutable(dropPos.GetLeftDouble(), dropPos.GetRightDouble());

                        Line line = new Line();
                        Editor.LineDropPosCalculate(line, posStart, posEnd);

                        line.setStroke(this.linePreview.getStroke());
                        line.setStrokeWidth(this.linePreview.getStrokeWidth());

                        this.LinePreviewRemove();

                        EDALine lineNode = new EDALine("Wire", line, false, this);
                        lineNode.Add();
                    }
                }
            }
        }
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
        Text globalHeader = new Text("Schematic Editor Settings:");
        globalHeader.setStyle("-fx-font-weight: bold;");
        globalHeader.setStyle("-fx-font-size: 16px;");
        EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(globalHeader);
        EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(new Separator());
    }

    public void SettingsApplySpecific()
    {}
}
