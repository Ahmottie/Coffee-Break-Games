package seda_project.control_alt_defeat.gamebox;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MyController {
    private int n = 0;

    @FXML
    private Label testText;

    @FXML
    protected void onMemoryButtonAction() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MemoryOptions.fxml"));
            Scene memoryScene = new Scene(loader.load(), 800,600);

            Stage stage = (Stage)  testText.getScene().getWindow();

            stage.setScene(memoryScene);
            stage.show();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }


}
