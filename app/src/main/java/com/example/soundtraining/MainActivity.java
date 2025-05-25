package com.example.soundtraining;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences statsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        statsPrefs = getSharedPreferences("stats", MODE_PRIVATE);

        boolean dark = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("dark_mode", false);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                dark
                        ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                        : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        );
        boolean mono = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("single_channel", false);
        Sound.setMonoMode(mono);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        Sound.setMonoMode(prefs.getBoolean("single_channel", false));
        Sound.setLowerFreqLimit(Integer.parseInt(prefs.getString("LOWER_FREQ", "100")));
        Sound.setUpperFreqLimit(Integer.parseInt(prefs.getString("UPPER_FREQ", "8000")));
        int sr = Integer.parseInt(prefs.getString("sample_rate", "44100"));
        Sound.setSampleRate(sr);
        int bs = Integer.parseInt(prefs.getString("buffer_size", "4096"));
        Sound.setBufferSize(bs);

        super.onCreate(savedInstanceState);
        setupEdgeToEdgeDisplay();

        if (savedInstanceState == null) {
            loadMainMenu();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        long now = System.currentTimeMillis();
        if (statsPrefs.getLong("first_open", 0) == 0) {
            statsPrefs.edit()
                    .putLong("first_open", now)
                    .apply();
        }
        int opens = statsPrefs.getInt("open_count", 0) + 1;
        statsPrefs.edit()
                .putInt("open_count", opens)
                .apply();

        logEvent("OPEN:" + now);
    }

    private void logEvent(String event) {
        try (FileOutputStream fos = openFileOutput("stats_log.txt", MODE_APPEND)) {
            fos.write((event + "\n").getBytes());
        } catch (Exception ignored) {}
    }

    private void setupEdgeToEdgeDisplay() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadMainMenu() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, new MainMenu());
        fragmentTransaction.commit();
    }
}
