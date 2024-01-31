/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

// TODO:
// Fix PCB line moving
// Implement gerber & drill file exports
// Fix holes overlaying traces completely
// Fix net list wire chain recognition
// Fix selection box appearing while dragging something
// Fix snap point shapes not disappearing after deleting node
// Fix file writing (?)
// Refactor symbol saving so the shapes aren't all parented to a Group
// Fix occasional dragging not recognized
// Refactor viewport mouse diff pos scaling
// Refactor all editor Panes to Groups
// Fix 3+ editors crashing
// Fix mouse-specific release callback function
// Implement proper getters & setters for all class fields

package com.cyte.edamame;
import com.cyte.edamame.editor.*;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.memento.TextAreaHandler;

import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.application.*;
import javafx.geometry.*;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

/**
 * Main Controller for the {@link EDAmame} Application.<p>
 *
 * Implements all general functionality that is not delegated to an editor or other sub-module. Actions such
 * as creating, opening, closing libraries, projects and files is handled by this controller.
 *
 * @author Jeff Wiegley, Ph.D.
 * @author jeffrey.wiegley@gmail.com
 */
public class EDAmameController implements Initializable
{
    //// GLOBAL VARIABLES ////

    final static public String[] Editor_Names = {"Symbol Editor", "Schematic Editor", "Footprint Editor", "PCB Editor"};
    final static public Double Editor_HeartbeatDelay = 0.01;

    final static public PairMutable Editor_TheaterSize = new PairMutable(1000.0, 1000.0);
    final static public Color[] Editor_BackgroundColors = {Color.BEIGE, Color.LIGHTBLUE, Color.DARKBLUE, Color.MAROON};
    final static public Color[] Editor_GridPointColors = {Color.GRAY, Color.GRAY, Color.YELLOW, Color.YELLOW};
    final static public Integer Editor_MaxShapes = 10000;
    final static public PairMutable Editor_ZoomLimits = new PairMutable(0.35, 5.0);
    final static public Double Editor_ZoomFactor = 1.5;
    final static public Double Editor_MouseDragFactor = 1.0;
    final static public Double Editor_MouseCheckTimeout = 0.0001;
    final static public Color[] Editor_SelectionBoxColors = {Color.BLACK, Color.BLACK, Color.YELLOW, Color.YELLOW};
    final static public Double Editor_SelectionBoxWidth = 1.0;
    final static public Integer Editor_MenuItemDefaultPriority = 10;
    final static public Double Editor_SnapPointRadius = 15.0;
    final static public Color Editor_SnapPointShapeColor = Color.DARKGREEN;
    final static public Double Editor_SnapPointShapeOpacity = 0.5;
    final static public Double Editor_PinLabelFontSize = 10.0;
    final static public PairMutable Editor_PinLabelOffset = new PairMutable(5.0, 10.0);
    final static public Integer Editor_UndoStackMaxLen = 10;
    final static public double[] Editor_SnapGridSpacings = {0.254, 0.508, 0.635, 1.27, 2.54, 5.08, 6.35};
    final static public double Editor_CursorPreviewRadius = 5;
    final static public double Editor_CursorPreviewBorderWidth = 2;
    final static public String[] Editor_PCBLayers = {"Copper Front", "Copper Rear", "Edge Cuts", "Silkscreen"};
    final static public Color[] Editor_PCBLayerColors = {Color.RED, Color.LIGHTBLUE, Color.WHITE, Color.YELLOW};
    final static public Color Editor_PCBExposedColor = Color.ORANGE;
    final static public Color Editor_PCBViaColor = Color.WHITE;

