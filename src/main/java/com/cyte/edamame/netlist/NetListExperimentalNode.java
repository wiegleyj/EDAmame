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
    private T value;
    private LinkedList<T> conns;

    public NetListExperimentalNode(T nodeValue)
    {
        this.value = nodeValue;
        this.conns = new LinkedList<T>();
    }

    public NetListExperimentalNode(NetListExperimentalNode<T> otherNode)
    {
        this.value = otherNode.value;
        this.conns = new LinkedList<T>();

        for (int i = 0; i < otherNode.ConnGetNum(); i++)
            this.conns.add(otherNode.ConnGet(i));
    }

    public void SetValue(T nodeValue)
    {
        this.value = nodeValue;
    }

    public T GetValue()
    {
        return this.value;
    }

    public void ConnAppend(T node)
    {
        this.conns.add(node);
    }

    public void ConnInsert(int idx, T node)
    {
        this.conns.add(idx, node);
    }

    public T ConnRemove(int idx)
    {
        return this.conns.remove(idx);
    }

    public void ConnClear()
    {
        this.conns.clear();
    }

    public void ConnSet(int idx, T value)
    {
        this.conns.set(idx, value);
    }

    public T ConnGet(int idx)
    {
        return this.conns.get(idx);
    }

    public int ConnGetNum()
    {
        return this.conns.size();
    }

    public String ToString()
    {
        if (this.value.getClass() == String.class)
        {
            return (String)this.value;
        }

        throw new java.lang.Error("ERROR: Attempting to stringify an unrecognized net list node value!");
    }
}
