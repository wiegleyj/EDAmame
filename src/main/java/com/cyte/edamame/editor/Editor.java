package com.cyte.edamame.editor;

import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.*;

public abstract class Editor {
    UUID editorID = UUID.randomUUID();

    protected Tab tab = null;
    protected List<StringProperty> tabIDs = new ArrayList<>();
    protected List<Tab> tabs = new ArrayList<>();
    protected ToolBar toolBar = null;
    protected Map<String, List<MenuItem>> menus = new HashMap<>();
    protected List<StringProperty> menuItemIDs = new ArrayList<>();

    public Tab getEditorTab() { return tab; }
    public StringProperty getEditorTabID() { return tab.idProperty(); }
    public List<Tab> getControlTabs() { return tabs; }
    public List<StringProperty> getControlTabIDs() { return tabIDs; }
    public ToolBar getToolBar() { return toolBar; }
    public StringProperty getToolBarID() { return toolBar.idProperty(); }
    public Map<String, List<MenuItem>> getMenus() { return menus; }
    public List<StringProperty> getMenuItemIDs() { return menuItemIDs; }

    public static Editor create() throws IOException { throw new UnsupportedOperationException(); }

    protected void dissect(Scene scene) throws InvalidClassException {
        Node root = scene.getRoot();
        if (root == null)
            throw new InvalidClassException("root of scene is null ");

        if (root.getClass() != VBox.class) {
            throw new InvalidClassException("Expected VBox but found " + root.getClass());
        }

        Iterator<Node> nodeIterator = ((VBox)root).getChildren().iterator();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();
            if (node.getClass() == ToolBar.class) {
                toolBar = (ToolBar) node;
                toolBar.setId(editorID.toString()+"_TOOLBAR");
            } else if (node.getClass() == MenuBar.class) {
                menus = new HashMap<>();
                Iterator<Menu> menuIterator = ((MenuBar) node).getMenus().iterator();
                while (menuIterator.hasNext()) {
                    Menu menu = menuIterator.next();
                    if (!menus.containsKey(menu.getText()))
                        menus.put(menu.getText(), new ArrayList<>());
                    List<MenuItem> itemlist = menus.get(menu.getText());
                    Iterator<MenuItem> itemIterator = menu.getItems().iterator();
                    while (itemIterator.hasNext()) {
                        MenuItem item = itemIterator.next();
                        item.setId(editorID.toString()+"_MENU_"+menu.getText()+"_ITEM_"+item.getText());
                        menuItemIDs.add(item.idProperty());
                        itemlist.add(item);
                        itemIterator.remove();
                    }
                    menuIterator.remove();
                }
            } else if (node.getClass() == TabPane.class) {
                tabs = null;
                Iterator<Tab> tabIterator = ((TabPane) node).getTabs().iterator();
                while (tabIterator.hasNext()) {
                    Tab item = tabIterator.next();
                    if (item.getText().equals("EditorTab")) {
                        tab = item;
                        tab.setId(editorID.toString()+"_EDITOR");
                    } else {
                        if (tabs == null)
                            tabs = new ArrayList<>();
                        item.setId(editorID.toString()+"_CONTROL_"+item.idProperty().getValue());
                        tabIDs.add(item.idProperty());
                        tabs.add(item);
                    }
                    tabIterator.remove();
                }
            }
            nodeIterator.remove();
        }
    }
}
