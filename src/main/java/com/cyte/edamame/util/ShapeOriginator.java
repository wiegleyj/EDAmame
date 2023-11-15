package com.cyte.edamame.util;

import javafx.scene.shape.Shape;

public class ShapeOriginator {
    private Shape state;

    public void setState(Shape state) {
        this.state = state;
    }

    public ShapeMemento saveStateToMemento() {
        return new ShapeMemento(state);
    }

    public void restoreStateFromMemento(ShapeMemento memento) {
        this.state = memento.getState();
    }

    public Shape getState() {
        return state;
    }
}

