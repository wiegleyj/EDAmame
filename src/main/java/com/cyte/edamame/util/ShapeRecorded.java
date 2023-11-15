/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.util;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ShapeRecorded {
    public static void main(String[] args) {
        ShapePane shapePane = new ShapePane();
        ShapeOriginator originator = new ShapeOriginator();
        ShapeRecorder recorder = new ShapeRecorder();

        // Add initial shape
        Circle circle = new Circle(50);
        circle.setFill(Color.BLUE);
        shapePane.getChildren().add(circle);
        originator.setState(circle);
        recorder.addMemento(originator.saveStateToMemento());

        // Handle undo and redo using key events
        shapePane.setOnKeyReleased(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                undo(originator, recorder);
            } else if (event.isControlDown() && event.getCode() == KeyCode.Y) {
                redo(originator, recorder);
            }
        });

        // Other shape manipulation code here...

        // Save state after manipulating shapes
        recorder.addMemento(originator.saveStateToMemento());
    }

    private static void undo(ShapeOriginator originator, ShapeRecorder recorder) {
        if (recorder.getMementoList().size() > 1) {
            ShapeMemento memento = (ShapeMemento) recorder.getMementoList().get(recorder.getMementoList().size() - 2);
            recorder.getMementoList().remove(recorder.getMementoList().size() - 1);
            originator.restoreStateFromMemento(memento);
        }
    }

    private static void redo(ShapeOriginator originator, ShapeRecorder recorder) {
        if (recorder.getMementoList().size() < 2) {
            return; // No redo available
        }

        ShapeMemento memento = (ShapeMemento) recorder.getMementoList().get(recorder.getMementoList().size() - 1);
        originator.restoreStateFromMemento(memento);
    }
}