package com.example.ar1.ui.mission;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ar1.R;
import com.example.ar1.databinding.FragmentMissionBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Mission extends Fragment {
    String userId, userName;
    MissionListAdapter adapter;
    private LinearLayout categoryContainer;
    private ListView missionList;
    private Map<String, List<String>> categoryMissions = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mission, container, false);

        categoryContainer = root.findViewById(R.id.categoryLayout);
        missionList = root.findViewById(R.id.missionList);

        initializeCategoryMissions();
        // 초기 카테고리를 '헬스'로 설정하고 미션 리스트 업데이트
        updateMissionList("헬스");
        setupCategoryButtons();


        // 프래그먼트에서 상태바 색상 변경
        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.black));
        }

        SharedPreferences preferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", "");
        userName = preferences.getString("userName", "");

        return root;
    }
    private void setupCategoryButtons() {
        Button initialSelectedButton = null;

        for (String category : categoryMissions.keySet()) {
            Button button = new Button(getContext());
            button.setText(category);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // 텍스트 크기 조정
            button.setTextColor(Color.WHITE); // 흰색 텍스트 색상
            button.setTypeface(button.getTypeface(), Typeface.BOLD); // 텍스트를 굵게 설정
            // 강조 표시된 버튼에 대한 스타일 설정
            button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_button_mint)); // 둥근 모서리 배경 설정
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
                button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_button_mint)); // 둥근 모서리 배경 설정
            } else {
                button.setBackgroundColor(Color.parseColor("#000000")); // 기본 색상 설정
            }
        }
    }
    private void initializeCategoryMissions() {
        categoryMissions.put("헬스", Arrays.asList("스쿼트", "푸쉬업","턱걸이(준비중)",  "만보계"));
        categoryMissions.put("교육", Arrays.asList("영단어 발음하기", "영문장 발음하기", "영단어 퀴즈퍼즐"));
        categoryMissions.put("게임", Arrays.asList("두더지게임", "파리잡기(준비중)"));
    }



    private void updateMissionList(String category) {
        List<String> missions = categoryMissions.get(category);
        adapter = new MissionListAdapter(getContext(), missions);
        missionList.setAdapter(adapter);
    }
}
