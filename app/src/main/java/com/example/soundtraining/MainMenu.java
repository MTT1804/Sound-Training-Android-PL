package com.example.soundtraining;

import android.app.AlertDialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.InputStream;

public class MainMenu extends Fragment {
    private static final String TAG = "MainMenu";
    private static final String BACKGROUND_PATH = "background/";
    private static final String IMAGE_EXT = ".jpg";

    private View mainView;
    private NavigationView navigationView;

    public enum ProgramMode {
        TEXT, IMAGES, INPUT, SPEECH_SYNTHESIZER, GUESS_FREQUENCY, FREQUENCY_GENERATOR, SIMILAR_WORDS
    }

    public enum ExerciseMode {
        SOUND, SPEECH, FREQUENCY
    }

    static ProgramMode mode;
    static ExerciseMode emode;
    static boolean similarWordsModeOn;
    static int whichDatabaseToUseInSimilarWordsModule;

    public MainMenu() {
        super(R.layout.main_menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.main_menu, container, false);
        loadAndSetBackgroundButtonsImages();
        setupNavigationDrawer();
        setRightPanelButton();
        similarWordsModeOn = false;
        setupButtonClickListeners();
        return mainView;
    }

    private void loadAndSetBackgroundButtonsImages() {
        AssetManager manager = requireContext().getAssets();
        setButtonBackground(manager, R.id.button1, "bg_sound", getString(R.string.button_sound));
        setButtonBackground(manager, R.id.button2, "bg_speech", getString(R.string.button_speech));
        setButtonBackground(manager, R.id.button3, "bg_frequency", getString(R.string.button_frequency));
        setButtonBackground(manager, R.id.button4, "bg_similar", getString(R.string.button_similar));
        setButtonBackground(manager, R.id.button5, "bg_other", getString(R.string.button_other));
        setButtonBackground(manager, R.id.button6, "bg_stories", getString(R.string.button_stories));
    }

    private void setButtonBackground(AssetManager manager, int buttonId, String imageName, String buttonText) {
        try (InputStream stream = manager.open(BACKGROUND_PATH + imageName + IMAGE_EXT)) {
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            Button button = mainView.findViewById(buttonId);
            button.setBackground(new BitmapDrawable(getResources(), bitmap));
            button.setBackgroundTintList(null);
            button.setText(buttonText);
        } catch (IOException e) {
            Log.e(TAG, "Error loading background image: " + imageName, e);
        }
    }


    private void setupButtonClickListeners() {
        mainView.findViewById(R.id.button1).setOnClickListener(v -> showModuleSelectionDialog(R.array.dzwiek, false, this::handleSoundModuleSelection));
        mainView.findViewById(R.id.button2).setOnClickListener(v -> showModuleSelectionDialog(R.array.mowa, false, this::handleSpeechModuleSelection));
        mainView.findViewById(R.id.button3).setOnClickListener(v -> showModuleSelectionDialog(R.array.czestotliwosc, false, this::handleFrequencyModuleSelection));
        mainView.findViewById(R.id.button4).setOnClickListener(v -> showModuleSelectionDialog(R.array.podobne_slowa, true, this::handleSimilarWordsModuleSelection));
        mainView.findViewById(R.id.button6).setOnClickListener(v -> showModuleSelectionDialog(R.array.opowiadania, false, this::handleStoriesModuleSelection));
    }

