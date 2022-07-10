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


    opens com.cyte.edamame to javafx.fxml, javafx.graphics;
    //exports com.cyte.edamame;
    opens com.cyte.edamame.editor to javafx.fxml, javafx.graphics;
    //exports com.cyte.edamame.editor;
}