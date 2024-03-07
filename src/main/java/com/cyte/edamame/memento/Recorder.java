/*
 * Copyright (c) 2022-2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

import com.cyte.edamame.editor.Editor;

import java.util.Stack;

/**
 * Manages the undo and redo operations for an application by keeping a history of changes in Memento objects.
 * This class supports undoing and redoing actions up to a specified stack size limit.
 * It operates on an Editor instance to track and revert state changes.
 */
public class Recorder {
    private final Stack<Memento> history;
    private final Stack<Memento> future;
    private final Editor editor;
    private final int maxStackSize = 10;

    /**
     * Initializes a new Recorder instance with a reference to an Editor.
     *
     * @param editorValue The Editor instance whose states are to be managed.
     */
    public Recorder(Editor editorValue)
    {
        this.history = new Stack<>();
        this.future = new Stack<>();
        this.editor = editorValue;
    }

    /**
     * Performs an undo operation by reverting the Editor's state to the previous state.
     * This method transfers the current state to the future stack for potential redo operations.
     */
    public void undo() {
        if (!history.isEmpty()) {
            Memento memento = history.pop();
            // Ensure future stack does not exceed maximum size
            if (future.size() >= maxStackSize) {
                future.remove(0); // Remove the oldest entry
            }
            future.push(memento.restore());
            resetCounters();
            System.out.println("Undo!");
        } else {
            System.out.println("Nothing to undo.");
        }
    }

    /**
     * Performs a redo operation by re-applying an action that was previously undone.
     * This method transfers the state from the future stack back to the history stack.
     */
    public void redo() {
        if (!future.isEmpty()) {
            Memento memento = future.pop();
            // Ensure history stack does not exceed maximum size
            if (history.size() >= maxStackSize) {
                history.remove(0); // Remove the oldest entry
            }
            history.push(memento.restore());
            resetCounters();
            System.out.println("Redo!");
        } else {
            System.out.println("Nothing to redo.");
        }
    }

    /**
     * Updates the current state by creating a new Memento from the Editor's state and recording it.
     * This is typically called after a state change in the Editor.
     */
    public void update() {
        if (editor.hasStateChanged()) {
            Memento memento = editor.saveToMemento();
            record(memento);
            editor.stateRecorded();
            System.out.println("State Recorded!");
        } else {
            System.out.println("Change was not recorded.");
        }
    }

    /**
     * Records a new state into the history, ensuring the history does not exceed the maximum stack size.
     *
     * @param memento The Memento object representing the current state to be recorded.
     */
    public void record(Memento memento) {
        while (history.size() >= maxStackSize) {
            history.remove(0); // Remove the oldest entries to enforce max stack size
        }
        history.push(memento);
        future.clear(); // Clear the future stack whenever a new state is recorded
    }

    /**
     * Resets counters or states within the Editor as part of the undo/redo process.
     */
    private void resetCounters() {
        this.editor.shapesHighlighted = 0;
        this.editor.shapesSelected = 0;
    }
}