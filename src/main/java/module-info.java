module com.cyte.edamame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.h2database;


    opens com.cyte.edamame to javafx.fxml;
    exports com.cyte.edamame;
}