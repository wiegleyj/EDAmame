/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.render.RenderShape;
import com.cyte.edamame.render.RenderSystem;

import java.util.*;
import java.util.logging.Level;

import com.cyte.edamame.util.MenuConfigLoader;
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
    final UUID Editor_ID = UUID.randomUUID();
    String Editor_Name = null;

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

    protected List<Menu> dynamicMenus = new ArrayList<>();

    //// MAIN FUNCTIONS ////

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

    /**
     * Returns a (possibly empty) structure of menu items for EDAmame to include in its Editor_Menus. The top Map maps
     * menu names to a list of items to include in a menu with that name. The values of this map are a list of
     * MenuItems that EDAmame should include/insert into any menu it has with a matching name.
     * @return a (possibly empty) structure of menu items
     */
    public ObservableMap<String, ObservableList<MenuItem>> Editor_GetMenus() {
        ObservableMap<String, ObservableList<MenuItem>> combinedMenus = FXCollections.observableHashMap();
        for (Menu menu : dynamicMenus) {
            String menuName = menu.getText();
            ObservableList<MenuItem> items = menu.getItems();
            combinedMenus.put(menuName, items);
        }
        return combinedMenus;
    }

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

        // Reading all global node properties...
        LinkedList<Double> shapesPosX = new LinkedList<Double>();
        LinkedList<Double> shapesPosY = new LinkedList<Double>();
        LinkedList<Double> shapesRots = new LinkedList<Double>();

        for (int i = 0; i < this.Editor_RenderSystem.shapes.size(); i++)
        {
            RenderShape shape = this.Editor_RenderSystem.shapes.get(i);

            if (!shape.selected)
                continue;

            shapesPosX.add(shape.shapeMain.getTranslateX() - this.Editor_RenderSystem.paneHolder.getWidth() / 2);
            shapesPosY.add(shape.shapeMain.getTranslateY() - this.Editor_RenderSystem.paneHolder.getHeight() / 2);
            shapesRots.add(shape.shapeMain.getRotate());
        }

        // Creating position box...
        {
            HBox posHBox = new HBox(10);
            posHBox.setId("posBox");
            posHBox.getChildren().add(new Label("Positions X: "));
            TextField posXText = new TextField();
            posXText.setMinWidth(100);
            posXText.setPrefWidth(100);
            posXText.setMaxWidth(100);
            posXText.setId("posX");
            posHBox.getChildren().add(posXText);
            posHBox.getChildren().add(new Label("Positions Y: "));
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
            rotHBox.getChildren().add(new Label("Rotations: "));
            TextField rotText = new TextField();
            rotText.setId("rot");
            rotHBox.getChildren().add(rotText);

            if (EDAmameController.Controller_IsListAllEqual(shapesRots))
                rotText.setText(Double.toString(shapesRots.get(0)));
            else
                rotText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(rotHBox);
        }

        EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
    }

    public void Editor_PropsGlobalApply()
    {
        VBox propsBox = EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox;

        // Iterating over all the nodes & attempting to apply global node properties if selected...
        for (int i = 0; i < this.Editor_RenderSystem.shapes.size(); i++)
        {
            RenderShape shape = this.Editor_RenderSystem.shapes.get(i);

            if (!shape.selected)
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
                            shape.shapeMain.setTranslateX(newPosX + this.Editor_RenderSystem.paneHolder.getWidth() / 2);
                    }

                    if (EDAmameController.Controller_IsStringNum(posYStr))
                    {
                        Double newPosY = Double.parseDouble(posYStr);

                        if ((newPosY >= -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2) &&
                                (newPosY <= EDAmameController.Editor_TheaterSize.GetRightDouble() / 2))
                            shape.shapeMain.setTranslateY(newPosY + this.Editor_RenderSystem.paneHolder.getHeight() / 2);
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
                        shape.shapeMain.setRotate(Double.parseDouble(rotStr));
                }
            }

            this.Editor_RenderSystem.shapes.set(i, shape);
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
        String prefix = Editor_ID.toString();
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

    public List<Menu> getDynamicMenus() {
        return this.dynamicMenus;
    }
}