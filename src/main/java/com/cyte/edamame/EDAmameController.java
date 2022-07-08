package com.cyte.edamame;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class EDAmameController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
        System.out.println("hi");

        String classpath = System.getProperty("java.class.path");
        String[] classPathValues = classpath.split(File.pathSeparator);
        for (String classPath: classPathValues) {
            System.out.println(classPath);
        }

        try {
            //Class.forName("org.hsqldb.jdbc.JDBCDriver");
            Class.forName ("org.hsqldb.jdbcDriver");
            System.out.println("HELLO");
            Connection c = DriverManager.getConnection("jdbc:hsqldb:file:~/test", "SA", "");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("FUCK");
            e.printStackTrace();
        }

    }
}