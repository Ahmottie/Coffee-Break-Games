package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class HostLanConfiguration  extends Controller {
    private String boardState;

    @FXML
    private VBox header;

    @FXML
    private TextField hostNameTF;

    @FXML
    private Label statusLabel;

    @FXML
    protected void onSearchAction() {
        String yourName = c.checkNameInput(hostNameTF.getText(),1);
        if (c.checkNameLength(yourName,1,statusLabel)) {
            Session s = Session.current();
            s.myName = yourName;
            s.isHost = true;
            if (boardState != null) {
                s.boardState = boardState;
            }
            WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/HexChess/WaitForOpponent.fxml", header, vS);
            controller.passHostData(yourName, boardState);
        }
    }


    @FXML
    protected void onBackAction() {
        Session.clear();
        c.backScene(header,vS);
    }

    public void handData(String name){
        hostNameTF.setText(name);
    }

    public void boardSelection(String notation, String p1Name) {
        this.boardState = notation;
        if (p1Name != null){
            hostNameTF.setText(p1Name);
        }
    }

    public void onCustomBoardAction() {
        BoardDesigner controller = (BoardDesigner) c.changeScene("/Views/HexChess/BoardDesigner.fxml",header,vS);

        controller.handNames(hostNameTF.getText(),null);
    }
}
