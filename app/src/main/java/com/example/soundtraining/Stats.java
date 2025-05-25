package com.example.soundtraining;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Stats extends Fragment {

    private SharedPreferences prefs;
    private TextView tvFirstOpen, tvTotalOpens;
    private TextView tvPointsSound, tvPointsSpeech, tvPointsFreq, tvPointsTotal;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.stats, container, false);

        prefs = requireActivity().getSharedPreferences("stats", 0);

        tvFirstOpen    = v.findViewById(R.id.tvFirstOpen);
        tvTotalOpens   = v.findViewById(R.id.tvTotalOpens);
        tvPointsSound  = v.findViewById(R.id.tvPointsSound);
        tvPointsSpeech = v.findViewById(R.id.tvPointsSpeech);
        tvPointsFreq   = v.findViewById(R.id.tvPointsFreq);
        tvPointsTotal  = v.findViewById(R.id.tvPointsTotal);

        loadTextStats();
        return v;
    }

    private void loadTextStats() {
        long first = prefs.getLong("first_open", 0);
        int opens = prefs.getInt("open_count", 0);

        int ptsSound  = prefs.getInt("points_Sound", 0);
        int ptsSpeech = prefs.getInt("points_Speech", 0);
        int ptsFreq   = prefs.getInt("points_Freq", 0);
        int totalPts  = ptsSound + ptsSpeech + ptsFreq;

        String firstDate = first == 0
                ? getString(R.string.stats_no_date)
                : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(first));

        tvFirstOpen.setText(getString(R.string.stats_first_open, firstDate));
        tvTotalOpens.setText(getString(R.string.stats_total_opens, opens));
        tvPointsSound.setText(getString(R.string.stats_points_sound, ptsSound));
        tvPointsSpeech.setText(getString(R.string.stats_points_speech, ptsSpeech));
        tvPointsFreq.setText(getString(R.string.stats_points_freq, ptsFreq));
        tvPointsTotal.setText(getString(R.string.stats_points_total, totalPts));
    }
}
