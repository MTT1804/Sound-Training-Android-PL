package com.example.soundtraining;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.gridlayout.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Speech extends Exercise {
    private static final String TAG = "Speech";
    private static final String IMAGES_SPEECH_DIR = "images_speech";
    private static final String IMAGES_STORIES_DIR = "images_stories";
    private static final String TEXT_DIR = "text/";
    private static final String WORDS_FILE = "words";
    private static final String SIMILAR_WORDS1_FILE = "similar_words1";
    private static final String SIMILAR_WORDS2_FILE = "similar_words2";
    private static final String IMAGE_EXT = ".jpg";

    private TextToSpeech textToSpeech;
    private AssetManager assetManager;
    private long moduleStart;
    private SharedPreferences statsPrefs;

    public Speech(int m, int n, int extra) {
        super(R.layout.sound);
        this.m = m;
        this.n = n;
        this.extra = extra;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.sound, container, false);
        this.assetManager = mainView.getContext().getAssets();
        this.statsPrefs = mainView.getContext().getSharedPreferences("stats", 0);

        currentInformations = mainView.findViewById(R.id.currentInformations);
        messageSystem = new UserMessages(currentInformations, mainView.getContext());

        initializeTextToSpeech();
        loadResourcesBasedOnMode();

        if (MainMenu.mode != MainMenu.ProgramMode.SPEECH_SYNTHESIZER) {
            setListForRandomGenerator();
        }

        setupUiBasedOnMode();
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

        long prevTime = statsPrefs.getLong("time_Speech", 0);
        statsPrefs.edit().putLong("time_Speech", prevTime + durationSec).apply();

        Calendar calEvent = Calendar.getInstance();
        calEvent.setTimeInMillis(now);
        Calendar calToday = Calendar.getInstance();
        int dayIndex = 0;
        while (dayIndex < 7 &&
                (calToday.get(Calendar.YEAR) != calEvent.get(Calendar.YEAR) ||
                        calToday.get(Calendar.DAY_OF_YEAR) != calEvent.get(Calendar.DAY_OF_YEAR))) {
            calToday.add(Calendar.DAY_OF_YEAR, -1);
            dayIndex++;
        }
        float prevSession = statsPrefs.getFloat("session_" + dayIndex, 0f);
        statsPrefs.edit()
                .putFloat("session_" + dayIndex, prevSession + durationSec / 60f)
                .apply();

        logEvent("SESSION:" + now + ":" + durationSec);
    }

    @Override
    protected void playSound(String sound) {
        if (textToSpeech != null) {
            textToSpeech.speak(sound, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void stopSound() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    @Override
    protected void playFrequencySound(int frequency) { }

    @Override
    protected void stopFrequencySound() { }

    protected void awardPoints(int pts) {
        long now = System.currentTimeMillis();
        int prevPts = statsPrefs.getInt("points_Speech", 0);
        statsPrefs.edit().putInt("points_Speech", prevPts + pts).apply();
        logEvent("POINT:" + now + ":Speech:" + pts);
    }

    private void loadResourcesBasedOnMode() {
        if (MainMenu.mode == MainMenu.ProgramMode.TEXT ||
                MainMenu.mode == MainMenu.ProgramMode.INPUT) {
            loadTextResources();
        } else if (MainMenu.mode == MainMenu.ProgramMode.IMAGES) {
            loadImageResources();
        }
    }

    private void setupUiBasedOnMode() {
        if (MainMenu.mode == MainMenu.ProgramMode.TEXT ||
                MainMenu.mode == MainMenu.ProgramMode.IMAGES) {
            insertAParticularNumberOfButtonsIntoGridLayout(m, n);
            setButtonsClickedBehaviour(m * n);
            setDefaultColorsOfButtons();
        } else if (MainMenu.mode == MainMenu.ProgramMode.INPUT) {
            setupInputMode();
        } else if (MainMenu.mode == MainMenu.ProgramMode.SPEECH_SYNTHESIZER) {
            setupSpeechSynthesizerMode();
        }
    }

    private void setupInputMode() {
        addTextViewAndEditTextToGridLayout();
        messageSystem.pressAButtonAndInputText();
        setButtonsClickedBehaviour(0);
        setAnswerChecker();
        generateACorrectAnswer(soundNames.length);
        Log.i(TAG, "Correct answer: " + soundNames[correctAnswer]);
    }

    private void setupSpeechSynthesizerMode() {
        messageSystem.pressAButtonAndProgramWillReadYourInput();
        addATextFieldForSpeechSynthesizer();
        setButtonsClickedBehaviour(0);
    }

    private void addATextFieldForSpeechSynthesizer() {
        GridLayout buttonsMatrix = mainView.findViewById(R.id.buttonsMatrix);
        buttonsMatrix.post(() -> {
            ScrollView scrollView = new ScrollView(mainView.getContext());
            LinearLayout linearLayout = new LinearLayout(mainView.getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(16, 16, 16, 16);
            linearLayout.setLayoutParams(layoutParams);
            EditText editText = new EditText(mainView.getContext());
            editText.setId(R.id.inputTextFieldSynthesizer);
            editText.setTextSize(24);
            editText.setHint(mainView.getContext().getString(R.string.exercise_hint));
            editText.setGravity(Gravity.CENTER);
            editText.setBackground(ContextCompat.getDrawable(
                    mainView.getContext(),
                    R.drawable.edittext_background));
            linearLayout.addView(editText);
            scrollView.addView(linearLayout);
            buttonsMatrix.addView(scrollView);
        });
    }

    private void loadTextResources() {
        ArrayList<String> wordsDatabase = new ArrayList<>();
        String fileName;
        if (!MainMenu.similarWordsModeOn) {
            fileName = WORDS_FILE;
        } else if (MainMenu.whichDatabaseToUseInSimilarWordsModule == 1) {
            fileName = SIMILAR_WORDS1_FILE;
        } else {
            fileName = SIMILAR_WORDS2_FILE;
        }
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(assetManager.open(TEXT_DIR + fileName)))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                wordsDatabase.add(line);
            }
            soundNames = wordsDatabase.toArray(new String[0]);
        } catch (IOException e) {
            Log.e(TAG, "Error loading text resources", e);
            soundNames = new String[0];
        }
    }

    private void loadImageResources() {
        String directory = extra == 2 ? IMAGES_STORIES_DIR : IMAGES_SPEECH_DIR;
        try {
            soundNames = assetManager.list(directory);
            for (int i = 0; i < soundNames.length; i++) {
                soundNames[i] = soundNames[i].replace(IMAGE_EXT, "");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading image resources", e);
            soundNames = new String[0];
        }
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(mainView.getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
            } else {
                Log.e(TAG, "TextToSpeech initialization failed with status: " + status);
            }
        });
    }

    private void logEvent(String event) {
        try (FileOutputStream fos = mainView.getContext()
                .openFileOutput("stats_log.txt", mainView.getContext().MODE_APPEND)) {
            fos.write((event + "\n").getBytes());
        } catch (Exception ignored) {}
    }
}
