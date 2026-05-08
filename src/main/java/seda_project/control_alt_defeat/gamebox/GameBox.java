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
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.Controller.MemoryMenu;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.Tetris.Controller.TetrisMenu;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.TetrisSettings;


public class GameBox extends Application {
    private static final Logger logger = LoggerFactory.getLogger(GameBox.class);
    private static ViewStack vS;
    Configuration c = new Configuration();
    TetrisSettings tS = new TetrisSettings();
    @Override
    public void start(Stage stage) throws IOException {
        vS = new ViewStack();

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
        MemoryMenu controller = (MemoryMenu) c.changeScene("/Views/Memory/MemoryMenu.fxml",header, vS);
        controller.handViewStack(vS,c);
    }
    @FXML
    public void onTetrisAction(){
        TetrisMenu controller = (TetrisMenu) c.changeScene("/Views/Tetris/TetrisMenu.fxml",header, vS);
        controller.handViewStack(vS,c);
        controller.handSettings(tS);
    }

    public static void cleanExit() {
        logger.debug("Shutting down");
        Platform.exit();
        System.exit(0);
    }

    public static ViewStack getvS(){
        return vS;
    }
    public void handViewStack(ViewStack vs,Configuration c){
        this.vS = vs;
        this.c = c;
    }
}