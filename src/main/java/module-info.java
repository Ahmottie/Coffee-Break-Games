module seda_project.control_alt_defeat.gamebox {
    // Required Libraries
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires java.xml;
    requires java.desktop;
    requires javafx.base;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires jdk.incubator.vector;
    requires jdk.dynalink;
    requires javafx.media;
    requires com.dlsc.fxmlkit;

    // Main Package
    exports seda_project.control_alt_defeat.gamebox;
    opens seda_project.control_alt_defeat.gamebox to javafx.fxml;

    // Memory Game Engine
    exports seda_project.control_alt_defeat.gamebox.Memory.engine;
    opens seda_project.control_alt_defeat.gamebox.Memory.engine to javafx.fxml;

    // Memory Controllers and Logic
    exports seda_project.control_alt_defeat.gamebox.Memory.Controller;
    opens seda_project.control_alt_defeat.gamebox.Memory.Controller to javafx.fxml;

    // Memory
    exports seda_project.control_alt_defeat.gamebox.Memory;
    opens seda_project.control_alt_defeat.gamebox.Memory to javafx.fxml;

    //Tetris Controller
    exports  seda_project.control_alt_defeat.gamebox.Tetris.Controller;
    opens seda_project.control_alt_defeat.gamebox.Tetris.Controller to javafx.fxml;

    //Hex Chess Controller
    exports  seda_project.control_alt_defeat.gamebox.HexChess.Controller;
    opens seda_project.control_alt_defeat.gamebox.HexChess.Controller to javafx.fxml;

    // UI
    exports seda_project.control_alt_defeat.gamebox.ui;
    opens seda_project.control_alt_defeat.gamebox.ui to javafx.fxml;

    // Network
    exports seda_project.control_alt_defeat.gamebox.network;
    exports seda_project.control_alt_defeat.gamebox.Tetris.Engine;
    opens seda_project.control_alt_defeat.gamebox.Tetris.Engine to javafx.fxml;

}