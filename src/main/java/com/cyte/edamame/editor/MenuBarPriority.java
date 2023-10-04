/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class MenuBarPriority {
    @JsonProperty("menuPriorities")
    private Map<String, MenuPriority> menuPriorities;

    public MenuBarPriority() {}

    public MenuBarPriority(Map<String, MenuPriority> menuPriorities) {
        this.menuPriorities = menuPriorities;
    }

    public Map<String, MenuPriority> getMenuPriorities() {
        return menuPriorities;
    }

    public void setMenuPriorities(Map<String, MenuPriority> menuPriorities) {
        this.menuPriorities = menuPriorities;
    }
}