    final static public Double EditorSymbol_CircleRadiusMin = 10.0;
    final static public Double EditorSymbol_CircleRadiusMax = 100.0;
    final static public Double EditorSymbol_RectWidthMin = 10.0;
    final static public Double EditorSymbol_RectWidthMax = 500.0;
    final static public Double EditorSymbol_RectHeightMin = 10.0;
    final static public Double EditorSymbol_RectHeightMax = 500.0;
    final static public Double EditorSymbol_TriLenMin = 10.0;
    final static public Double EditorSymbol_TriLenMax = 500.0;
    final static public Double EditorSymbol_BorderMin = 0.0;
    final static public Double EditorSymbol_BorderMax = 10.0;
    final static public Double EditorSymbol_TextFontSizeMin = 10.0;
    final static public Double EditorSymbol_TextFontSizeMax = 100.0;
    final static public Double EditorSymbol_LineWidthMin = 1.0;
    final static public Double EditorSymbol_LineWidthMax = 20.0;
    final static public Double EditorSymbol_PinRadiusMin = 5.0;
    final static public Double EditorSymbol_PinRadiusMax = 10.0;
    final static public Double EditorSymbol_WireWidthMin = 3.0;
    final static public Double EditorSymbol_WireWidthMax = 10.0;
    final static public int EditorSymbol_MaxChars = 10;

    final static public Double EditorFootprint_HoleRadiusOuterMin = 5.0;
    final static public Double EditorFootprint_HoleRadiusOuterMax = 10.0;
    final static public Double EditorFootprint_HoleRadiusInnerMin = 5.0;
    final static public Double EditorFootprint_HoleRadiusInnerMax = 10.0;
    final static public Double EditorFootprint_ViaRadiusMin = 1.0;
    final static public Double EditorFootprint_ViaRadiusMax = 5.0;

    final static public Logger logger = Logger.getLogger(EDAmame.class.getName());     // The logger for the entire application. All classes/modules should obtain and use this static logger.

    @FXML
    private Tab tabLog;                          // The Editor_Tab used to hold the TextArea used for application logging.
    @FXML
    private MenuItem menuLogItem;
    @FXML
    private TextArea logText;                    // The TextArea to display logged information on.
    @FXML
    private SplitPane splitPane;                 // The main split pane separating always available global controls on the left from editor Editor_Tabs on right.
    @FXML
    private StackPane stackPaneControls;         // The stack pane on left of split used to contain different control setups.
    @FXML
    private TabPane tabPaneControls;             // The TabPane used to hold all the main controls. Displayed in the "navigation" pane of the controls stack.
    @FXML
    private TabPane tabPane;                     // Primary TabPane displayed on the right of split used by EDAmame to present editors and information.
    @FXML
    private StackPane stackPaneEditorToolbars;   // A stack of toolbars to the right of the main EDAmame toolbar. Editors can provide own toolbars for here.
    @FXML
    private MenuBar menuBar;                     // The main EDAmame menu bar.
    @FXML
    public Label statusBar;                      // Load the menu config loader...

    // DO NOT EDIT

    public final Stage stage;                                                              // The stage hosting this controller.
    public Timeline heartbeatTimeline;
    private final ObservableMap<Tab, Editor> editors = FXCollections.observableHashMap();   // All editors instantiated are remembered in a HashMap for fast lookup keyed by their main Editor_Tab.
    static public EditorProps editorPropertiesWindow = null;
    static public EditorSettings editorSettingsWindow = null;
    static public LinkedList<KeyCode> pressedKeys = new LinkedList<KeyCode>();
    static public Label statusBarGlobal;

    //// MAIN FUNCTIONS ////

    /**
     * Constructs an instance of this controller with knowledge of the Controller_Stage that it is attached to. Knowledge of
     * the Controller_Stage allows the controller to entirely take control of proper closing activities even when its an
     * onCloseRequest generated by a user killing the application of an operating system's "close" window
     * decoration.
     *
     * @param stage The Controller_Stage that is hosting/using this controller.
     */
    public EDAmameController(Stage stage)
    {
        this.stage = stage;
        stage.setOnShown((event) -> ExecuteOnShown());
    }

