module com.cyte.edamame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.cyte.edamame to javafx.fxml;
    exports com.cyte.edamame;
}