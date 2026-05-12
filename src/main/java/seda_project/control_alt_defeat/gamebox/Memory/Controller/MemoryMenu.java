package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class MemoryMenu extends Controller {

    @FXML
    private VBox header;

    @FXML
    protected void onLocalAction(){
        c.changeScene("/Views/Memory/LocalGameConfiguration.fxml",header,vS);
    }
    @FXML
    protected void onHostAction(){
        c.changeScene("/Views/Memory/HostLanConfiguration.fxml",header,vS);
    }

    @FXML
    protected void onJoinAction(){
        c.changeScene("/Views/Memory/JoinLanGame.fxml",header,vS);
    }

    @FXML
    protected void onExitAction(){
        c.backScene(header, vS);

    }
}
