package com.example.ar1.ui.graph;
import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.example.ar1.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PedometerMarkerView extends MarkerView {

    private final TextView tvContent;
    private JSONArray dailyDataArray;
    int step;
    double cal;
    String time;
    public PedometerMarkerView(Context context, int layoutResource, JSONArray dailyDataArray) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
        this.dailyDataArray = dailyDataArray;
        Log.d(TAG, "만보계 그래프 데이터: "+dailyDataArray);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);

        // X 값은 날짜의 일(day)를 나타냅니다.
        int dayOfMonth = (int) e.getX();
        String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                dayOfMonth);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(calendar.getTime());
        for (int i = 0; i < dailyDataArray.length(); i++) {
            try {
                JSONObject dailyData = dailyDataArray.getJSONObject(i);
                // "date" 필드에서 날짜 부분만 추출합니다. (시간 정보는 제외)
                String dateStr = dailyData.getString("date").split("T")[0];
                if (selectedDate.equals(dateStr)) {
                    int steps = dailyData.optInt("dailyStepCount", 0);
                    float cals = (float) dailyData.optDouble("dailyCal", 0.0);
                    String time = dailyData.optString("dailyTime", "N/A");

                    String markerText = String.format(Locale.getDefault(),
                            "%s\n걸음수: %d 보\n소모 칼로리: %.2f 칼로리\n보행시간: %s",
                            formattedDate, steps, cals, time);
                    tvContent.setText(markerText);
                    break;
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }



    // You can override other methods for size and positioning (e.g., getOffsetForDrawingAtPoint)
}