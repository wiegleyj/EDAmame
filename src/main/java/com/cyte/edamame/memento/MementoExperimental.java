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
    private Stack<LinkedList<RenderNode>> Memento_NodeHistoryUndo;
    private Stack<LinkedList<RenderNode>> Memento_NodeHistoryRedo; // Redo stack
    private Editor Memento_Editor;

    // Memento class constructor
    public MementoExperimental(Editor editorValue)
    {
        this.Memento_NodeHistoryUndo = new Stack<>();
        this.Memento_NodeHistoryRedo = new Stack<>(); // Initialize the redo stack
        this.Memento_Editor = editorValue;
    }

    // Function for performing a single undo step by overriding the Editor's current nodes with the latest node history stack entry (and pops it from the history stack).
    public void Memento_NodesUndo()
    {
        if (!this.Memento_NodeHistoryUndo.isEmpty())
        {
            // Pop the change from the undo stack
            LinkedList<RenderNode> undoneChange = this.Memento_NodeHistoryUndo.pop();

            // Push the undone change onto the redo stack
            this.Memento_NodeHistoryRedo.push(undoneChange);

            // Apply the undone change to the editor's render system
            this.Memento_Editor.Editor_RenderSystem.RenderSystem_NodesClear();
            this.Memento_Editor.Editor_RenderSystem.RenderSystem_NodesAdd(undoneChange);

            // Reset counters
            this.Memento_Editor.Editor_ShapesHighlighted = 0;
            this.Memento_Editor.Editor_ShapesSelected = 0;

            System.out.println("Undo!");
        }
        else
        {
            System.out.println("Nothing to undo.");
        }
    }

    // Function for performing a single redo step by overriding the Editor's current nodes with the next node history stack entry (and pushes it back to the history stack).
    public void Memento_NodesRedo()
    {
        if (!this.Memento_NodeHistoryRedo.isEmpty())
        {
            // Pop the change from the redo stack
            LinkedList<RenderNode> redoChange = this.Memento_NodeHistoryRedo.pop();

            // Push the redo change onto the undo stack
            this.Memento_NodeHistoryUndo.push(redoChange);

            // Apply the redo change to the editor's render system
            this.Memento_Editor.Editor_RenderSystem.RenderSystem_NodesClear();
            this.Memento_Editor.Editor_RenderSystem.RenderSystem_NodesAdd(redoChange);

            // Reset counters
            this.Memento_Editor.Editor_ShapesHighlighted = 0;
            this.Memento_Editor.Editor_ShapesSelected = 0;

            System.out.println("Redo!");
        }
        else
        {
            System.out.println("Nothing to redo.");
        }
    }

    // Function for pushing the current Editor's RenderNodes onto the node history stack (also ensures that the node history stack stays under the limit).
    public void Memento_NodeHistoryUpdate()
    {
        this.Memento_NodeHistoryUndo.push(this.Memento_Editor.Editor_RenderSystem.RenderSystem_NodesClone());

        this.Memento_NodeHistoryRedo.clear();
        while (this.Memento_NodeHistoryUndo.size() > EDAmameController.Editor_UndoStackMaxLen)
            this.Memento_NodeHistoryUndo.remove(0);

        System.out.println("Undo pushed!");
    }
}
