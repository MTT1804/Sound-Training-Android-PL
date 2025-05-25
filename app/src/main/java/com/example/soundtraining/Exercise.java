package com.example.soundtraining;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
public abstract class Exercise extends Fragment {
    private static final int BUTTON_MARGIN = 2;
    private static final String IMAGE_SOUNDS_PATH = "images_sounds/";
    private static final String IMAGE_SPEECH_PATH = "images_speech/";
    private static final String IMAGE_STORIES_PATH = "images_stories/";
    private static final String IMAGE_FILE_EXT = ".jpg";
    private static final String SOUND_FILE_PATH = "sounds/";
    private static final String SOUND_FILE_EXT = ".ogg";

    protected View mainView;
    protected String[] soundNames;
    protected Integer[] choices;
    protected int m, n;
    protected GridLayout buttonsMatrix;
    protected TextView currentInformations;
    protected UserMessages messageSystem;
    protected int correctAnswer;
    protected int userAnswer;
    protected int buttonCounter;
    protected MediaPlayer mediaPlayer;
    protected AssetManager assetManager;
    protected boolean deactivateAllButtons = false;
    protected EditText editText;
    protected boolean lockPoints = false;
    protected List<Integer> numbers = new ArrayList<>();
    protected SeekBar frequencyBar;
    protected TextView frequencyText;
    protected int correctFrequency;
    protected int userFrequency;
    protected int extra;
    private Vibrator vibrator;

    public Exercise(int layout) {
        super(layout);
    }

    protected abstract void playSound(String sound);
    protected abstract void stopSound();
    protected abstract void playFrequencySound(int frequency);
    protected abstract void stopFrequencySound();

    protected void addTextViewAndEditTextToGridLayout() {
        buttonsMatrix = mainView.findViewById(R.id.buttonsMatrix);
        buttonsMatrix.removeAllViews();
        buttonsMatrix.setRowCount(1);
        buttonsMatrix.setColumnCount(2);
        buttonsMatrix.post(() -> {
            TextView textView = createInstructionTextView();
            editText = createUserInputEditText();
            GridLayout.LayoutParams textParams = new GridLayout.LayoutParams(
                    GridLayout.spec(0, 1),
                    GridLayout.spec(0, 1, 1f)
            );
            textParams.width = 0;
            textParams.setMargins(8, 8, 8, 8);
            GridLayout.LayoutParams editParams = new GridLayout.LayoutParams(
                    GridLayout.spec(0, 1),
                    GridLayout.spec(1, 1, 1f)
            );
            editParams.width = 0;
            editParams.setMargins(8, 8, 8, 8);
            buttonsMatrix.addView(textView, textParams);
            buttonsMatrix.addView(editText, editParams);
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        vibrator = null;
    }
    private void vibrate(boolean correct) {
        if (vibrator != null && vibrator.hasVibrator()) {
            long duration = correct ? 50 : 200;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                        VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                );
            } else {
                vibrator.vibrate(duration);
            }
        }
    }
    private TextView createInstructionTextView() {
        TextView textView = new TextView(mainView.getContext());
        textView.setText(mainView.getContext().getString(R.string.exercise_instruction));
        textView.setTextSize(24);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private EditText createUserInputEditText() {
        EditText editText = new EditText(mainView.getContext());
        editText.setTextSize(24);
        editText.setHint(mainView.getContext().getString(R.string.exercise_hint));
        editText.setGravity(Gravity.CENTER);
        editText.setPadding(0, 20, 0, 0);
        return editText;
    }

    private GridLayout.LayoutParams createCenteredGridLayoutParams(int rowIndex) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = GridLayout.LayoutParams.WRAP_CONTENT;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        params.rowSpec = GridLayout.spec(rowIndex, 1f);
        params.columnSpec = GridLayout.spec(0, 1f);
        return params;
    }

