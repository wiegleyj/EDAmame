/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.render.RenderNode;
import com.cyte.edamame.render.RenderSystem;

import java.util.*;
import java.util.logging.Level;

import com.cyte.edamame.util.PairMutable;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

import java.io.InvalidClassException;

/**
 * Librarys and projects are all modified through the use of special purpose editor modules.<p>
 *
 * All Controller_Editors are based on the abstract Editor class so that they conform with what the EDAmame main
 * application expects and will attempt to obtain and display editor specific controls for.
 *
 * @author Jeff Wiegley, Ph.D.
 * @author jeffrey.wiegley@gmail.com
 */
public abstract class Editor
{
    //// GLOBAL VARIABLES ////

    /** Every editor can be uniquely identified by a random UUID (which do not persist across application execution. */
    final public String Editor_ID = UUID.randomUUID().toString();
    public String Editor_Name = null;

    // DO NOT EDIT

    // holders for the UI elements. The UI elements are instantiated in a single FXML file/load
    // The individual elements are extracted to these for use by the EDAmame Application.
    /** The main Editor_Tab for EDAmame to include in its main Editor_Tab list. */
    protected Tab Editor_Tab = null;

    /** An optional ToolBar to provide EDAmame to include/append to its toolbars. */
    protected ToolBar Editor_ToolBar = null;

    /** a list of Editor_Tabs to include in the navigation Editor_Tab pane when this editor is active. (can be empty) */
    protected ObservableList<Tab> Editor_Tabs = FXCollections.observableArrayList();

    /**
     * A structure of menu items to include in EDAmame's Editor_Menus. Any menuitems associated with a string will
     * be inserted/visible under the menu with the same name in the EDAmame main menubar. The string must
     * match exactly including mneumonic underscores and such. Missing Editor_Menus at the EDAmame level are not created.
     */
    protected ObservableMap<String, ObservableList<MenuItem>> Editor_Menus = FXCollections.observableHashMap();

    public RenderSystem Editor_RenderSystem;

    // DO NOT EDIT

    public boolean Editor_Visible = false;
    public boolean Editor_PressedLMB = false;
    public boolean Editor_PressedRMB = false;

    //// MAIN FUNCTIONS ////

    public void Editor_Heartbeat()
    {
        // Handling centering of holder pane & crosshair...
        {
            PairMutable canvasSize = new PairMutable(this.Editor_RenderSystem.canvas.getWidth(),
                                                     this.Editor_RenderSystem.canvas.getHeight());
            PairMutable paneSize = new PairMutable(this.Editor_RenderSystem.paneListener.getWidth(),
                                                   this.Editor_RenderSystem.paneListener.getHeight());
            PairMutable centeredPos = new PairMutable(paneSize.GetLeftDouble() / 2 - canvasSize.GetLeftDouble() / 2,
                                                      paneSize.GetRightDouble() / 2 - canvasSize.GetRightDouble() / 2);

            this.Editor_RenderSystem.paneHolder.setLayoutX(centeredPos.GetLeftDouble());
            this.Editor_RenderSystem.paneHolder.setLayoutY(centeredPos.GetRightDouble());

            this.Editor_RenderSystem.crosshair.setTranslateX(this.Editor_RenderSystem.paneListener.getWidth() / 2);
            this.Editor_RenderSystem.crosshair.setTranslateY(this.Editor_RenderSystem.paneListener.getHeight() / 2);
        }
    }

    // ????
    /**
     * Request an editor to close. Handling any information/state saving as it needs.
     * @return true if the editor was able to close without unsaved information/state, false otherwise.
     */
    public boolean close()
    {
        return true;
    }

    //// GETTER FUNCTIONS ////

    /**
     * Returns the main editor Tab node for inclusion by EDAmame.
     * @return the main Editor_Tab for this editor.
     */
    public Tab Editor_GetTab() { return Editor_Tab; }

    /**
     * Returns the optional ToolBar of this editor for inclusion by EDAmame.
     * @return The ToolBar if available, null otherwise.
     */
    public ToolBar Editor_GetToolBar() { return Editor_ToolBar; }

    /**
     * Returns a list of control Editor_Tabs for EDAmame to include in the main controls Editor_Tab pane.
     * @return A (possibly empty) list of control Editor_Tabs.
     */
    public ObservableList<Tab> Editor_GetControlTabs() { return Editor_Tabs; }

    //// CALLBACK FUNCTIONS ////

