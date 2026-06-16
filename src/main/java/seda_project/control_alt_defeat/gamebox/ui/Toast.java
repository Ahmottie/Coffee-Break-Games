//https://stackoverflow.com/questions/26792812/android-toast-equivalent-in-javafx
package seda_project.control_alt_defeat.gamebox.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public final class Toast
{
    public static void makeText(StackPane rootPane, String toastMsg)
    {
        int toastMsgTime = 1000;
        int fadeInTime = 250;
        int fadeOutTime= 250;

        Label label = new Label(toastMsg);
        label.getStyleClass().add("normalText");
        label.getStyleClass().add("box");
        label.setStyle("-fx-padding: 20px;");
        label.wrapTextProperty().set(true);
        label.alignmentProperty().set(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);

        StackPane.setAlignment(label, Pos.CENTER);
        rootPane.getChildren().add(label);

        Timeline fadeInTimeline = new Timeline(
                new KeyFrame(Duration.millis(fadeInTime), new KeyValue(label.opacityProperty(), 1))
        );
        fadeInTimeline.setOnFinished((ae) ->
        {
            new Thread(() -> {
                try
                {
                    Thread.sleep(toastMsgTime);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    Timeline fadeOutTimeline = new Timeline(
                            new KeyFrame(Duration.millis(fadeOutTime), new KeyValue(label.opacityProperty(), 0))
                    );
                    fadeOutTimeline.setOnFinished((aeb) -> rootPane.getChildren().remove(label));
                    fadeOutTimeline.play();
                });
            }).start();
        });
        fadeInTimeline.play();
    }
}