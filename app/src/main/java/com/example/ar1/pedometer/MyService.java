package com.example.ar1.pedometer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.ar1.R;

public class MyService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private int stepCount;
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 센서 리스너 등록
        registerSensorListener();
        Log.d("MyService", "onStartCommand");

        // Foreground Service로 설정
        startForeground(1, createNotification());

        // 서비스가 종료되어도 자동으로 재시작하도록 설정
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("Error", "onTaskRemoved - 강제 종료 " + rootIntent);
    }

    private void createNotificationChannel() {
        // Android 8.0 이상에서는 Notification Channel을 생성하여 설정해야 합니다.
        NotificationChannel channel = new NotificationChannel(
                "stepForeground",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @NonNull
    private Notification createNotification() {
        // Notification을 생성하여 반환
        // 필요에 따라 NotificationChannel 설정 (Android 8.0 이상)
        // NotificationManagerCompat을 사용하여 Notification을 보낼 수 있음
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "stepForeground");

        // Android 8.0 이상에서는 Notification Channel을 생성하여 설정해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }


        // 알림을 탭했을 때 앱을 열도록 Intent 설정
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        builder.setContentTitle("MissionAlarm                                        ")
                .setContentText("오늘의 걸음 : "+stepCount)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.stepimage);

        // Notification을 반환
        return builder.build();
    }

    private void updateNotification(int stepCount) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Android 8.0 이상에서는 Notification Channel을 생성하여 설정해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        // 알림을 생성하거나 업데이트합니다.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "stepForeground")
                .setContentTitle("MissionAlarm")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.stepimage);

        // 알림 내용에 업데이트된 걸음 수를 설정합니다.
        builder.setContentText("오늘의 걸음: "+stepCount);

        // 알림을 탭했을 때 앱을 열도록 Intent 설정
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, builder.build());
        }
    }

    private String span(String cc){
        // 텍스트에 적용할 스타일을 설정
        SpannableString spannableText = new SpannableString(cc);

        // 텍스트의 색상을 변경하는 스팬을 적용
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
        spannableText.setSpan(colorSpan, 0, spannableText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 텍스트의 크기를 변경하는 스팬을 적용
        RelativeSizeSpan sizeSpan = new RelativeSizeSpan(1.5f); // 텍스트 크기를 1.5배로 변경
        spannableText.setSpan(sizeSpan, 0, spannableText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return cc;
    }


    private void registerSensorListener() {
        // 걸음 센서를 위한 기본 센서 가져오기
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // 센서 리스너 등록
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_GAME);

        } else {
            // 걸음 센서를 사용할 수 없는 경우 처리
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // 걸음 수 변경에 대한 기존 로직
            // 측정된 걸음 수 가져오기
            stepCount = (int) event.values[0]- pedometer.getStepCount(this);

            // Notification 업데이트
            updateNotification(stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){
        // 정확도 변경에 대한 처리 (필요한 경우)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "삭제됨 얍알");

        startForeground(1, createNotification());
    }
}