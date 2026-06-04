package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class HostLanConfiguration  extends Controller {
    @FXML
    private VBox header;

    @FXML
    protected void onSearchAction() {
        //TODO Change Scene and Lan Stuff
    }

    @FXML
    protected void onBackAction() {
        c.backScene(header,vS);
    }
}
