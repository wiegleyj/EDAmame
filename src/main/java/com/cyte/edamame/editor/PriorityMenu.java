package com.cyte.edamame.editor;/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class PriorityMenu extends Menu implements Comparable<PriorityMenu> {
        private int priority;

        @Override
        public int compareTo(@NotNull PriorityMenu o) {
                return priority - o.priority;
        }
}
