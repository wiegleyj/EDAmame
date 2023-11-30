/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.netlist;

import java.util.LinkedList;

public class NetList<T>
{
    private LinkedList<T> NetList_List;

    public NetList()
    {
        this.NetList_List = new LinkedList<T>();
    }

    public void Append(T node)
    {
        this.NetList_List.add(node);
    }

    public void Insert(int idx, T node)
    {
        this.NetList_List.add(idx, node);
    }

    public T Remove(int idx)
    {
        return this.NetList_List.remove(idx);
    }

    public void Clear()
    {
        this.NetList_List.clear();
    }

    public T Get(int idx)
    {
        return this.NetList_List.get(idx);
    }
}
