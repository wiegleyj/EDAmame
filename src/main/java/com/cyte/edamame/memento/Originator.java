/*
 * Copyright (c) 2022-2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

/**
 * {@link Originator}s are objects that can checkpoint themselves with {@link Memento}s.<p>
 *
 * A {@link Memento} represents the state of an object at an instance in time. A caretaker can call saveToMemento on
 * an {@link Originator} instance to retrieve a {@link Memento} instance. The {@link Memento} has the ability to restore
 * the {@link Originator} instance back to its state at the time the {@link Memento} was created.
 *
 * @author Jeff Wiegley
 * @author jeffrey.wiegley@gmail.com
  */
@FunctionalInterface
public interface Originator {
    /**
     * Create a {@link Memento} that remembers the {@link Originator}'s state at the time saveToMemento was executed.
     * @return a {@link Memento} object representing the {@link Originator}'s current state.
     */
    Memento saveToMemento();
}
