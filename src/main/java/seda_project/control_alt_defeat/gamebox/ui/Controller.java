package seda_project.control_alt_defeat.gamebox.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.SoundController;
import seda_project.control_alt_defeat.gamebox.ViewStack;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.header != null) {
            header.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                        if (mouseEvent.getClickCount() == 2) {
                            c.crimson = !c.crimson;
                            if (c.crimson) {
                                root.getStylesheets().remove(Controller.class.getResource("/css/stylesheets/MainTheme.css").toExternalForm());
                                root.getStylesheets().add(Controller.class.getResource("/css/stylesheets/CrimsonTheme.css").toExternalForm());
                            } else {
                                root.getStylesheets().remove(Controller.class.getResource("/css/stylesheets/CrimsonTheme.css").toExternalForm());
                                root.getStylesheets().add(Controller.class.getResource("/css/stylesheets/MainTheme.css").toExternalForm());
                            }
                        }
                    }
                }
            });
        }
        if (c.crimson){
            root.getStylesheets().remove(Controller.class.getResource("/css/stylesheets/MainTheme.css").toExternalForm());
            root.getStylesheets().add(Controller.class.getResource("/css/stylesheets/CrimsonTheme.css").toExternalForm());
        }
        Rectangle clip = new Rectangle();

        clip.setArcWidth(32);
        clip.setArcHeight(32);
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        root.setClip(clip);
    }
}
