package com.example.ar1.ui.rank;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

public class Ranking extends Fragment {
    private ListView rankingListView;
    private RankingAdapter rankingAdapter;
    private List<RankingItem> rankingItemList;
    private RankingViewModel mViewModel;

    public static Ranking newInstance() {
        return new Ranking();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ranking, container, false);

        rankingListView = root.findViewById(R.id.rankingList); // ListView의 ID를 설정해주세요
        rankingItemList = new ArrayList<>();
        rankingAdapter = new RankingAdapter(getContext(), rankingItemList);
        rankingListView.setAdapter(rankingAdapter);
        SharedPreferences preferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userId = preferences.getString("userId", "");
        String userName = preferences.getString("userName", "");
        fetchRankings();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RankingViewModel.class);
        // TODO: Use the ViewModel
    }
    private void fetchRankings() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://sw--zqbli.run.goorm.site/getAllRankings")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @SuppressLint("RestrictedApi")
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
}
