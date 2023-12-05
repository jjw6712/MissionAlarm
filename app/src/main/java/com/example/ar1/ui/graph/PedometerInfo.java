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
import android.widget.Toast;
import com.example.ar1.ui.graph.PedometerMarkerView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
                .url("https://sw--zqbli.run.goorm.site/getAllPedometerData/" + userId)
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
                        JSONObject todayData = jsonObject.getJSONObject("today");
                        JSONObject weeklyData = jsonObject.getJSONObject("weekly");
                        JSONObject monthlyData = jsonObject.getJSONObject("monthly");
                        JSONArray dailyDataArray = jsonObject.getJSONArray("daily");

                        runOnUiThread(() -> {
                            tvTodayCount.setText(String.valueOf(todayData.optInt("todayStepCount", 0)));
                            tvWeeklyCount.setText(String.valueOf(weeklyData.optInt("weeklyStepCount", 0)));
                            tvMonthlyCount.setText(String.valueOf(monthlyData.optInt("monthlyStepCount", 0)));
                            Calendar cal = Calendar.getInstance();
                            int lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                                    List<BarEntry> entries = new ArrayList<>();
                                    for (int day = 1; day <= lastDayOfMonth; day++) {
                                        int dailyStepCount = 0; // Initialize to 0

                                        for (int i = 0; i < dailyDataArray.length(); i++) {
                                            JSONObject dailyData = null;
                                            try {
                                                dailyData = dailyDataArray.getJSONObject(i);
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                            String dateStr = null;
                                            try {
                                                dateStr = dailyData.getString("date");
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                            int dayOfMonth = Integer.parseInt(dateStr.split("-")[2].split("T")[0]);

                                            if (day == dayOfMonth) {
                                                try {
                                                    dailyStepCount = dailyData.getInt("dailyStepCount");
                                                } catch (JSONException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                break;
                                            }
                                        }
                                        entries.add(new BarEntry(day, dailyStepCount));
                                    }



                            BarDataSet dataSet = new BarDataSet(entries, "Step Count");
                            dataSet.setColor(Color.parseColor("#FF00DD9B"));
                            dataSet.setBarBorderWidth(0.9f);
                            dataSet.setValueTextColor(Color.parseColor("#FFFFFF"));
                            dataSet.setValueTextSize(0f);
                            dataSet.setHighlightEnabled(true);
                            dataSet.setDrawValues(false);

                            BarData barData = new BarData(dataSet);
                            barData.setBarWidth(0.9f);

                            chart.setData(barData);

                            XAxis xAxis = chart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setDrawGridLines(false);
                            xAxis.setGranularity(1f);
                            xAxis.setTextColor(Color.parseColor("#FF00DD9B"));
// 차트에 MyMarkerView 설정
                            // 마커 뷰 인스턴스 생성
                            PedometerMarkerView markerView = new PedometerMarkerView(
                                    PedometerInfo.this, R.layout.custom_marker_view, dailyDataArray);
                            chart.setMarker(markerView);
                            chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                @Override
                                public void onValueSelected(Entry e, Highlight h) {
                                    BarEntry barEntry = (BarEntry) e;
                                    int dayOfMonth = (int) barEntry.getX();
                                    String selectedDate = String.format("%d-%02d-%02d",
                                            Calendar.getInstance().get(Calendar.YEAR),
                                            Calendar.getInstance().get(Calendar.MONTH) + 1,
                                            dayOfMonth);

                                    for (int i = 0; i < dailyDataArray.length(); i++) {
                                        try {
                                            JSONObject dailyData = dailyDataArray.getJSONObject(i);
                                            String dateStr = dailyData.getString("date").split("T")[0];
                                            if (selectedDate.equals(dateStr)) {
                                                // 여기서 MarkerView의 데이터를 업데이트하는 대신 차트에 highlight를 설정합니다.
                                                chart.highlightValue(h);
                                                break;
                                            }
                                        } catch (JSONException ex) {
                                            // 예외 처리
                                            ex.printStackTrace();
                                        }
                                    }
                                }


                                @Override
                                public void onNothingSelected() {
                                    // No action needed
                                }
                            });

                            chart.animateY(2000);
                            chart.invalidate();
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}