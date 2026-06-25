package seda_project.control_alt_defeat.gamebox;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;

public class SoundController {
    private static SoundController instance;
    private final HashMap<String, Media> musicclips = new HashMap<>();
    private final HashMap<String, AudioClip> sfxclips = new HashMap<>();
    private AudioClip looping;
    private boolean konami = false;
    private int konamiSound = 1;
    private MediaPlayer mediaPlayer;
    private boolean muteState = false;

    public static SoundController getInstance() {
        if (instance == null) {
            instance = new SoundController();
        }
        return instance;
    }

    //Method for sound Indicators like buttons etc.
    public void play(String name){
        play(name,1.0);
    }

    public void play(String name,double volume){
        if (muteState) return;
        AudioClip clip = sfxclips.computeIfAbsent(name, key -> {
            URL url = getClass().getResource("/Sounds/" + key + ".wav");
            return new AudioClip(url.toString());
        });

        clip.play(volume);
    }
    //Method for background Music
    public void playLooping(String name) {
        playLooping(name,1.0);
    }

    public void playLooping(String name, double volume) {
        if (konami) return;

        stopLooping();

        Media media = musicclips.computeIfAbsent(name, key -> {
            URL url = getClass().getResource("/Sounds/" + key + ".wav");
            return new Media(url.toString());
        });

        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volume);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        if (muteState) return;
        mediaPlayer.play();
    }

    public void stopLooping() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    public void activateKonami(){
        if (!konami) {
            konami = true;
            stopLooping();
            next();
        }

    }
    private void next(){
        Media media = new Media(getClass().getResource("/Sounds/konami_"+konamiSound+".wav").toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia( () -> {
            if (konamiSound == 11){
                konamiSound = 1;
            }
            else {
                konamiSound ++;
            }
            next();
        });
        if (muteState) return;
        mediaPlayer.play();
    }

    public boolean getMute() {
        return muteState;
    }

    public void setMute(boolean muteState) {
        this.muteState = muteState;
        if (muteState) {
            pause();
        }
        else {
            resume();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }
}
