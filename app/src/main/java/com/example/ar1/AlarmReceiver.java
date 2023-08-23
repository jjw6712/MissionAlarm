package com.example.ar1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 알람 ID 받아오기
        int alarmId = intent.getIntExtra("id", -1); // Ensure alarmId is correctly retrieved
        Log.d("TAG", "Received alarmId: " + alarmId);

        // AlarmActivity 띄우기
        Intent alarmActivityIntent = new Intent(context, AlarmActivity.class);
        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 알람 ID를 인텐트에 추가
        alarmActivityIntent.putExtra("alarm_id", alarmId); // Ensure alarmId is correctly put as an extra

        context.startActivity(alarmActivityIntent);
    }
}