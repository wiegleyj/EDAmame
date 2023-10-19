/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

// TODO:
// Finish renaming all variables
// Implement line drawing in symbol editor
// Fix occasional dragging not recognized
// Bind highlighted & selected shapes sizes to main shape size
// Refactor viewport mouse diff pos scaling
// Implement shape bounds highlight
// Implement wire connection points into symbols
// Refactor dissect editor function searching for canvas
// Fix 3+ editors crashing
// Fix mouse-specific release callback function
// Implement undo-redo functionalities

package com.cyte.edamame;

import com.cyte.edamame.editor.*;
import com.cyte.edamame.render.RenderNode;
import com.cyte.edamame.util.MenuConfigLoader;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.util.TextAreaHandler;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
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

    final static public String[] Editor_Names = {"Symbol Editor", "Footprint Editor"};
    final static public Double Editor_HeartbeatDelay = 0.01;

    final static public PairMutable Editor_TheaterSize = new PairMutable(1000.0, 1000.0);
    final static public Color[] Editor_BackgroundColors = {Color.BEIGE, Color.DARKBLUE};
    final static public Color[] Editor_GridPointColors = {Color.GRAY, Color.YELLOW};
    final static public Color[] Editor_GridBoxColors = {Color.BLACK, Color.YELLOW};
    final static public Integer Editor_MaxShapes = 10000;
    final static public PairMutable Editor_ZoomLimits = new PairMutable(0.35, 5.0);
    final static public Double Editor_ZoomFactor = 1.5;
    final static public Double Editor_MouseDragFactor = 1.0;
    final static public Double Editor_MouseCheckTimeout = 0.0001;
    final static public Color[] Editor_SelectionBoxColors = {Color.BLACK, Color.YELLOW};
    final static public Double Editor_SelectionBoxWidth = 1.0;

    final static public Double Editor_CircleRadiusMin = 10.0;
    final static public Double Editor_CircleRadiusMax = 100.0;
    final static public Double Editor_RectWidthMin = 10.0;
    final static public Double Editor_RectWidthMax = 500.0;
    final static public Double Editor_RectHeightMin = 10.0;
    final static public Double Editor_RectHeightMax = 500.0;
    final static public Double Editor_TriLenMin = 10.0;
    final static public Double Editor_TriLenMax = 500.0;
    final static public Double Editor_TextFontSizeMin = 10.0;
    final static public Double Editor_TextFontSizeMax = 100.0;

    final static public Logger Controller_Logger = Logger.getLogger(EDAmame.class.getName());     // The logger for the entire application. All classes/modules should obtain and use this static logger.

    @FXML
    private Tab Controller_TabLog;                          // The Editor_Tab used to hold the TextArea used for application logging.
    @FXML
    private TextArea Controller_LogText;                    // The TextArea to display logged information on.
    @FXML
    private SplitPane Controller_SplitPane;                 // The main split pane separating always available global controls on the left from editor Editor_Tabs on right.
    @FXML
    private StackPane Controller_StackPaneControls;         // The stack pane on left of split used to contain different control setups.
    @FXML
    private TabPane Controller_TabPaneControls;             // The TabPane used to hold all the main controls. Displayed in the "navigation" pane of the controls stack.
    @FXML
    private TabPane Controller_TabPane;                     // Primary TabPane displayed on the right of split used by EDAmame to present Controller_Editors and information.
    @FXML
    private StackPane Controller_StackPaneEditorToolBars;   // A stack of toolbars to the right of the main EDAmame toolbar. Editors can provide own toolbars for here.
    @FXML
    private MenuBar Controller_MenuBar;                     // The main EDAmame menu bar.
    @FXML
    public Label Controller_StatusBar;                      // Load the menu config loader...
    // DO NOT EDIT

    public Timeline Editor_HeartbeatTimeline;
    private final Stage Controller_Stage;                                                              // The Controller_Stage hosting this controller.
    private final ObservableMap<Tab, Editor> Controller_Editors = FXCollections.observableHashMap();   // All Controller_Editors instantiated are remembered in a HashMap for fast lookup keyed by their main Editor_Tab.
    private MenuItem Controller_MenuLogItem = null;                                                    // The menu item for toggling log visibility.
    static public EditorProps Controller_EditorPropertiesWindow = null;
    static public LinkedList<KeyCode> Controller_PressedKeys = new LinkedList<KeyCode>();
    static public Label Controller_StatusBarGlobal;
    private Map<String, MenuBarPriority> Controller_EditorMenuConfig;
    static public LinkedList<PairMutable> Controller_RenderShapesDelayedBoundsRefresh = new LinkedList<PairMutable>();

    //// MAIN FUNCTIONS ////

    /**
     * Constructs an instance of this controller with knowledge of the Controller_Stage that it is attached to. Knowledge of
     * the Controller_Stage allows the controller to entirely take control of proper closing activities even when its an
     * onCloseRequest generated by a user killing the application of an operating system's "close" window
     * decoration.
     *
     * @param Controller_Stage The Controller_Stage that is hosting/using this controller.
     */
    public EDAmameController(Stage Controller_Stage)
    {
        this.Controller_Stage = Controller_Stage;
        Controller_Stage.setOnShown((event) -> Controller_ExecuteOnShown());
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
        Controller_LoggingChangeToTab();
        Controller_Logger.log(Level.INFO, "Initialization Commenced...\n");

        // Creating the menu bar...
        {
            // Load the menu config loader...
            try
            {
                Controller_EditorMenuConfig = MenuConfigLoader.loadMenuConfig();
            }
            catch (Exception exception)
            {
                Controller_Logger.log(Level.SEVERE, "Failed to load menu configuration.", exception);

                return;
            }

            // Adding static menus...
            MenuBarPriority staticMenuPriority = Controller_EditorMenuConfig.get("EDAmame");

            if (staticMenuPriority != null)
            {
                List<Menu> staticMenus = createMenusFromConfig(staticMenuPriority, null);

                // Searching for the log tab visibility button ("View" menu, "Hide/Show Log Tab" item)...
                for (int i = 0; i < staticMenus.size(); i++)
                {
                    Menu currMenu = staticMenus.get(i);

                    if (currMenu.getText().equals("View"))
                    {
                        for (int j = 0; j < currMenu.getItems().size(); j++)
                        {
                            MenuItem currMenuItem = currMenu.getItems().get(j);

                            if (currMenuItem.getText().equals("Hide/Show Log Tab"))
                            {
                                this.Controller_MenuLogItem = currMenuItem;
                                this.Controller_MenuLogItem.setOnAction(event -> {Controller_LogToggleVisibility();});

                                break;
                            }
                        }
                    }

                    if (Controller_MenuLogItem != null)
                        break;
                }

                if (this.Controller_MenuLogItem == null)
                    throw new java.lang.Error("ERROR: Unable to locate log tab visibility menu item in static menus config!");

                Controller_MenuBar.getMenus().addAll(staticMenus);
            }
        }

        // Set the Controller_Stage to close gracefully.
        Controller_Stage.setOnCloseRequest(evt -> { evt.consume(); Controller_Exit(); });
        Controller_Logger.log(Level.INFO, "Stage configured to close gracefully.\n");

        // Changing Editor_Tabs in the main Editor_Tab pane is a significant task for changing
        // between Controller_Editors and modules. This logic is handled through Editor_Tab change events.
        Editor_EnableSelect();

        // Restore the previous location and size of windows. (Location of split pane dividers needs
        // to be handled elsewhere since layout of nodes isn't completed at time of initialization.
        Controller_WindowContextLoad();

        // Initializing editor heartbeat loops
        this.Editor_HeartbeatTimeline = new Timeline(new KeyFrame(Duration.seconds(Editor_HeartbeatDelay), e -> this.Controller_EditorsHeartbeat()));
        this.Editor_HeartbeatTimeline.setCycleCount(Animation.INDEFINITE);
        this.Editor_HeartbeatTimeline.playFromStart();

        // Setting the global status bar...
        Controller_StatusBarGlobal = this.Controller_StatusBar;

        // correct the text in the show log menu item
        Controller_LogToggleItemText();
        Controller_Logger.log(Level.INFO, "Initialization Complete\n");
    }

    /**
     * Execute activites required to be done once the main Application Controller_Stage is finally shown. (Activities
     * performed in the {@link EDAmameController#initialize(URL, ResourceBundle)} happen before the various
     * scene nodes have been sized, laid out, and rendered. This provides a location to perform all
     * startup tasks required but unsuitable for standard initialization.
     */
    private void Controller_ExecuteOnShown()
    {
        Controller_DividerRestore();
    }

    //// LOGGING FUNCTIONS ////

    static public void Controller_SetStatusBar(String msg)
    {
        Controller_StatusBarGlobal.setText(msg);
    }

    /**
     * Change the log handler for the package wide logger from stdout to a TextArea node.
     *
     * EDAmame has a dedicated Editor_Tab for displaying log information. Controller_LoggingChangeToTab
     * removes all log handlers from the logger and replaces them with a handler that
     * appends requested logging information to a TextArea in the dedicated log Editor_Tab.
     */
    private void Controller_LoggingChangeToTab()
    {
        for (Handler handler : Controller_Logger.getHandlers())
            Controller_Logger.removeHandler(handler);

        Controller_Logger.setUseParentHandlers(false);
        Controller_Logger.addHandler(new TextAreaHandler(Controller_LogText));
    }

    //// EDITOR FUNCTIONS ////

    public void Controller_EditorsHeartbeat()
    {
        ObservableList<Tab> tabs = Controller_TabPane.getTabs();

        // Checking all Editor_Tabs in the main Editor_Tab pane...
        for (int i = 0; i < tabs.size(); i++)
        {
            Tab tab = tabs.get(i);

            // Handling symbol Controller_Editors
            if (!tab.getText().equals("Log"))
            {
                Editor editor = this.Controller_Editors.get(tab);

                if ((editor == null) || !editor.Editor_Visible)
                    continue;

                editor.Editor_Heartbeat();

                // Handling delayed bound node setting...
                if (!Controller_RenderShapesDelayedBoundsRefresh.isEmpty())
                {
                    if (Controller_RenderShapesDelayedBoundsRefresh.size() >= 100)
                        throw new java.lang.Error("ERROR: Too many items in \"Controller_NodesDelayedCalcBounds\"!");

                    for (int j = 0; j < Controller_RenderShapesDelayedBoundsRefresh.size(); j++)
                    {
                        PairMutable curr = Controller_RenderShapesDelayedBoundsRefresh.get(j);

                        RenderNode renderNode = curr.GetLeftPair().GetLeftRenderShape();
                        Integer loopsNum = curr.GetLeftPair().GetRightInteger();
                        PairMutable oldSize = curr.GetRightPair();

                        renderNode.RenderNode_ShapeSelectedRefresh();
                        renderNode.RenderNode_ShapeHighlightedRefresh();
                        Bounds bounds = renderNode.node.getBoundsInLocal();

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
                }

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
     * Enables to controller logic for switching between editor Editor_Tabs.
     *
     * Editors are composed of several nodes.
     *     A single main editor Editor_Tab that is always visible in editorTabPane.
     *     A set of control Editor_Tabs that are only present/visible in the Controller_TabPaneControls when the editor is selected.
     *     A Editor_ToolBar that is only visible in the Editor_ToolBar area when the editor is selected.
     *     A set of Editor_Menus and items that are only present in the Editor_Menus when the editor is selected.
     *
     * Editor_EnableSelect provides the control for the presence and visibility of these items
     * based on Editor_Tab selection. When a Editor_Tab is selected the UI components for the previously selected editor
     * are removed/hidden and the components for the newly selected editor are enabled/shown.
     */
    private void Editor_EnableSelect()
    {
        Controller_TabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // if switching away from an editor Editor_Tab, then deactivate the controls/visibility for it.
                    Editor editor = Controller_Editors.get(oldValue);

                    if (editor != null) // if the old Editor_Tab was an editor then deactivate it.
                        Editor_Deactivate(editor);

                    editor = Controller_Editors.get(newValue);

                    if (newValue != null) // if the new Editor_Tab selected is an editor then activate it.
                        Editor_Activate(editor);
                }
        );
    }

    /**
     * Symbol Libraries, Footprint Libraries, Schematics, PCBs are all handled by their own Controller_Editors. Editors
     * supply their own controllers and controls. The EDAmameController folds these controls into its own
     * Editor_Menus, toolbars and windows by obtaining, reparenting and controlling the visibility of those controls.
     *
     * Editors are activated by EDAmame by making sure that only the selected Editor_Tab's controls are visible and
     * accessible.
     *
     * @param editor Which editor to make controls visible for.
     */
    private void Editor_Activate(Editor editor)
    {
        if (editor == null)
            return;

        // Making editor toolbars visible...
        ToolBar bar = editor.Editor_GetToolBar();

        if (bar != null)
            editor.Editor_GetToolBar().setVisible(true);

        // Adding all the editor control tabs...
        Controller_TabPaneControls.getTabs().addAll(editor.Editor_GetControlTabs());

        // Showing any editor's menus...
        for (int i = 0; i < this.Controller_MenuBar.getMenus().size(); i++)
        {
            Menu currMenu = this.Controller_MenuBar.getMenus().get(i);
            String currMenuId = currMenu.getId();

            if ((currMenuId != null) && currMenuId.contains("dynamic") && currMenuId.contains(editor.Editor_ID))
                currMenu.setVisible(true);
        }

        editor.Editor_Visible = true;
    }

    /**
     * Editors are deactivated by EDAmame by making sure that unselected editor Editor_Tabs' controls are invisible and
     * inaccessible.
     *
     * @param editor Which editor to make controls invisible for.
     */
    private void Editor_Deactivate(Editor editor)
    {
        // Removing any active property windows for the active editor...
        if (EDAmameController.Controller_EditorPropertiesWindow != null)
            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_Stage.close();

        // Removing any active toolbars...
        editor.Editor_GetToolBar().setVisible(false);

        // Removing any active control tabs...
        Controller_TabPaneControls.getTabs().removeAll(editor.Editor_GetControlTabs());

        // Hiding all the editor's menus...
        for (int i = 0; i < this.Controller_MenuBar.getMenus().size(); i++)
        {
            Menu currMenu = this.Controller_MenuBar.getMenus().get(i);
            String currMenuId = currMenu.getId();

            if ((currMenuId != null) && currMenuId.contains("dynamic") && currMenuId.contains(editor.Editor_ID))
                currMenu.setVisible(false);
        }

        editor.Editor_Visible = false;
    }

    /**
     * Editors are added by EDAmame by including their menu items and toolbars in its Editor_Menus and toolbar areas.
     *
     * @param editor Which editor to setup and add controls for. They all start out invisible/unavailable.
     */
    private void Editor_Add(Editor editor, MenuBarPriority menuBarPriority)
    {
        Tab editorTab = editor.Editor_GetTab();

        // Adding editor's main tab...
        Controller_Editors.put(editorTab, editor);

        if (editorTab != null)
        {
            // Adding a close callback
            editorTab.setOnCloseRequest(e -> {
                // attempt to close the editor
                if (editor.close())
                {
                    Editor_Deactivate(editor);
                    Editor_Remove(editor);
                }
                else
                {
                    e.consume();
                }
            });

            // Adding all the Editor_Tabs
            Controller_TabPane.getTabs().add(editorTab);

            // Preparing the canvas
            //editor.renderSystem.BindSize((Node)Controller_LogText);
        }

        // Make sure the toolbar is invisible
        editor.Editor_GetToolBar().setVisible(false);
        // add the editor's toolbar to the toolbar stack. The
        Controller_StackPaneEditorToolBars.getChildren().add(editor.Editor_GetToolBar());

        // The control Editor_Tabs don't need to be handled. Control of their
        // visibility done through addition and removal from the children
        // of the control TabPane.

        // Adding all the editor's menus...
        {
            List<Menu> dynamicMenus = createMenusFromConfig(menuBarPriority, editor);

            for (int i = 0; i < dynamicMenus.size(); i++)
            {
                Menu currMenu = dynamicMenus.get(i);
                currMenu.setId("dynamic_" + editor.Editor_ID + "_" + i);
                Controller_MenuBar.getMenus().add(currMenu);
            }
        }

        // move the log Editor_Tab to the end.
        if (Controller_TabPane.getTabs().contains(Controller_TabLog))
        {
            Controller_TabPane.getTabs().remove(Controller_TabLog);
            Controller_TabPane.getTabs().add(Controller_TabLog);
        }

        // Select the new Editor_Tab
        Controller_TabPane.getSelectionModel().select(editorTab);
    }

    /**
     * Editors are removed by EDAmame by removing their menu items and toolbars in its Editor_Menus and toolbar areas.
     * This should really only be done once an editor has been closed and has no need to save data.
     *
     * @param editor Which editor to remove controls for.
     */
    private void Editor_Remove(Editor editor)
    {
        Editor_Deactivate(editor);

        // Removing any editor's toolbars...
        Controller_StackPaneEditorToolBars.getChildren().remove(editor.Editor_GetToolBar());

        // Removing any editor's control tabs...
        Controller_TabPaneControls.getTabs().removeAll(editor.Editor_GetControlTabs());

        // Removing any editor's menus...
        for (int i = 0; i < this.Controller_MenuBar.getMenus().size(); i++)
        {
            Menu currMenu = this.Controller_MenuBar.getMenus().get(i);
            String currMenuId = currMenu.getId();

            if ((currMenuId != null) && currMenuId.contains("dynamic") && currMenuId.contains(editor.Editor_ID))
                Controller_MenuBar.getMenus().remove(currMenu);
        }

        // Remove the main editor tab...
        Controller_TabPane.getTabs().remove(editor.Editor_GetTab());
    }

    public List<Menu> createMenusFromConfig(MenuBarPriority menuBarPriority, Editor editor)
    {
        List<Menu> menus = new ArrayList<>();

        menuBarPriority.getMenuPriorities().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparing(MenuPriority::getPriority)))
                .forEach(menuEntry -> {
                    Menu menu = new Menu(menuEntry.getKey());

                    menuEntry.getValue().getItemPriorities().entrySet().stream()
                            .sorted(Map.Entry.comparingByValue())
                            .forEach(itemEntry -> {
                                MenuItem menuItem = new MenuItem(itemEntry.getKey());
                                menuItem.setOnAction(event -> setMenuItemActions(menuItem, editor));
                                menu.getItems().add(menuItem);
                            });

                    menus.add(menu);
                });
        return menus;
    }

    // TODO: All menu item actions need to be added here
    private void setMenuItemActions(MenuItem menuItem, Editor editor)
    {
        String itemName = menuItem.getText();

        switch(itemName) {
            case "Exit":
                menuItem.setOnAction(event -> {
                    Editor_Remove(editor);
                });
                break;
            case "Open":
                menuItem.setOnAction(event -> {
                    // Open file
                });
                break;
            default:
                // Error Handling
                break;
        }
    }

    //// SAVING & LOADING FUNCTIONS ////
    
    /**
     * Saves the window position and size of the application as Java Preferences.
     *
     * The values of the size and position of the application are saved to the Java Preference storage.
     * User preferences and settings for application behavior are stored in YAML files to avoid overly
     * complicating platform dependent stores such as registries.
     */
    private void Controller_WindowContextSave()
    {
        Preferences prefs = Preferences.userRoot().node("EDAmame");
        prefs.putDouble("WINDOW_POSITION_X", Controller_Stage.getX());
        prefs.putDouble("WINDOW_POSITION_Y", Controller_Stage.getY());
        prefs.putDouble("WINDOW_WIDTH", Controller_Stage.getWidth());
        prefs.putDouble("WINDOW_HEIGHT", Controller_Stage.getHeight());
        double pos = Controller_SplitPane.getDividerPositions()[0];
        prefs.putDouble("DIVIDER_POSITION", pos);
    }

    /**
     * Restores the window position and size of the application.
     *
     * The values of the size and position of the application when this context was last saved is restored.
     * Context is typically saved when the application exits gracefully.
     */
    private void Controller_WindowContextLoad()
    {
        Preferences prefs = Preferences.userRoot().node("EDAmame");

        // Save the application's current size
        Controller_Stage.setWidth(prefs.getDouble("WINDOW_WIDTH",800));
        Controller_Stage.setHeight(prefs.getDouble("WINDOW_HEIGHT",1000));

        // Save the current position of the application
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double x = prefs.getDouble("WINDOW_POSITION_X", (bounds.getWidth()-Controller_Stage.getWidth())/2);
        double y = prefs.getDouble("WINDOW_POSITION_Y", (bounds.getHeight()-Controller_Stage.getHeight())/2);
        // If the application is off the screen then adjust it so that it lies within the screen's boundaries.
        if (x>bounds.getWidth()-Controller_Stage.getWidth()) x=bounds.getWidth()-Controller_Stage.getWidth();
        if (x<0) x=0;
        if (y>bounds.getHeight()-Controller_Stage.getHeight()) y=bounds.getHeight()-Controller_Stage.getHeight();
        if (y<0) y=0;

        Controller_Stage.setX(x);
        Controller_Stage.setY(y);

        //String home = System.getProperty("user.home");
        Controller_Logger.log(Level.INFO, "EDAmame window context restored.\n");
    }

    /**
     * Restores the main split pane's divider position between runs of the application.<p>
     *
     * The application establishes a main look and feel that consists of a split pane containing two nodes.
     * The left node is used to contain to level controls specific to the entire EDAmame application. The
     * right pane is a Editor_Tab pane that provides tabbed locations for Controller_Editors and other sub-modules to place
     * their controls. The user's selection of divider position is maintained between runs of the application.
     */
    private void Controller_DividerRestore()
    {
        Preferences prefs = Preferences.userRoot().node("EDAmame");

        // Ugh. The windows/Controller_Stage is supposedly "shown" at this point but the children are not
        // yet *really* shown. So really... what you probably need to work on here hasn't been rendered and its
        // layout hasn't been finalized despite being "shown". So we need the horrid "runLater" which is
        // *wildly* undefined as to "when".
        try {
            double value = prefs.getDouble("DIVIDER_POSITION",Double.MAX_VALUE);
            if (value != Double.MAX_VALUE)
                Platform.runLater(() -> Controller_SplitPane.setDividerPositions(value));
        } catch (Throwable ignored) {
        }
    }

    //// CALLBACK FUNCTIONS ////

    /**
     * Controller_Exit() implements graceful and intelligent shutdown of the EDAmame application.
     *
     * All open Controller_Editors will be requested to perform their close/save/shutdown activities and the EDAmame
     * application will exit gracefully. This action is triggered by menu items or On Close Request.
     */
    @FXML
    private void Controller_Exit()
    {
        Controller_WindowContextSave();
        Controller_Logger.log(Level.INFO, "EDAmame exited gracefully.\n");
        Platform.exit();
    }

    /**
     * A simple event handle to clear the TextArea/Pane where logging happens.
     */
    @FXML
    protected void Controller_LogClear()
    {
        Controller_LogText.clear();
    }

    /**
     * The log Editor_Tab can be made visible or not. It is always the right most Editor_Tab of all Editor_Tabs. When explicitly added
     * the log Editor_Tab is automatically selected.
     */
    private void Controller_LogToggleVisibility()
    {
        if (Controller_TabPane.getTabs().contains(Controller_TabLog))
        {
            Controller_TabPane.getTabs().remove(Controller_TabLog);
        }
        else
        {
            Controller_TabPane.getTabs().add(Controller_TabLog);
            Controller_TabPane.getSelectionModel().select(Controller_TabLog);
        }

        Controller_LogToggleItemText();
    }

    /**
     * Simple function to correct the textual information present on the menu item for toggling the state of
     * the log window.
     */
    @FXML
    private void Controller_LogToggleItemText()
    {
        if (Controller_TabPane.getTabs().contains(Controller_TabLog))
            Controller_MenuLogItem.setText("Hide Log Tab");
        else
            Controller_MenuLogItem.setText("Show Log Tab");
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
    private void Controller_NavigationTabPaneSelect()
    {
        Controller_StackPaneControls.getChildren().forEach(e -> e.setVisible(e==Controller_TabPaneControls));
    }

    @FXML
    public void Controller_OnKeyPressed(KeyEvent event)
    {
        // Adding pressed key to the pressed keys list...
        EDAmameController.Controller_KeyPressed(event.getCode());

        // Calling editor callbacks...
        ObservableList<Tab> tabs = Controller_TabPane.getTabs();

        for (int i = 0; i < tabs.size(); i++)
        {
            Tab tab = tabs.get(i);

            if (!tab.getText().equals("Log"))
            {
                Editor editor = this.Controller_Editors.get(tab);

                if ((editor != null) && editor.Editor_Visible)
                    editor.Editor_RenderSystem.RenderSystem_OnKeyPressed(event);
            }
        }

        event.consume();
    }

    @FXML
    public void Controller_OnKeyReleased(KeyEvent event)
    {
        // Removing pressed key from the pressed keys list...
        EDAmameController.Controller_KeyReleased(event.getCode());

        // Calling editor callbacks...
        ObservableList<Tab> tabs = Controller_TabPane.getTabs();

        for (int i = 0; i < tabs.size(); i++)
        {
            Tab tab = tabs.get(i);

            if (!tab.getText().equals("Log"))
            {
                Editor editor = this.Controller_Editors.get(tab);

                if ((editor != null) && editor.Editor_Visible)
                    editor.Editor_RenderSystem.RenderSystem_OnKeyReleased(event);
            }
        }

        event.consume();
    }

    //// SUPPORT FUNCTIONS ////

    static public <T> boolean Controller_IsListAllEqual(LinkedList<T> list)
    {
        return list.stream().distinct().limit(2).count() <= 1;
    }

    static public Integer Controller_FindNodeById(ObservableList<Node> nodes, String id)
    {
        for (int i = 0; i < nodes.size(); i++)
            if ((nodes.get(i).getId() != null) && nodes.get(i).getId().equals(id))
                return i;

        return -1;
    }

    static public Node Controller_GetNodeById(ObservableList<Node> nodes, String id)
    {
        for (int i = 0; i < nodes.size(); i++)
            if ((nodes.get(i).getId() != null) && nodes.get(i).getId().equals(id))
                return nodes.get(i);

        return null;
    }

    static public boolean Controller_IsStringNum(String str)
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

    static public boolean Controller_IsKeyPressed(KeyCode key)
    {
        return Controller_PressedKeys.contains(key);
    }

    static public void Controller_KeyPressed(KeyCode key)
    {
        if (!Controller_IsKeyPressed(key))
            Controller_PressedKeys.add(key);
    }

    static public void Controller_KeyReleased(KeyCode key)
    {
        if (Controller_IsKeyPressed(key))
            Controller_PressedKeys.remove(key);
    }

    //// TESTING FUNCTIONS ////

    /**
     * A test method for a test button to add a fake editor to the page to verify concept.
     */
    @FXML
    protected void EditorSymbolNewButton()
    {
        try
        {
            if (Controller_EditorMenuConfig == null)
            {
                System.out.println("editorsConfig is null!");
                return;  // exit the method if editorsConfig is null
            }

            MenuBarPriority menuBarPriorityForSymbolEditor = Controller_EditorMenuConfig.get("SymbolEditor");

            if (menuBarPriorityForSymbolEditor == null)
            {
                System.out.println("menuBarPriorityForSymbolEditor is null!");
                return;  // exit the method if menuBarPriorityForSymbolEditor is null
            }

            Editor editorInstance = EditorFactory.createEditor("SymbolEditor");

            if (editorInstance == null)
            {
                System.out.println("editorInstance is null!");
                return;  // exit the method if editorInstance is null
            }

            Editor_Add(editorInstance, menuBarPriorityForSymbolEditor);
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    @FXML
    protected void EditorFootprintNewButton()
    {
        try {
            if (Controller_EditorMenuConfig == null) {
                System.out.println("editorsConfig is null!");
                return;  // exit the method if editorsConfig is null
            }

            MenuBarPriority menuBarPriorityForFootprintEditor = Controller_EditorMenuConfig.get("FootprintEditor");

            if (menuBarPriorityForFootprintEditor == null)
            {
                System.out.println("menuBarPriorityForFootprintEditor is null!");
                return;  // exit the method if menuBarPriorityForFootprintEditor is null
            }

            Editor editorInstance = EditorFactory.createEditor("FootprintEditor");

            if (editorInstance == null)
            {
                System.out.println("editorInstance is null!");
                return;  // exit the method if editorInstance is null
            }

            Editor_Add(editorInstance, menuBarPriorityForFootprintEditor);
        }
        catch (IOException exception)
        {
            System.out.println("ERROR: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    /** A test method for checking database connectivity. */
    @FXML
    protected void onTestButtonClickDatabase()
    {
        System.out.println("hi");

        String classpath = System.getProperty("java.class.path");
        String[] classPathValues = classpath.split(File.pathSeparator);
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
    @FXML
    void performExitAction(ActionEvent event) {
        System.exit(0);
    }
}
