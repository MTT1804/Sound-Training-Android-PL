package com.example.soundtraining;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.ListPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.SeekBarPreference;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        ListPreference sampleRate = findPreference("sample_rate");
        ListPreference bufferSize = findPreference("buffer_size");
        ListPreference ttsLang = findPreference("tts_language");
        SwitchPreferenceCompat monoPref = findPreference("single_channel");

        if (sampleRate != null) sampleRate.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        if (bufferSize != null) bufferSize.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        if (ttsLang != null) ttsLang.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        EditTextPreference upperFreq = findPreference("UPPER_FREQ");
        EditTextPreference lowerFreq = findPreference("LOWER_FREQ");
        if (upperFreq != null) upperFreq.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        if (lowerFreq != null) lowerFreq.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        if (upperFreq != null) {
            upperFreq.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
            upperFreq.setOnPreferenceChangeListener((pref, newValue) -> {
                int val = Integer.parseInt((String)newValue);
                Sound.setUpperFreqLimit(val);
                return true;
            });
        }
        if (lowerFreq != null) {
            lowerFreq.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
            lowerFreq.setOnPreferenceChangeListener((pref, newValue) -> {
                int val = Integer.parseInt((String)newValue);
                Sound.setLowerFreqLimit(val);
                return true;
            });
        }
        SwitchPreferenceCompat darkMode = findPreference("dark_mode");
        if (darkMode != null) {
            darkMode.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (Boolean) newValue;
                AppCompatDelegate.setDefaultNightMode(
                        enabled
                                ? AppCompatDelegate.MODE_NIGHT_YES
                                : AppCompatDelegate.MODE_NIGHT_NO
                );
                return true;
            });
        findPreference("tts_settings").setOnPreferenceClickListener(this);
        findPreference("export_stats").setOnPreferenceClickListener(this);
        if (sampleRate != null) {
            sampleRate.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            sampleRate.setOnPreferenceChangeListener((pref, newValue) -> {
                int sr = Integer.parseInt((String)newValue);
                Sound.setSampleRate(sr);
                return true;
            });
        }

        if (bufferSize != null) {
            bufferSize.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
            bufferSize.setOnPreferenceChangeListener((pref, newValue) -> {
                int bs = Integer.parseInt((String)newValue);
                Sound.setBufferSize(bs);
                return true;
            });
        }


        }

        SwitchPreferenceCompat singleChannel = findPreference("single_channel");
        if (singleChannel != null) {
            singleChannel.setSummaryProvider(pref ->
                    ((SwitchPreferenceCompat)pref).isChecked()
                            ? "Mono"
                            : "Stereo"
            );
            singleChannel.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean mono = (Boolean)newValue;
                Sound.setMonoMode(mono);
                return true;
            });
        }

        SeekBarPreference ttsSpeed = findPreference("tts_speed");
        SeekBarPreference ttsPitch = findPreference("tts_pitch");
        if (ttsSpeed != null) ttsSpeed.setOnPreferenceChangeListener((p, v) -> true);
        if (ttsPitch != null) ttsPitch.setOnPreferenceChangeListener((p, v) -> true);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if ("tts_settings".equals(key)) {
            startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
            return true;
        }
        if ("export_stats".equals(key)) {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                Toast.makeText(getContext(), getString(R.string.error_create_downloads_folder), Toast.LENGTH_LONG).show();
                return true;
            }
            File outFile = new File(downloadsDir, "soundtraining_stats.csv");
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                String header = "Date,Opens,TimeSec,Points_Sound,Points_Speech,Points_Freq\n";
                fos.write(header.getBytes());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String now = df.format(new Date());
                SharedPreferences stats = requireContext().getSharedPreferences("stats", Context.MODE_PRIVATE);
                int opens    = stats.getInt("open_count", 0);
                long timeMs  = stats.getLong("total_time", 0);
                int ptsSound = stats.getInt("points_Sound", 0);
                int ptsSpeech= stats.getInt("points_Speech", 0);
                int ptsFreq  = stats.getInt("points_Freq", 0);
                String row = String.format(Locale.US, "%s,%d,%.1f,%d,%d,%d\n",
                        now, opens, timeMs/1000.0, ptsSound, ptsSpeech, ptsFreq);
                fos.write(row.getBytes());
                Toast.makeText(getContext(),
                        getString(R.string.stats_saved, outFile.getAbsolutePath()),
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), getString(R.string.error_save_stats, e.getMessage()), Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return false;
    }

}
