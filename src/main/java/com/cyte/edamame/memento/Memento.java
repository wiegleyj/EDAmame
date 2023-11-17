/*
 * Copyright (c) 2022-2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

/**
 * {@link Memento}s are objects that save the state of an {@link Originator} object from an instance in time.<p>
 *
 * {@link Memento}s are used by {@link com.cyte.edamame.EDAmame} to implement all undo/restore behavior.
 *
 * @author Jeff Wiegley
 * @author jeffrey.wiegley@gmail.com
 */
@FunctionalInterface
public interface Memento {
    /**
     * Restores the state of the {@link Originator} instance used to create this {@link Memento}.
     *
     * @return a new {@link Memento} that represents the state of the {@link Originator} prior to restoration.
     */
    Memento restore();
}
