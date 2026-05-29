package seda_project.control_alt_defeat.gamebox.ui;

// copied from https://gist.github.com/TheItachiUchiha/12e40a6f3af6e1eb6f75

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ToggleSwitch extends HBox {
    private final Label label = new Label();
    private final Button button = new Button();

    private SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty switchOnProperty() { return switchedOn; }

    private void init() {

        label.setText("OFF");

        getChildren().addAll(label, button);
        button.setOnAction((e) -> {
            switchedOn.set(!switchedOn.get());
        });
        label.setOnMouseClicked((e) -> {
            switchedOn.set(!switchedOn.get());
        });
        setStyle();
        bindProperties();
    }

    private void setStyle() {
        //Default Width
        setWidth(80);
        label.setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: grey; -fx-text-fill:black; -fx-background-radius: 5;");
        setAlignment(Pos.CENTER_LEFT);
    }

    private void bindProperties() {
        label.prefWidthProperty().bind(widthProperty().divide(2));
        label.prefHeightProperty().bind(heightProperty());
        button.prefWidthProperty().bind(widthProperty().divide(2));
        button.prefHeightProperty().bind(heightProperty());
    }

    public ToggleSwitch() {
        init();
        switchedOn.addListener((a,b,c) -> {
            if (c) {
                label.setText("ON");
                setStyle("-fx-background-color: rgba(10, 160, 60, 0.45);-fx-text-fill:black; -fx-background-radius: 5;");
                label.toFront();
            }
            else {
                label.setText("OFF");
                setStyle("-fx-background-color: grey;-fx-text-fill:black; -fx-background-radius: 5;");
                button.toFront();
            }
        });
    }

    public void setSwitchedOn(boolean value) {
        switchedOn.set(value);
    }
}
