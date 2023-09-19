package com.example.ar1.ui.mission;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ar1.R;
import com.example.ar1.databinding.FragmentMissionBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Mission extends Fragment {
    String userId, userName;
    com.example.ar1.ui.mission.MissionListAdapter adapter;
    private FragmentMissionBinding binding;
    private boolean isTimerRunning = false;
    private int i = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MissionViewModel dashboardViewModel =
                new ViewModelProvider(this).get(MissionViewModel.class);

        binding = FragmentMissionBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.fragment_mission, container, false);

        // 프래그먼트에서 상태바 색상 변경
        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.black));
        }


        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ListView listView = root.findViewById(R.id.missionList);

        List<String> itemList = new ArrayList<>();
        // 아이템 리스트에 버튼에 표시할 내용 추가
        itemList.add("스쿼트");
        itemList.add("푸쉬업");

        adapter = new MissionListAdapter(getActivity(), itemList);
        listView.setAdapter(adapter);

        SharedPreferences preferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", "");
        userName = preferences.getString("userName", "");


        return root;
    }


}
