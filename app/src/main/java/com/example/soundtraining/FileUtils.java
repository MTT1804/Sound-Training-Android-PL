package com.example.soundtraining;

import android.content.Context;
import android.util.Log;

import com.example.soundtraining.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtils {
    private static final String TAG = "FileUtils";
    public static int readPointsFromFile(Context context, String fileName) {
        int points = 0;
        try (FileInputStream fis = context.openFileInput(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String pointsString = reader.readLine();
            if (pointsString != null) {
                points = Integer.parseInt(pointsString.trim());
            }
        } catch (IOException | NumberFormatException e) {
            Log.e(TAG, context.getString(R.string.error_read_points, fileName), e);
            savePointsToFile(context, points, fileName);
        }
        return points;
    }

    public static void savePointsToFile(Context context, int points, String fileName) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
            writer.write(String.valueOf(points));
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, context.getString(R.string.error_save_points, fileName), e);
        }
    }
}