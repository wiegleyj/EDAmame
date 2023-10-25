/*
 * Copyright (c) 2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.file;

import com.cyte.edamame.EDAmameApplication;
import com.cyte.edamame.EDAmameController;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class YAML
{
    static public LinkedList<Object> YAML_ListLoad()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("YAML File", "*.yaml"));
        File file = fileChooser.showOpenDialog(EDAmameApplication.App_Controller.Controller_Stage);

        if (file == null)
        {
            EDAmameController.Controller_SetStatusBar("Unable to load YAML file because the entered directory is invalid!");
        }
        else
        {
            try
            {
                return YAML_ListRead(file.getAbsolutePath());
            }
            catch (IOException e)
            {
                EDAmameController.Controller_SetStatusBar("Encountered error while loading YAML file!");
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
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("YAML File", "*.yaml"));
        fileChooser.setInitialFileName("file.yaml");
        File file = fileChooser.showSaveDialog(EDAmameApplication.App_Controller.Controller_Stage);

        if (file == null)
        {
            EDAmameController.Controller_SetStatusBar("Unable to save YAML file because the entered directory is invalid!");
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
                EDAmameController.Controller_SetStatusBar("Encountered error while saving YAML file!");
            }
        }

        return false;
    }

    static public void YAML_ListWrite(String filePath, LinkedList<Object> list) throws IOException
    {
        Yaml yaml = new Yaml();
        FileWriter fileWriter = new FileWriter(filePath);
        yaml.dump(list, fileWriter);
    }
}