    protected void setAnswerChecker() {
        buttonsMatrix.post(() -> {
            editText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (checkUserAnswerString(s.toString())) {
                        if (!lockPoints) {
                            messageSystem.correctAnswer();
                            awardPoints(10);
                            lockPoints = true;
                        } else {
                            messageSystem.pointsHaveBeenAlreadyAssigned();
                        }
                    } else {
                        messageSystem.stillWrong();
                    }
                }
            });
        });
    }

    protected void setDefaultColorsOfButtons() {
        buttonsMatrix.post(() -> {
            if (MainMenu.mode == MainMenu.ProgramMode.TEXT) {
                for (int i = 0; i < buttonsMatrix.getChildCount(); i++) {
                    Button buttonChosen = (Button) buttonsMatrix.getChildAt(i);
                    buttonChosen.setBackgroundResource(android.R.drawable.btn_default);
                }
            } else if (MainMenu.mode == MainMenu.ProgramMode.IMAGES) {
                for (int i = 0; i < buttonsMatrix.getChildCount(); i++) {
                    ImageButton buttonChosen = (ImageButton) buttonsMatrix.getChildAt(i);
                    buttonChosen.setBackgroundColor(ContextCompat.getColor(mainView.getContext(), android.R.color.transparent));
                }
            }
        });
    }

    protected void generateAndInsertRandomlyChosenSounds(int fieldAmount) {
        choices = MainMenu.similarWordsModeOn ?
                generateRandomChoicesSimilarWords(fieldAmount) :
                generateRandomChoices(fieldAmount);

        generateACorrectAnswer(fieldAmount);

        for (int buttonCounter = 0; buttonCounter < fieldAmount; buttonCounter++) {
            int buttonId = getResources().getIdentifier("button_" + buttonCounter, "id", mainView.getContext().getPackageName());
            if (MainMenu.mode == MainMenu.ProgramMode.TEXT) {
                ((Button) mainView.findViewById(buttonId)).setText(soundNames[choices[buttonCounter]]);
            } else if (MainMenu.mode == MainMenu.ProgramMode.IMAGES) {
                setupImageButton(buttonId, buttonCounter);
            }
        }
    }

    private void setupImageButton(int buttonId, int buttonCounter) {
        ImageButton imageButton = ((ImageButton) mainView.findViewById(buttonId));
        try (InputStream inputStream = getImageInputStream(buttonCounter)) {
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                displayScaledBitmap(imageButton, bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream getImageInputStream(int buttonCounter) throws IOException {
        String imagePath;
        String imageName = soundNames[choices[buttonCounter]];

        if (MainMenu.emode == MainMenu.ExerciseMode.SOUND) {
            imagePath = IMAGE_SOUNDS_PATH + imageName + IMAGE_FILE_EXT;
        } else if (MainMenu.emode == MainMenu.ExerciseMode.SPEECH) {
            imagePath = (extra != 2) ?
                    IMAGE_SPEECH_PATH + imageName + IMAGE_FILE_EXT :
                    IMAGE_STORIES_PATH + imageName + IMAGE_FILE_EXT;
        } else {
            return null;
        }

        return mainView.getContext().getAssets().open(imagePath);
    }

    private void displayScaledBitmap(ImageButton imageButton, Bitmap bitmap) {
        int targetWidth = imageButton.getWidth();
        int targetHeight = imageButton.getHeight();

        float scale = Math.min(
                (float) targetWidth / bitmap.getWidth(),
                (float) targetHeight / bitmap.getHeight()
        );

        int scaledWidth = Math.round(scale * bitmap.getWidth());
        int scaledHeight = Math.round(scale * bitmap.getHeight());

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
        imageButton.setImageBitmap(scaledBitmap);
        imageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    protected Integer[] generateRandomChoices(int fieldAmount) {
        Collections.shuffle(numbers);
        return numbers.subList(0, fieldAmount).toArray(new Integer[fieldAmount]);
    }

    protected Integer[] generateRandomChoicesSimilarWords(int fieldAmount) {
        int howManyGroups = soundNames.length / fieldAmount;
        int whichGroupOfWordsWeUse = new Random().nextInt(howManyGroups);
        int startIndex = whichGroupOfWordsWeUse * fieldAmount;
        return numbers.subList(startIndex, startIndex + fieldAmount).toArray(new Integer[fieldAmount]);
    }

    protected void setListForRandomGenerator() {
        numbers.clear();
        for (int i = 0; i < soundNames.length; i++) {
            numbers.add(i);
        }
    }

    protected void insertAParticularNumberOfButtonsIntoGridLayout(int m, int n) {
        this.m = m;
        this.n = n;
        buttonsMatrix = mainView.findViewById(R.id.buttonsMatrix);
        buttonsMatrix.setColumnCount(n);
        buttonsMatrix.setRowCount(m);

        buttonCounter = 0;

        if (MainMenu.mode == MainMenu.ProgramMode.TEXT) {
            createTextButtons(m, n);
        } else if (MainMenu.mode == MainMenu.ProgramMode.IMAGES) {
            createImageButtons(m, n);
        }
    }

    private void createTextButtons(int m, int n) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                Button button = new Button(mainView.getContext());
                int buttonId = getResources().getIdentifier("button_" + buttonCounter, "id", mainView.getContext().getPackageName());
                button.setId(buttonId);
                buttonsMatrix.addView(button);
                buttonCounter++;
            }
        }

        buttonsMatrix.post(() -> {
            setupButtonSizes(m, n);
            generateAndInsertRandomlyChosenSounds(m * n);
        });
    }

    private void createImageButtons(int m, int n) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                ImageButton button = new ImageButton(mainView.getContext());
                int buttonId = getResources().getIdentifier("button_" + buttonCounter, "id", mainView.getContext().getPackageName());
                button.setId(buttonId);
                buttonsMatrix.addView(button);
                buttonCounter++;
            }
        }

        buttonsMatrix.post(() -> {
            setupButtonSizes(m, n);
            generateAndInsertRandomlyChosenSounds(m * n);
            initialization();
        });
    }

    private void setupButtonSizes(int m, int n) {
        int buttonWidth = buttonsMatrix.getWidth() / n;
        int buttonHeight = buttonsMatrix.getHeight() / m;

        for (int i = 0; i < buttonsMatrix.getChildCount(); i++) {
            View button = buttonsMatrix.getChildAt(i);
            GridLayout.LayoutParams params = createButtonLayoutParams(buttonWidth, buttonHeight);
            button.setLayoutParams(params);
        }
    }

    @NonNull
    private GridLayout.LayoutParams createButtonLayoutParams(int width, int height) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = width;
        params.height = height;
        params.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);
        params.setGravity(Gravity.FILL);
        return params;
    }

    protected void initialization() {
        ((ImageButton) mainView.findViewById(R.id.next)).performClick();
    }

    protected void setButtonsClickedBehaviour(int fieldAmount) {
        setupControlButtons();

        if (MainMenu.mode == MainMenu.ProgramMode.TEXT) {
            setupTextButtonsListeners();
        } else if (MainMenu.mode == MainMenu.ProgramMode.IMAGES) {
            setupImageButtonsListeners();
        } else if (MainMenu.emode == MainMenu.ExerciseMode.FREQUENCY) {
            setupFrequencyButtonListeners();
        }
    }

    private void setupControlButtons() {
        setupPlayButton();
        setupStopButton();
        setupNextButton();
    }

    private void setupPlayButton() {
        ((ImageButton) mainView.findViewById(R.id.play)).setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(mainView.getContext(), R.anim.button_pressed_animation);
            ((ImageButton) mainView.findViewById(R.id.play)).startAnimation(animation);

            if (MainMenu.emode == MainMenu.ExerciseMode.SOUND) {
                playSound(SOUND_FILE_PATH +
                        (MainMenu.mode != MainMenu.ProgramMode.INPUT ?
                                soundNames[choices[correctAnswer]] :
                                soundNames[correctAnswer]) +
                        SOUND_FILE_EXT);
            } else if (MainMenu.emode == MainMenu.ExerciseMode.SPEECH) {
                if (MainMenu.mode == MainMenu.ProgramMode.SPEECH_SYNTHESIZER) {
                    EditText et = mainView.findViewById(R.id.inputTextFieldSynthesizer);
                    playSound(et.getText().toString());
                } else {
                    playSound(MainMenu.mode != MainMenu.ProgramMode.INPUT ?
                            soundNames[choices[correctAnswer]] :
                            soundNames[correctAnswer]);
                }
            } else if (MainMenu.mode == MainMenu.ProgramMode.GUESS_FREQUENCY) {
                playFrequencySound(correctFrequency);
            } else if (MainMenu.mode == MainMenu.ProgramMode.FREQUENCY_GENERATOR) {
                stopFrequencySound();
                playFrequencySound(userFrequency);
            }
        });
    }

    private void setupStopButton() {
        ((ImageButton) mainView.findViewById(R.id.stop)).setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(mainView.getContext(), R.anim.button_pressed_animation);
            ((ImageButton) mainView.findViewById(R.id.stop)).startAnimation(animation);

            if (MainMenu.emode != MainMenu.ExerciseMode.FREQUENCY) {
                stopSound();
            } else {
                stopFrequencySound();
            }
        });
    }

    private void setupNextButton() {
        ((ImageButton) mainView.findViewById(R.id.next)).setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(mainView.getContext(), R.anim.button_pressed_animation);
            ((ImageButton) mainView.findViewById(R.id.next)).startAnimation(animation);
            stopSound();

            if (MainMenu.emode != MainMenu.ExerciseMode.FREQUENCY) {
                handleNextForSoundOrSpeech();
            } else {
                handleNextForFrequency();
            }
        });
    }

    private void handleNextForSoundOrSpeech() {
        buttonsMatrix.post(() -> {
            if (MainMenu.mode == MainMenu.ProgramMode.SPEECH_SYNTHESIZER) {
                stopSound();
                EditText et = mainView.findViewById(R.id.inputTextFieldSynthesizer);
                et.setText("");
            } else {
                if (MainMenu.mode != MainMenu.ProgramMode.INPUT) {
                    setDefaultColorsOfButtons();
                    generateAndInsertRandomlyChosenSounds(m * n);
                    deactivateAllButtons = false;
                    messageSystem.pressAButtonToPlayASound();
                } else {
                    generateACorrectAnswer(soundNames.length);
                    editText.setText("");
                    messageSystem.pressAButtonAndInputText();
                    lockPoints = false;
                }
            }
        });
    }

    private void handleNextForFrequency() {
        messageSystem.pressAButtonToPlayASoundAndSelectAFrequency();
        correctFrequency = Sound.generateCorrectFrequency();
        deactivateAllButtons = false;
        stopFrequencySound();
    }

    private void setupTextButtonsListeners() {
        buttonsMatrix.post(() -> {
            for (int i = 0; i < buttonsMatrix.getChildCount(); i++) {
                int idx = i;
                Button b = (Button) buttonsMatrix.getChildAt(i);
                b.setOnClickListener(v -> {
                    if (!deactivateAllButtons) {
                        deactivateAllButtons = true;
                        userAnswer = idx;
                        boolean correct = checkUserAnswer();
                        vibrate(correct);
                        if (correct) {
                            b.setBackgroundResource(R.color.green);
                            messageSystem.correctAnswer();
                            awardPoints(10);
                        } else {
                            b.setBackgroundResource(R.color.red);
                            Button correctB = (Button) buttonsMatrix.getChildAt(correctAnswer);
                            correctB.setBackgroundResource(R.color.green);
                            messageSystem.incorrectAnswer(soundNames[choices[correctAnswer]]);
                            awardPoints(-10);
                        }
                    }
                });
            }
        });
    }

    private void setupImageButtonsListeners() {
        buttonsMatrix.post(() -> {
            for (int i = 0; i < buttonsMatrix.getChildCount(); i++) {
                int idx = i;
                ImageButton b = (ImageButton) buttonsMatrix.getChildAt(i);
                b.setOnClickListener(v -> {
                    if (!deactivateAllButtons) {
                        deactivateAllButtons = true;
                        userAnswer = idx;
                        boolean correct = checkUserAnswer();
                        vibrate(correct);
                        if (correct) {
                            b.setBackgroundColor(ContextCompat.getColor(mainView.getContext(), R.color.green));
                            messageSystem.correctAnswer();
                            awardPoints(10);
                        } else {
                            b.setBackgroundColor(ContextCompat.getColor(mainView.getContext(), R.color.red));
                            ImageButton correctB = (ImageButton) buttonsMatrix.getChildAt(correctAnswer);
                            correctB.setBackgroundColor(ContextCompat.getColor(mainView.getContext(), R.color.green));
                            messageSystem.incorrectAnswer(soundNames[choices[correctAnswer]]);
                            awardPoints(-10);
                        }
                    }
                });
            }
        });
    }

    private void handleAnswerResult(Button buttonChosen) {
        boolean correct = checkUserAnswer();
        vibrate(correct);

        if (correct) {
            buttonChosen.setBackgroundResource(R.color.green);
            messageSystem.correctAnswer();
        } else {
            buttonChosen.setBackgroundResource(R.color.red);
            Button buttonCorrect = (Button) buttonsMatrix.getChildAt(correctAnswer);
            buttonCorrect.setBackgroundResource(R.color.green);
            messageSystem.incorrectAnswer(soundNames[choices[correctAnswer]]);
        }
    }

    private void handleAnswerResultForImage(ImageButton buttonChosen) {
        boolean correct = checkUserAnswer();
        vibrate(correct);

        if (correct) {
            buttonChosen.setBackgroundColor(
                    ContextCompat.getColor(mainView.getContext(), R.color.green));
            messageSystem.correctAnswer();
        } else {
            buttonChosen.setBackgroundColor(
                    ContextCompat.getColor(mainView.getContext(), R.color.red));
            ImageButton buttonCorrect = (ImageButton) buttonsMatrix.getChildAt(correctAnswer);
            buttonCorrect.setBackgroundColor(
                    ContextCompat.getColor(mainView.getContext(), R.color.green));
            messageSystem.incorrectAnswer(soundNames[choices[correctAnswer]]);
        }
    }

    private void setupFrequencyButtonListeners() {
        ((Button) mainView.findViewById(R.id.checkSetFrequency)).setOnClickListener(v -> {
            if (!deactivateAllButtons) {
                checkFrequencyAnswer();
            }
        });
    }

    private void checkFrequencyAnswer() {
        int difference = userFrequency - correctFrequency;
        int absThreshold = Sound.FREQUENCY_LIMIT_TO_EARN_10_POINTS;
        for (int i = 0; i < 10; i++) {
            if (Math.abs(difference) <= Math.round(absThreshold / (10 - i))) {
                messageSystem.youAreCloseToTheCorrectAnswer(10 - i, difference, correctFrequency);
                awardPoints(10 - i);
                deactivateAllButtons = true;
                return;
            }
        }
        messageSystem.youAreFarAwayFromCorrectAnswer(difference, correctFrequency);
        deactivateAllButtons = true;
    }

    protected boolean checkUserAnswer() {
        return userAnswer == correctAnswer;
    }

    protected boolean checkUserAnswerString(String s) {
        return s.equals(soundNames[correctAnswer]);
    }

    protected void generateACorrectAnswer(int fieldAmount) {
        Random r = new Random();
        correctAnswer = r.nextInt(fieldAmount);
    }

    protected void setAFrequencySlider() {
        frequencyBar = mainView.findViewById(R.id.frequencyBar);
        frequencyText = mainView.findViewById(R.id.frequencyText);

        frequencyBar.setMax(7900);
        frequencyBar.setProgress(0);
        frequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress += Sound.getLowerFreqLimit();
                userFrequency = progress;
                frequencyText.setText(mainView.getContext().getString(R.string.selected_frequency, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    protected void removeUnnecesaryButtonsFromFrequencyModules() {
        ((Button)mainView.findViewById(R.id.checkSetFrequency)).setVisibility(View.GONE);
        ((ImageButton)mainView.findViewById(R.id.next)).setVisibility(View.GONE);
    }
    protected void awardPoints(int pts) {
        if (!lockPoints) {
            SharedPreferences prefs = mainView.getContext()
                    .getSharedPreferences("stats", Context.MODE_PRIVATE);
            int prev = prefs.getInt("points_" + this.getClass().getSimpleName(), 0);
            prefs.edit()
                    .putInt("points_" + this.getClass().getSimpleName(), prev + pts)
                    .apply();
            lockPoints = true;
        }
    }
}