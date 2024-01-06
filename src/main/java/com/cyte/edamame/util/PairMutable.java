/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.util;
import com.cyte.edamame.node.EDANode;

public class PairMutable
{
    public Object left;
    public Object right;

    public PairMutable()
    {
        this.left = null;
        this.right = null;
    }

    public PairMutable(Object leftValue, Object rightValue)
    {
        this.left = leftValue;
        this.right = rightValue;
    }

    public PairMutable(PairMutable otherPair)
    {
        this.left = otherPair.left;
        this.right = otherPair.right;
    }

    public Integer GetLeftInteger()
    {
        return (Integer)left;
    }

    public Integer GetRightInteger()
    {
        return (Integer)right;
    }

    public Double GetLeftDouble()
    {
        return (Double)left;
    }

    public Double GetRightDouble()
    {
        return (Double)right;
    }

    public String GetLeftString()
    {
        return (String)left;
    }

    public String GetRightString()
    {
        return (String)right;
    }

    public PairMutable GetLeftPair()
    {
        return (PairMutable)left;
    }

    public PairMutable GetRightPair()
    {
        return (PairMutable)right;
    }

    public EDANode GetLeftRenderShape()
    {
        return (EDANode)left;
    }

    public EDANode GetRightRenderShape()
    {
        return (EDANode)right;
    }

    public boolean EqualsDouble(PairMutable otherPair)
    {
        return this.GetLeftDouble().equals(otherPair.GetLeftDouble()) && this.GetRightDouble().equals(otherPair.GetRightDouble());
    }

    public String ToStringDouble()
    {
        return "(" + this.GetLeftDouble() + ", " + this.GetRightDouble() + ")";
    }
}