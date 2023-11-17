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
    Stack<LinkedList<RenderNode>> Memento_NodeHistory;
    // Reference to the Editor which the Memento belongs to (used for grabbing & modifying the Editor's elements)
    Editor Memento_Editor;

    // Memento class constructor
    public MementoExperimental(Editor editorValue)
    {
        this.Memento_NodeHistory = new Stack<LinkedList<RenderNode>>();
        this.Memento_Editor = editorValue;
    }

    // Function for performing a single undo step by overriding the Editor's current nodes with the latest node history stack entry (and pops it from the history stack).
    public void Memento_NodesUndo()
    {
        if (!this.Memento_NodeHistory.isEmpty())
        {
            this.Memento_Editor.Editor_RenderSystem.RenderSystem_NodesClear();
            this.Memento_Editor.Editor_RenderSystem.RenderSystem_NodesAdd(this.Memento_NodeHistory.pop());

            this.Memento_Editor.Editor_ShapesHighlighted = 0;
            this.Memento_Editor.Editor_ShapesSelected = 0;
        }

        System.out.println("Undo!");
    }

    // Function for pushing the current Editor's RenderNodes onto the node history stack (also ensures that the node history stack stays under the limit).
    public void Memento_NodeHistoryUpdate()
    {
        this.Memento_NodeHistory.push(this.Memento_Editor.Editor_RenderSystem.RenderSystem_NodesClone());

        while (this.Memento_NodeHistory.size() > EDAmameController.Editor_UndoStackMaxLen)
            this.Memento_NodeHistory.remove(0);

        System.out.println("Pushed!");
    }
}