    abstract public void Editor_ViewportOnDragOver(DragEvent event);
    abstract public void Editor_ViewportOnDragDropped(DragEvent event);
    abstract public void Editor_ViewportOnMouseMoved(MouseEvent event);
    abstract public void Editor_ViewportOnMousePressed(MouseEvent event);
    abstract public void Editor_ViewportOnMouseReleased(MouseEvent event);
    abstract public void Editor_ViewportOnMouseDragged(MouseEvent event);
    abstract public void Editor_ViewportOnScroll(ScrollEvent event);
    abstract public void Editor_ViewportOnKeyPressed(KeyEvent event);
    abstract public void Editor_ViewportOnKeyReleased(KeyEvent event);

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void Editor_PropsGlobalLoad()
    {
        Text globalHeader = new Text("Global Properties:");
        globalHeader.setStyle("-fx-font-weight: bold;");
        globalHeader.setStyle("-fx-font-size: 16px;");
        EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(globalHeader);
        EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());

        if (this.Editor_RenderSystem.shapesSelected == 0)
            return;

        // Reading all global node properties...
        LinkedList<Double> shapesPosX = new LinkedList<Double>();
        LinkedList<Double> shapesPosY = new LinkedList<Double>();
        LinkedList<Double> shapesRots = new LinkedList<Double>();
        LinkedList<String> textContents = new LinkedList<String>();
        LinkedList<Double> textFontSizes = new LinkedList<Double>();
        LinkedList<Color> textFontColors = new LinkedList<Color>();

        for (int i = 0; i < this.Editor_RenderSystem.nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.nodes.get(i);

            if (!renderNode.selected)
                continue;

            shapesPosX.add(renderNode.node.getTranslateX() - this.Editor_RenderSystem.paneHolder.getWidth() / 2);
            shapesPosY.add(renderNode.node.getTranslateY() - this.Editor_RenderSystem.paneHolder.getHeight() / 2);
            shapesRots.add(renderNode.node.getRotate());

            if (renderNode.node.getClass() == Label.class)
            {
                textContents.add(((Label)renderNode.node).getText());
                textFontSizes.add(((Label)renderNode.node).getFont().getSize());
                textFontColors.add((Color)((Label)renderNode.node).getTextFill());
            }
        }

        // Creating position box...
        {
            HBox posHBox = new HBox(10);
            posHBox.setId("posBox");
            posHBox.getChildren().add(new Label("Element Positions X: "));
            TextField posXText = new TextField();
            posXText.setMinWidth(100);
            posXText.setPrefWidth(100);
            posXText.setMaxWidth(100);
            posXText.setId("posX");
            posHBox.getChildren().add(posXText);
            posHBox.getChildren().add(new Label("Y: "));
            TextField posYText = new TextField();
            posYText.setId("posY");
            posYText.setMinWidth(100);
            posYText.setPrefWidth(100);
            posYText.setMaxWidth(100);
            posHBox.getChildren().add(posYText);

            if (EDAmameController.Controller_IsListAllEqual(shapesPosX))
                posXText.setText(Double.toString(shapesPosX.get(0)));
            else
                posXText.setText("<mixed>");

            if (EDAmameController.Controller_IsListAllEqual(shapesPosY))
                posYText.setText(Double.toString(shapesPosY.get(0)));
            else
                posYText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(posHBox);
        }

        // Creating rotation box...
        {
            HBox rotHBox = new HBox(10);
            rotHBox.setId("rotBox");
            rotHBox.getChildren().add(new Label("Element Rotations: "));
            TextField rotText = new TextField();
            rotText.setId("rot");
            rotHBox.getChildren().add(rotText);

            if (EDAmameController.Controller_IsListAllEqual(shapesRots))
                rotText.setText(Double.toString(shapesRots.get(0)));
            else
                rotText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(rotHBox);
        }

        // Creating text box...
        if (!textFontSizes.isEmpty() && !textFontColors.isEmpty())
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

            HBox textFontColorHBox = new HBox(10);
            textFontColorHBox.setId("fontColorBox");
            textFontColorHBox.getChildren().add(new Label("Text Font Colors: "));
            ColorPicker textFontColorPicker = new ColorPicker();
            textFontColorPicker.setId("fontColor");
            textFontColorHBox.getChildren().add(textFontColorPicker);

            if (EDAmameController.Controller_IsListAllEqual(textContents))
                textContentText.setText(textContents.get(0));
            else
                textContentText.setText("<mixed>");

            if (EDAmameController.Controller_IsListAllEqual(textFontSizes))
                textFontSizeText.setText(Double.toString(textFontSizes.get(0)));
            else
                textFontSizeText.setText("<mixed>");

