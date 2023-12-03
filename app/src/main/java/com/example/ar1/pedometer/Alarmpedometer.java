package com.example.ar1.pedometer;


import android.content.Context;
import android.content.SharedPreferences;

// 만보기 제작

public class Alarmpedometer {
    private static final String AlarmPREF_NAME = "AlarmStepCountPreferences";
    private static final String AlarmKEY_STEP_COUNT = "AlarmstepCount";
    private static final String AlarmKEY_STEP_TIME = "AlarmstepTime";
    private static final String AlarmKEY_ACTIVE_TIME = "AlarmactiveTime"; // 추가된 상수

    public static void saveStepCount(Context context, int stepCount) {
        SharedPreferences Alarmpreferences = context.getSharedPreferences(AlarmPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = Alarmpreferences.edit();
        editor.putInt(AlarmKEY_STEP_COUNT, stepCount);
        editor.apply();
    }
    public static void saveStepTime(Context context, int time) {
        SharedPreferences Alarmpreferences = context.getSharedPreferences(AlarmPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = Alarmpreferences.edit();
        editor.putInt(AlarmKEY_STEP_TIME, time);
        editor.apply();
    }
    public static void saveActiveTime(Context context, long activeTime) {
        SharedPreferences Alarmpreferences = context.getSharedPreferences(AlarmPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = Alarmpreferences.edit();
        editor.putLong(AlarmKEY_ACTIVE_TIME, activeTime);
        editor.apply();
    }

    public static long getActiveTime(Context context) {
        SharedPreferences Alarmpreferences = context.getSharedPreferences(AlarmPREF_NAME, Context.MODE_PRIVATE);
        return Alarmpreferences.getLong(AlarmKEY_ACTIVE_TIME, 0);
    }
    public static int getStepCount(Context context) {
        SharedPreferences Alarmpreferences = context.getSharedPreferences(AlarmPREF_NAME, Context.MODE_PRIVATE);
        return Alarmpreferences.getInt(AlarmKEY_STEP_COUNT, 0);
    }
    public static int getStepTime(Context context) {
        SharedPreferences Alarmpreferences = context.getSharedPreferences(AlarmPREF_NAME, Context.MODE_PRIVATE);
        return Alarmpreferences.getInt(AlarmKEY_STEP_TIME, 0);
    }
}
