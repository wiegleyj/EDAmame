/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.netlist;

import java.util.LinkedList;

public class NetListNode<T>
{
    private T NetListNode_Node;
    private LinkedList<T> NetListNode_Connections;

    public void NetListNode(T nodeValue)
    {
        this.NetListNode_Node = nodeValue;
        this.NetListNode_Connections = new LinkedList<T>();
    }

    public void Set(T nodeValue)
    {
        this.NetListNode_Node = nodeValue;
    }

    public T Get()
    {
        return this.NetListNode_Node;
    }

    public void ConnAppend(T node)
    {
        this.NetListNode_Connections.add(node);
    }

    public void ConnInsert(int idx, T node)
    {
        this.NetListNode_Connections.add(idx, node);
    }

    public T ConnRemove(int idx)
    {
        return this.NetListNode_Connections.remove(idx);
    }

    public void ConnClear()
    {
        this.NetListNode_Connections.clear();
    }

    public T ConnGet(int idx)
    {
        return this.NetListNode_Connections.get(idx);
    }
}
