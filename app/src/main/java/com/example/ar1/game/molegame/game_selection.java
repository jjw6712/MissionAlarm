package com.example.ar1.game.molegame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.MLkit.MLkitMotionDemo;
import com.example.ar1.R;

public class game_selection extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private MediaPlayer backgroundMusic;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_selection);
// 배경음악 로드 및 재생
        backgroundMusic = MediaPlayer.create(this, R.raw.gamebgm);
        backgroundMusic.setLooping(true); // 음악 반복 설정
        backgroundMusic.start();

        // 상태바 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.game_selection);
        sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Button easyButton = findViewById(R.id.easyButton);
        easyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame("초급");
                editor.putString("selected_defalut_stretching_count_", "초급");
                editor.apply();
                finish();
            }
        });

        Button middleButton = findViewById(R.id.MiddleButton);
        middleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame("중급");
                editor.putString("selected_defalut_stretching_count_", "중급");
                editor.apply();
                finish();
            }
        });

        Button advancedButton = findViewById(R.id.advancedButton);
        advancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame("고급");
                editor.putString("selected_defalut_stretching_count_", "고급");
                editor.apply();
                finish();
            }
        });
    }

    private void startGame(String difficulty) {
        Intent intent = new Intent(this, GameProcess.class);
        intent.putExtra("difficulty", difficulty);
        String selectedOption = "두더지게임";
        SharedPreferences.Editor editorOption = sharedPreferences.edit();
        editorOption.putString("selected_stretching_default_mode_", selectedOption);
        editorOption.apply();
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 배경음악 정지 및 리소스 해제
        if (backgroundMusic != null) {
            if (backgroundMusic.isPlaying()) {
                backgroundMusic.stop();
            }
            backgroundMusic.release();
        }
    }

}
