/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.netlist;

import com.cyte.edamame.render.RenderSystem;

import java.util.LinkedList;

public class NetListExperimental<T>
{
    private LinkedList<NetListExperimentalNode<T>> NetList_List;

    public NetListExperimental()
    {
        this.NetList_List = new LinkedList<NetListExperimentalNode<T>>();
    }

    public NetListExperimental(NetListExperimental<T> otherNetList)
    {
        this.NetList_List = new LinkedList<NetListExperimentalNode<T>>();

        for (int i = 0; i < otherNetList.GetNodeNum(); i++)
            this.NetList_List.add(new NetListExperimentalNode<T>(otherNetList.Get(i)));
    }

    public void Append(NetListExperimentalNode<T> node)
    {
        this.NetList_List.add(node);
    }

    public void Insert(int idx, NetListExperimentalNode<T> node)
    {
        this.NetList_List.add(idx, node);
    }

    public NetListExperimentalNode<T> Remove(int idx)
    {
        return this.NetList_List.remove(idx);
    }

    public void Clear()
    {
        this.NetList_List.clear();
    }

    public void Set(int idx, T value)
    {
        NetListExperimentalNode<T> node = this.NetList_List.get(idx);
        node.SetValue(value);
        this.NetList_List.set(idx, node);
    }

    public NetListExperimentalNode<T> Get(int idx)
    {
        return this.NetList_List.get(idx);
    }

    public int Find(T searchValue)
    {
        for (int i = 0; i < this.NetList_List.size(); i++)
            if (this.NetList_List.get(i).GetValue() == searchValue)
                return i;

        return -1;
    }

    public int GetNodeNum()
    {
        return this.NetList_List.size();
    }

    public String ToString()
    {
        //for (int i = 0; i < this.NetList_List.size(); i++)
        //    System.out.print(this.NetList_List.get(i).GetValue());
        //System.out.println("\n\n");

        String str = "[\n";

        for (int i = 0; i < this.NetList_List.size(); i++)
        {
            str += this.NetList_List.get(i).ToString() + " --> ";

            for (int j = 0; j < this.NetList_List.get(i).ConnGetNum(); j++)
                str += this.NetList_List.get(i).ConnGet(j) + ", ";

            str += "\n";
        }

        str += "]";

        return str;
    }
}
