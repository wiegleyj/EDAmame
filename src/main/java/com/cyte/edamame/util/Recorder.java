/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.util;

import java.util.Stack;

public class Recorder {
    private final Stack<Memento> history = new Stack<>();
    private final Stack<Memento> future = new Stack<>();

    public void undo() {
        future.push(history.pop().restore());
    }

    public void redo() {
        history.push(future.pop().restore());
    }

    public void record(Memento memento) {
        history.push(memento);
    }
}
