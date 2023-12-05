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

    public void ReplaceNodeIDsWithNames(RenderSystem renderSystem)
    {
        for (int i = 0; i < this.NetList_List.size(); i++)
        {
            /*for (int j = 0; j < this.NetList_List.get(i).ConnGetNum(); j++)
            {
                String name = renderSystem.RenderSystem_Nodes.get(renderSystem.RenderSystem_NodeFind((String)this.NetList_List.get(i).ConnGet(j))).RenderNode_Name;
                NetListExperimentalNode<String> newConnNode = new NetListExperimentalNode<String>(name);

                this.NetList_List.set(i, (NetListExperimentalNode<T>)newConnNode);
            }*/

            String name = renderSystem.RenderSystem_Nodes.get(renderSystem.RenderSystem_NodeFind((String)this.NetList_List.get(i).GetValue())).RenderNode_Name;
            NetListExperimentalNode<String> newNode = new NetListExperimentalNode<String>(name);

            this.NetList_List.set(i, (NetListExperimentalNode<T>)newNode);
        }


    }

    public String ToString()
    {
        String str = "";

        for (int i = 0; i < this.NetList_List.size(); i++)
        {
            str += this.NetList_List.get(i).ToString() + " --> ";

            for (int j = 0; j < this.NetList_List.get(i).ConnGetNum(); j++)
                str += this.NetList_List.get(i).ConnGet(j) + ", ";

            str += "\n\n";
        }

        return str;
    }
}
