module org.nstu.se.sorting {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jspecify;

    opens org.nstu.se.lab1 to
            javafx.fxml;
    opens org.nstu.se.lab1.ui.animation to
            javafx.fxml;
    opens org.nstu.se.lab1.ui.menu to
            javafx.fxml;

    exports org.nstu.se.lab1;
    exports org.nstu.se.lab1.model;
    exports org.nstu.se.lab1.ui.animation;
    exports org.nstu.se.lab1.ui.menu;
}
