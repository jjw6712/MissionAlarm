package com.example.ar1.pedometer;


import android.content.Context;
import android.content.SharedPreferences;

// 만보기 제작

public class pedometer {
    private static final String PREF_NAME = "StepCountPreferences";
    private static final String KEY_STEP_COUNT = "stepCount";
    private static final String KEY_STEP_TIME = "stepTime";
    private static final String KEY_ACTIVE_TIME = "activeTime"; // 추가된 상수

    public static void saveStepCount(Context context, int stepCount) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_STEP_COUNT, stepCount);
        editor.apply();
    }
    public static void saveStepTime(Context context, int time) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_STEP_TIME, time);
        editor.apply();
    }
    public static void saveActiveTime(Context context, long activeTime) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_ACTIVE_TIME, activeTime);
        editor.apply();
    }

    public static long getActiveTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(KEY_ACTIVE_TIME, 0);
    }
    public static int getStepCount(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_STEP_COUNT, 0);
    }
    public static int getStepTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_STEP_TIME, 0);
    }
}
