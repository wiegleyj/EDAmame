/**
 * EDAmame access module.
 *
 * @author Jeff Wiegley, Ph.D.
 * @author jeffrey.wiegley@gmail.com
 */
module com.cyte.edamame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.h2database;
    requires java.prefs;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires org.yaml.snakeyaml;


    opens com.cyte.edamame to javafx.fxml, javafx.graphics;
    //exports com.cyte.edamame;
    opens com.cyte.edamame.editor to javafx.fxml, javafx.graphics, com.fasterxml.jackson.databind;
    opens com.cyte.edamame.util to javafx.fxml, javafx.graphics;
    //exports com.cyte.edamame.editor;
}