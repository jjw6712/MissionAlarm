package com.example.ar1.ui.graph;
import android.content.Context;
import android.widget.TextView;

import com.example.ar1.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyMarkerView extends MarkerView {

    private final TextView tvContent;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        // Find your layout components
        tvContent = findViewById(R.id.tvContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        // Convert the X value (day of the month) to a full date string
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, (int)e.getX());

        SimpleDateFormat sdf = new SimpleDateFormat("YYYY년MM월dd일 EEEE", Locale.getDefault()); // EEEE will give you the full day name
        String formattedDate = sdf.format(calendar.getTime());
        // Set your data here
        tvContent.setText(String.format("%s \n\n횟수: %d 회", formattedDate, (int)e.getY()));
    }

    // You can override other methods for size and positioning (e.g., getOffsetForDrawingAtPoint)
}