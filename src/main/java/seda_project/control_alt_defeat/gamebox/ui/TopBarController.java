package seda_project.control_alt_defeat.gamebox.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.SoundController;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class TopBarController implements Initializable {
    private Image mute = new Image(Objects.requireNonNull(Controller.class.getResource("/Images/others/mute.png")).toExternalForm());
    private Image unmute = new Image(Objects.requireNonNull(Controller.class.getResource("/Images/others/unmute.png")).toExternalForm());
    private SoundController sC = SoundController.getInstance();
    @FXML
    private HBox topBar;

    @FXML
    protected void closeWindow() {
        Stage s = (Stage) topBar.getScene().getWindow();
        s.close();
    }

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (muteButton!= null) {
            initMute();
        }
    }
}
