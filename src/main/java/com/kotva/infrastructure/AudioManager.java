package com.kotva.infrastructure;

import java.net.URL;
import java.util.Objects;
import javafx.scene.media.AudioClip;

/**
 * Plays UI sound effects.
 */
public final class AudioManager {
    private static final String AUDIO_BASE_PATH = "/audio/";

    private final AudioClip uiClick;
    private final AudioClip actionConfirm;
    private final AudioClip tilePlace;
    private final AudioClip tileRecall;

    private double bgmVolume = 0.5;
    private double sfxVolume = 0.5;
    private double masterVolume = 0.5;
    private boolean warmedUp;

    public AudioManager() {
        this.uiClick = loadClip("ui_click.wav");
        this.actionConfirm = loadClip("action_confirm.wav");
        this.tilePlace = loadClip("tile_place.wav");
        this.tileRecall = loadClip("tile_recall.wav");
    }

    public void playUIClick() {
        playSoundEffect(uiClick);
    }

    public void playButtonClick() {
        playUIClick();
    }

    public void playActionConfirm() {
        playSoundEffect(actionConfirm);
    }

    public void playTilePlace() {
        playSoundEffect(tilePlace);
    }

    public void playTileRecall() {
        playSoundEffect(tileRecall);
    }

    public void playSoundEffect(AudioClip effect) {
        if (effect == null || sfxVolume <= 0.0) {
            return;
        }
        effect.play(sfxVolume * masterVolume);
    }

    public void setBGMVolume(double volume) {
        bgmVolume = normalizeVolume(volume);
    }

    public void setSFXVolume(double volume) {
        sfxVolume = normalizeVolume(volume);
    }

    public void setMasterVolume(double volume) {
        masterVolume = normalizeVolume(volume);
    }

    public double getBGMVolume() {
        return bgmVolume;
    }

    public double getSFXVolume() {
        return sfxVolume;
    }

    public double getMasterVolume() {
        return masterVolume;
    }

    public void warmUpSoundEffects() {
        if (warmedUp) {
            return;
        }
        warmedUp = true;
        warmUpClip(uiClick);
        warmUpClip(actionConfirm);
        warmUpClip(tilePlace);
        warmUpClip(tileRecall);
    }

    private AudioClip loadClip(String fileName) {
        Objects.requireNonNull(fileName, "fileName cannot be null.");
        URL resource = getClass().getResource(AUDIO_BASE_PATH + fileName);
        if (resource == null) {
            throw new IllegalStateException("Audio resource not found: " + AUDIO_BASE_PATH + fileName);
        }
        return new AudioClip(resource.toExternalForm());
    }

    private void warmUpClip(AudioClip clip) {
        if (clip == null) {
            return;
        }
        clip.play(0.0);
    }

    private double normalizeVolume(double volume) {
        if (Double.isNaN(volume) || Double.isInfinite(volume)) {
            throw new IllegalArgumentException("volume must be a finite number.");
        }
        return Math.max(0.0, Math.min(1.0, volume));
    }
}
