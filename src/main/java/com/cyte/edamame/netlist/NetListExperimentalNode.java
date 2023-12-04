/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.netlist;

import java.util.LinkedList;

public class NetListExperimentalNode<T>
{
    private T NetListNode_Value;
    private LinkedList<T> NetListNode_Connections;

    public NetListExperimentalNode(T nodeValue)
    {
        this.NetListNode_Value = nodeValue;
        this.NetListNode_Connections = new LinkedList<T>();
    }

    public void SetValue(T nodeValue)
    {
        this.NetListNode_Value = nodeValue;
    }

    public T GetValue()
    {
        return this.NetListNode_Value;
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

    public String ToString()
    {
        if (this.NetListNode_Value.getClass() == String.class)
        {
            return (String)this.NetListNode_Value;
        }

        throw new java.lang.Error("ERROR: Attempting to stringify an unrecognized net list node value!");
    }
}
