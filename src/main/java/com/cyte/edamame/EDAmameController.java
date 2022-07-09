package com.cyte.edamame;

import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.editor.SymbolEditor;
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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class EDAmameController implements Initializable {
    private final static Logger LOGGER = Logger.getLogger(EDAmame.class.getName());

    private final Stage stage;

    @FXML
    private Tab logTab;

    @FXML
    private TabPane controlTabPane;

    @FXML
    private TabPane editorTabPane;

    @FXML
    private TextArea logArea;

    @FXML
    private StackPane editorToolBarStack;

    @FXML
    private SplitPane splitPane;

    @FXML
    private MenuBar menuBar;

    private final ObservableMap<Tab, Editor> editors = FXCollections.observableHashMap();

    public EDAmameController(Stage stage) {
        this.stage = stage;
        stage.setOnShown((event) -> executeOnShown());
    }

    private void executeOnShown() {
        restoreDividerPosition();
    }

    private void restoreDividerPosition() {
        Preferences prefs = Preferences.userRoot().node("EDAmame");

        // what a stupid handler. The windows/stage is shown at this point but the children are not
        // yet shown. So really... what you probably need to work on here hasn't been rendered and its
        // layout hasn't been finalized. So we need the horrid "runLater" which is *wildly* undefined
        // as to "when".
        try {
            double value = getDoubleIfExists(prefs, "DIVIDER_POSITION");
            Platform.runLater(() -> splitPane.setDividerPositions(value));
        } catch (Throwable ignored) {
        }
    }

    public void initialize(URL url, ResourceBundle rb) {
        changeLoggingToTab();
        LOGGER.log(Level.INFO, "Initialization Commenced...\n");

        // Set the stage to close gracefully.
        stage.setOnCloseRequest(evt -> { evt.consume(); performExit(); });
        LOGGER.log(Level.INFO, "Stage configured to close gracefully.\n");

        enableEditorTabSelectionLogic();
        restoreWindowContext();
        LOGGER.log(Level.INFO, "Initialization Complete\n");
    }

    /**
     * Change the log handler for the package wide logger from stdout to a TextArea node.
     *
     * EDAmame has a dedicated tab for displaying log information. changeLoggingToTab
     * removes all log handlers from the logger and replaces them with a handler that
     * appends requested logging information to a TextArea in the dedicated log tab.
     */
    private void changeLoggingToTab() {
        for (Handler handler : LOGGER.getHandlers())
            LOGGER.removeHandler(handler);

        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(new TextAreaHandler(logArea));
    }

    /**
     * performExit() implements graceful and intelligent shutdown of the EDAmame application.
     *
     * All open editors will be requested to perform their close/save/shutdown activities and the EDAmame
     * application will exit gracefully. This action is triggered by menu items or On Close Request.
     */
    @FXML
    private void performExit() {
        saveWindowContext();
        LOGGER.log(Level.INFO, "EDAmame exited gracefully.\n");
        Platform.exit();
    }

    /**
     * Saves the window position and size of the application as Java Preferences.
     *
     * The values of the size and position of the application are saved to the Java Preference storage.
     */
    private void saveWindowContext() {
        Preferences prefs = Preferences.userRoot().node("EDAmame");
        prefs.putDouble("WINDOW_POSITION_X", stage.getX());
        prefs.putDouble("WINDOW_POSITION_Y", stage.getY());
        prefs.putDouble("WINDOW_WIDTH", stage.getWidth());
        prefs.putDouble("WINDOW_HEIGHT", stage.getHeight());
        double pos = splitPane.getDividerPositions()[0];
        prefs.putDouble("DIVIDER_POSITION", pos);
    }

    private double getDoubleIfExists(Preferences prefs, String name)
            throws NoSuchElementException, BackingStoreException {
        for (String key: prefs.keys())
            if (key.equals(name))
                return prefs.getDouble(name,0);
        throw new NoSuchElementException("requested key, " + name + ", does not exist");
    }

    /**
     * Restores the window position and size of the application.
     *
     * The values of the size and position of the application when this context was last saved is restored.
     * Context is typically saved when the application exits gracefully.
     */
    private void restoreWindowContext() {
        Preferences prefs = Preferences.userRoot().node("EDAmame");
        stage.setWidth(prefs.getDouble("WINDOW_WIDTH",800));
        stage.setHeight(prefs.getDouble("WINDOW_HEIGHT",1000));
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double x = prefs.getDouble("WINDOW_POSITION_X", (bounds.getWidth()-stage.getWidth())/2);
        double y = prefs.getDouble("WINDOW_POSITION_Y", (bounds.getHeight()-stage.getHeight())/2);
        if (x>bounds.getWidth()-stage.getWidth())
            x=bounds.getWidth()-stage.getWidth();
        if (x<0)
            x=0;
        if (y>bounds.getHeight()-stage.getHeight())
            y=bounds.getHeight()-stage.getHeight();
        if (y<0)
            y=0;

        stage.setX(x);
        stage.setY(y);

        //String home = System.getProperty("user.home");
        LOGGER.log(Level.INFO, "EDAmame window context restored.\n");
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
    private void enableEditorTabSelectionLogic() {
        editorTabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // if switching away from an editor tab, then deactivate the controls/visibility for it.
                    Editor editor = editors.get(oldValue);
                    if (editor != null)
                        deactivateEditor(editor);
                    if (newValue != null)
                        activateEditor(editors.get(newValue));
                }
        );
    }

    private void activateEditor(Editor editor) {
        // Select this editor in the panes and make all it's controls available.
        if (editor != null) {
            // set the editor ToolBars to not visible
            editor.getToolBar().setVisible(true);

            // add all the control tabs for the editor
            controlTabPane.getTabs().addAll(editor.getControlTabs());

            // change all of this editor's menu items to be visible.
            for (ObservableList<MenuItem> items : editor.getMenus().values())
                for (MenuItem menuitem : items)
                    menuitem.setVisible(true);
        }
    }

    private void deactivateEditor(Editor editor) {
        // set the editor ToolBars to not visible
        editor.getToolBar().setVisible(false);

        // remove all control tabs from the controls tab pane
        controlTabPane.getTabs().removeAll(editor.getControlTabs());

        // change all the menu items to not visible
        for (ObservableList<MenuItem> items : editor.getMenus().values())
            for (MenuItem menuitem : items)
                menuitem.setVisible(false);
    }

    private void addEditor(Editor editor) {
        editors.put(editor.getEditorTab(), editor);

        // Place the main editor window in the TabPane
        if (editor.getEditorTab() != null) {
            editor.getEditorTab().setOnCloseRequest(e -> {
                // attempt to close the editor
                if (editor.close()) {
                    deactivateEditor(editor);
                    removeEditor(editor);
                } else
                    e.consume();
            });
            editorTabPane.getTabs().add(editor.getEditorTab());
        }

        // Make sure the toolbar is invisible
        editor.getToolBar().setVisible(false);
        // add the editor's toolbar to the toolbar stack. The
        editorToolBarStack.getChildren().add(editor.getToolBar());

        // The control tabs don't need to be handled. Control of their
        // visibility done through addition and removal from the children
        // of the control TabPane.

        // add all the menus of the editor to the EDAmame MenuBar
        for (Menu menu : menuBar.getMenus()) {
            // get all the editor's menu items for this menu name.
            ObservableList<MenuItem> editoritems = editor.getMenus().get(menu.getText());
            if (editoritems != null) {
                // get an iterator for this menu's items
                ListIterator<MenuItem> iterator = menu.getItems().listIterator();

                // advance the iterator to the either the end of the menu or until a seperator with ID "editorItemsBegin"
                boolean found = false;
                while (iterator.hasNext() && !found) {
                    MenuItem item = iterator.next();
                    if (item.getId() != null && item.getId().equals("editorItemsBegin"))
                        found = true;
                }

                for (MenuItem item : editoritems) {
                    item.setVisible(false);
                    iterator.add(item);
                }
            }
        }

        // move the log tab to the end.
        if (editorTabPane.getTabs().contains(logTab)) {
            editorTabPane.getTabs().remove(logTab);
            editorTabPane.getTabs().add(logTab);
        }
    }

    private void removeEditor(Editor editor) {
        deactivateEditor(editor);
        editorToolBarStack.getChildren().remove(editor.getToolBar());
        controlTabPane.getTabs().remove(editor.getControlTabs());

        for (Menu menu: menuBar.getMenus())
            for (ObservableList<MenuItem> itemlist: editor.getMenus().values())
                menu.getItems().removeAll(itemlist);

        editorTabPane.getTabs().remove(editor.getEditorTab());
    }

    @FXML
    protected void clearLogAction() {
        logArea.clear();
    }

    @FXML
    protected void toggleLogTabVisibility() {
        if (editorTabPane.getTabs().contains(logTab))
            editorTabPane.getTabs().remove(logTab);
        else {
            editorTabPane.getTabs().add(logTab);
            editorTabPane.getSelectionModel().select(logTab);
        }
    }

    @FXML
    private void toggleTestItem() {
        Set<Map.Entry<Tab, Editor>> entries = editors.entrySet();
        Iterator<Map.Entry<Tab, Editor>> iterator = entries.iterator();
        if (iterator.hasNext()) {
            Map.Entry<Tab, Editor> entry = iterator.next();
            if (entry.getValue().close()) {
                deactivateEditor(entry.getValue());
                editors.remove(entry.getKey());
            }
        }
    }

    @FXML
    protected void onTestButtonClick() {
        try {
            // create a new editor
            Editor editor = SymbolEditor.create();
            addEditor(editor);
        } catch (IOException ignored) {
        }
    }

    @FXML
    protected void onTestButtonClickDatabase() {
        System.out.println("hi");

        String classpath = System.getProperty("java.class.path");
        String[] classPathValues = classpath.split(File.pathSeparator);
        for (String classPath: classPathValues) {
            System.out.println(classPath);
        }

        try {
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }

    }

}