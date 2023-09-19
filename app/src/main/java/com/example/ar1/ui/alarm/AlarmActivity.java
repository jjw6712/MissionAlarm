package com.example.ar1.ui.alarm;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.MLkit.MLkitMotion;
import com.example.ar1.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {

    private TextView tvAlarmTime, tvCurrentTime;
    private Button btnStopAlarm;
    private View view;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
    private boolean start_motion = false;
    private int alarmId;
    String stretchingOptionSaved;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);


        // 이 코드가 화면이 자동으로 꺼지는 것을 막습니다.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 액티비티를 투명하게 설정하여 잠금 화면 위에 푸시 알림을 표시합니다.
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        // 액티비티의 뷰와 변수를 매핑합니다.
        tvAlarmTime = findViewById(R.id.alarm_time_tv);
        btnStopAlarm = findViewById(R.id.stop_button);

        // 알람 시간과 현재 시간을 표시합니다.
        Calendar calendar = Calendar.getInstance();
        String time = timeFormat.format(calendar.getTime());

        tvAlarmTime.setText(time);

        // 음량을 100으로 설정
        setMediaVolume(0);

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // 진동 초기화
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {1000, 500, 1000, 500}; // 1초간 진동 후 1초 대기 반복
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));

        // 알람 ID 받아오기
        alarmId = getIntent().getIntExtra("alarm_id", -1); // 알람 ID 받아오기
        sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        if (alarmId != -1) {
            stretchingOptionSaved = sharedPreferences.getString("selected_stretching_mode_" + alarmId, "default"); // 알람 ID 사용하여 스트레칭 모드 불러오기
            Log.d(TAG, "알람액티비티 알람아이디: " + alarmId + " 스트레칭 옵션 " + stretchingOptionSaved);
        } else {
            Log.e(TAG, "알람 아이디가 유효하지 않습니다.");
        }
        if ("선택안함".equals(stretchingOptionSaved)) { //아무런 미션을 선태하지 않으면 일반 알람처럼 꺼짐
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnStopAlarm.setText("알람끄기");
                }
            });
        }

        // 알람을 멈추는 버튼을 클릭하면 액티비티와 미디어 플레이어를 종료합니다.
        btnStopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_motion = true;
                // 미디어 플레이어를 중지시킴
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                vibrator.cancel();
                // 액티비티 종료 시 음량을 원래대로 되돌림
                //setMediaVolume(AudioManager.USE_DEFAULT_STREAM_TYPE);

                if ("선택안함".equals(stretchingOptionSaved)) { //아무런 미션을 선태하지 않으면 일반 알람처럼 꺼짐
                    finish();
                } else {
                    Intent mlkitMotionIntent = new Intent(AlarmActivity.this, MLkitMotion.class);

                    // 알람 ID를 인텐트에 추가
                    mlkitMotionIntent.putExtra("alarm_id", alarmId);

                    startActivity(mlkitMotionIntent);
                    finish();
                }
            }
        });
    }

    // 미디어 스트림의 음량을 설정하는 메소드
    private void setMediaVolume(int volume) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11 이상에서 사용
                WindowInsetsController insetsController = getWindow().getInsetsController();
                if (insetsController != null) {
                    insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    insetsController.hide(WindowInsets.Type.systemBars());
                }
            } else {
                // Android 11 미만에서 사용
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
            }
        }
    }

    @Override
    public void onBackPressed() { //뒤로가기 버튼 기능 오버라이드 하여 모션인식 중에 뒤로가기키 비활성화
        // super.onBackPressed(); // 원하는 작업을 수행하도록 이 줄을 주석 처리하거나 제거합니다.
        Toast.makeText(this, "뒤로 가기 버튼이 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onPause() {
        super.onPause();

        if (!start_motion) {
            // 사용자가 강제로 액티비티를 종료하려고 할 때만 알람 액티비티를 다시 시작하도록 처리
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }
}