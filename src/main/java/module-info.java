module com.cyte.edamame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.h2database;
    requires java.prefs;


    opens com.cyte.edamame to javafx.fxml;
    exports com.cyte.edamame;
    opens com.cyte.edamame.editor to javafx.fxml;
    exports com.cyte.edamame.editor;
}