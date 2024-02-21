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
    private final Stack<Memento> history = new Stack<>();
    private final Stack<Memento> future = new Stack<>();

    public void undo() {
        if (!history.isEmpty()) {
            Memento memento = history.pop(); // Pop the memento
            future.push(memento.restore()); // Restore the state and push it to future
        } else {
            System.out.println("Nothing to undo.");
        }
    }

    public void redo() {
        if (!future.isEmpty()) {
            Memento memento = future.pop(); // Pop the memento
            history.push(memento.restore()); // Restore the state and push it to history
        } else {
            System.out.println("Nothing to redo.");
        }
    }

    public void record(Memento memento) {
        history.push(memento);
    }
}