package com.example.ar1.ui.graph;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;

public class SpeachWordsInfo extends AppCompatActivity {
    ImageButton btBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speachwords_info); // XML 레이아웃 파일명을 입력하세요.
        btBack = findViewById(R.id.btBack);
        Button btnTodayWords = findViewById(R.id.btnTodayWords);
        Button btnWeekWords = findViewById(R.id.btnWeekWords);
        Button btnMonthWords = findViewById(R.id.btnMonthWords);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        btnTodayWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWordListActivity("today");
            }
        });

        btnWeekWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWordListActivity("week");
            }
        });

        btnMonthWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWordListActivity("month");
            }
        });
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void openWordListActivity(String timeFrame) {
        Intent intent = new Intent(SpeachWordsInfo.this, WordListActivity.class);
        intent.putExtra("TIME_FRAME", timeFrame);
        startActivity(intent);
    }
}
