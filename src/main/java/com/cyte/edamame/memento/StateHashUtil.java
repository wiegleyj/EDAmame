/*
 * Copyright (c) 2024. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

import java.util.Collection;

/**
 * Provides utility methods for working with objects that implement the StateHashable interface.
 * This class includes a method for generating a hash code based on the state of a StateHashable object.
 */
public class StateHashUtil {

    /**
     * Generates a hash code for the state of a given StateHashable object.
     *
     * @param stateHashable The object whose state hash is to be generated.
     * @return An int representing the hash code of the object's state.
     */
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
