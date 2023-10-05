/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.util;

import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.editor.MenuBarPriority;
import com.cyte.edamame.editor.MenuPriority;
import com.cyte.edamame.render.RenderShape;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;

import java.util.*;

public class Utils
{
    static public Integer FindCanvasShape(LinkedList<RenderShape> shapes, String name)
    {
        for (int i = 0; i < shapes.size(); i++)
            if (shapes.get(i).name.equals(name))
                return i;

        return -1;
    }

    static public Integer ListFindMaxIdx(LinkedList<Double> list)
    {
        int maxIdx = -1;
        Double max = -Double.MAX_VALUE;

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) > max)
            {
                maxIdx = i;
                max = list.get(i);
            }
        }

        return maxIdx;
    }

    static public Integer ListFindMinIdx(LinkedList<Double> list)
    {
        int minIdx = -1;
        Double min = Double.MAX_VALUE;

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) < min)
            {
                minIdx = i;
                min = list.get(i);
            }
        }

        return minIdx;
    }

    static public Double ListFindMax(LinkedList<Double> list)
    {
        int maxIdx = -1;
        Double max = -Double.MAX_VALUE;

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) > max)
            {
                maxIdx = i;
                max = list.get(i);
            }
        }

        return list.get(maxIdx);
    }

    static public Double ListFindMin(LinkedList<Double> list)
    {
        int minIdx = -1;
        Double min = Double.MAX_VALUE;

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) < min)
            {
                minIdx = i;
                min = list.get(i);
            }
        }

        return list.get(minIdx);
    }

    public static List<Menu> createMenusFromConfig(MenuBarPriority menuBarPriority, Editor editor, TabPane tabPane) {
        List<Menu> menus = new ArrayList<>();

        menuBarPriority.getMenuPriorities().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparing(MenuPriority::getPriority)))
                .forEach(menuEntry -> {
                    Menu menu = new Menu(menuEntry.getKey());

                    menuEntry.getValue().getItemPriorities().entrySet().stream()
                                    .sorted(Map.Entry.comparingByValue())
                                            .forEach(itemEntry -> {
                                                MenuItem menuItem = new MenuItem(itemEntry.getKey());
                                                menuItem.setOnAction(event -> setMenuItemActions(menuItem, editor, tabPane));
                                                menu.getItems().add(menuItem);
                                            });

                    menus.add(menu);
                });
        return menus;
    }

    private static void setMenuItemActions(MenuItem menuItem, Editor editor, TabPane tabPane) {
        String itemName = menuItem.getText();

        switch(itemName) {
            case "Exit":
                menuItem.setOnAction(event -> {
                    tabPane.getTabs().remove((editor.Editor_GetTab()));
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
}