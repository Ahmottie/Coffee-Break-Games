package seda_project.control_alt_defeat.gamebox.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.SoundController;
import seda_project.control_alt_defeat.gamebox.ViewStack;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    protected Configuration c = Configuration.getInstance();
    protected ViewStack vS = ViewStack.getInstance();
    protected SoundController sC = SoundController.getInstance();
    protected boolean flipped = false;
    protected boolean rainbowed = false;


    private Image mute = new Image(Objects.requireNonNull(Controller.class.getResource("/Images/others/mute.png")).toExternalForm());
    private Image unmute = new Image(Objects.requireNonNull(Controller.class.getResource("/Images/others/unmute.png")).toExternalForm());

    @FXML
    protected StackPane root;

    @FXML
    protected VBox header;

    @FXML
    private ImageView muteButton;

    @FXML
    protected void changeMute(){
        boolean next = sC.getMute();
        sC.setMute(!next);
        setMuteImageView(!next);
        changeSound(next);
    }

    public void initMute(){
        boolean current = sC.getMute();
        setMuteImageView(current);
    }

    public void setMuteImageView(boolean toSet){
        if (toSet){
            muteButton.setImage(mute);
        }
        else{
            muteButton.setImage(unmute);
        }
    }

    public void changeSound(boolean toSet){
        if (toSet){
            sC.resume();
        }
        else {
            sC.pause();
        }
    }

    public void flip() {
        flipped = true;
        root.rotateProperty().setValue(180);
    }

    public void rainbow() {
        rainbowed = true;
        IntegerProperty hue = new SimpleIntegerProperty(0);

        hue.addListener((observable, oldValue, newValue) -> {
            root.setStyle(String.format("-fx-background-color: hsb(%d, 80%%, 90%%);", newValue.intValue()));
        });

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(hue, 0)),
                new KeyFrame(Duration.seconds(10), new KeyValue(hue, 360))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMute();
    }
}
