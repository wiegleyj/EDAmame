/*
 * Copyright (c) 2022-2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

import com.cyte.edamame.editor.Editor;

import java.util.Stack;

public class Recorder {
    private final Stack<Memento> history;
    private final Stack<Memento> future;
    private final Editor editor;
    private final int maxStackSize = 10;

    // Recorder class constructor
    public Recorder(Editor editorValue)
    {
        this.history = new Stack<>();
        this.future = new Stack<>();
        this.editor = editorValue;
    }

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

    public void record(Memento memento) {
        while (history.size() >= maxStackSize) {
            history.remove(0); // Remove the oldest entries to enforce max stack size
        }
        history.push(memento);
        future.clear(); // Clear the future stack whenever a new state is recorded
    }

    private void resetCounters() {
        this.editor.shapesHighlighted = 0;
        this.editor.shapesSelected = 0;
    }
}