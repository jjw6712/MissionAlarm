package com.example.ar1.pedometer;

import static androidx.fragment.app.FragmentManager.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ar1.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final static String Tag = "MainActivity";
    private SensorManager sensorManager;
    private Sensor stepCountSensor;
    private int stepCount;
    public int mstepCount;// 현재 걸음 수
    protected TextView countTV;
    private TextView time;
    private TextView cal;
    private long lastStepTime = 0;  // 마지막 걸음이 감지된 시간
    private long totalActiveTime = 0;  // 활성화된 총 시간
    public double calories;
    public String activeTime;
    long activeHours;
    long activeMinutes;
    long activeSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);
        // 액티비티에서 상태바 색상 변경
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        countTV = findViewById(R.id.cnt_txt);
        time = findViewById(R.id.steptime);
        cal = findViewById(R.id.stepcal);

        // - TYPE_STEP_DETECTOR:  리턴 값이 무조건 1, 앱이 종료되면 다시 0부터 시작
        // - TYPE_STEP_COUNTER : 앱 종료와 관계없이 계속 기존의 값을 가지고 있다가 1씩 증가한 값을 리턴
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
// 1시간마다 데이터 전송 스케줄링
        scheduleHourlyUpdate();
// 자정 리셋 스케줄링
        scheduleMidnightReset();
        // 앱 종료시 횟수 카운트
        startMyService(this);
        //resetData();

        // permission line -------------------------------------------------------------------------
        if (ContextCompat.checkSelfPermission(this, Manifest.
                permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SET_ALARM) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.SET_ALARM}, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.
                permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.
                permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.FOREGROUND_SERVICE}, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.
                permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
        // time set line ---------------------------------------------------------------------------

        // 코드 실행 시간 설정
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //  00:00
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.MINUTE, 0);

        // Create an Intent for your BroadcastReceiver
        Intent intent = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // AlarmManager service 가져오기
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

        // method ----------------------------------------------------------------------------------

        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_LONG).show();
        }

    }//----------------------------<onCreate>-------------------------------------------------------

    // - SENSOR_DELAY_NORMAL: 20,000 초 딜레이
    // - SENSOR_DELAY_UI: 6,000 초 딜레이
    // - SENSOR_DELAY_GAME: 20,000 초 딜레이
    // - SENSOR_DELAY_FASTEST: 딜레이 없음
    @Override
    protected void onStart() {
        super.onStart();
        // SharedPreferences에서 저장된 보행 시간을 불러옵니다.
        totalActiveTime = pedometer.getActiveTime(this);
        // 센서 리스너 등록
        if (sensorManager != null) {
            sensorManager.registerListener(this, stepCountSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    // MyService 실행
    public static void startMyService(Context context) {
        Intent serviceIntent = new Intent(context, MyService.class);
        context.startService(serviceIntent);
    }

    // 센서 이벤트 리스너 메소드
    // 센서는 동작을 감지 하면 이벤트를 발생하여 onSensorChanged에 값을 전달합니다.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = pedometer.getStepCount(this);
            if (stepCount < 1) {
                stepCount = (int) event.values[0];
                pedometer.saveStepCount(this, stepCount);
            }
            long currentTime = System.currentTimeMillis();
            if (lastStepTime > 0) {
                // 마지막으로 걸음이 감지된 시간과 현재 시간의 차이를 누적
                totalActiveTime += currentTime - lastStepTime;
                // 누적된 시간을 SharedPreferences에 저장
                pedometer.saveActiveTime(this, totalActiveTime);
            }
            lastStepTime = currentTime;

            // 활성화된 시간을 시:분:초 형식으로 표시
            activeHours = totalActiveTime / 3600000;
            activeMinutes = (totalActiveTime % 3600000) / 60000;
            activeSeconds = (totalActiveTime % 60000) / 1000;
            activeTime = String.format("%02d:%02d:%02d", activeHours, activeMinutes, activeSeconds);
            time.setText(activeTime);

            mstepCount = (int) event.values[0] - stepCount;
            countTV.setText(String.valueOf(mstepCount));
            calories = mstepCount * 0.05;
            cal.setText(String.format("%.2f", calories));
        }
        SharedPreferences preferences = getSharedPreferences("pedometer_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("steps", mstepCount);
        editor.putInt("stepCount", stepCount);
        editor.putFloat("calories", (float) calories);
        editor.putString("activeTime", activeTime);
        editor.putLong("totalActiveTime", totalActiveTime);
        editor.apply();
    }



    // no return
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressLint("RestrictedApi")
    private void scheduleHourlyUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, HourlyUpdateReceiver.class);
        Log.d(TAG, "scheduleHourlyUpdate 실행됨");
        // PendingIntent.FLAG_IMMUTABLE 추가
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        long interval = 10*1000; // 1분을 밀리초로 표현
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }
    @SuppressLint("ScheduleExactAlarm")
    private void scheduleMidnightReset() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MidnightResetReceiver.class);
        // PendingIntent.FLAG_IMMUTABLE 추가
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
    // MainActivity 내부에 resetData 메소드 추가
    public void resetData() {
        mstepCount = 0;
        stepCount = 0;
        cal.setText("0.00");
        lastStepTime = 0;
        totalActiveTime = 0; // 총 활동 시간 변수 추가
        activeHours = 0;
        activeMinutes = 0;
        activeSeconds = 0;
        pedometer.saveActiveTime(this, 0);
        pedometer.saveStepTime(this, 0);
        pedometer.saveStepCount(this, 0);
        SharedPreferences preferences = getSharedPreferences("pedometer_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("steps", mstepCount);
        editor.putInt("stepCount", stepCount);
        editor.putFloat("calories", (float) calories);
        //editor.putString("activeTime", activeTime);
        editor.putLong("totalActiveTime", totalActiveTime);
        editor.apply();

        countTV.setText(String.valueOf(mstepCount));
        //cal.setText(String.format("%.2f", totalCalories));
        time.setText("00:00:00");
    }
}
