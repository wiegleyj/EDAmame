package com.cyte.edamame;

import com.cyte.edamame.editor.Editor;
import com.cyte.edamame.editor.SymbolEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EDAmameController implements Initializable {
    private final static Logger LOGGER = Logger.getLogger(EDAmame.class.getName());

    @FXML
    private TabPane controlTabPane;

    @FXML
    private TabPane editorTabPane;

    @FXML
    private TextArea logArea;

    @FXML
    protected void clearLogAction() {
        System.out.println();
        logArea.clear();
    }
    @FXML
    protected void onTestButtonClick() {
        System.out.println("test clicked");
        try {
            Editor editor = SymbolEditor.create();
            if (editor.getEditorTab() != null)
                editorTabPane.getTabs().add(editor.getEditorTab());
            if (editor.getControlTabIDs() != null)
                controlTabPane.getTabs().addAll(editor.getControlTabs());
            for (var id : editor.getControlTabIDs())
                System.out.println("    " + id.getValue());
            System.out.println("ToolBar ID: " + editor.getToolBarID().getValue());
            System.out.println("Menus:");
            for (var id : editor.getMenuItemIDs())
                System.out.println("  " + id.getValue());
        } catch (IOException ignored) {
        }
    }

    @FXML
    protected void onTestButtonClickDatabase() {
//        welcomeText.setText("Welcome to JavaFX Application!");
        System.out.println("hi");

        String classpath = System.getProperty("java.class.path");
        String[] classPathValues = classpath.split(File.pathSeparator);
        for (String classPath: classPathValues) {
            System.out.println(classPath);
        }

        try {
            Class.forName ("org.h2.Driver");
            System.out.println("HELLO");
            Connection c = DriverManager.getConnection("jdbc:h2:~/testtest", "SA", "");

            String statement = """
                    CREATE TABLE symbols (
                    SymbolID BINARY(16) NOT NULL PRIMARY KEY,
                    Version SMALLINT NOT NULL DEFAULT 1,
                    Created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    Modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    Author VARCHAR(255),
                    YAML BLOB NOT NULL);""";
            System.out.println(statement);
            try (Statement stmt = c.createStatement()) {
                System.out.println("executing");
                int result = stmt.executeUpdate("DROP TABLE symbols IF EXISTS;");
                System.out.println("dropped");
                Thread.sleep(5000);
                stmt.executeUpdate(statement);
                System.out.println("completed");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            c.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }

    }

    public void initialize(URL url, ResourceBundle rb) {
        for (Handler handler : LOGGER.getHandlers()) {
            LOGGER.removeHandler(handler);
        }

        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(new TextAreaHandler(logArea));
        LOGGER.log(Level.INFO, "Initialization Commenced...\n");
        LOGGER.log(Level.INFO, "Initialization Complete\n");
    }
}