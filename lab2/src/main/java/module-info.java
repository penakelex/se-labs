module org.nstu.se.rx {
    requires javafx.controls;
    requires javafx.fxml;
    requires static org.jspecify;

    opens org.nstu.se.lab2.ui to
            javafx.fxml;

    exports org.nstu.se.lab2;
    exports org.nstu.se.lab2.annotations;
    exports org.nstu.se.lab2.core;
    exports org.nstu.se.lab2.core.exceptions;
    exports org.nstu.se.lab2.core.convert;
    exports org.nstu.se.lab2.core.parsing;
    exports org.nstu.se.lab2.core.reflection;
    exports org.nstu.se.lab2.services;
    exports org.nstu.se.lab2.ui;
}
