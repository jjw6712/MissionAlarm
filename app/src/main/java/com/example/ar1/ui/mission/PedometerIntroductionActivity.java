package com.example.ar1.ui.mission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.MLkit.MLkitMotionDemo;
import com.example.ar1.R;
import com.example.ar1.pedometer.MainActivity;

public class PedometerIntroductionActivity extends AppCompatActivity {
    ImageButton btBack;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer_introduction);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        btBack = findViewById(R.id.btBack);
        VideoView videoView = findViewById(R.id.videoView);
        String videoFileName = "pedometer"; // 동영상 파일 이름

        int videoResId = getResources().getIdentifier(videoFileName, "raw", getPackageName());
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videoResId));
        Button startSquatMissionButton = findViewById(R.id.startSquatMissionButton);
        // 동영상 재생이 완료될 때 호출되는 리스너 설정
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 동영상 재생이 완료되면 다시 시작
                videoView.start();
            }
        });

// 동영상 재생 시작
        videoView.start();
        btBack = findViewById(R.id.btBack);
        startSquatMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 미션 시작 다이얼로그 표시
                startSquatMission();
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private void startSquatMission() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}