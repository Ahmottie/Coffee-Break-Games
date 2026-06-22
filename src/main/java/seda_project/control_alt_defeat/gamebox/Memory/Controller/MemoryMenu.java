package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class MemoryMenu extends Controller {

    @FXML
    protected void onLocalAction(){
        sC.play("button");
        c.changeScene("/Views/Memory/LocalGameConfiguration.fxml",header,vS);
    }
    @FXML
    protected void onHostAction(){
        sC.play("button");
        c.changeScene("/Views/Memory/HostLanConfiguration.fxml",header,vS);
    }

    @FXML
    protected void onJoinAction(){
        sC.play("button");
        c.changeScene("/Views/Memory/JoinLanGame.fxml",header,vS);
    }

    @FXML
    protected void onExitAction(){
        sC.play("button");
        c.backScene(header, vS);

    }
}
