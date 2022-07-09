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

public abstract class Editor {
    final UUID editorID = UUID.randomUUID();

    // holders for the UI elements. The UI elements are instantiated in a single FXML file/load
    // The individual elements are extracted to these for use by the EDAmame Application.
    protected Tab tab = null;
    protected ToolBar toolBar = null;
    protected ObservableList<Tab> tabs = FXCollections.observableArrayList();
    protected ObservableMap<String, ObservableList<MenuItem>> menus = FXCollections.observableHashMap();

    // accessors for the UI elements
    public Tab getEditorTab() { return tab; }
    public ToolBar getToolBar() { return toolBar; }
    public ObservableList<Tab> getControlTabs() { return tabs; }
    public ObservableMap<String, ObservableList<MenuItem>> getMenus() { return menus; }

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

    public boolean close() {
        return true;
    }
}