            if (EDAmameController.Controller_IsListAllEqual(textFontColors))
                textFontColorPicker.setValue(textFontColors.get(0));
            else
                textFontColorPicker.setValue(Color.TRANSPARENT);

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(textContentHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(textFontSizeHBox);
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(textFontColorHBox);
        }

        EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
    }

    public void Editor_PropsGlobalApply()
    {
        if (this.Editor_RenderSystem.shapesSelected == 0)
            return;

        VBox propsBox = EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox;

        // Iterating over all the nodes & attempting to apply global node properties if selected...
        for (int i = 0; i < this.Editor_RenderSystem.nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.nodes.get(i);

            if (!renderNode.selected)
                continue;

            // Applying position...
            {
                Integer posBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "posBox");

                if (posBoxIdx != -1)
                {
                    HBox posBox = (HBox)propsBox.getChildren().get(posBoxIdx);
                    TextField posXText = (TextField)EDAmameController.Controller_GetNodeById(posBox.getChildren(), "posX");
                    TextField posYText = (TextField)EDAmameController.Controller_GetNodeById(posBox.getChildren(), "posY");

                    if (posXText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"posX\" node in global properties window \"posBox\" entry!");
                    if (posYText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"posY\" node in global properties window \"posBox\" entry!");

                    String posXStr = posXText.getText();
                    String posYStr = posYText.getText();

                    if (EDAmameController.Controller_IsStringNum(posXStr))
                    {
                        Double newPosX = Double.parseDouble(posXStr);

                        if ((newPosX >= -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2) &&
                            (newPosX <= EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2))
                        {
                            renderNode.node.setTranslateX(newPosX + this.Editor_RenderSystem.paneHolder.getWidth() / 2);
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply element X position because the entered field is outside the theater limits!");
                        }
                    }
                    else if (!posXStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                    }

                    if (EDAmameController.Controller_IsStringNum(posYStr))
                    {
                        Double newPosY = Double.parseDouble(posYStr);

                        if ((newPosY >= -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2) &&
                            (newPosY <= EDAmameController.Editor_TheaterSize.GetRightDouble() / 2))
                        {
                            renderNode.node.setTranslateY(newPosY + this.Editor_RenderSystem.paneHolder.getHeight() / 2);
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply element Y position because the entered field is outside the theater limits!");
                        }
                    }
                    else if (!posYStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply element Y position because the entered field is non-numeric!");
                    }
                }
            }

            // Applying rotation...
            {
                Integer rotBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "rotBox");

                if (rotBoxIdx != -1)
                {
                    HBox rotBox = (HBox)propsBox.getChildren().get(rotBoxIdx);
                    TextField rotText = (TextField)EDAmameController.Controller_GetNodeById(rotBox.getChildren(), "rot");

                    if (rotText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"rot\" node in global properties window \"rotBox\" entry!");

                    String rotStr = rotText.getText();

                    if (EDAmameController.Controller_IsStringNum(rotStr))
                    {
                        renderNode.node.setRotate(Double.parseDouble(rotStr));
                    }
                    else if (!rotStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply element rotation because the entered field is non-numeric!");
                    }
                }
            }

            // Applying text contents & fonts...
            if (renderNode.node.getClass() == Label.class)
            {
                Integer contentBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "textContentBox");

                if (contentBoxIdx != -1)
                {
                    HBox contentBox = (HBox)propsBox.getChildren().get(contentBoxIdx);
                    TextField contentText = (TextField)EDAmameController.Controller_GetNodeById(contentBox.getChildren(), "textContent");

                    if (contentText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"textContent\" node in global properties window \"textContentBox\" entry!");

                    String content = contentText.getText();

                    if (!content.isEmpty())
                    {
                        if (!content.equals("<mixed>"))
                            ((Label)renderNode.node).setText(content);
                    }
                    else
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply text contents because the entered field is empty!");
                    }
                }

                Integer fontSizeBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "fontSizeBox");

                if (fontSizeBoxIdx != -1)
                {
                    HBox fontSizeBox = (HBox)propsBox.getChildren().get(fontSizeBoxIdx);
                    TextField fontSizeText = (TextField)EDAmameController.Controller_GetNodeById(fontSizeBox.getChildren(), "fontSize");

                    if (fontSizeText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"fontSize\" node in global properties window \"fontSizeBox\" entry!");

                    String fontSizeStr = fontSizeText.getText();

                    if (EDAmameController.Controller_IsStringNum(fontSizeStr))
                    {
                        double fontSize = Double.parseDouble(fontSizeStr);

                        if (((fontSize >= EDAmameController.Editor_TextFontSizeMin) && (fontSize <= EDAmameController.Editor_TextFontSizeMax)))
                        {
                            ((Label)renderNode.node).setFont(new Font("Arial", fontSize));
                        }
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply text font size because the entered field is is outside the limits! (Font size limits: " + EDAmameController.Editor_TextFontSizeMin + ", " + EDAmameController.Editor_TextFontSizeMax + ")");
                        }
                    }
                    else if (!fontSizeStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply text font size because the entered field is non-numeric!");
                    }
                }

                Integer fontColorBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "fontColorBox");

                if (fontColorBoxIdx != -1)
                {
                    HBox fontColorBox = (HBox)propsBox.getChildren().get(fontColorBoxIdx);
                    ColorPicker fontColorPicker = (ColorPicker)EDAmameController.Controller_GetNodeById(fontColorBox.getChildren(), "fontColor");

                    if (fontColorPicker == null)
                        throw new java.lang.Error("ERROR: Unable to find \"fontColor\" node in Symbol Editor properties window \"fontColorBox\" entry!");

                    Color fontColor = fontColorPicker.getValue();

                    if ((fontColor != Color.TRANSPARENT) && (fontColor.hashCode() != 0x00000000))
                    {
                        ((Label)renderNode.node).setTextFill(fontColor);
                    }
                    else
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply text font color because the entered color is transparent!");
                    }
                }
            }

            this.Editor_RenderSystem.nodes.set(i, renderNode);
        }
    }

    abstract public void Editor_PropsSpecificLoad();
    abstract public void Editor_PropsSpecificApply();

    //// SUPPORT FUNCTIONS ////

    /** Editor_Dissect a controller into its component for delivery to EDAmame<p>
     *
     * The design of Controller_Editors is intended to be done through FXML and SceneBuilder. SceneBuilder doesn't
     * support multiple scenes in a single FXML. So, all required components are boxed into a single VBOX.
     * Once an editor factory has loaded an FXML file which
     * @param scene The scene to Editor_Dissect for expected UI elements.
     * @throws InvalidClassException if the expected UI scene organization is not found as expected.
     */
    protected void Editor_Dissect(Integer editorType, Scene scene) throws InvalidClassException
    {
        Node root = scene.getRoot();

        if (root == null)
            throw new InvalidClassException("root of scene is null");
        if (root.getClass() != VBox.class)
            throw new InvalidClassException("Expected VBox but found " + root.getClass());

        // Searching the scene for all the required elements
        Iterator<Node> nodeIterator = ((VBox)root).getChildren().iterator();
        String prefix = this.Editor_ID;
        Pane foundPaneListener = null;
        Pane foundPaneHolder = null;
        Pane foundPaneHighlights = null;
        Pane foundPaneSelections = null;
        Canvas foundCanvas = null;
        Shape foundCrosshair = null;

        while (nodeIterator.hasNext())
        {
            Node node = nodeIterator.next();

            if (node.getClass() == ToolBar.class)
            {
                EDAmameController.Controller_Logger.log(Level.INFO, "Dissecting a ToolBar in Editor \"" + Editor_ID + "\"...\n");

                Editor_ToolBar = (ToolBar) node;
                Editor_ToolBar.setVisible(false); // toolbar starts invisible. Becomes visible on Tab selection.
                Editor_ToolBar.setId(prefix + "_TOOLBAR");
            }
            else if (node.getClass() == MenuBar.class)
            {
                EDAmameController.Controller_Logger.log(Level.INFO, "Dissecting a MenuBar in Editor \"" + Editor_ID + "\"...\n");

                Iterator<Menu> menuIterator = ((MenuBar)node).getMenus().iterator();

                while (menuIterator.hasNext())
                {
                    Menu menu = menuIterator.next();

                    if (!Editor_Menus.containsKey(menu.getText()))
                        Editor_Menus.put(menu.getText(), FXCollections.observableArrayList());

                    List<MenuItem> itemlist = Editor_Menus.get(menu.getText());
                    Iterator<MenuItem> itemIterator = menu.getItems().iterator();

                    while (itemIterator.hasNext())
                    {
                        MenuItem item = itemIterator.next();
                        itemlist.add(item);
                        itemIterator.remove();
                    }

                    menuIterator.remove();
                }
            }
            else if (node.getClass() == TabPane.class)
            {
                EDAmameController.Controller_Logger.log(Level.INFO, "Dissecting a TabPane in Editor \"" + Editor_ID + "\"...\n");

                TabPane paneNode = (TabPane)node;
                Iterator<Tab> tabIterator = paneNode.getTabs().iterator();

                while (tabIterator.hasNext())
                {
                    Tab item = tabIterator.next();

                    // Processing the editor Editor_Tab
                    if (item.getText().equals("EditorTab"))
                    {
                        // Searching the editor Editor_Tab for the stack pane and the canvas
                        HBox editorBox = (HBox)item.getContent();

                        // Searching for the stack pane...
                        for (int i = 0; i < editorBox.getChildren().size(); i++)
                        {
                            Node nextNodeA = editorBox.getChildren().get(i);

                            if (nextNodeA.getClass() == StackPane.class)
                            {
                                StackPane foundStackPane = (StackPane)nextNodeA;

                                // Searching for the listener pane...
                                for (int j = 0; j < foundStackPane.getChildren().size(); j++)
                                {
                                    Node nextNodeB = foundStackPane.getChildren().get(j);

                                    if (nextNodeB.getClass() == Pane.class)
                                    {
                                        foundPaneListener = (Pane)nextNodeB;

                                        // Searching for the holder pane and the crosshair...
                                        for (int k = 0; k < foundPaneListener.getChildren().size(); k++)
                                        {
                                            Node nextNodeC = foundPaneListener.getChildren().get(k);

                                            if (nextNodeC.getClass() == Pane.class)
                                            {
                                                foundPaneHolder = (Pane)nextNodeC;

                                                // Searching for the highlight pane, selections pane and the canvas...
                                                for (int l = 0; l < foundPaneHolder.getChildren().size(); l++)
                                                {
                                                    Node nextNodeD = foundPaneHolder.getChildren().get(l);

                                                    if (nextNodeD.getClass() == Pane.class)
                                                    {
                                                        if (foundPaneSelections == null)
                                                        {
                                                            foundPaneSelections = (Pane)nextNodeD;

                                                            EDAmameController.Controller_Logger.log(Level.INFO, "Found selections pane of an editor with name \"" + this.Editor_Name + "\".\n");
                                                        }
                                                        else if (foundPaneHighlights == null)
                                                        {
                                                            foundPaneHighlights = (Pane)nextNodeD;

                                                            EDAmameController.Controller_Logger.log(Level.INFO, "Found highlights pane of an editor with name \"" + this.Editor_Name + "\".\n");
                                                        }
                                                    }
                                                    else if (nextNodeD.getClass() == Canvas.class)
                                                    {
                                                        foundCanvas = (Canvas)nextNodeD;

                                                        EDAmameController.Controller_Logger.log(Level.INFO, "Found canvas of an editor with name \"" + this.Editor_Name + "\".\n");
                                                    }
                                                }
                                            }
                                            else if (nextNodeC.getClass() == Circle.class)
                                            {
                                                foundCrosshair = (Circle)nextNodeC;
                                            }
                                        }

                                        break;
                                    }
                                }

                                break;
                            }
                        }

                        // Setting the pointers to the editor Editor_Tab
                        item.setText(EDAmameController.Editor_Names[editorType]);
                        Editor_Tab = item;
                    }
                    else
                    {
                        Editor_Tabs.add(item);
                    }
                }
            }
        }

        if (foundPaneListener == null)
            throw new InvalidClassException("Unable to locate listener pane for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundPaneHolder == null)
            throw new InvalidClassException("Unable to locate holder pane for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundPaneHighlights == null)
            throw new InvalidClassException("Unable to locate highlights pane for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundPaneSelections == null)
            throw new InvalidClassException("Unable to locate selections pane for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundCanvas == null)
            throw new InvalidClassException("Unable to locate canvas for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundCrosshair == null)
            throw new InvalidClassException("Unable to locate crosshair shape for an editor with name \"" + this.Editor_Name + "\"!");

        this.Editor_RenderSystem = new RenderSystem(this,
                                                    foundPaneListener,
                                                    foundPaneHolder,
                                                    foundPaneHighlights,
                                                    foundPaneSelections,
                                                    foundCanvas,
                                                    foundCrosshair,
                                                    EDAmameController.Editor_TheaterSize,
                                                    EDAmameController.Editor_BackgroundColors[editorType],
                                                    EDAmameController.Editor_GridPointColors[editorType],
                                                    EDAmameController.Editor_GridBoxColors[editorType],
                                                    EDAmameController.Editor_MaxShapes,
                                                    EDAmameController.Editor_ZoomLimits,
                                                    EDAmameController.Editor_ZoomFactor,
                                                    EDAmameController.Editor_MouseDragFactor,
                                                    EDAmameController.Editor_MouseCheckTimeout,
                                                    EDAmameController.Editor_SelectionBoxColors[editorType],
                                                    EDAmameController.Editor_SelectionBoxWidth);
    }
}