/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

// TODO:
// Ask about editor sub-class overriding
// Ask about symbol drawing method
// REFACTOR ALL COMMENTS
// REFACTOR ALL FUNCTIONS & FUNCTION NAMES
// Fix slow rendering
// Implement dropping onto canvas
// Fix mouse-specific release callback function

// Implement wire connection edges (into symbols)
// Implement wires
// Implement save & load features
// Implement symbol editor
// Implement undo-redo functions
// Fix symbols not moving when viewport center on theater edges
// Fix symbols pushing away from theater edges when zooming
// Add zooming into cursor
// Add symbol rotation while dropping
// Fix symbol edge moving
// Add symbol selection box blending
// Fix symbol highlights only triggering when cursor moved
// Fix symbol highlights not appearing instantly
// Fix symbol highlights top-only not triggering instantly
// Fix symbols remaining highlighted for a single frame when zooming
// Implement transition to PCB

package com.cyte.edamame;
import com.cyte.edamame.render.CanvasRenderShape;
import com.cyte.edamame.util.PairMutable;
import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.editor.SymbolEditor;
import com.cyte.edamame.util.TextAreaHandler;

import java.util.LinkedList;

import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;
import javafx.scene.input.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
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

    final static public String[] EditorTypes = {"Symbol Editor"};
    final static public Double EditorsHeartbeatDelay = 0.01;

    final static public PairMutable EditorsTheaterSize = new PairMutable(1000.0, 1000.0);
    final static public Color EditorsBackgroundColor = Color.BEIGE;
    final static public Integer EditorsMaxShapes = 10000;
    final static public PairMutable EditorsZoomLimits = new PairMutable(0.5, 5.0);
    final static public Double EditorsZoomFactor = 1.5;
    final static public Double EditorsMouseDragFactor = 1.0;
    final static public Double EditorsMouseCheckTimeout = 0.0001;

    final static public Logger LOGGER = Logger.getLogger(EDAmame.class.getName());     // The logger for the entire application. All classes/modules should obtain and use this static logger.

    @FXML
    private Tab logTab;                     // The tab used to hold the TextArea used for application logging.
    @FXML
    private TextArea logArea;               // The TextArea to display logged information on.
    @FXML
    private SplitPane splitPane;            // The main split pane separating always available global controls on the left from editor tabs on right.
    @FXML
    private StackPane controlsStackPane;    // The stack pane on left of split used to contain different control setups.
    @FXML
    private TabPane controlTabPane;         // The TabPane used to hold all the main controls. Displayed in the "navigation" pane of the controls stack.
    @FXML
    private TabPane mainTabPane;            // Primary TabPane displayed on the right of split used by EDAmame to present editors and information.
    @FXML
    private StackPane editorToolBarStack;   // A stack of toolbars to the right of the main EDAmame toolbar. Editors can provide own toolbars for here.
    @FXML
    private MenuBar menuBar;                // The main EDAmame menu bar.
    @FXML
    private MenuItem viewLogItem;           // The menu item for toggling log visibility.

    // DO NOT EDIT

    private final Stage stage;                                                              // The stage hosting this controller.
    private final ObservableMap<Tab, Editor> editors = FXCollections.observableHashMap();   // All editors instantiated are remembered in a HashMap for fast lookup keyed by their main tab.

    public Timeline editorsHeartbeatTimeline;

    static public LinkedList<KeyCode> pressedKeys = new LinkedList<KeyCode>();

    static public LinkedList<CanvasRenderShape> basicShapes = new LinkedList<CanvasRenderShape>();

    //// MAIN FUNCTIONS ////

    /**
     * Constructs an instance of this controller with knowledge of the stage that it is attached to. Knowledge of
     * the stage allows the controller to entirely take control of proper closing activities even when its an
     * onCloseRequest generated by a user killing the application of an operating system's "close" window
     * decoration.
     *
     * @param stage The stage that is hosting/using this controller.
     */
    public EDAmameController(Stage stage)
    {
        this.stage = stage;
        stage.setOnShown((event) -> executeOnShown());

        CreateBasicCanvasShapes();
    }

    /**
     * JavaFX stage initialization procedures
     *
     * @param url
     * The location used to resolve relative paths for the root object, or
     * {@code null} if the location is not known.
     *
     * @param rb
     * The resources used to localize the root object, or {@code null} if
     * the root object was not localized.
     */
    public void initialize(URL url, ResourceBundle rb)
    {
        changeLoggingToTab();
        LOGGER.log(Level.INFO, "Initialization Commenced...\n");

        // Set the stage to close gracefully.
        stage.setOnCloseRequest(evt -> { evt.consume(); performExit(); });
        LOGGER.log(Level.INFO, "Stage configured to close gracefully.\n");

        // Changing tabs in the main tab pane is a significant task for changing
        // between editors and modules. This logic is handled through tab change events.
        enableEditorTabSelectionLogic();

        // Restore the previous location and size of windows. (Location of split pane dividers needs
        // to be handled elsewhere since layout of nodes isn't completed at time of initialization.
        restoreWindowContext();

        // Initializing editor heartbeat loops
        this.editorsHeartbeatTimeline = new Timeline(new KeyFrame(Duration.seconds(this.EditorsHeartbeatDelay), e -> this.editorsHeartbeat()));
        this.editorsHeartbeatTimeline.setCycleCount(Animation.INDEFINITE);
        this.editorsHeartbeatTimeline.playFromStart();

        // correct the text in the show log menu item
        correctViewLogItemText();
        LOGGER.log(Level.INFO, "Initialization Complete\n");
    }

    /**
     * Execute activites required to be done once the main Application stage is finally shown. (Activities
     * performed in the {@link EDAmameController#initialize(URL, ResourceBundle)} happen before the various
     * scene nodes have been sized, laid out, and rendered. This provides a location to perform all
     * startup tasks required but unsuitable for standard initialization.
     */
    private void executeOnShown()
    {
        restoreDividerPosition();
    }

    //// LOGGING FUNCTIONS ////

    /**
     * Change the log handler for the package wide logger from stdout to a TextArea node.
     *
     * EDAmame has a dedicated tab for displaying log information. changeLoggingToTab
     * removes all log handlers from the logger and replaces them with a handler that
     * appends requested logging information to a TextArea in the dedicated log tab.
     */
    private void changeLoggingToTab()
    {
        for (Handler handler : LOGGER.getHandlers())
            LOGGER.removeHandler(handler);

        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(new TextAreaHandler(logArea));
    }

    //// EDITOR FUNCTIONS ////

    public void editorsHeartbeat()
    {
        ObservableList<Tab> tabs = mainTabPane.getTabs();

        // Checking all tabs in the main tab pane...
        for (int i = 0; i < tabs.size(); i++)
        {
            Tab tab = tabs.get(i);

            // Handling symbol editors
            if (tab.getText().equals("Symbol Editor"))
            {
                Editor editor = this.editors.get(tab);

                if (!editor.visible)
                    continue;

                editor.renderSystem.Render();
            }
        }
    }

    /**
     * Enables to controller logic for switching between editor tabs.
     *
     * Editors are composed of several nodes.
     *     A single main editor tab that is always visible in editorTabPane.
     *     A set of control tabs that are only present/visible in the controlTabPane when the editor is selected.
     *     A toolBar that is only visible in the toolBar area when the editor is selected.
     *     A set of menus and items that are only present in the menus when the editor is selected.
     *
     * enableEditorTabSelectionLogic provides the control for the presence and visibility of these items
     * based on tab selection. When a tab is selected the UI components for the previously selected editor
     * are removed/hidden and the components for the newly selected editor are enabled/shown.
     */
    private void enableEditorTabSelectionLogic()
    {
        mainTabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // if switching away from an editor tab, then deactivate the controls/visibility for it.
                    Editor editor = editors.get(oldValue);

                    if (editor != null) // if the old tab was an editor then deactivate it.
                        deactivateEditor(editor);

                    editor = editors.get(newValue);

                    if (newValue != null) // if the new tab selected is an editor then activate it.
                        activateEditor(editor);
                }
        );
    }

    /**
     * Symbol Libraries, Footprint Libraries, Schematics, PCBs are all handled by their own editors. Editors
     * supply their own controllers and controls. The EDAmameController folds these controls into its own
     * menus, toolbars and windows by obtaining, reparenting and controlling the visibility of those controls.
     *
     * Editors are activated by EDAmame by making sure that only the selected tab's controls are visible and
     * accessible.
     *
     * @param editor Which editor to make controls visible for.
     */
    private void activateEditor(Editor editor)
    {
        // Select this editor in the panes and make all it's controls available.
        if (editor != null) {
            // set the editor ToolBars to not visible
            ToolBar bar = editor.getToolBar();

            if (bar != null)
                editor.getToolBar().setVisible(true);

            // add all the control tabs for the editor
            controlTabPane.getTabs().addAll(editor.getControlTabs());

            // change all of this editor's menu items to be visible.
            for (ObservableList<MenuItem> items : editor.getMenus().values())
                for (MenuItem menuitem : items)
                    menuitem.setVisible(true);

            editor.visible = true;
            //editor.renderSystem.Clear();
            //editor.canvasSettings.setMaxWidth(25);
            //editor.canvasSettings.setMinWidth(25);
            //editor.canvasSettings.setPrefWidth(25);
            //System.out.println(new PairMutable(editor.canvasSettings.getWidth(), editor.canvasSettings.getHeight()).ToStringDouble());
        }
    }

    /**
     * Editors are deactivated by EDAmame by making sure that unselected editor tabs' controls are invisible and
     * inaccessible.
     *
     * @param editor Which editor to make controls invisible for.
     */
    private void deactivateEditor(Editor editor)
    {
        // set the editor ToolBars to not visible
        editor.getToolBar().setVisible(false);

        // remove all control tabs from the controls tab pane
        controlTabPane.getTabs().removeAll(editor.getControlTabs());

        // change all the menu items to not visible
        for (ObservableList<MenuItem> items : editor.getMenus().values())
            for (MenuItem menuitem : items)
                menuitem.setVisible(false);

        editor.visible = false;
    }

    /**
     * Editors are added by EDAmame by including their menu items and toolbars in its menus and toolbar areas.
     *
     * @param editor Which editor to setup and add controls for. They all start out invisible/unavailable.
     */
    private void addEditor(Editor editor)
    {
        Tab editorTab = editor.getEditorTab();

        editors.put(editorTab, editor);

        // Place the main editor window in the TabPane
        if (editorTab != null)
        {
            // Adding a close callback
            editorTab.setOnCloseRequest(e -> {
                // attempt to close the editor
                if (editor.close())
                {
                    deactivateEditor(editor);
                    removeEditor(editor);
                }
                else
                {
                    e.consume();
                }
            });

            // Adding all the tabs
            mainTabPane.getTabs().add(editorTab);

            // Preparing the canvas
            editor.renderSystem.BindSize((Node)logArea);
        }

        // Make sure the toolbar is invisible
        editor.getToolBar().setVisible(false);
        // add the editor's toolbar to the toolbar stack. The
        editorToolBarStack.getChildren().add(editor.getToolBar());

        // The control tabs don't need to be handled. Control of their
        // visibility done through addition and removal from the children
        // of the control TabPane.

        // add all the menus of the editor to the EDAmame MenuBar
        for (Menu menu : menuBar.getMenus())
        {
            // get all the editor's menu items for this menu name.
            ObservableList<MenuItem> editoritems = editor.getMenus().get(menu.getText());

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

        // move the log tab to the end.
        if (mainTabPane.getTabs().contains(logTab))
        {
            mainTabPane.getTabs().remove(logTab);
            mainTabPane.getTabs().add(logTab);
        }

        // Select the new tab
        mainTabPane.getSelectionModel().select(editorTab);
    }

    /**
     * Editors are removed by EDAmame by removing their menu items and toolbars in its menus and toolbar areas.
     * This should really only be done once an editor has been closed and has no need to save data.
     *
     * @param editor Which editor to remove controls for.
     */
    private void removeEditor(Editor editor)
    {
        deactivateEditor(editor); // make sure the editor is not visible/active.

        // remove the toolbar
        editorToolBarStack.getChildren().remove(editor.getToolBar());

        // remove control tabs
        controlTabPane.getTabs().removeAll(editor.getControlTabs());

        // remove all menu items
        for (Menu menu: menuBar.getMenus())
            for (ObservableList<MenuItem> itemlist: editor.getMenus().values())
                menu.getItems().removeAll(itemlist);

        // remove the main editor tab itself.
        mainTabPane.getTabs().remove(editor.getEditorTab());
    }

    /**
     * Saves the window position and size of the application as Java Preferences.
     *
     * The values of the size and position of the application are saved to the Java Preference storage.
     * User preferences and settings for application behavior are stored in YAML files to avoid overly
     * complicating platform dependent stores such as registries.
     */
    private void saveWindowContext()
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
    private void restoreWindowContext()
    {
        Preferences prefs = Preferences.userRoot().node("EDAmame");

        // Save the application's current size
        stage.setWidth(prefs.getDouble("WINDOW_WIDTH",800));
        stage.setHeight(prefs.getDouble("WINDOW_HEIGHT",1000));

        // Save the current position of the application
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double x = prefs.getDouble("WINDOW_POSITION_X", (bounds.getWidth()-stage.getWidth())/2);
        double y = prefs.getDouble("WINDOW_POSITION_Y", (bounds.getHeight()-stage.getHeight())/2);
        // If the application is off the screen then adjust it so that it lies within the screen's boundaries.
        if (x>bounds.getWidth()-stage.getWidth()) x=bounds.getWidth()-stage.getWidth();
        if (x<0) x=0;
        if (y>bounds.getHeight()-stage.getHeight()) y=bounds.getHeight()-stage.getHeight();
        if (y<0) y=0;

        stage.setX(x);
        stage.setY(y);

        //String home = System.getProperty("user.home");
        LOGGER.log(Level.INFO, "EDAmame window context restored.\n");
    }

    /**
     * Restores the main split pane's divider position between runs of the application.<p>
     *
     * The application establishes a main look and feel that consists of a split pane containing two nodes.
     * The left node is used to contain to level controls specific to the entire EDAmame application. The
     * right pane is a tab pane that provides tabbed locations for editors and other sub-modules to place
     * their controls. The user's selection of divider position is maintained between runs of the application.
     */
    private void restoreDividerPosition()
    {
        Preferences prefs = Preferences.userRoot().node("EDAmame");

        // Ugh. The windows/stage is supposedly "shown" at this point but the children are not
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

    /**
     * performExit() implements graceful and intelligent shutdown of the EDAmame application.
     *
     * All open editors will be requested to perform their close/save/shutdown activities and the EDAmame
     * application will exit gracefully. This action is triggered by menu items or On Close Request.
     */
    @FXML
    private void performExit()
    {
        saveWindowContext();
        LOGGER.log(Level.INFO, "EDAmame exited gracefully.\n");
        Platform.exit();
    }

    /**
     * A simple event handle to clear the TextArea/Pane where logging happens.
     */
    @FXML
    protected void clearLogAction()
    {
        logArea.clear();
    }

    /**
     * The log tab can be made visible or not. It is always the right most tab of all tabs. When explicitly added
     * the log tab is automatically selected.
     */
    @FXML
    private void toggleLogTabVisibility()
    {
        if (mainTabPane.getTabs().contains(logTab))
        {
            mainTabPane.getTabs().remove(logTab);
        }
        else
        {
            mainTabPane.getTabs().add(logTab);
            mainTabPane.getSelectionModel().select(logTab);
        }

        correctViewLogItemText();
    }

    /**
     * Simple function to correct the textual information present on the menu item for toggling the state of
     * the log window.
     */
    @FXML
    private void correctViewLogItemText()
    {
        if (mainTabPane.getTabs().contains(logTab))
            viewLogItem.setText("Hide Log Tab");
        else
            viewLogItem.setText("Show Log Tab");
    }

    /**
     * Select the "navigation" pane is the main controls to show.
     *
     * The left element of the main split pane is a global controls area and is designed to support future
     * features. It is a stacked pane containing as many controls as needed by EDAmame development. The primary
     * control is a tab pane. Editors add their controls into this tab pane as additional tabs. This primary
     * tab pane control is called the "Navigation" Pane.
     */
    @FXML
    private void selectNavigationTabPane()
    {
        controlsStackPane.getChildren().forEach(e -> e.setVisible(e==controlTabPane));
    }

    @FXML
    public void globalKeyPress(KeyEvent event)
    {
        // Adding pressed key to the pressed keys list
        if (!isGlobalKeyPressed(event.getCode()))
            pressedKeys.add(event.getCode());

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
    public void globalKeyRelease(KeyEvent event)
    {
        if (isGlobalKeyPressed(event.getCode()))
            pressedKeys.remove(event.getCode());

        event.consume();
    }

    //// SUPPORT FUNCTIONS ////

    static public boolean isGlobalKeyPressed(KeyCode key)
    {
        return pressedKeys.contains(key);
    }

    //// TESTING FUNCTIONS ////

    static public void CreateBasicCanvasShapes()
    {
        CanvasRenderShape gridPoint = new CanvasRenderShape();
        gridPoint.name = "GridPoint";
        gridPoint.AddPoint(0.0, 0.0, 5.0, Color.GRAY, 0.5);
        gridPoint.permanent = true;

        basicShapes.add(gridPoint);

        CanvasRenderShape gridBox = new CanvasRenderShape();
        gridBox.name = "GridBox";
        gridBox.AddPoint(-EditorsTheaterSize.GetLeftDouble() / 2, -EditorsTheaterSize.GetRightDouble() / 2, 0.0, Color.BLACK, 1.0);
        gridBox.AddPoint(EditorsTheaterSize.GetLeftDouble() / 2, -EditorsTheaterSize.GetRightDouble() / 2, 0.0, Color.BLACK, 1.0);
        gridBox.AddPoint(EditorsTheaterSize.GetLeftDouble() / 2, EditorsTheaterSize.GetRightDouble() / 2, 0.0, Color.BLACK, 1.0);
        gridBox.AddPoint(-EditorsTheaterSize.GetLeftDouble() / 2, EditorsTheaterSize.GetRightDouble() / 2, 0.0, Color.BLACK, 1.0);
        gridBox.AddLine(0, 1, 1.5, Color.BLACK, 1.0);
        gridBox.AddLine(1, 2, 1.5, Color.BLACK, 1.0);
        gridBox.AddLine(2, 3, 1.5, Color.BLACK, 1.0);
        gridBox.AddLine(3, 0, 1.5, Color.BLACK, 1.0);
        gridBox.permanent = true;

        basicShapes.add(gridBox);

        CanvasRenderShape crosshair = new CanvasRenderShape();
        crosshair.name = "Crosshair";
        crosshair.AddPoint(0.0, -5.0, 0.0, Color.RED, 1.0);
        crosshair.AddPoint(0.0, 5.0, 0.0, Color.RED, 1.0);
        crosshair.AddPoint(-5.0, 0.0, 0.0, Color.RED, 1.0);
        crosshair.AddPoint(5.0, 0.0, 0.0, Color.RED, 1.0);
        crosshair.AddLine(0, 1, 0.5, Color.RED, 1.0);
        crosshair.AddLine(2, 3, 0.5, Color.RED, 1.0);
        crosshair.zoomScaling = false;
        crosshair.permanent = true;
        crosshair.posStatic = true;

        basicShapes.add(crosshair);
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
            addEditor(SymbolEditor.create());
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