/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.render.RenderNode;

import java.util.Stack;
import java.util.LinkedList;

// An experimental Memento class that uses a stack of RenderNode lists to maintain a running history of a given Editor's dropped elements

public class MementoExperimental
{
    // Stack that holds the entire history of an Editor's RenderNodes
    private Stack<LinkedList<RenderNode>> nodeHistoryUndo;
    private Stack<LinkedList<RenderNode>> nodeHistoryRedo; // Redo stack

    private Editor editor;

    // Memento class constructor
    public MementoExperimental(Editor editorValue)
    {
        this.nodeHistoryUndo = new Stack<>();
        this.nodeHistoryRedo = new Stack<>(); // Initialize the redo stack
        this.editor = editorValue;
    }

    // Function for performing a single undo step by overriding the Editor's current nodes with the latest node history stack entry (and pops it from the history stack).
    public void NodesUndo()
    {
        if (!this.nodeHistoryUndo.isEmpty())
        {
            // Pop the change from the undo stack
            LinkedList<RenderNode> undoneChange = this.nodeHistoryUndo.pop();

            // Push the undone change onto the redo stack
            this.nodeHistoryRedo.push(undoneChange);

            // Apply the undone change to the editor's render system
            this.editor.renderSystem.NodesClear();
            this.editor.renderSystem.NodesAdd(undoneChange);

            // Reset counters
            this.editor.shapesHighlighted = 0;
            this.editor.shapesSelected = 0;

            System.out.println("Undo!");
        }
        else
        {
            System.out.println("Nothing to undo.");
        }
    }

    // Function for performing a single redo step by overriding the Editor's current nodes with the next node history stack entry (and pushes it back to the history stack).
    public void NodesRedo()
    {
        if (!this.nodeHistoryRedo.isEmpty())
        {
            // Pop the change from the redo stack
            LinkedList<RenderNode> redoChange = this.nodeHistoryRedo.pop();

            // Push the redo change onto the undo stack
            this.nodeHistoryUndo.push(redoChange);

            // Apply the redo change to the editor's render system
            this.editor.renderSystem.NodesClear();
            this.editor.renderSystem.NodesAdd(redoChange);

            // Reset counters
            this.editor.shapesHighlighted = 0;
            this.editor.shapesSelected = 0;

            System.out.println("Redo!");
        }
        else
        {
            System.out.println("Nothing to redo.");
        }
    }

    // Function for pushing the current Editor's RenderNodes onto the node history stack (also ensures that the node history stack size stays under the limit).
    public void NodeHistoryUpdate()
    {
        this.nodeHistoryUndo.push(this.editor.renderSystem.NodesClone());

        this.nodeHistoryRedo.clear();
        while (this.nodeHistoryUndo.size() > EDAmameController.Editor_UndoStackMaxLen)
            this.nodeHistoryUndo.remove(0);

        System.out.println("Undo pushed!");
    }
}
