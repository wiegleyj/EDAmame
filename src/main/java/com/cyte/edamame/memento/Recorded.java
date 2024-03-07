/*
 * Copyright (c) 2022-2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

/**
 * Defines an interface for objects that can register a Recorder for tracking state changes.
 * Implementing this interface allows objects to be part of an undo/redo framework by recording their states as Mementos.
 */
public interface Recorded {
    void registerRecorder(Recorder recorder);
}

