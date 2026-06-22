package seda_project.control_alt_defeat.gamebox.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.SoundController;
import seda_project.control_alt_defeat.gamebox.ViewStack;

import java.awt.*;

public class Controller {
    protected Configuration c = Configuration.getInstance();
    protected ViewStack vS = ViewStack.getInstance();
    protected SoundController sC = SoundController.getInstance();
    protected boolean flipped = false;
    protected boolean rainbowed = false;

    @FXML
    protected StackPane root;

    @FXML
    protected VBox header;

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

}
