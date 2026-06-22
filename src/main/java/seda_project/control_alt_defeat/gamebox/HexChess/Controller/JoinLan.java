package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.network.Advertisement;
import seda_project.control_alt_defeat.gamebox.network.Discovery;
import seda_project.control_alt_defeat.gamebox.network.GameMode;
import seda_project.control_alt_defeat.gamebox.network.Listener;
import seda_project.control_alt_defeat.gamebox.network.LanClient;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class JoinLan extends Controller implements Initializable {

    private final ArrayList<Label> availableHosts = new ArrayList<>();

    private Listener discoveryListener;
    private Timeline refreshTimeline;
    private Set<String> shownHostIps = new HashSet<>();

    @FXML
    private VBox scrollElements;

    @FXML
    private TextField joinPlayerNameTF;

    @FXML
    private Label joinStatus, selectedHost;

    @FXML
    protected void onBackAction() {
        sC.play("button");
        c.backScene(header,vS);
    }
    @FXML
    protected void onConnectAction() {
        sC.play("button");
        if (selectedHost == null){
            joinStatus.setVisible(true);
            sC.play("error");
            joinStatus.setText("Select a Game to join!");
            return;
        }

        String yourName = c.checkNameInput(joinPlayerNameTF.getText(),2);
        if (!c.checkNameLength(yourName,2,joinStatus)) {
            joinStatus.setVisible(true);
            sC.play("error");
            joinStatus.setText("Your name cant be longer than 16 characters");
            return;
        }

        Advertisement ad = (Advertisement) selectedHost.getUserData();
        stopDiscovery();

        try {
            NetworkLayer layer = LanClient.join(ad.ipAddress(), ad.tcpPort());
            Session s = Session.current();
            s.myName   = yourName;
            s.isHost   = false;
            s.network  = layer;
            s.peerName = ad.name();
            s.boardState = ad.boardState();

            WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/HexChess/WaitForOpponent.fxml", header, vS);
            controller.passJoinData(yourName, ad.ipAddress());
        } catch (Exception e) {
            joinStatus.setVisible(true);
            sC.play("error");
            joinStatus.setText("Could not connect: " + e.getMessage());
        }
    }

    public void handData(String name){
        joinPlayerNameTF.setText(name);
    }

    private void startDiscovery() {
        try {
            discoveryListener = Discovery.listen();
            discoveryListener.disableSelfFilter();
        } catch (IOException e) {
            joinStatus.setVisible(true);
            joinStatus.setText("Could not start discovery: " + e.getMessage());
            return;
        }

        // poll the listener every 500ms
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.millis(500), e -> refreshHostList()));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
        refreshHostList();
    }

    private void stopDiscovery() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
        if (discoveryListener != null) {
            try { discoveryListener.close(); } catch (Exception ignored) {}
            discoveryListener = null;
        }
    }

    private void refreshHostList() {
        if (discoveryListener == null) return;
        List<Advertisement> ads = discoveryListener.currentHosts();

        Set<String> newIps = new HashSet<>();
        for (Advertisement ad : ads) newIps.add(ad.ipAddress());
        if (newIps.equals(shownHostIps)) return;

        String previouslySelectedIp = null;
        if (selectedHost != null) {
            Advertisement prev = (Advertisement) selectedHost.getUserData();
            previouslySelectedIp = prev != null ? prev.ipAddress() : null;
        }

        scrollElements.getChildren().clear();
        availableHosts.clear();
        selectedHost = null;

        for (Advertisement ad : ads) {
            if (ad.gameMode() != GameMode.HEXCHESS) continue;
            Label l = makeHostLabel(ad);
            availableHosts.add(l);
            scrollElements.getChildren().add(l);
            if (ad.ipAddress().equals(previouslySelectedIp)) {
                selectedHost = l;
                l.getStyleClass().add("box");
                l.getStyleClass().add("ready");
            }
        }
        shownHostIps = newIps;
    }


    private Label makeHostLabel(Advertisement ad) {
        Label l = new Label(ad.name() + "   (" + ad.ipAddress() + ")");
        l.setUserData(ad);
        l.setAlignment(Pos.CENTER);
        l.setOnMouseClicked(mouseEvent -> {
            deselectAll();
            selectedHost = l;
            l.getStyleClass().add("box");
            l.getStyleClass().add("ready");
        });
        return l;
    }

    private void deselectAll() {
        for (Label l : availableHosts) l.getStyleClass().clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        joinStatus.setVisible(false);

        startDiscovery();
    }

}
