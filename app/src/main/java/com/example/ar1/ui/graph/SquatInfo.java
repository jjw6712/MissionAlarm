package com.example.ar1.ui.graph;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;

public class SquatInfo extends AppCompatActivity {
    private TextView tvSquatCount, tvPushupCount;
    private OkHttpClient client;
    private static final String TAG = "SquatInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squat_info);

        int color = Color.parseColor("#FFFFFF");

        // 상태바 색상 변경
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.white));

        tvSquatCount = findViewById(R.id.tvTodayCount);

        client = new OkHttpClient();

        loadMissionCounts();
    }

    private void loadMissionCounts() {
        SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        Request request = new Request.Builder()
                .url("https://sw--zqbli.run.goorm.site/getTodaysMissionCount/" + userId)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 에러 처리
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        int squatCount = jsonObject.getInt("squatCount");
                        int pushupCount = jsonObject.getInt("pushUpCount");
                        Log.d(TAG, "squatCount: " + squatCount);
                        Log.d(TAG, "pushUpCount: " + pushupCount);

                        runOnUiThread(() -> {
                            tvSquatCount.setText(String.valueOf(squatCount));
                            tvPushupCount.setText(String.valueOf(pushupCount));
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}