package com.example.ar1.pedometer;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class MidnightResetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "만보계 초기화: ");
        // SharedPreferences를 사용하여 데이터 리셋
        SharedPreferences prefs = context.getSharedPreferences("pedometer_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("steps", 0);
        editor.putInt("stepCount", 0);
        editor.putFloat("calories", 0.0f);
        editor.putString("activeTime", "00:00:00");
        editor.putLong("totalActiveTime", 0);
        editor.putInt("lastSentSteps", 0);
        editor.apply();
        pedometer.saveActiveTime(context, 0);
        pedometer.saveStepTime(context, 0);
        pedometer.saveStepCount(context, 0);


        // 필요하다면 추가적인 액션, 예를 들어 Notification을 보내거나, 다른 컴포넌트에 broadcast를 보낼 수 있습니다.
    }
}

