package seda_project.control_alt_defeat.gamebox;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seda_project.control_alt_defeat.gamebox.Memory.Controller.MemoryMenu;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;


public class GameBox extends Application {
    private static final Logger logger = LoggerFactory.getLogger(GameBox.class);
    private static ViewStack vS;
    @Override
    public void start(Stage stage) throws IOException {
        vS = new ViewStack();

        stage.setTitle("GameBox");
        stage.centerOnScreen();
        stage.show();
        stage.setOnCloseRequest(_ -> cleanExit());

        String address = "/Views/Memory/MemoryMenu.fxml";
        FXMLLoader loader = new FXMLLoader(GameBox.class.getResource(address));
        vS.addFxmlLoaders(address);

        Parent root = loader.load();
        MemoryMenu controller = loader.getController();
        controller.handViewStack(vS);

        final var mainMenuScene = new Scene(root, 800, 600);
        stage.setScene(mainMenuScene);
        logger.debug("Startup completed");
    }

    public static void cleanExit() {
        logger.debug("Shutting down");
        Platform.exit();
        System.exit(0);
    }

    public static ViewStack getvS(){
        return vS;
    }
}