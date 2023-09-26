/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class PriorityMenuItem extends MenuItem implements Comparable<PriorityMenuItem>{
    private int priority;

    @Override
    public int compareTo(@NotNull PriorityMenuItem o) {
        return priority - o.priority;
    }
}
