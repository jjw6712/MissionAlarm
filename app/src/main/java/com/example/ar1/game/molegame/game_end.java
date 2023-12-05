package com.example.ar1.game.molegame;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;
import com.example.ar1.ui.alarm.AlarmDBHelper;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class game_end extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_end);
        // 상태바 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        // 난이도와 점수 받아오기
        String difficulty = getIntent().getStringExtra("difficulty");
        int score = getIntent().getIntExtra("score", 0);

        // 텍스트 뷰 찾기 및 표시
        TextView difficultyTextView = findViewById(R.id.difficultyTextView);
        TextView scoreTextView = findViewById(R.id.scoreTextView);
        Button btok = findViewById(R.id.restartButton);

        difficultyTextView.setText("난이도: " + difficulty);
        scoreTextView.setText("점수: " + score);

        sendgamescore(String.valueOf(score));
        btok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void sendgamescore(String score) { // 서버로 데이터 보내기
        OkHttpClient client = new OkHttpClient();
        SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        String userId = preferences.getString("userId", ""); //로그인한 유저 id 가져오기

        // 현재 날짜 가져오기
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        String currentDate = sdfDate.format(calendar.getTime());

        // AlarmDBHelper 인스턴스 생성
        AlarmDBHelper dbHelper = new AlarmDBHelper(this); //sqlite 로컬db 객체 초기화

        // alarmId를 항상 null로 설정
        String alarmId = "기본모드";

// 현재 날짜와 시간을 구합니다.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        HashMap<String, String> data = new HashMap<>(); // 해쉬맵으로 데이터를 구성하기 위한 해쉬맵 객체 초기화
        data.put("userId", userId); // 로그인한 사용자 ID 삽입
        data.put("alarmId", alarmId); // 항상 null로 설정
        data.put("alarmTime", currentDateAndTime); // 현재 날짜와 시간 삽입
        data.put("missionName", "두더지게임"); // 해당 알람 미션 종류 삽입
        data.put("missionCount", score); // 해당 알람 미션 완료 갯수 삽입
        Log.d(TAG, ("userId: " + userId + "\nalarmId: " + alarmId + "\nalarmTime: " + currentDateAndTime + "\nmissionMode: " + "영단어 발음하기" + "\nmissionCount: " + score));

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject(data);

        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        String url = "https://sw--zqbli.run.goorm.site/mission"; //앤드포인드  mission으로 리퀘스트 보냄
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

}