    /**
     * JavaFX Controller_Stage initialization procedures
     *
     * @param url
     * The location used to resolve relative paths for the root object, or
     * {@code null} if the location is not known.
     *
     * @param rb
     * The resources used to localize the root object, or {@code null} if
     * the root object was not localized.
     */
    @FXML
    public void initialize(URL url, ResourceBundle rb)
    {
        LoggingChangeToTab();
        logger.log(Level.INFO, "Initialization Commenced...\n");

        // Set the Controller_Stage to close gracefully.
        stage.setOnCloseRequest(evt -> { evt.consume(); Exit(); });
        logger.log(Level.INFO, "Stage configured to close gracefully.\n");

        // Changing Editor_Tabs in the main Editor_Tab pane is a significant task for changing
        // between Controller_Editors and modules. This logic is handled through Editor_Tab change events.
        EditorEnableSelect();

        // Restore the previous location and size of windows. (Location of split pane dividers needs
        // to be handled elsewhere since layout of nodes isn't completed at time of initialization.
        WindowContextLoad();

        // Initializing editor heartbeat loops
        this.heartbeatTimeline = new Timeline(new KeyFrame(Duration.seconds(Editor_HeartbeatDelay), e -> this.EditorsHeartbeat()));
        this.heartbeatTimeline.setCycleCount(Animation.INDEFINITE);
        this.heartbeatTimeline.playFromStart();

        // Setting the global status bar...
        statusBarGlobal = this.statusBar;

        // correct the text in the show log menu item
        LogToggleItemText();
        logger.log(Level.INFO, "Initialization Complete\n");
    }

    /**
     * Execute activites required to be done once the main Application Controller_Stage is finally shown. (Activities
     * performed in the {@link EDAmameController#initialize(URL, ResourceBundle)} happen before the various
     * scene nodes have been sized, laid out, and rendered. This provides a location to perform all
     * startup tasks required but unsuitable for standard initialization.
     */
    public void ExecuteOnShown()
    {
        DividerRestore();
    }

    //// LOGGING FUNCTIONS ////

    static public void SetStatusBar(String msg)
    {
        statusBarGlobal.setText(msg);
    }

    /**
     * Change the log handler for the package wide logger from stdout to a TextArea node.
     *
     * EDAmame has a dedicated Editor_Tab for displaying log information. Controller_LoggingChangeToTab
     * removes all log handlers from the logger and replaces them with a handler that
     * appends requested logging information to a TextArea in the dedicated log Editor_Tab.
     */
    public void LoggingChangeToTab()
    {
        for (Handler handler : logger.getHandlers())
            logger.removeHandler(handler);

        logger.setUseParentHandlers(false);
        logger.addHandler(new TextAreaHandler(logText));
    }

    //// EDITOR FUNCTIONS ////

    public void EditorsHeartbeat()
    {
        ObservableList<Tab> tabs = tabPane.getTabs();

        // Checking all Editor_Tabs in the main Editor_Tab pane...
        for (int i = 0; i < tabs.size(); i++)
        {
            Tab tab = tabs.get(i);

            // Handling symbol Controller_Editors
            if (!tab.getText().equals("Log"))
            {
                Editor editor = this.editors.get(tab);

                if ((editor == null) || !editor.visible)
                    continue;

                editor.Heartbeat();

                // Handling delayed bound node setting...
                /*if (!Controller_RenderShapesDelayedBoundsRefresh.isEmpty())
                {
                    if (Controller_RenderShapesDelayedBoundsRefresh.size() >= 100)
                        throw new java.lang.Error("ERROR: Too many items in \"Controller_NodesDelayedCalcBounds\"!");

                    for (int j = 0; j < Controller_RenderShapesDelayedBoundsRefresh.size(); j++)
                    {
                        PairMutable curr = Controller_RenderShapesDelayedBoundsRefresh.get(j);

                        RenderNode renderNode = curr.GetLeftPair().GetLeftRenderShape();
                        Integer loopsNum = curr.GetLeftPair().GetRightInteger();
                        PairMutable oldSize = curr.GetRightPair();

                        renderNode.RenderNode_ShapeHighlightedRefresh();
                        renderNode.RenderNode_ShapeSelectedRefresh();

                        Bounds bounds = renderNode.RenderNode_Node.getBoundsInLocal();

                        if ((bounds.getWidth() != oldSize.GetLeftDouble()) && (bounds.getHeight() != oldSize.GetRightDouble()) ||
                            (loopsNum >= 5))
                        {
                            Controller_RenderShapesDelayedBoundsRefresh.remove(j);
                            j--;
                        }
                        else
                        {
                            curr.GetLeftPair().right = loopsNum + 1;
                        }
                    }
                }*/

                //System.out.println(editor.Editor_RenderSystem.shapesHighlighted);
                //System.out.println(editor.Editor_RenderSystem.shapesSelected);
                //System.out.println(editor.Editor_RenderSystem.shapesMoving);
                //System.out.println("(" + editor.Editor_RenderSystem.paneHighlights.getLayoutX() + ", " + editor.Editor_RenderSystem.paneHighlights.getLayoutY() + ")");
                //System.out.println(editor.Editor_RenderSystem.center.ToStringDouble());
                //System.out.println(editor.Editor_RenderSystem.paneHolder.getBoundsInLocal().toString());
                //System.out.println(EDAmameController.Controller_PressedKeys.toString());
            }
        }
    }

