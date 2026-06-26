package seda_project.control_alt_defeat.gamebox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dlsc.fxmlkit.fxml.FxmlKit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameBox extends Application {
    @FXML
    VBox header;

    private static final Logger logger = LoggerFactory.getLogger(GameBox.class);
    private static ViewStack vS =  ViewStack.getInstance();
    private SoundController sC = SoundController.getInstance();
    Configuration c = Configuration.getInstance();
    private List<KeyCode> konamiList = new ArrayList<>();
    private List<KeyCode> comparisionList = List.of(KeyCode.UP,KeyCode.UP, KeyCode.DOWN,KeyCode.DOWN, KeyCode.LEFT,KeyCode.RIGHT, KeyCode.LEFT,KeyCode.RIGHT, KeyCode.B,KeyCode.A);

    @Override
    public void start(Stage stage) throws IOException {
        FxmlKit.enableDevelopmentMode();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Coffee Break Game");
        stage.centerOnScreen();
        stage.show();
        stage.setMinHeight(600);
        stage.setMinWidth(800);
        stage.resizableProperty().setValue(Boolean.FALSE);
        stage.getIcons().add(new Image(GameBox.class.getResource("/Images/others/Application_Icon.png").toExternalForm()));
        String address = "/Views/StartingScreen.fxml";
        FXMLLoader loader = new FXMLLoader(GameBox.class.getResource(address));
        vS.addFxmlLoaders(address);
        sC.playLooping("lobby_background",.2);
        var mainMenuScene = new Scene(loader.load());
        mainMenuScene.setFill(Color.TRANSPARENT);
        konamiListener(stage);
        stage.setScene(mainMenuScene);
        stage.sizeToScene();
        stage.centerOnScreen();
        logger.debug("Startup completed");
    }

    private void konamiListener(Stage stage) {
        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(konamiList.size() == 1 && !konamiList.getFirst().equals(KeyCode.UP)) {
                konamiList.clear();
            }
            else if (konamiList.size() == 2){
                KeyCode first = konamiList.getFirst();
                KeyCode second = konamiList.get(1);
                if (first.equals(KeyCode.UP) &&  second.equals(KeyCode.UP)) {
                    if (event.getCode().equals(KeyCode.UP)){
                        konamiList.removeFirst();
                    }
                }
            } else if (konamiList.size() >= 3 && event.getCode().equals(KeyCode.UP)) {
                konamiList.clear();
            }
            konamiList.add(event.getCode());
            verify();
            System.out.println(konamiList.toString());
        });
    }

    private void verify(){
        if (konamiList.equals(comparisionList)) {
            System.out.println("Is the same");
            sC.activateKonami();
        }
        else if (konamiList.size() >= 10){
            System.out.println("Clear");
            konamiList.clear();
        }
        else if (konamiList.stream().anyMatch(keyCode -> !comparisionList.contains(keyCode))) {
            System.out.println("Clear");
            konamiList.clear();
        }
    }

    public static void cleanExit() {
        logger.debug("Shutting down");
        Platform.exit();
        System.exit(0);
    }
}