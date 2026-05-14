package seda_project.control_alt_defeat.gamebox;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GameBox extends Application {
    private static final Logger logger = LoggerFactory.getLogger(GameBox.class);
    private static ViewStack vS =  ViewStack.getInstance();
    Configuration c = Configuration.getInstance();

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("GameBox");
        stage.centerOnScreen();
        stage.show();
        stage.setOnCloseRequest(_ -> cleanExit());
        String address = "/Views/StartingScreen.fxml";
        FXMLLoader loader = new FXMLLoader(GameBox.class.getResource(address));
        vS.addFxmlLoaders(address);
        final var mainMenuScene = new Scene(loader.load(), 800, 600);

        stage.setScene(mainMenuScene);
        logger.debug("Startup completed");
    }

    @FXML
    VBox header;

    @FXML
    public void onExitAction(){
        cleanExit();
    }
    @FXML
    public void onMemoryAction(){
        c.changeScene("/Views/Memory/MemoryMenu.fxml",header, vS);
    }
    @FXML
    public void onTetrisAction(){
        c.changeScene("/Views/Tetris/TetrisMenu.fxml",header, vS);
    }

    public static void cleanExit() {
        logger.debug("Shutting down");
        Platform.exit();
        System.exit(0);
    }
}