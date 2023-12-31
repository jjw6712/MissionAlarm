package com.example.ar1.ui.rank;

import static androidx.fragment.app.FragmentManager.TAG;


import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ar1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import androidx.viewpager2.widget.ViewPager2;

public class Ranking extends Fragment {
    private static final int REAL_COUNT = 7;
    private ListView rankingListView;
    private RankingAdapter rankingAdapter;
    private List<RankingItem> rankingItemList;
    ViewPager2 viewPager;
    private static boolean isAdapterInitialized = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ranking, container, false);
        TextView title = root.findViewById(R.id.title); // ID를 설정해주세요.
        title.setText("통합 랭킹");


        // ListView 설정
        rankingListView = root.findViewById(R.id.rankingList); // ListView의 ID를 설정해주세요

        if (rankingItemList == null) {
            rankingItemList = new ArrayList<>();
        }

        if (rankingAdapter == null) {
            rankingAdapter = new RankingAdapter(getContext(), rankingItemList);
        }

        rankingListView.setAdapter(rankingAdapter);

        if (!isAdapterInitialized) {
            // ViewPager 설정
            viewPager = root.findViewById(R.id.viewPager);
            viewPager.setAdapter(new RankingPagerAdapter(getChildFragmentManager(), getLifecycle()));
            viewPager.setCurrentItem(REAL_COUNT); // 중간 위치에서 시작
            isAdapterInitialized = true;
        }

        fetchRankings(); // 랭킹 데이터 가져오기

        return root;
    }

    private void fetchRankings() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://sw--zqbli.run.goorm.site/getAllRankings")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonData = response.body().string();
                Log.d("Ranking Response", jsonData);
                try {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String userImage = jsonObject.getString("userImage");
                        String userName = jsonObject.getString("userName");
                        int missionCount = jsonObject.getInt("missionCount");
                        Log.d(TAG, "랭킹: "+userImage+"\n"+userName+"\n"+missionCount);

                        RankingItem item = new RankingItem(userImage, userName, missionCount);
                        rankingItemList.add(item);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rankingAdapter.notifyDataSetChanged();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isAdapterInitialized = false;
    }

}