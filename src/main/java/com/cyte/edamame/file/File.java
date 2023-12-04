/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.file;

import com.cyte.edamame.EDAmame;
import com.cyte.edamame.EDAmameApplication;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.render.RenderNode;

import com.cyte.edamame.util.PairMutable;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.URL;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.scene.text.*;

import java.util.LinkedList;

public class File
{
    static public boolean File_NodesSave(LinkedList<Node> nodes, boolean center)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Symbol");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("FXML File", "*.fxml"));
        fileChooser.setInitialFileName("symbol.fxml");
        java.io.File file = fileChooser.showSaveDialog(EDAmameApplication.App_Controller.Controller_Stage);

        if (file == null)
        {
            EDAmameController.Controller_SetStatusBar("Unable to save FXML because the entered directory is invalid!");

            return false;
        }

        return File_NodesWrite(file.getAbsolutePath(), nodes, center);
    }

    static public boolean File_NodesWrite(String filePath, LinkedList<Node> nodes, boolean center)
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
            childMidPos = RenderNode.RenderNode_NodesGetMiddlePos(nodes);
            childMidPos.left = -childMidPos.GetLeftDouble();
            childMidPos.right = -childMidPos.GetRightDouble();
        }

        for (int i = 0; i < nodes.size(); i++)
            data += RenderNode.RenderNode_ToFXMLString(nodes.get(i), childMidPos, 2) + "\n";

        data += "\t</children>\n";
        data += "</Group>\n";

        return File_Write(filePath, data, true);
    }

    static public LinkedList<Node> File_NodesLoad(boolean center)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Symbol");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("FXML File", "*.fxml"));
        java.io.File file = fileChooser.showOpenDialog(EDAmameApplication.App_Controller.Controller_Stage);

        if (file == null)
        {
            EDAmameController.Controller_SetStatusBar("Unable to load FXML file because the entered directory is invalid!");
        }
        else
        {
            try
            {
                return File_NodesRead(file.getAbsolutePath(), center);
            }
            catch (IOException e)
            {
                EDAmameController.Controller_SetStatusBar("Encountered error while loading FXML file!");
            }
        }

        return null;
    }

    static public LinkedList<Node> File_NodesRead(String filePath, boolean center) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader();
        filePath = filePath.replace('\\', '/');
        fxmlLoader.setLocation(new URL("file:///" + filePath));
        Node root = fxmlLoader.<Node>load();

        if (root.getClass() != Group.class)
        {
            EDAmameController.Controller_SetStatusBar("Attempting to load FXML file without parent Group node!");

            return null;
        }

        LinkedList<Node> nodes = new LinkedList<Node>();
        ObservableList<Node> children = ((Group)root).getChildren();

        for (int i = 0; i < children.size(); i++)
        {
            Node node = children.get(i);

            if ((node.getClass() == Circle.class) ||
                (node.getClass() == Rectangle.class) ||
                (node.getClass() == Polygon.class) ||
                (node.getClass() == Line.class) ||
                (node.getClass() == Text.class) ||
                (node.getClass() == Group.class))
            {
                nodes.add(node);
            }
            else
            {
                EDAmameController.Controller_SetStatusBar("Attempting to load FXML file with unrecognized shape types!");

                return null;
            }
        }

        if (center)
        {
            PairMutable childMidPos = RenderNode.RenderNode_NodesGetMiddlePos(nodes);
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

    /*static public LinkedList<Object> YAML_ListLoad()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("File File", "*.yaml"));
        java.io.File file = fileChooser.showOpenDialog(EDAmameApplication.App_Controller.Controller_Stage);

        if (file == null)
        {
            EDAmameController.Controller_SetStatusBar("Unable to load File file because the entered directory is invalid!");
        }
        else
        {
            try
            {
                return YAML_ListRead(file.getAbsolutePath());
            }
            catch (IOException e)
            {
                EDAmameController.Controller_SetStatusBar("Encountered error while loading File file!");
            }
        }

        return null;
    }

    static public LinkedList<Object> YAML_ListRead(String filePath) throws IOException
    {
        Yaml yaml = new Yaml();
        FileReader fileReader = new FileReader(filePath);

        return yaml.load(fileReader);
    }

    static public boolean YAML_ListSave(LinkedList<Object> list)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("File File", "*.yaml"));
        fileChooser.setInitialFileName("file.yaml");
        java.io.File file = fileChooser.showSaveDialog(EDAmameApplication.App_Controller.Controller_Stage);

        if (file == null)
        {
            EDAmameController.Controller_SetStatusBar("Unable to save File file because the entered directory is invalid!");
        }
        else
        {
            try
            {
                YAML_ListWrite(file.getAbsolutePath(), list);

                return true;
            }
            catch (IOException e)
            {
                EDAmameController.Controller_SetStatusBar("Encountered error while saving File file!");
            }
        }

        return false;
    }

    static public void YAML_ListWrite(String filePath, LinkedList<Object> list) throws IOException
    {
        Yaml yaml = new Yaml();
        FileWriter fileWriter = new FileWriter(filePath);
        yaml.dump(list, fileWriter);
    }*/

    static public boolean File_Write(String filePath, String data, boolean overwrite)
    {
        try
        {
            PrintWriter file = new PrintWriter(new FileOutputStream(filePath, !overwrite));
            file.print(data);
            file.close();
        }
        catch (IOException e)
        {
            return false;
        }

        return true;
    }
}