    /**
     * Enables to controller logic for switching between editor tabs.
     *
     * Editors are composed of several nodes.
     *     A single main editor Editor_Tab that is always visible in editorTabPane.
     *     A set of control Editor_Tabs that are only present/visible in the tabPaneControls when the editor is selected.
     *     A Editor_ToolBar that is only visible in the Editor_ToolBar area when the editor is selected.
     *     A set of Editor_Menus and items that are only present in the Editor_Menus when the editor is selected.
     *
     * Editor_EnableSelect provides the control for the presence and visibility of these items
     * based on tab selection. When a Editor_Tab is selected the UI components for the previously selected editor
     * are removed/hidden and the components for the newly selected editor are enabled/shown.
     */
    public void EditorEnableSelect()
    {
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // if switching away from an editor Editor_Tab, then deactivate the controls/visibility for it.
                    Editor editor = editors.get(oldValue);

                    if (editor != null) // if the old Editor_Tab was an editor then deactivate it.
                        EditorDeactivate(editor);

                    editor = editors.get(newValue);

                    if (newValue != null) // if the new Editor_Tab selected is an editor then activate it.
                        EditorActivate(editor);
                }
        );
    }

    /**
     * Symbol Libraries, Footprint Libraries, Schematics, PCBs are all handled by their own editors. Editors
     * supply their own controllers and controls. The EDAmameController folds these controls into its own
     * Editor_Menus, toolbars and windows by obtaining, reparenting and controlling the visibility of those controls.
     *
     * Editors are activated by EDAmame by making sure that only the selected Editor_Tab's controls are visible and
     * accessible.
     *
     * @param editor Which editor to make controls visible for.
     */
    public void EditorActivate(Editor editor)
    {
        if (editor == null)
            return;

        // Making editor toolbars visible...
        ToolBar bar = editor.GetToolBar();

        if (bar != null)
            editor.GetToolBar().setVisible(true);

        // Adding all the editor control tabs...
        tabPaneControls.getTabs().addAll(editor.GetControlTabs());

        // Adding all the editor's menus...
        {
            for (Map.Entry<String, ObservableList<MenuItem>> currEntry : editor.menus.entrySet())
            {
                String currMenuName = currEntry.getKey();
                ObservableList<MenuItem> currMenuItems = FXCollections.observableArrayList(GetClonedMenuItems(currEntry.getValue()));

                // Checking whether we need to merge menus...
                Integer existingIdx = -1;

                for (int j = 0; j < this.menuBar.getMenus().size(); j++)
                {
                    if (this.menuBar.getMenus().get(j).getText().equals(currMenuName))
                    {
                        existingIdx = j;

                        break;
                    }
                }

                // If we don't need to merge, we create a new menu & insert all the items into it...
                if (existingIdx == -1)
                {
                    Menu currMenu = new Menu(currMenuName);
                    currMenu.setId("dynamicMenu_" + editor.id);
                    currMenu.getItems().addAll(currMenuItems);

                    this.menuBar.getMenus().add(currMenu);
                }
                // If we need to merge, we iterate through all the items in the current menu item list and insert them into the existing main menu...
                else
                {
                    for (int j = 0; j < currMenuItems.size(); j++)
                    {
                        MenuItem currMenuItem = currMenuItems.get(j);
                        String currMenuItemId = "dynamicMenuItem_" + editor.id;

                        if (currMenuItem.getId() != null)
                            currMenuItemId += "_" + currMenuItem.getId();

                        currMenuItem.setId(currMenuItemId);

                        this.menuBar.getMenus().get(existingIdx).getItems().add(currMenuItem);
                    }
                }
            }

            // Sorting all final menus in ascending priority order...
            for (int i = 0; i < this.menuBar.getMenus().size(); i++)
            {
                Menu currMenu = this.menuBar.getMenus().get(i);
                HashMap<MenuItem, Integer> currMenuItems = new HashMap<MenuItem, Integer>();

                for (int j = 0; j < currMenu.getItems().size(); j++)
                {
                    MenuItem currMenuItem = currMenu.getItems().get(j);
                    String currMenuItemId = currMenuItem.getId();
                    Integer currMenuItemPriority = EDAmameController.Editor_MenuItemDefaultPriority;

                    try
                    {
                        currMenuItemPriority = Integer.parseInt(currMenuItemId.substring(currMenuItemId.lastIndexOf('_') + 1, currMenuItemId.length()));
                    }
                    catch (Exception e)
                    {}

                    currMenuItems.put(currMenuItem, currMenuItemPriority);
                }

                Stream<Entry<MenuItem, Integer>> currMenuItemsSorted = currMenuItems.entrySet().stream().sorted(Map.Entry.comparingByValue());

                currMenu.getItems().clear();
                currMenuItemsSorted.forEach(currMenuItem -> {currMenu.getItems().add(currMenuItem.getKey());});
            }
        }

        editor.visible = true;
    }

    /**
     * Editors are deactivated by EDAmame by making sure that unselected editor tabs' controls are invisible and
     * inaccessible.
     *
     * @param editor Which editor to make controls invisible for.
     */
    public void EditorDeactivate(Editor editor)
    {
        // Removing any active property windows for the active editor...
        if (EDAmameController.editorPropertiesWindow != null)
            EDAmameController.editorPropertiesWindow.stage.close();

        // Removing the editor's settings window (if open)...
        if (EDAmameController.editorSettingsWindow != null)
            EDAmameController.editorSettingsWindow.stage.close();

        // Removing any active toolbars...
        editor.GetToolBar().setVisible(false);

        // Removing any active control tabs...
        tabPaneControls.getTabs().removeAll(editor.GetControlTabs());

        // Removing any editor's menus...
        for (int i = 0; i < this.menuBar.getMenus().size(); i++)
        {
            Menu currMenu = this.menuBar.getMenus().get(i);

            if (currMenu == null)
                continue;

            if ((currMenu.getId() != null) && currMenu.getId().contains("dynamicMenu") && currMenu.getId().contains(editor.id))
            {
                menuBar.getMenus().remove(i);
                i--;
            }
            else
            {
                for (int j = 0; j < currMenu.getItems().size(); j++)
                {
                    MenuItem currMenuItem = currMenu.getItems().get(j);

                    if ((currMenuItem.getId() != null) && currMenuItem.getId().contains("dynamicMenuItem") && currMenuItem.getId().contains(editor.id))
                    {
                        currMenu.getItems().remove(j);
                        j--;
                    }
                }
            }
        }

        editor.visible = false;
    }

    /**
     * Editors are added by EDAmame by including their menu items and toolbars in its menus and toolbar areas.
     *
     * @param editor Which editor to setup and add controls for. They all start out invisible/unavailable.
     */
    public void EditorAdd(Editor editor)
    {
        Tab editorTab = editor.GetTab();
        editorTab.setId("editorTab_" + editor.id);

        // Adding editor's main tab...
        editors.put(editorTab, editor);

        if (editorTab != null)
        {
            // Adding a close callback
            editorTab.setOnCloseRequest(event -> {
                EditorDeactivate(editor);
                EditorRemove(editor);

                event.consume();
            });

            // Adding all the Editor_Tabs
            tabPane.getTabs().add(editorTab);
        }

        // Make sure the toolbar is invisible
        editor.GetToolBar().setVisible(false);
        // add the editor's toolbar to the toolbar stack. The
        stackPaneEditorToolbars.getChildren().add(editor.GetToolBar());

        // The control Editor_Tabs don't need to be handled. Control of their
        // visibility done through addition and removal from the children
        // of the control TabPane.

        // move the log Editor_Tab to the end.
        if (tabPane.getTabs().contains(tabLog))
        {
            tabPane.getTabs().remove(tabLog);
            tabPane.getTabs().add(tabLog);
        }

        // Select the new Editor_Tab
        tabPane.getSelectionModel().select(editorTab);
    }

    /**
     * Editors are removed by EDAmame by removing their menu items and toolbars in its menus and toolbar areas.
     * This should really only be done once an editor has been closed and has no need to save data.
     *
     * @param editor Which editor to remove controls for.
     */
    public void EditorRemove(Editor editor)
    {
        EditorDeactivate(editor);

        // Removing any editor's toolbars...
        stackPaneEditorToolbars.getChildren().remove(editor.GetToolBar());

        // Removing any editor's control tabs...
        tabPaneControls.getTabs().removeAll(editor.GetControlTabs());

        // Remove the main editor tab...
        tabPane.getTabs().remove(editor.GetTab());
    }

    //// SAVING & LOADING FUNCTIONS ////
    
    /**
     * Saves the window position and size of the application as Java Preferences.
     *
     * The values of the size and position of the application are saved to the Java Preference storage.
     * User preferences and settings for application behavior are stored in File files to avoid overly
     * complicating platform dependent stores such as registries.
     */
    public void WindowContextSave()
    {
        Preferences prefs = Preferences.userRoot().node("EDAmame");
        prefs.putDouble("WINDOW_POSITION_X", stage.getX());
        prefs.putDouble("WINDOW_POSITION_Y", stage.getY());
        prefs.putDouble("WINDOW_WIDTH", stage.getWidth());
        prefs.putDouble("WINDOW_HEIGHT", stage.getHeight());
        double pos = splitPane.getDividerPositions()[0];
        prefs.putDouble("DIVIDER_POSITION", pos);
    }

    /**
     * Restores the window position and size of the application.
     *
     * The values of the size and position of the application when this context was last saved is restored.
     * Context is typically saved when the application exits gracefully.
     */
    public void WindowContextLoad()
    {
        Preferences prefs = Preferences.userRoot().node("EDAmame");

        // Save the application's current size
        stage.setWidth(prefs.getDouble("WINDOW_WIDTH",800));
        stage.setHeight(prefs.getDouble("WINDOW_HEIGHT",1000));

        // Save the current position of the application
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double x = prefs.getDouble("WINDOW_POSITION_X", (bounds.getWidth()- stage.getWidth())/2);
        double y = prefs.getDouble("WINDOW_POSITION_Y", (bounds.getHeight()- stage.getHeight())/2);
        // If the application is off the screen then adjust it so that it lies within the screen's boundaries.
        if (x>bounds.getWidth()- stage.getWidth()) x=bounds.getWidth()- stage.getWidth();
        if (x<0) x=0;
        if (y>bounds.getHeight()- stage.getHeight()) y=bounds.getHeight()- stage.getHeight();
        if (y<0) y=0;

        stage.setX(x);
        stage.setY(y);

        //String home = System.getProperty("user.home");
        logger.log(Level.INFO, "EDAmame window context restored.\n");
    }

    /**
     * Restores the main split pane's divider position between runs of the application.<p>
     *
     * The application establishes a main look and feel that consists of a split pane containing two nodes.
     * The left node is used to contain to level controls specific to the entire EDAmame application. The
     * right pane is a Editor_Tab pane that provides tabbed locations for Controller_Editors and other sub-modules to place
     * their controls. The user's selection of divider position is maintained between runs of the application.
     */
    public void DividerRestore()
    {
        Preferences prefs = Preferences.userRoot().node("EDAmame");

        // Ugh. The windows/Controller_Stage is supposedly "shown" at this point but the children are not
        // yet *really* shown. So really... what you probably need to work on here hasn't been rendered and its
        // layout hasn't been finalized despite being "shown". So we need the horrid "runLater" which is
        // *wildly* undefined as to "when".
        try {
            double value = prefs.getDouble("DIVIDER_POSITION",Double.MAX_VALUE);
            if (value != Double.MAX_VALUE)
                Platform.runLater(() -> splitPane.setDividerPositions(value));
        } catch (Throwable ignored) {
        }
    }

    //// CALLBACK FUNCTIONS ////

    @FXML
    public void EditorSymbolNewPressed()
    {
        try
        {
            Editor editorInstance = EditorSymbol.Create();

            if (editorInstance == null)
            {
                System.out.println("editorInstance is null!");

                return;
            }

            EditorAdd(editorInstance);
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    @FXML
    public void EditorSchematicNewPressed()
    {
        try
        {
            Editor editorInstance = EditorSchematic.Create();

            if (editorInstance == null)
            {
                System.out.println("editorInstance is null!");

                return;
            }

            this.EditorAdd(editorInstance);
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    @FXML
    public void EditorFootprintNewPressed()
    {
        try
        {
            Editor editorInstance = EditorFootprint.Create();

            if (editorInstance == null)
            {
                System.out.println("editorInstance is null!");
                return;  // exit the method if editorInstance is null
            }

            this.EditorAdd(editorInstance);
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    @FXML
    public void EditorPCBNewPressed()
    {
        try
        {
            Editor editorInstance = EditorPCB.Create();

            if (editorInstance == null)
            {
                System.out.println("editorInstance is null!");
                return;  // exit the method if editorInstance is null
            }

            this.EditorAdd(editorInstance);
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * Controller_Exit() implements graceful and intelligent shutdown of the EDAmame application.
     *
     * All open Controller_Editors will be requested to perform their close/save/shutdown activities and the EDAmame
     * application will exit gracefully. This action is triggered by menu items or On Close Request.
     */
    @FXML
    public void Exit()
    {
        WindowContextSave();
        logger.log(Level.INFO, "EDAmame exited gracefully.\n");
        Platform.exit();
    }

    /**
     * A simple event handle to clear the TextArea/Pane where logging happens.
     */
    @FXML
    public void LogClear()
    {
        logText.clear();
    }

    /**
     * The log Editor_Tab can be made visible or not. It is always the right most Editor_Tab of all Editor_Tabs. When explicitly added
     * the log Editor_Tab is automatically selected.
     */
    @FXML
    public void LogToggleVisibility()
    {
        if (tabPane.getTabs().contains(tabLog))
        {
            tabPane.getTabs().remove(tabLog);
        }
        else
        {
            tabPane.getTabs().add(tabLog);
            tabPane.getSelectionModel().select(tabLog);
        }

        LogToggleItemText();
    }

    /**
     * Simple function to correct the textual information present on the menu item for toggling the state of
     * the log window.
     */
    @FXML
    public void LogToggleItemText()
    {
        if (tabPane.getTabs().contains(tabLog))
            menuLogItem.setText("Hide Log Tab");
        else
            menuLogItem.setText("Show Log Tab");
    }

    /**
     * Select the "navigation" pane is the main controls to show.
     *
     * The left element of the main split pane is a global controls area and is designed to support future
     * features. It is a stacked pane containing as many controls as needed by EDAmame development. The primary
     * control is a Editor_Tab pane. Editors add their controls into this Editor_Tab pane as additional Editor_Tabs. This primary
     * Editor_Tab pane control is called the "Navigation" Pane.
     */
    @FXML
    public void NavigationTabPaneSelect()
    {
        stackPaneControls.getChildren().forEach(e -> e.setVisible(e== tabPaneControls));
    }

    @FXML
    public void OnKeyPressed(KeyEvent event)
    {
        // Adding pressed key to the pressed keys list...
        EDAmameController.KeyPressed(event.getCode());

        // Calling editor callbacks...
        ObservableList<Tab> tabs = tabPane.getTabs();

        for (int i = 0; i < tabs.size(); i++)
        {
            Tab tab = tabs.get(i);

            if (!tab.getText().equals("Log"))
            {
                Editor editor = this.editors.get(tab);

                if ((editor != null) && editor.visible)
                {
                    // Handling editor-specific callback actions
                    editor.OnKeyPressedSpecific(event);

                    // Handling global callback actions
                    editor.OnKeyPressedGlobal(event);
                }
            }
        }

        event.consume();
    }

    @FXML
    public void OnKeyReleased(KeyEvent event)
    {
        // Removing pressed key from the pressed keys list...
        EDAmameController.KeyReleased(event.getCode());

        // Calling editor callbacks...
        ObservableList<Tab> tabs = tabPane.getTabs();

        for (int i = 0; i < tabs.size(); i++)
        {
            Tab tab = tabs.get(i);

            if (!tab.getText().equals("Log"))
            {
                Editor editor = this.editors.get(tab);

                if ((editor != null) && editor.visible)
                {
                    // Handling editor-specific callback actions
                    editor.OnKeyReleasedSpecific(event);

                    // Handling global callback actions
                    editor.OnKeyReleasedGlobal(event);
                }
            }
        }

        event.consume();
    }

    //// SUPPORT FUNCTIONS ////

    static public <T> boolean IsListAllEqual(LinkedList<T> list)
    {
        return list.stream().distinct().limit(2).count() <= 1;
    }

    static public Integer FindNodeById(ObservableList<Node> nodes, String id)
    {
        for (int i = 0; i < nodes.size(); i++)
            if ((nodes.get(i).getId() != null) && nodes.get(i).getId().equals(id))
                return i;

        return -1;
    }

    static public Node GetNodeById(ObservableList<Node> nodes, String id)
    {
        for (int i = 0; i < nodes.size(); i++)
            if ((nodes.get(i).getId() != null) && nodes.get(i).getId().equals(id))
                return nodes.get(i);

        return null;
    }

    static public boolean IsStringNum(String str)
    {
        try
        {
            Double.parseDouble(str);

            return true;
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }

    static public boolean IsKeyPressed(KeyCode key)
    {
        return pressedKeys.contains(key);
    }

    static public void KeyPressed(KeyCode key)
    {
        if (!IsKeyPressed(key))
            pressedKeys.add(key);
    }

    static public void KeyReleased(KeyCode key)
    {
        if (IsKeyPressed(key))
            pressedKeys.remove(key);
    }

    //// TESTING FUNCTIONS ////

    /** A test method for checking database connectivity. */
    @FXML
    public void OnTestButtonClickDatabase()
    {
        System.out.println("hi");

        String classpath = System.getProperty("java.class.path");
        String[] classPathValues = classpath.split(java.io.File.pathSeparator);
        for (String classPath: classPathValues) {
            System.out.println(classPath);
        }

        try
        {
            Class.forName ("org.h2.Driver");
            System.out.println("HELLO");
            Connection c = DriverManager.getConnection("jdbc:h2:~/testtest", "SA", "");

            String statement = """
                    CREATE TABLE symbols (
                    SymbolID BINARY(16) NOT NULL PRIMARY KEY,
                    Version SMALLINT NOT NULL DEFAULT 1,
                    Created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    Modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    Author VARCHAR(255),
                    YAML BLOB NOT NULL);""";
            System.out.println(statement);
            try (Statement stmt = c.createStatement()) {
                System.out.println("executing");
                int result = stmt.executeUpdate("DROP TABLE symbols IF EXISTS;");
                System.out.println("dropped");
                Thread.sleep(5000);
                stmt.executeUpdate(statement);
                System.out.println("completed");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            c.close();
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            //e.printStackTrace();
        }
    }

    private List<MenuItem> GetClonedMenuItems(ObservableList<MenuItem> originalItems) {
        List<MenuItem> clonedItems = new ArrayList<>();
        for (MenuItem item : originalItems) {
            clonedItems.add(CloneMenuItem(item));
        }
        return clonedItems;
    }

    private MenuItem CloneMenuItem(MenuItem original) {
        MenuItem cloned = new MenuItem(original.getText());
        cloned.setOnAction(original.getOnAction());
        cloned.setId(original.getId()); // Might reconsider this if ID collisions are problematic
        // Add additional clone properties of the original menu item as needed
        return cloned;
    }
}