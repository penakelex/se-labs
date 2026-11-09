module org.nstu.se.sound {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires static org.jspecify;

    opens org.nstu.se.lab4.ui to
            javafx.fxml;

    exports org.nstu.se.lab4;
    exports org.nstu.se.lab4.audio;
    exports org.nstu.se.lab4.audio.event;
    exports org.nstu.se.lab4.ui;
}
