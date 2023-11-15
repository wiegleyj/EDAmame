package com.cyte.edamame.util;

import javafx.scene.shape.Shape;

public class ShapeMemento {
    private Shape state;

    public ShapeMemento(Shape state) {
        this.state = state;
    }

    public Shape getState() {
        return state;
    }
}

