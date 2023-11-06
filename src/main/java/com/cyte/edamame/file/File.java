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

import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.collections.ObservableList;
import java.io.PrintWriter;
import java.net.URL;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.geometry.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class File
{
    static public boolean File_NodesSave(LinkedList<RenderNode> nodes)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Symbol");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("FXML File", "*.fxml"));
        fileChooser.setInitialFileName("symbol.fxml");
        java.io.File file = fileChooser.showSaveDialog(EDAmameApplication.App_Controller.Controller_Stage);

        if (file == null)
        {
            EDAmameController.Controller_SetStatusBar("Unable to save FXML because the entered directory is invalid!");
        }
        else
        {
            try
            {
                File_NodesWrite(file.getAbsolutePath(), nodes);

                return true;
            }
            catch (IOException e)
            {
                EDAmameController.Controller_SetStatusBar("Encountered error while saving FXML file!");
            }
        }

        return false;
    }

    static public void File_NodesWrite(String filePath, LinkedList<RenderNode> nodes) throws IOException
    {
        PrintWriter file = new PrintWriter(filePath, "UTF-8");

        file.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        file.println("<?import java.lang.Double?>");
        file.println("<?import javafx.scene.layout.Pane?>");
        file.println("<?import javafx.scene.shape.Circle?>");
        file.println("<?import javafx.scene.shape.Rectangle?>");
        file.println("<?import javafx.scene.shape.Polygon?>");
        file.println("<?import javafx.scene.shape.Line?>");
        file.println("<?import javafx.scene.control.Label?>");
        file.println("<?import javafx.scene.text.Font?>\n");

        file.println("<Pane maxHeight=\"-Infinity\" maxWidth=\"-Infinity\" minHeight=\"-Infinity\" minWidth=\"-Infinity\" xmlns=\"http://javafx.com/javafx/20.0.1\" xmlns:fx=\"http://javafx.com/fxml/1\">");
        file.println("\t<children>");

        for (int i = 0; i < nodes.size(); i++)
            file.println("\t\t" + nodes.get(i).RenderNode_ToFXMLString());

        file.println("\t</children>");
        file.println("</Pane>");

        file.close();
    }

    static public LinkedList<RenderNode> File_NodesLoad()
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
                return File_NodesRead(file.getAbsolutePath());
            }
            catch (IOException e)
            {
                EDAmameController.Controller_SetStatusBar("Encountered error while loading FXML file!");
            }
        }

        return null;
    }

    static public LinkedList<RenderNode> File_NodesRead(String filePath) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader();
        filePath = filePath.replace('\\', '/');
        fxmlLoader.setLocation(new URL("file:///" + filePath));
        Node root = fxmlLoader.<Node>load();

        if (root.getClass() != Pane.class)
        {
            EDAmameController.Controller_SetStatusBar("Attempting to load FXML file without parent Pane class!");

            return null;
        }

        LinkedList<RenderNode> renderNodes = new LinkedList<RenderNode>();
        ObservableList<Node> children = ((Pane)root).getChildren();

        for (int i = 0; i < children.size(); i++)
        {
            Node node = children.get(i);

            if ((node.getClass() == Circle.class) ||
                (node.getClass() == Rectangle.class) ||
                (node.getClass() == Polygon.class) ||
                (node.getClass() == Line.class) ||
                (node.getClass() == Label.class))
            {
                RenderNode renderNode = new RenderNode("Loaded Node", node, false, null);

                renderNodes.add(renderNode);
            }
            else
            {
                EDAmameController.Controller_SetStatusBar("Attempting to load FXML file with unrecognized shape types!");

                return null;
            }
        }

        return renderNodes;
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
}
