/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

// TODO:
// Fix viewport jolting when dropping first shape
// Implement shape dropping in symbol editor
// Implement line drawing in symbol editor
// Implement wire connection points into symbols
// Implement shape selection
// Implement shape moving
// Implement shape deletion
// Implement shape properties window
// Implement text dropping
// Refactor the stupid canvas zooming mouse diff pos thing
// Refactor dissect editor function searching for canvas
// Fix mouse-specific release callback function

package com.cyte.edamame;
import com.cyte.edamame.editor.EditorSymbol;
import com.cyte.edamame.render.RenderShape;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.util.TextAreaHandler;

import java.util.LinkedList;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import javafx.application.*;
import javafx.geometry.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ListIterator;
import java.util.ResourceBundle;
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

    final static public String[] Editor_Names = {"Symbol Editor"};
    final static public Double Editor_HeartbeatDelay = 0.01;

    final static public PairMutable Editor_TheaterSize = new PairMutable(1000.0, 1000.0);
    final static public Color Editor_BackgroundColor = Color.BEIGE;
    final static public Integer Editor_MaxShapes = 10000;
    final static public PairMutable Editor_ZoomLimits = new PairMutable(0.5, 5.0);
    final static public Double Editor_ZoomFactor = 1.5;
    final static public Double Editor_MouseDragFactor = 1.0;
    final static public Double Editor_MouseCheckTimeout = 0.0001;

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
    private MenuItem Controller_MenuLogItem;                // The menu item for toggling log visibility.

    // DO NOT EDIT

    private final Stage Controller_Stage;                                                              // The Controller_Stage hosting this controller.
    private final ObservableMap<Tab, Editor> Controller_Editors = FXCollections.observableHashMap();   // All Controller_Editors instantiated are remembered in a HashMap for fast lookup keyed by their main Editor_Tab.

    public Timeline Editor_HeartbeatTimeline;

    static public LinkedList<KeyCode> Controller_PressedKeys = new LinkedList<KeyCode>();

    static public LinkedList<RenderShape> Controller_BasicShapes = new LinkedList<RenderShape>();

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

        CreateBasicCanvasShapes();
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
        this.Editor_HeartbeatTimeline = new Timeline(new KeyFrame(Duration.seconds(Editor_HeartbeatDelay), e -> this.Editor_Heartbeat()));
        this.Editor_HeartbeatTimeline.setCycleCount(Animation.INDEFINITE);
        this.Editor_HeartbeatTimeline.playFromStart();

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

    public void Editor_Heartbeat()
    {
        ObservableList<Tab> Editor_Tabs = Controller_TabPane.getTabs();

        // Checking all Editor_Tabs in the main Editor_Tab pane...
        for (int i = 0; i < Editor_Tabs.size(); i++)
        {
            Tab Editor_Tab = Editor_Tabs.get(i);

            // Handling symbol Controller_Editors
            if (Editor_Tab.getText().equals("Symbol Editor"))
            {
                Editor editor = this.Controller_Editors.get(Editor_Tab);

                if (!editor.Editor_Visible)
                    continue;

                // Adjusting the central layout of the canvas relative to the stack pane size
                {
                    PairMutable canvasSize = new PairMutable(editor.Editor_RenderSystem.canvas.getWidth(),
                                                             editor.Editor_RenderSystem.canvas.getHeight());
                    PairMutable paneSize = new PairMutable(editor.Editor_RenderSystem.paneListener.getWidth(),
                                                           editor.Editor_RenderSystem.paneListener.getHeight());
                    PairMutable centeredPos = new PairMutable(paneSize.GetLeftDouble() / 2 - canvasSize.GetLeftDouble() / 2,
                                                              paneSize.GetRightDouble() / 2 - canvasSize.GetRightDouble() / 2);

                    editor.Editor_RenderSystem.paneHolder.setLayoutX(centeredPos.GetLeftDouble());
                    editor.Editor_RenderSystem.paneHolder.setLayoutY(centeredPos.GetRightDouble());
                }
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
        // Select this editor in the panes and make all it's controls available.
        if (editor != null) {
            // set the editor ToolBars to not visible
            ToolBar bar = editor.Editor_GetToolBar();

            if (bar != null)
                editor.Editor_GetToolBar().setVisible(true);

            // add all the control Editor_Tabs for the editor
            Controller_TabPaneControls.getTabs().addAll(editor.Editor_GetControlTabs());

            // change all of this editor's menu items to be visible.
            for (ObservableList<MenuItem> items : editor.Editor_GetMenus().values())
                for (MenuItem menuitem : items)
                    menuitem.setVisible(true);

            editor.Editor_Visible = true;

            //editor.Editor_RenderSystem.Clear();
        }
    }

    /**
     * Editors are deactivated by EDAmame by making sure that unselected editor Editor_Tabs' controls are invisible and
     * inaccessible.
     *
     * @param editor Which editor to make controls invisible for.
     */
    private void Editor_Deactivate(Editor editor)
    {
        // set the editor ToolBars to not visible
        editor.Editor_GetToolBar().setVisible(false);

        // remove all control Editor_Tabs from the controls Editor_Tab pane
        Controller_TabPaneControls.getTabs().removeAll(editor.Editor_GetControlTabs());

        // change all the menu items to not visible
        for (ObservableList<MenuItem> items : editor.Editor_GetMenus().values())
            for (MenuItem menuitem : items)
                menuitem.setVisible(false);

        editor.Editor_Visible = false;
    }

    /**
     * Editors are added by EDAmame by including their menu items and toolbars in its Editor_Menus and toolbar areas.
     *
     * @param editor Which editor to setup and add controls for. They all start out invisible/unavailable.
     */
    private void Editor_Add(Editor editor)
    {
        Tab editorTab = editor.Editor_GetTab();

        Controller_Editors.put(editorTab, editor);

        // Place the main editor window in the TabPane
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

        // add all the Editor_Menus of the editor to the EDAmame MenuBar
        for (Menu menu : Controller_MenuBar.getMenus())
        {
            // get all the editor's menu items for this menu name.
            ObservableList<MenuItem> editoritems = editor.Editor_GetMenus().get(menu.getText());

            if (editoritems != null)
            {
                // get an iterator for this menu's items
                ListIterator<MenuItem> iterator = menu.getItems().listIterator();

                // advance the iterator to the either the end of the menu or until a seperator with ID "editorItemsBegin"
                boolean found = false;

                while (iterator.hasNext() && !found)
                {
                    MenuItem item = iterator.next();

                    if (item.getId() != null && item.getId().equals("editorItemsBegin"))
                        found = true;
                }

                for (MenuItem item : editoritems)
                {
                    item.setVisible(false);
                    iterator.add(item);
                }
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
        Editor_Deactivate(editor); // make sure the editor is not visible/active.

        // remove the toolbar
        Controller_StackPaneEditorToolBars.getChildren().remove(editor.Editor_GetToolBar());

        // remove control Editor_Tabs
        Controller_TabPaneControls.getTabs().removeAll(editor.Editor_GetControlTabs());

        // remove all menu items
        for (Menu menu: Controller_MenuBar.getMenus())
            for (ObservableList<MenuItem> itemlist: editor.Editor_GetMenus().values())
                menu.getItems().removeAll(itemlist);

        // remove the main editor Editor_Tab itself.
        Controller_TabPane.getTabs().remove(editor.Editor_GetTab());
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
    @FXML
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
    public void Controller_KeyPress(KeyEvent event)
    {
        // Adding pressed key to the pressed keys list
        if (!Controller_IsKeyPressed(event.getCode()))
            Controller_PressedKeys.add(event.getCode());

        // Handling symbol deletion
        /*if (this.EditorSchematic_IsKeyPressed(KeyCode.BACK_SPACE) || this.EditorSchematic_IsKeyPressed(KeyCode.DELETE))
        {
            Integer i = 0;

            while (i < this.EditorSchematic_ViewportSymbolsDropped.size())
            {
                EditorSchematic_Symbol symbol = this.EditorSchematic_ViewportSymbolsDropped.get(i);

                if (symbol.selected)
                {
                    this.EditorSchematic_ViewportSymbolsDropped.remove(symbol);
                    i--;
                }

                i++;
            }
        }*/

        event.consume();
    }

    @FXML
    public void Controller_KeyRelease(KeyEvent event)
    {
        if (Controller_IsKeyPressed(event.getCode()))
            Controller_PressedKeys.remove(event.getCode());

        event.consume();
    }

    //// SUPPORT FUNCTIONS ////

    static public boolean Controller_IsKeyPressed(KeyCode key)
    {
        return Controller_PressedKeys.contains(key);
    }

    //// TESTING FUNCTIONS ////

    static public void CreateBasicCanvasShapes()
    {
        RenderShape gridPoint = new RenderShape("GridPoint", 1);
        gridPoint.AddPoint(0.0, 0.0, 5.0, Color.GRAY, 0.5);
        //gridPoint.permanent = true;

        Controller_BasicShapes.add(gridPoint);

        RenderShape gridBox = new RenderShape("GridBox", 1);
        gridBox.AddPoint(-Editor_TheaterSize.GetLeftDouble() / 2, -Editor_TheaterSize.GetRightDouble() / 2, 0.0, Color.BLACK, 1.0);
        gridBox.AddPoint(Editor_TheaterSize.GetLeftDouble() / 2, -Editor_TheaterSize.GetRightDouble() / 2, 0.0, Color.BLACK, 1.0);
        gridBox.AddPoint(Editor_TheaterSize.GetLeftDouble() / 2, Editor_TheaterSize.GetRightDouble() / 2, 0.0, Color.BLACK, 1.0);
        gridBox.AddPoint(-Editor_TheaterSize.GetLeftDouble() / 2, Editor_TheaterSize.GetRightDouble() / 2, 0.0, Color.BLACK, 1.0);
        gridBox.AddLine(0, 1, 1.5, Color.BLACK, 1.0);
        gridBox.AddLine(1, 2, 1.5, Color.BLACK, 1.0);
        gridBox.AddLine(2, 3, 1.5, Color.BLACK, 1.0);
        gridBox.AddLine(3, 0, 1.5, Color.BLACK, 1.0);
        //gridBox.permanent = true;

        Controller_BasicShapes.add(gridBox);

        RenderShape crosshair = new RenderShape("Crosshair", 1);
        crosshair.AddPoint(0.0, -5.0, 0.0, Color.RED, 1.0);
        crosshair.AddPoint(0.0, 5.0, 0.0, Color.RED, 1.0);
        crosshair.AddPoint(-5.0, 0.0, 0.0, Color.RED, 1.0);
        crosshair.AddPoint(5.0, 0.0, 0.0, Color.RED, 1.0);
        crosshair.AddLine(0, 1, 0.5, Color.RED, 1.0);
        crosshair.AddLine(2, 3, 0.5, Color.RED, 1.0);
        //crosshair.zoomScaling = false;
        //crosshair.permanent = true;
        //crosshair.posStatic = true;

        Controller_BasicShapes.add(crosshair);
    }

    /**
     * A test method for a test button to add a fake editor to the page to verify concept.
     */
    @FXML
    protected void onTestButtonClick()
    {
        try
        {
            // create and add a new editor
            Editor_Add(EditorSymbol.create());
        }
        catch (IOException e)
        {
            System.out.println("ERROR!");
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
}