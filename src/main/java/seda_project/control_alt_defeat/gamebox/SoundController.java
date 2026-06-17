package seda_project.control_alt_defeat.gamebox;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;

public class SoundController {
    private static SoundController instance;
    private final HashMap<String, AudioClip> clips = new HashMap<>();
    private AudioClip looping;
    private boolean konami = false;
    private int konamiSound = 1;
    private MediaPlayer mediaPlayer;

    public static SoundController getInstance() {
        if (instance == null) {
            instance = new SoundController();
        }
        return instance;
    }

    //Method for sound Indicators like buttons etc.
    public void play(String name){
        clips.  computeIfAbsent(name, key -> {
            URL url = getClass().getResource("/Sounds/"+key+".wav");
            return new AudioClip(url.toString());
        });

        AudioClip clip = clips.get(name);
        if (clip != null) clip.play();
    }
    public void play(String name,double volume){
        clips.computeIfAbsent(name, key -> {
            URL url = getClass().getResource("/Sounds/"+key+".wav");
            return new AudioClip(url.toString());
        });

        AudioClip clip = clips.get(name);
        if (clip != null) clip.play(volume);
    }
    //Method for background Music
    public void playLooping(String name) {
        if(!konami) {
            stopLooping();
            play(name);
            AudioClip clip = clips.get(name);
            if (clip != null) {
                clip.setCycleCount(AudioClip.INDEFINITE);
                clip.play();
                looping = clip;
            }
        }
    }

    public void playLooping(String name, double volume) {
        if (!konami) {
            stopLooping();
            play(name,volume);
            AudioClip clip = clips.get(name);
            if (clip != null) {
                clip.setCycleCount(AudioClip.INDEFINITE);
                clip.play(volume);
                looping = clip;
            }
        }
    }

    public void stopLooping() {
        if (looping != null) {
            looping.stop();
            looping = null;
        }
    }

    public void activateKonami(){
        konami = true;
        stopLooping();
        next();

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
        mediaPlayer.play();
    }
}
