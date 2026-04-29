module seda.project.control.alt.defeat.gamebox {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires javafx.graphics;
    requires java.xml;

    opens seda_project.control_alt_defeat.gamebox to javafx.fxml;
    exports seda_project.control_alt_defeat.gamebox to javafx.graphics;

    opens seda_project.control_alt_defeat.gamebox.Memory.Controller to javafx.fxml;
    exports seda_project.control_alt_defeat.gamebox.Memory.Controller to javafx.fxml;
    exports seda_project.control_alt_defeat.gamebox.Memory to javafx.fxml;
    opens seda_project.control_alt_defeat.gamebox.Memory to javafx.fxml;
    exports seda_project.control_alt_defeat.gamebox.ui to javafx.graphics;
    opens seda_project.control_alt_defeat.gamebox.ui to javafx.fxml;

    exports seda_project.control_alt_defeat.gamebox.network;
    exports seda_project.control_alt_defeat.gamebox.Memory.engine;
}
