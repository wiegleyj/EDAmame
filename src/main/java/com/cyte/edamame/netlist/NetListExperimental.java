/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.netlist;

import java.util.LinkedList;

public class NetListExperimental<T>
{
    private LinkedList<NetListExperimentalNode<T>> list;

    public NetListExperimental()
    {
        this.list = new LinkedList<NetListExperimentalNode<T>>();
    }

    public NetListExperimental(NetListExperimental<T> otherNetList)
    {
        this.list = new LinkedList<NetListExperimentalNode<T>>();

        for (int i = 0; i < otherNetList.GetNodeNum(); i++)
            this.list.add(new NetListExperimentalNode<T>(otherNetList.Get(i)));
    }

    public void Append(NetListExperimentalNode<T> node)
    {
        this.list.add(node);
    }

    public void Insert(int idx, NetListExperimentalNode<T> node)
    {
        this.list.add(idx, node);
    }

    public NetListExperimentalNode<T> Remove(int idx)
    {
        return this.list.remove(idx);
    }

    public void Clear()
    {
        this.list.clear();
    }

    public void Set(int idx, T value)
    {
        NetListExperimentalNode<T> node = this.list.get(idx);
        node.SetValue(value);
        this.list.set(idx, node);
    }

    public NetListExperimentalNode<T> Get(int idx)
    {
        return this.list.get(idx);
    }

    public int Find(T searchValue)
    {
        for (int i = 0; i < this.list.size(); i++)
            if (this.list.get(i).GetValue() == searchValue)
                return i;

        return -1;
    }

    public int GetNodeNum()
    {
        return this.list.size();
    }

    public String ToString()
    {
        //for (int i = 0; i < this.NetList_List.size(); i++)
        //    System.out.print(this.NetList_List.get(i).GetValue());
        //System.out.println("\n\n");

        String str = "[\n";

        for (int i = 0; i < this.list.size(); i++)
        {
            str += this.list.get(i).ToString() + " --> ";

            for (int j = 0; j < this.list.get(i).ConnGetNum(); j++)
                str += this.list.get(i).ConnGet(j) + ", ";

            str += "\n";
        }

        str += "]";

        return str;
    }
}
