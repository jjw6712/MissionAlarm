package com.example.ar1.alarmmission;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ar1.R;
import com.example.ar1.ui.mission.MissionListAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlarmMissionActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SQUAT = 1;
    private static final int REQUEST_CODE_PUSHUP = 2;
    private static final int REQUEST_CODE_Speachwords = 3;
    private static final int REQUEST_CODE_Speachsentences = 4;
    private static final int REQUEST_CODE_Quiz = 5;
    private static final int REQUEST_CODE_Pedometer = 6;
    String userId, userName;
    AlarmMissionListAdapter adapter;
    private LinearLayout categoryContainer;
    private ListView missionList;
    private Map<String, List<String>> categoryMissions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_mission); // 액티비티에 맞는 레이아웃 파일명으로 변경하세요.

        categoryContainer = findViewById(R.id.categoryLayout);
        missionList = findViewById(R.id.missionList);

        initializeCategoryMissions();
        // 초기 카테고리를 '헬스'로 설정하고 미션 리스트 업데이트
        updateMissionList("헬스");
        setupCategoryButtons();

        // 액티비티에서 상태바 색상 변경
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));

        SharedPreferences preferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", "");
        userName = preferences.getString("userName", "");
    }
    private void setupCategoryButtons() {
        Button initialSelectedButton = null;

        for (String category : categoryMissions.keySet()) {
            Button button = new Button(this);
            button.setText(category);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // 텍스트 크기 조정
            button.setTextColor(Color.WHITE); // 흰색 텍스트 색상
            button.setTypeface(button.getTypeface(), Typeface.BOLD); // 텍스트를 굵게 설정
            // 강조 표시된 버튼에 대한 스타일 설정
            button.setBackground(ContextCompat.getDrawable(AlarmMissionActivity.this, R.drawable.rounded_button_mint)); // 둥근 모서리 배경 설정
            button.setOnClickListener(v -> {
                updateMissionList(category);
                highlightSelectedCategoryButton(button); // 선택된 카테고리 버튼 강조 표시
            });
            categoryContainer.addView(button);

            if (category.equals("헬스")) {
                initialSelectedButton = button;
            }
        }

        // 초기에 '헬스' 카테고리 버튼 강조
        if (initialSelectedButton != null) {
            highlightSelectedCategoryButton(initialSelectedButton);
        }
    }
    // 선택된 카테고리 버튼 강조 표시를 위한 메소드
    private void highlightSelectedCategoryButton(Button selectedButton) {
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            Button button = (Button) categoryContainer.getChildAt(i);
            if (button.equals(selectedButton)) {
                button.setBackground(ContextCompat.getDrawable(AlarmMissionActivity.this, R.drawable.rounded_button_mint)); // 둥근 모서리 배경 설정
            } else {
                button.setBackgroundColor(Color.parseColor("#000000")); // 기본 색상 설정
            }
        }
    }
    private void initializeCategoryMissions() {
        categoryMissions.put("헬스", Arrays.asList("스쿼트", "푸쉬업","턱걸이(준비중)",  "만보계"));
        categoryMissions.put("교육", Arrays.asList("영단어 발음하기", "영문장 발음하기", "영단어 퀴즈퍼즐"));
        categoryMissions.put("게임", Arrays.asList("두더지게임(준비중)", "파리잡기(준비중)"));
    }



    private void updateMissionList(String category) {
        List<String> missions = categoryMissions.get(category);
        adapter = new AlarmMissionListAdapter(this, missions);
        missionList.setAdapter(adapter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SQUAT:
                    String missionName = data.getStringExtra("missionName");

                    String missionCount = data.getStringExtra("missionCount");

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("missionName", missionName);
                    returnIntent.putExtra("missionCount", missionCount);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    break;
                case REQUEST_CODE_PUSHUP:
                    missionName = data.getStringExtra("missionName");
                    missionCount = data.getStringExtra("missionCount");

                    returnIntent = new Intent();
                    returnIntent.putExtra("missionName", missionName);
                    returnIntent.putExtra("missionCount", missionCount);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    break;
                case REQUEST_CODE_Speachwords:
                    missionName = data.getStringExtra("missionName");
                    missionCount = data.getStringExtra("missionCount");

                    returnIntent = new Intent();
                    returnIntent.putExtra("missionName", missionName);
                    returnIntent.putExtra("missionCount", missionCount);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    break;
                case REQUEST_CODE_Speachsentences:
                    missionName = data.getStringExtra("missionName");
                    missionCount = data.getStringExtra("missionCount");

                    returnIntent = new Intent();
                    returnIntent.putExtra("missionName", missionName);
                    returnIntent.putExtra("missionCount", missionCount);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    break;
                case REQUEST_CODE_Quiz:
                    missionName = data.getStringExtra("missionName");
                    missionCount = data.getStringExtra("missionCount");

                    returnIntent = new Intent();
                    returnIntent.putExtra("missionName", missionName);
                    returnIntent.putExtra("missionCount", missionCount);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    break;
                case REQUEST_CODE_Pedometer:
                    missionName = data.getStringExtra("missionName");
                    missionCount = data.getStringExtra("missionCount");

                    returnIntent = new Intent();
                    returnIntent.putExtra("missionName", missionName);
                    returnIntent.putExtra("missionCount", missionCount);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                    break;
                // 다른 요청 코드에 대한 처리 ...
            }
        }
    }
}

