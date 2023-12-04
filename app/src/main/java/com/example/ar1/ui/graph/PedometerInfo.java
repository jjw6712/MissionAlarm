package com.example.ar1.ui.graph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class PedometerInfo extends AppCompatActivity {
    private TextView tvTodayCount;
    private TextView tvWeeklyCount;
    private TextView tvMonthlyCount;
    private OkHttpClient client;
    private BarChart chart;
    ImageButton ibBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer_info);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));

        tvTodayCount = findViewById(R.id.tvTodayCount);
        tvWeeklyCount = findViewById(R.id.tvWeekCount);
        tvMonthlyCount = findViewById(R.id.tvMonthCount);
        chart = findViewById(R.id.BarChart);
        ibBack = findViewById(R.id.btBack);

        client = new OkHttpClient();

        loadAllMissionCounts();
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadAllMissionCounts() {
        SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        Request request = new Request.Builder()
                .url("https://sw--zqbli.run.goorm.site/getAllSquatMissionCount/" + userId)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        int todaySquatCount = jsonObject.optInt("todaySquatCount", 0);  // 0이 기본값입니다.
                        int weeklySquatCount = jsonObject.optInt("weeklySquatCount", 0);
                        int monthlySquatCount = jsonObject.optInt("monthlySquatCount", 0);

                        JSONArray dailyCountsArray = jsonObject.getJSONArray("dailyCountsForMonth");

                        runOnUiThread(() -> {
                            tvTodayCount.setText(String.valueOf(todaySquatCount == 0 ? 0 : todaySquatCount));
                            tvWeeklyCount.setText(String.valueOf(weeklySquatCount == 0 ? 0 : weeklySquatCount));
                            tvMonthlyCount.setText(String.valueOf(monthlySquatCount == 0 ? 0 : monthlySquatCount));

                            Calendar cal = Calendar.getInstance();
                            int lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                            List<BarEntry> entries = new ArrayList<>();
                            for (int day = 1; day <= lastDayOfMonth; day++) {
                                int dailySquatCount = 0; // Initialize to 0

                                for (int i = 0; i < dailyCountsArray.length(); i++) {
                                    JSONObject dailyCountObject = null; // JSON exception은 catch에서 처리하므로 제거
                                    try {
                                        dailyCountObject = dailyCountsArray.getJSONObject(i);
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    String dateStr = null; // JSON exception은 catch에서 처리하므로 제거
                                    try {
                                        dateStr = dailyCountObject.getString("date");
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                    int dayFromServer = Integer.parseInt(dateStr.split("-")[2].split("T")[0]);

                                    if (day == dayFromServer) {
                                        try {
                                            dailySquatCount = dailyCountObject.getInt("dailySquatCount"); // JSON exception은 catch에서 처리하므로 제거
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                        break;
                                    }
                                }
                                entries.add(new BarEntry(day, dailySquatCount));  // 0도 추가됩니다.
                            }

                            BarDataSet dataSet = new BarDataSet(entries, "Squat Count");
                            dataSet.setColor(Color.parseColor("#FF00DD9B"));
                            dataSet.setBarBorderWidth(0.9f);
                            dataSet.setValueTextColor(Color.parseColor("#FFFFFF"));  // Set to white to effectively hide it
                            dataSet.setValueTextSize(0f);  // Set to 0 to hide it
                            dataSet.setHighlightEnabled(true);
                            dataSet.setDrawValues(false); // Hide the original label above the bar

                            BarData barData = new BarData(dataSet);
                            barData.setBarWidth(0.9f);

                            chart.setData(barData);

                            XAxis xAxis = chart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setTextColor(Color.parseColor("#FF00DD9B"));

                            chart.setMarker(new MyMarkerView(getApplicationContext(), R.layout.custom_marker_view));  // Assume MyMarkerView is your custom marker view

                            chart.animateY(2000);
                            chart.invalidate();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}