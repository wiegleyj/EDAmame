/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.file;

import com.cyte.edamame.EDAmameApplication;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.node.EDANode;

import com.cyte.edamame.util.PairMutable;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.URL;

import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

import java.util.LinkedList;

public class File
{
    static public boolean NodesSave(LinkedList<Node> nodes, boolean center)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Symbol");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("FXML File", "*.fxml"));
        fileChooser.setInitialFileName("file.fxml");
        java.io.File file = fileChooser.showSaveDialog(EDAmameApplication.controller.stage);

        if (file == null)
        {
            EDAmameController.SetStatusBar("Unable to save FXML because the entered directory is invalid!");

            return false;
        }

        return NodesWrite(file.getAbsolutePath(), nodes, center);
    }

    static public boolean NodesWrite(String filePath, LinkedList<Node> nodes, boolean center)
    {
        String data = "";

        data += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";
        data += "<?import java.lang.Double?>\n";
        data += "<?import javafx.scene.Group?>\n";
        data += "<?import javafx.scene.shape.Circle?>\n";
        data += "<?import javafx.scene.shape.Rectangle?>\n";
        data += "<?import javafx.scene.shape.Polygon?>\n";
        data += "<?import javafx.scene.shape.Line?>\n";
        data += "<?import javafx.scene.text.Text?>\n";
        data += "<?import javafx.scene.text.Font?>\n\n";

        data += "<Group xmlns=\"http://javafx.com/javafx/20.0.1\" xmlns:fx=\"http://javafx.com/fxml/1\">\n";
        data += "\t<children>\n";

        PairMutable childMidPos = new PairMutable(0.0, 0.0);

        if (center)
        {
            childMidPos = EDANode.NodesGetMiddlePos(nodes);
            childMidPos.left = -childMidPos.GetLeftDouble();
            childMidPos.right = -childMidPos.GetRightDouble();
        }

        for (int i = 0; i < nodes.size(); i++)
            data += EDANode.NodeToFXMLString(nodes.get(i), childMidPos, 2) + "\n";

        data += "\t</children>\n";
        data += "</Group>\n";

        return Write(filePath, data, true);
    }

    static public LinkedList<Node> NodesLoad(boolean center)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Symbol");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("FXML File", "*.fxml"));
        java.io.File file = fileChooser.showOpenDialog(EDAmameApplication.controller.stage);

        if (file == null)
        {
            EDAmameController.SetStatusBar("Unable to load FXML file because the entered directory is invalid!");
        }
        else
        {
            try
            {
                return NodesRead(file.getAbsolutePath(), center);
            }
            catch (IOException e)
            {
                EDAmameController.SetStatusBar("Encountered error while loading FXML file!");
            }
        }

        return null;
    }

    static public LinkedList<Node> NodesRead(String filePath, boolean center) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader();
        filePath = filePath.replace('\\', '/');
        fxmlLoader.setLocation(new URL("file:///" + filePath));
        Node root = fxmlLoader.<Node>load();

        if (root.getClass() != Group.class)
        {
            EDAmameController.SetStatusBar("Attempting to load FXML file without parent Group node!");

            return null;
        }

        LinkedList<Node> nodes = new LinkedList<Node>();
        ObservableList<Node> children = ((Group)root).getChildren();

        for (int i = 0; i < children.size(); i++)
            nodes.add(children.get(i));

        if (center)
        {
            PairMutable childMidPos = EDANode.NodesGetMiddlePos(nodes);
            childMidPos.left = -childMidPos.GetLeftDouble();
            childMidPos.right = -childMidPos.GetRightDouble();

            for (int i = 0; i < nodes.size(); i++)
            {
                Node node = nodes.get(i);

                node.setTranslateX(node.getTranslateX() + childMidPos.GetLeftDouble());
                node.setTranslateY(node.getTranslateY() + childMidPos.GetRightDouble());
            }
        }

        return nodes;
    }

    static public boolean Write(String filePath, String data, boolean overwrite)
    {
        try
        {
            PrintWriter file = new PrintWriter(new FileOutputStream(filePath, !overwrite));
            file.print(data);
            file.close();

            //System.out.println("Wrote \"" + data + "\".");
        }
        catch (IOException e)
        {
            System.out.println("Error writing to file \"" + filePath + "\"!");

            return false;
        }

        return true;
    }
}