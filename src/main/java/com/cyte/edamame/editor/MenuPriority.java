/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MenuPriority {
    private int priority;
    @JsonProperty("itemPriorities")
    private Map<String, Integer> itemPriorities;

    public MenuPriority() {}

    public MenuPriority(int priority, Map<String, Integer> itemPriorities) {
        this.priority = priority;
        this.itemPriorities = itemPriorities;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Map<String, Integer> getItemPriorities() {
        return itemPriorities;
    }

    public void setItemPriorities(Map<String, Integer> itemPriorities) {
        this.itemPriorities = itemPriorities;
    }
}
