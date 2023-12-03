package com.example.ar1.pedometer;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HourlyUpdateReceiver extends BroadcastReceiver {
    private String userId;
    String currentDateAndTime;

    @SuppressLint("RestrictedApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", ""); // 로그인한 유저 id 가져오기

        // 현재 날짜와 시간을 구합니다.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        currentDateAndTime = sdf.format(new Date());

        int steps = preferences.getInt("steps", 0);
        double calories = preferences.getFloat("calories", 0.0f);
        String activeTime = preferences.getString("activeTime", "00:00:00");

        // 마지막으로 전송된 steps 값 가져오기
        int lastSentSteps = preferences.getInt("lastSentSteps", -1);

        if (steps != lastSentSteps) {
            // 서버로 데이터를 전송하는 로직을 호출합니다.
            sendDataToServer(context, steps, calories, activeTime);
            Log.d(TAG, "만보계 정보 서버로");

            // 현재 steps 값을 'lastSentSteps'로 저장합니다.
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("lastSentSteps", steps);
            editor.apply();
        }else
        Log.d(TAG, "만보계 정보 변화없음");
    }
    private void sendDataToServer(Context context, int steps, double calories, String activeTime) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://sw--zqbli.run.goorm.site/updatepedometer"; // 서버의 URL로 대체


        JSONObject data = new JSONObject();
        try {
            data.put("userId", userId);
            data.put("step", steps);
            data.put("cal", calories);
            data.put("time", activeTime);
            data.put("alarmTime", currentDateAndTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(data.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 요청 실패 처리
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 요청 성공 처리
                    final String responseData = response.body().string();
                    // UI 업데이트는 여기서 수행
                }
            }
        });
    }
}