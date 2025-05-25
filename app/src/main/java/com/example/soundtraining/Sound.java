package com.example.soundtraining;

import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class Sound extends Exercise {
    private static final String TAG = "Sound";
    private static int sampleRate = 44100;
    private static final int DURATION_MS = 4000;
    private static final int DURATION_GENERATOR_MS = 20000;
    private static int LOWER_FREQ_LIMIT = 100;
    private static int UPPER_FREQ_LIMIT = 8000;
    public static int FREQUENCY_LIMIT_TO_EARN_10_POINTS = 2000;
    private static boolean monoMode = false;
    private static int bufferSize = 4096;

    private AudioTrack audioTrack;
    private MediaPlayer mediaPlayer;
    private AssetManager assetManager;
    private long moduleStart;
    private SharedPreferences statsPrefs;

    public Sound(int m, int n) {
        super(R.layout.sound);
        this.m = m;
        this.n = n;
    }

    public Sound() {
        super(R.layout.frequency);
    }

    public static void setLowerFreqLimit(int v) { LOWER_FREQ_LIMIT = v; }
    public static void setUpperFreqLimit(int v) { UPPER_FREQ_LIMIT = v; }
    public static int getLowerFreqLimit() { return LOWER_FREQ_LIMIT; }
    public static int getUpperFreqLimit() { return UPPER_FREQ_LIMIT; }
    public static void setMonoMode(boolean mono) { monoMode = mono; }
    private static int getChannelConfig() {
        return monoMode ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
    }
    public static void setBufferSize(int bs) { bufferSize = bs; }
    public static void setSampleRate(int sr) { sampleRate = sr; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(
                MainMenu.emode == MainMenu.ExerciseMode.FREQUENCY ? R.layout.frequency : R.layout.sound,
                container, false);
        assetManager = mainView.getContext().getAssets();
        statsPrefs = mainView.getContext().getSharedPreferences("stats", 0);
        currentInformations = mainView.findViewById(R.id.currentInformations);
        messageSystem = new UserMessages(currentInformations, mainView.getContext());
        loadSounds();
        setListForRandomGenerator();
        setupBasedOnMode();
        return mainView;
    }

    @Override
    public void onResume() {
        super.onResume();
        moduleStart = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        long now = System.currentTimeMillis();
        long durationSec = (now - moduleStart) / 1000;
        long prevTime = statsPrefs.getLong("time_Sound", 0);
        statsPrefs.edit().putLong("time_Sound", prevTime + durationSec).apply();

        Calendar cal = Calendar.getInstance();
        String dayKey = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(now));
        int dayIndex = 0;
        Calendar today = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            if (today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)) {
                break;
            }
            today.add(Calendar.DAY_OF_YEAR, -1);
            dayIndex++;
        }
        float prevSession = statsPrefs.getFloat("session_" + dayIndex, 0f);
        statsPrefs.edit().putFloat("session_" + dayIndex, prevSession + durationSec / 60f).apply();
        logEvent("SESSION:" + now + ":" + durationSec);
    }

    protected void awardPoints(int pts) {
        long now = System.currentTimeMillis();
        int prevPts = statsPrefs.getInt("points_Sound", 0);
        statsPrefs.edit().putInt("points_Sound", prevPts + pts).apply();
        logEvent("POINT:" + now + ":Sound:" + pts);
    }

    @Override
    protected void playSound(String sound) {
        releaseMediaPlayer();
        mediaPlayer = new MediaPlayer();
        try {
            String assetPath = sound;
            AssetFileDescriptor descriptor = assetManager.openFd(assetPath);
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build());
            mediaPlayer.setDataSource(
                    descriptor.getFileDescriptor(),
                    descriptor.getStartOffset(),
                    descriptor.getLength());
            descriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
        } catch (IOException e) {
            Log.e(TAG, "Error playing sound: " + sound, e);
            releaseMediaPlayer();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void stopSound() {
        releaseMediaPlayer();
        stopFrequencySound();
    }

    @Override
    protected void playFrequencySound(int frequency) {
        int numSamples = MainMenu.mode == MainMenu.ProgramMode.GUESS_FREQUENCY
                ? (int)(DURATION_MS * (sampleRate / 1000.0))
                : (int)(DURATION_GENERATOR_MS * (sampleRate / 1000.0));

        double[] sample = new double[numSamples];
        for (int i = 0; i < numSamples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / (double) frequency));
        }

        byte[] generatedSnd = new byte[2 * numSamples];
        int idx = 0;
        for (double dVal : sample) {
            short val = (short)(dVal * 32767);
            generatedSnd[idx++] = (byte)(val & 0x00ff);
            generatedSnd[idx++] = (byte)((val & 0xff00) >>> 8);
        }

        stopFrequencySound();

        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(generatedSnd.length)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();

        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }

    @Override
    protected void stopFrequencySound() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    public static int generateCorrectFrequency() {
        return new Random().nextInt(UPPER_FREQ_LIMIT - LOWER_FREQ_LIMIT + 1) + LOWER_FREQ_LIMIT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        stopFrequencySound();
    }

    private void logEvent(String event) {
        try (FileOutputStream fos = getContext().openFileOutput(
                "stats_log.txt", getContext().MODE_APPEND)) {
            fos.write((event + "\n").getBytes());
        } catch (Exception ignored) {}
    }

    private void loadSounds() {
        try {
            String[] list = assetManager.list("sounds");
            if (list != null) {
                soundNames = new String[list.length];
                for (int i = 0; i < list.length; i++) {
                    soundNames[i] = list[i].replace(".ogg", "");
                }
            } else {
                soundNames = new String[0];
            }
        } catch (IOException e) {
            soundNames = new String[0];
        }
    }

    private void setupBasedOnMode() {
        if (MainMenu.mode == MainMenu.ProgramMode.GUESS_FREQUENCY) {
            setAFrequencySlider();
            correctFrequency = generateCorrectFrequency();
            setButtonsClickedBehaviour(0);
            new UserMessages(currentInformations, getContext())
                    .pressAButtonToPlayASoundAndSelectAFrequency();
            userFrequency = LOWER_FREQ_LIMIT;
        } else if (MainMenu.mode == MainMenu.ProgramMode.FREQUENCY_GENERATOR) {
            setAFrequencySlider();
            setButtonsClickedBehaviour(0);
            new UserMessages(currentInformations, getContext())
                    .pressAButtonToPlayASoundAndSetAFrequencyUsingSlider();
            userFrequency = LOWER_FREQ_LIMIT;
            removeUnnecesaryButtonsFromFrequencyModules();
            ((TextView) mainView.findViewById(R.id.moduleTitleFreq))
                    .setText(getString(R.string.generator_frequency_title));
        } else if (MainMenu.mode == MainMenu.ProgramMode.INPUT) {
            addTextViewAndEditTextToGridLayout();
            new UserMessages(currentInformations, getContext())
                    .pressAButtonAndInputText();
            setButtonsClickedBehaviour(0);
            setAnswerChecker();
            generateACorrectAnswer(soundNames.length);
            Log.i(TAG, "Correct answer: " + soundNames[correctAnswer]);
        } else {
            insertAParticularNumberOfButtonsIntoGridLayout(m, n);
            setButtonsClickedBehaviour(m * n);
            setDefaultColorsOfButtons();
        }
    }
}
