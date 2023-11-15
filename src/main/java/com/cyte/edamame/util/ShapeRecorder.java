package com.cyte.edamame.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShapeRecorder {
    private List<ShapeMemento> mementoList = new ArrayList<>();

    public void addMemento(ShapeMemento memento) {
        mementoList.add(memento);
    }

    public ShapeMemento getMemento(int index) {
        return mementoList.get(index);
    }

    public Map<Object, Object> getMementoList() {
        return null;
    }
}