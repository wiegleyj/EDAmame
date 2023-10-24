/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;

import java.io.IOException;

public class EditorFactory
{
    public static Editor createEditor(String key) throws IOException
    {
        switch (key)
        {
            case "SymbolEditor":
                return EditorSymbol.EditorSymbol_Create();
            case "FootprintEditor":
                return EditorFootprint.EditorFootprint_Create();
            default:
                throw new java.lang.Error("ERROR: Invalid editor key: " + key);
        }
    }
}

