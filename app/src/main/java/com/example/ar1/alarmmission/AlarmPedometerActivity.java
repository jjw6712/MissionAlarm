package com.example.ar1.alarmmission;

import static com.example.ar1.pedometer.MainActivity.startMyService;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ar1.R;
import com.example.ar1.pedometer.AlarmMyService;
import com.example.ar1.pedometer.Alarmpedometer;
import com.example.ar1.pedometer.MyService;
import com.example.ar1.pedometer.pedometer;

public class AlarmPedometerActivity extends AppCompatActivity implements SensorEventListener {


    private SensorManager sensorManager;
    private Sensor stepCountSensor;
    private int stepCount;
    public int mstepCount;// 현재 걸음 수
    public int mtargetCount;
    protected TextView countTV;
    private TextView time;
    private TextView cal;
    private TextView targetcnt;
    private long lastStepTime = 0;  // 마지막 걸음이 감지된 시간
    private long totalActiveTime ; // 활성화된 총 시간
    public double calories;
    public String activeTime;
    private int alarmId;
    private boolean isDialogShown = false; // 다이얼로그 표시 여부를 추적하는 플래그
    boolean isPowerModeEnabled;
    boolean isQuizEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_pedometer);
        isQuizEnd = false;
        calories = 0;
        totalActiveTime = 0;
        mstepCount = 0; // 걸음 수 초기화
        stepCount = 0;
        activeTime = "";
        Alarmpedometer.saveStepCount(this, 0);
        Alarmpedometer.saveStepTime(this, 0);
        Alarmpedometer.saveActiveTime(this, 0);
        // 액티비티에서 상태바 색상 변경
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        countTV = findViewById(R.id.cnt_txt);
        time = findViewById(R.id.steptime);
        cal = findViewById(R.id.stepcal);
        targetcnt = findViewById(R.id.targetcnt);
        // Shared Preferences에서 데이터 불러오기
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        alarmId = getIntent().getIntExtra("alarm_id", -1);
        String stretchingCount = sharedPreferences.getString("selected_stretching_count_" + alarmId, "0");
        isPowerModeEnabled = sharedPreferences.getBoolean("PowerMode", false);
        mstepCount = sharedPreferences.getInt("mstepCount", 0);
        stepCount = sharedPreferences.getInt("stepCount", -1);
        totalActiveTime = sharedPreferences.getLong("totalActiveTime", 0); // Long 타입으로 올바르게 불러오기

        if (mstepCount == -1) {
            mstepCount = 0;
            Alarmpedometer.saveStepCount(this, 0);
        }
        mtargetCount = Integer.parseInt(stretchingCount);

        // targetcnt TextView에 mtargetCount 설정
        targetcnt.setText(String.valueOf(mtargetCount));
        // - TYPE_STEP_DETECTOR:  리턴 값이 무조건 1, 앱이 종료되면 다시 0부터 시작
        // - TYPE_STEP_COUNTER : 앱 종료와 관계없이 계속 기존의 값을 가지고 있다가 1씩 증가한 값을 리턴
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // 앱 종료시 횟수 카운트
        startMyService(this);

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

        // method ----------------------------------------------------------------------------------

        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_LONG).show();
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        // SharedPreferences에서 저장된 보행 시간을 불러옵니다.
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        totalActiveTime = sharedPreferences.getLong("totalActiveTime", 0); // Long 타입으로 올바르게 불러오기
        // 센서 리스너 등록
        if (sensorManager != null) {
            sensorManager.registerListener(this, stepCountSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (stepCount == -1) {
                stepCount = (int) event.values[0];
                SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("stepCount", stepCount);
                editor.apply();
            }
            long currentTime = System.currentTimeMillis();
            if (lastStepTime > 0) {
                totalActiveTime += currentTime - lastStepTime;
            }
            lastStepTime = currentTime;

            // 활성화된 시간을 시:분:초 형식으로 변환하여 표시
            activeTime = formatTime(totalActiveTime);
            time.setText(activeTime);

            // 활성화된 시간을 시:분:초 형식으로 표시
            long activeHours = totalActiveTime / 3600000;
            long activeMinutes = (totalActiveTime % 3600000) / 60000;
            long activeSeconds = (totalActiveTime % 60000) / 1000;
            activeTime = String.format("%02d:%02d:%02d", activeHours, activeMinutes, activeSeconds);
            time.setText(activeTime);
            mstepCount = (int) event.values[0] - stepCount;
            countTV.setText(String.valueOf(mstepCount));
            calories = mstepCount * 0.05;
            cal.setText(String.format("%.2f", calories));
            // mstepCount 값이 변경될 때마다 저장
            saveStepCount();
            saveActiveTime(); // 보행 시간 저장
        }
        if (mstepCount >= mtargetCount && !isDialogShown) {
            // 목표 달성 시 다이얼로그 표시 및 플래그 설정
            showCompletionDialog();
            isDialogShown = true; // 다이얼로그가 표시되었음을 표시

        }


    }
    // 밀리초를 시:분:초 형식으로 변환하는 메소드
    private String formatTime(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = (millis % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    private void showCompletionDialog() {
        if (!isFinishing() && !isDialogShown) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("mstepCount"); // 키 삭제
            editor.remove("stepCount"); // 키 삭제
            editor.remove("totalActiveTime"); // 키 삭제
            activeTime = "";
            editor.apply();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("목표 달성!")
                    .setMessage("칼로리: " + String.format("%.2f", calories) + "\n활동 시간: " + activeTime)
                    .setPositiveButton("확인", (dialog, which) -> finish());

            builder.create().show();
            isDialogShown = true;
            isQuizEnd = true;
        }
    }
    private void saveStepCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("mstepCount", mstepCount);
        editor.apply();
    }
    // SharedPreferences에서 누적된 활동 시간 불러오기 및 설정
    private void loadActiveTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String timeStr = sharedPreferences.getString("totalActiveTime", "0");
        try {
            totalActiveTime = Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            totalActiveTime = 0; // 오류 발생 시 기본값으로 설정
        }
    }


    // 활동 시간 저장
    private void saveActiveTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("totalActiveTime", totalActiveTime);
        editor.apply();
    }


    // no return
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onBackPressed() {
        if (!isPowerModeEnabled) {
            super.onBackPressed(); // 파워 모드가 비활성화되어 있으면 기본 뒤로 가기 기능 수행
        } else {
            // 파워 모드가 활성화되어 있으면 토스트 메시지 표시
            Toast.makeText(this, "뒤로 가기 버튼이 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isQuizEnd) {
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("mstepCount", mstepCount);
        editor.putInt("stepCount", stepCount);
        editor.putLong("totalActiveTime", totalActiveTime);
        editor.apply();

        if (isPowerModeEnabled) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

}
