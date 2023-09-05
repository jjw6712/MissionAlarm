package com.example.ar1.ui.graph;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ar1.R;
import com.example.ar1.databinding.FragmentGraphBinding;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;

public class Graph extends Fragment {

    private FragmentGraphBinding binding;
    private TextView tvSquatCount, tvPushupCount;
    private OkHttpClient client;
    private static final String TAG = "Graph";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GraphViewModel notificationsViewModel =
                new ViewModelProvider(this).get(GraphViewModel.class);

        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.activity_graph, container, false);

        int color = Color.parseColor("#FFFFFF");
        // 프래그먼트에서 상태바 색상 변경
        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }

        tvSquatCount = root.findViewById(R.id.tvSquatCount);
        tvPushupCount = root.findViewById(R.id.tvPushupCount);

        client = new OkHttpClient();

        loadMissionCounts();

        return root;
    }

    private void loadMissionCounts() {
        SharedPreferences prefs = this.requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
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
                        Log.d(TAG, "squatCount: "+squatCount);
                        Log.d(TAG, "squatCount: "+pushupCount);

                        requireActivity().runOnUiThread(() -> {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}