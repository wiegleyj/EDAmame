/*
 * Copyright (c) 2024. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

import java.util.Collection;

public class StateHashUtil {
    public static int generateStateHash(StateHashable stateHashable) {
        Collection<?> stateObjects = stateHashable.getState();

        if (stateObjects == null) {
            return 0;
        }

        int hash = 1;

        for (Object obj : stateObjects) {
            hash = 31 * hash + (obj == null ? 0 : obj.hashCode());
        }

        return hash;
    }
}
