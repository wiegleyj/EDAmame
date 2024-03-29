/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.InvalidClassException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Librarys and projects are all modified through the use of special purpose editor modules.<p>
 *
 * All editors are based on the abstract Editor class so that they conform with what the EDAmame main
 * application expects and will attempt to obtain and display editor specific controls for.
 *
 * @author Jeff Wiegley, Ph.D.
 * @author jeffrey.wiegley@gmail.com
 */
public abstract class Editor {
    /** Every editor can be uniquely identified by a random UUID (which do not persist across application execution. */
    final UUID editorID = UUID.randomUUID();

    // holders for the UI elements. The UI elements are instantiated in a single FXML file/load
    // The individual elements are extracted to these for use by the EDAmame Application.
    /** The main tab for EDAmame to include in its main tab list. */
    protected Tab tab = null;

    /** An optional ToolBar to provide EDAmame to include/append to its toolbars. */
    protected ToolBar toolBar = null;

    /** a list of tabs to include in the navigation tab pane when this editor is active. (can be empty) */
    protected ObservableList<Tab> tabs = FXCollections.observableArrayList();

    /**
     * A structure of menu items to include in EDAmame's menus. Any menuitems associated with a string will
     * be inserted/visible under the menu with the same name in the EDAmame main menubar. The string must
     * match exactly including mneumonic underscores and such. Missing menus at the EDAmame level are not created.
     */
    protected ObservableMap<String, ObservableList<MenuItem>> menus = FXCollections.observableHashMap();

    // accessors for the UI elements

    /**
     * Returns the main editor Tab node for inclusion by EDAmame.
     * @return the main tab for this editor.
     */
    public Tab getEditorTab() { return tab; }

    /**
     * Returns the optional ToolBar of this editor for inclusion by EDAmame.
     * @return The ToolBar if available, null otherwise.
     */
    public ToolBar getToolBar() { return toolBar; }

    /**
     * Returns a list of control tabs for EDAmame to include in the main controls tab pane.
     * @return A (possibly empty) list of control tabs.
     */
    public ObservableList<Tab> getControlTabs() { return tabs; }

    /**
     * Returns a (possibly empty) structure of menu items for EDAmame to include in its menus. The top Map maps
     * menu names to a list of items to include in a menu with that name. The values of this map are a list of
     * MenuItems that EDAmame should include/insert into any menu it has with a matching name.
     * @return a (possibly empty) structure of menu items
     */
    public ObservableMap<String, ObservableList<MenuItem>> getMenus() { return menus; }

    /** dissect a controller into its component for delivery to EDAmame<p>
     *
     * The design of editors is intended to be done through FXML and SceneBuilder. SceneBuilder doesn't
     * support multiple scenes in a single FXML. So, all required components are boxed into a single VBOX.
     * Once an editor factory has loaded an FXML file which
     * @param scene The scene to dissect for expected UI elements.
     * @throws InvalidClassException if the expected UI scene organization is not found as expected.
     */
    protected void dissect(Scene scene) throws InvalidClassException {
        Node root = scene.getRoot();
        if (root == null)
            throw new InvalidClassException("root of scene is null ");

        if (root.getClass() != VBox.class) {
            throw new InvalidClassException("Expected VBox but found " + root.getClass());
        }

        Iterator<Node> nodeIterator = ((VBox)root).getChildren().iterator();
        String prefix = editorID.toString();

        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            if (node.getClass() == ToolBar.class) {
                toolBar = (ToolBar) node;
                toolBar.setVisible(false); // toolbar starts invisible. Becomes visible on Tab selection.
                toolBar.setId(prefix+"_TOOLBAR");
            } else if (node.getClass() == MenuBar.class) {
                Iterator<Menu> menuIterator = ((MenuBar) node).getMenus().iterator();
                while (menuIterator.hasNext()) {
                    Menu menu = menuIterator.next();
                    if (!menus.containsKey(menu.getText()))
                        menus.put(menu.getText(), FXCollections.observableArrayList());
                    List<MenuItem> itemlist = menus.get(menu.getText());
                    Iterator<MenuItem> itemIterator = menu.getItems().iterator();
                    while (itemIterator.hasNext()) {
                        MenuItem item = itemIterator.next();
                        itemlist.add(item);
                        itemIterator.remove();
                    }
                    menuIterator.remove();
                }
            } else if (node.getClass() == TabPane.class) {
                Iterator<Tab> tabIterator = ((TabPane) node).getTabs().iterator();
                while (tabIterator.hasNext()) {
                    Tab item = tabIterator.next();
                    if (item.getText().equals("EditorTab")) {
                        tab = item;
                    } else {
                        tabs.add(item);
                    }
                    tabIterator.remove();
                }
            }
            nodeIterator.remove();
        }
    }

    /**
     * Request an editor to close. Handling any information/state saving as it needs.
     * @return true if the editor was able to close without unsaved information/state, false otherwise.
     */
    public boolean close() {
        return true;
    }
}