    private void showModuleSelectionDialog(int itemsArrayId, boolean enableSimilarWordsMode, DialogSelectionHandler handler) {
        similarWordsModeOn = enableSimilarWordsMode;
        new AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.select_exercise_title))
                .setItems(itemsArrayId, (dialog, which) -> handler.handleSelection(which))
                .create()
                .show();
    }

    private void handleSoundModuleSelection(int which) {
        Sound module;
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        switch (which) {
            case 0:
                module = new Sound(2, 1);
                mode = ProgramMode.TEXT;
                break;
            case 1:
                module = new Sound(2, 2);
                mode = ProgramMode.TEXT;
                break;
            case 2:
                module = new Sound(3, 3);
                mode = ProgramMode.TEXT;
                break;
            case 3:
                module = new Sound(2, 1);
                mode = ProgramMode.IMAGES;
                break;
            case 4:
                module = new Sound(2, 2);
                mode = ProgramMode.IMAGES;
                break;
            case 5:
                module = new Sound(3, 3);
                mode = ProgramMode.IMAGES;
                break;
            case 6:
                module = new Sound(-1, -1);
                mode = ProgramMode.INPUT;
                break;
            default:
                return;
        }

        emode = ExerciseMode.SOUND;
        replaceFragment(ft, module);
    }

    private void handleSpeechModuleSelection(int which) {
        Speech module;
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        switch (which) {
            case 0:
                module = new Speech(2, 1, 0);
                mode = ProgramMode.TEXT;
                break;
            case 1:
                module = new Speech(2, 2, 0);
                mode = ProgramMode.TEXT;
                break;
            case 2:
                module = new Speech(3, 3, 0);
                mode = ProgramMode.TEXT;
                break;
            case 3:
                module = new Speech(2, 1, 0);
                mode = ProgramMode.IMAGES;
                break;
            case 4:
                module = new Speech(2, 2, 0);
                mode = ProgramMode.IMAGES;
                break;
            case 5:
                module = new Speech(3, 3, 0);
                mode = ProgramMode.IMAGES;
                break;
            case 6:
                module = new Speech(3, 3, 0);
                mode = ProgramMode.INPUT;
                break;
            case 7:
                module = new Speech(-1, -1, 0);
                mode = ProgramMode.SPEECH_SYNTHESIZER;
                break;
            default:
                return;
        }

        emode = ExerciseMode.SPEECH;
        replaceFragment(ft, module);
    }

    private void handleFrequencyModuleSelection(int which) {
        Sound module;
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (which == 0) {
            module = new Sound();
            mode = ProgramMode.GUESS_FREQUENCY;
            emode = ExerciseMode.FREQUENCY;
            replaceFragment(ft, module);
        } else if (which == 1) {
            module = new Sound();
            mode = ProgramMode.FREQUENCY_GENERATOR;
            emode = ExerciseMode.FREQUENCY;
            replaceFragment(ft, module);
        }
    }

    private void handleSimilarWordsModuleSelection(int which) {
        Speech module;
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        switch (which) {
            case 0:
                module = new Speech(2, 1, 3);
                whichDatabaseToUseInSimilarWordsModule = 1;
                break;
            case 1:
                module = new Speech(2, 2, 3);
                whichDatabaseToUseInSimilarWordsModule = 1;
                break;
            case 2:
                module = new Speech(2, 1, 4);
                whichDatabaseToUseInSimilarWordsModule = 2;
                break;
            case 3:
                module = new Speech(2, 2, 4);
                whichDatabaseToUseInSimilarWordsModule = 2;
                break;
            default:
                return;
        }

        mode = ProgramMode.TEXT;
        emode = ExerciseMode.SPEECH;
        replaceFragment(ft, module);
    }

    private void handleStoriesModuleSelection(int which) {
        Speech module;
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        switch (which) {
            case 0:
                module = new Speech(2, 1, 2);
                break;
            case 1:
                module = new Speech(2, 2, 2);
                break;
            case 2:
                module = new Speech(3, 3, 2);
                break;
            default:
                return;
        }

        mode = ProgramMode.IMAGES;
        emode = ExerciseMode.SPEECH;
        replaceFragment(ft, module);
    }

    private void replaceFragment(FragmentTransaction ft, Fragment fragment) {
        ft.replace(R.id.fragmentContainerView, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void setupNavigationDrawer() {
        navigationView = mainView.findViewById(R.id.nav_view);
        navigationView.inflateMenu(R.menu.drawer_menu);
        navigationView.setNavigationItemSelectedListener(this::handleNavigationItemSelected);
    }

    private boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (itemId == R.id.nav_settings) {
            SettingsFragment settingsFragment = new SettingsFragment();
            //settingsFragment.setMainView(mainView);
            replaceFragment(fragmentTransaction, settingsFragment);
        } else if (itemId == R.id.nav_stats) {
            replaceFragment(fragmentTransaction, new Stats());
        } else if (itemId == R.id.nav_info) {
            replaceFragment(fragmentTransaction, new Info());
        }

        DrawerLayout drawerLayout = mainView.findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.END);

        return true;
    }

    private void setRightPanelButton() {
        Button openNavButton = mainView.findViewById(R.id.button5);
        openNavButton.setOnClickListener(v -> {
            DrawerLayout drawerLayout = mainView.findViewById(R.id.drawer_layout);
            drawerLayout.openDrawer(GravityCompat.END);
        });
    }

    private interface DialogSelectionHandler {
        void handleSelection(int which);
    }
}