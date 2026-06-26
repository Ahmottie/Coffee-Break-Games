package seda_project.control_alt_defeat.gamebox;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Configuration {

    private static Configuration instance;
    public boolean crimson = false;

    private Configuration() {
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }


    public void deckSize(int tupleSize, RadioButton smallGame, RadioButton mediumGame, RadioButton largeGame){
        int max = (int) 45/tupleSize;
        int stepsize = max/3;
        System.out.println("tuple size: "+tupleSize);
        System.out.println("max: "+max);
        System.out.println("stepsize: "+stepsize);
        if (max == 2){
            smallGame.setText(String.valueOf(tupleSize));
            mediumGame.setDisable(false);
            mediumGame.setText(String.valueOf((max-stepsize)*tupleSize));
            largeGame.setDisable(true);
            largeGame.setText("---");
        }
        else if (max == 1){
            smallGame.setText(String.valueOf(tupleSize));
            mediumGame.setDisable(true);
            mediumGame.setText("---");
            largeGame.setDisable(true);
            largeGame.setText("---");
        }
        else {
            largeGame.setText(String.valueOf(max*tupleSize));
            mediumGame.setDisable(false);
            mediumGame.setText(String.valueOf((max-stepsize)*tupleSize));
            largeGame.setDisable(false);
            smallGame.setText(String.valueOf((max-2*stepsize)*tupleSize));
        }
    }

    public boolean checkNameLength(String name, int player, Label statusLabel) {
        int max = 16;
        int length = name.length();
        if (length > max){
            statusLabel.setVisible(true);
            statusLabel.setText("The name of player " + player +" may not be longer than "+max+" characters!");
            return false;
        }
        return true;
    }
    public String checkNameInput(String name, int player){
        if (name.equals("")){
            name = "Player " + player;
        }
        return name;
    }

    public int ActivePlayer(){
        //TODO create function that either returns 1 or 2
        return 1;
    }

    public Object changeScene(String address, VBox header, ViewStack vS){
        try {
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            vS.addFxmlLoaders(address);
            var controller = loader.getController();
            Stage stage = (Stage) header.getScene().getWindow();
            Scene currentScene = stage.getScene();

            Platform.runLater(() -> {
                currentScene.setFill(Color.TRANSPARENT);
                currentScene.setRoot(root);
                stage.sizeToScene();
                stage.centerOnScreen();
            });

            if (controller != null){
                return controller;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Object changeScene(String address, VBox header, ViewStack vS, int v, int v1){
        try {
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();

            vS.addFxmlLoaders(address);
            var controller = loader.getController();
            Scene newScene = new Scene(root, v, v1);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
            if (controller != null){
                return controller;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Object backScene(VBox header, ViewStack vS) {
        try {
            vS.popFxmlLoader();
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(vS.getFxmlLoader()));
            Parent root = loader.load();
            var controller = loader.getController();

            Stage stage = (Stage) header.getScene().getWindow();
            Scene currentScene = stage.getScene();

            currentScene.setRoot(root);

            if (controller != null) {
                return controller;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean checkFlip(String player1Name, String player2Name) {
        if (player1Name.equals("Upsidedown")){
            return true;
        }
        if (player2Name.equals("Upsidedown")){
            return true;
        }
        if (player1Name.equals("Dinnerbone")){
            return true;
        }
        if (player2Name.equals("Dinnerbone")){
            return true;
        }
        if (player1Name.equals("Grumm")){
            return true;
        }
        if (player2Name.equals("Grumm")){
            return true;
        }
        return false;
    }

    public boolean checkRainbow(String player1Name, String player2Name){
        if (player1Name.equals("jeb_")){
            return true;
        }
        if (player2Name.equals("jeb_")){
            return true;
        }
        if (player1Name.equals("Rainbow")){
            return true;
        }
        if (player2Name.equals("Rainbow")){
            return true;
        }
        return false;
    }
}
