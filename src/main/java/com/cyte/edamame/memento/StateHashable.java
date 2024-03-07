/*
 * Copyright (c) 2024. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

import java.util.Collection;

/**
 * Defines an interface for objects that can provide a hashable state representation.
 * This interface facilitates generating a hash code based on the object's current state.
 */
public interface StateHashable {
    
    /**
     * Retrieves the state of the object for hashing purposes.
     *
     * @return A Collection of objects representing the state to be hashed.
     */
    Collection<?> getState();
}
