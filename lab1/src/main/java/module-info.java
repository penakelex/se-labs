module com.example.sort {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.sort to javafx.fxml;
    exports com.example.sort;
}