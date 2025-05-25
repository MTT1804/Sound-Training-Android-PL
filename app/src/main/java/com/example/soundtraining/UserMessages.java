package com.example.soundtraining;

import android.content.Context;
import android.widget.TextView;

public class UserMessages {
    public static int points;
    TextView currentInformations;
    Context context;

    public void setPoints(int points) {
        this.points = points;
    }

    public UserMessages(TextView currentInformations, Context context) {
        this.currentInformations = currentInformations;
        this.context = context;
        this.points = FileUtils.readPointsFromFile(context, "points");
    }

    public void setText(String newText) {
        currentInformations.setText(newText);
    }

    public void correctAnswer() {
        points += 10;
        FileUtils.savePointsToFile(context, points, "points");
        currentInformations.setText(context.getString(R.string.msg_correct_answer, points));
    }

    public void incorrectAnswer(String correctAnswer) {
        points -= 10;
        FileUtils.savePointsToFile(context, points, "points");
        currentInformations.setText(context.getString(R.string.msg_incorrect_answer, correctAnswer, points));
    }

    public void pressAButtonToPlayASound() {
        currentInformations.setText(context.getString(R.string.msg_press_button_play_sound, points));
    }

    public void pressAButtonToPlayASoundAndSelectAFrequency() {
        currentInformations.setText(context.getString(R.string.msg_press_button_play_sound_and_select_freq, points));
    }

    public void soundPaused() {
        currentInformations.setText(context.getString(R.string.msg_sound_paused, points));
    }

    public void pressAButtonAndInputText() {
        currentInformations.setText(context.getString(R.string.msg_press_button_and_input_text, points));
    }

    public void stillWrong() {
        currentInformations.setText(context.getString(R.string.msg_still_wrong, points));
    }

    public void pointsHaveBeenAlreadyAssigned() {
        currentInformations.setText(context.getString(R.string.msg_points_already_assigned, points));
    }

    public void pressAButtonAndProgramWillReadYourInput(){
        currentInformations.setText(context.getString(R.string.msg_press_button_and_read_input));
    }

    public void youAreCloseToTheCorrectAnswer(int score, int difference, int correct){
        points += score;
        currentInformations.setText(context.getString(R.string.msg_close_to_correct, difference, correct, score, points));
    }

    public void youAreFarAwayFromCorrectAnswer(int difference, int correct){
        currentInformations.setText(context.getString(R.string.msg_far_from_correct, difference, correct, points));
    }

    public void pressAButtonToPlayASoundAndSetAFrequencyUsingSlider() {
        currentInformations.setText(context.getString(R.string.msg_press_button_and_set_freq_slider, points));
    }

    public void playingSound() {
        currentInformations.setText(context.getString(R.string.msg_playing_sound, points));
    }

    public void selectAnswerFromOptions() {
        currentInformations.setText(context.getString(R.string.msg_select_answer_from_options, points));
    }

    public void selectNewSound() {
        currentInformations.setText(context.getString(R.string.msg_select_new_sound, points));
    }

    public void repeatExercise() {
        currentInformations.setText(context.getString(R.string.msg_repeat_exercise, points));
    }

    public void startingNewExercise() {
        currentInformations.setText(context.getString(R.string.msg_starting_new_exercise, points));
    }

    public void textSuccessfullyRead() {
        currentInformations.setText(context.getString(R.string.msg_text_successfully_read, points));
    }
}