module org.nstu.se.loglevel {
    requires javafx.controls;
    requires javafx.fxml;
    requires static org.jspecify;
    requires static com.github.spotbugs.annotations;

    opens org.nstu.se.lab3.ui to
            javafx.fxml;
    opens org.nstu.se.lab3.ui.settings to
            javafx.fxml;

    exports org.nstu.se.lab3;
    exports org.nstu.se.lab3.listener;
    exports org.nstu.se.lab3.model;
    exports org.nstu.se.lab3.timer;
    exports org.nstu.se.lab3.ui;
    exports org.nstu.se.lab3.ui.history;
    exports org.nstu.se.lab3.ui.indicator;
    exports org.nstu.se.lab3.ui.settings;
}
