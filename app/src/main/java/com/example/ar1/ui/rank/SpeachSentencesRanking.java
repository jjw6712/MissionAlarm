package com.example.ar1.ui.rank;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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

public class SpeachSentencesRanking extends Fragment {
    private ListView rankingListView;
    private RankingAdapter rankingAdapter;
    private List<RankingItem> rankingItemList;
    private RankingViewModel mViewModel;
    private static SpeachSentencesRanking instance = null;



    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ranking, container, false);
        TextView title = root.findViewById(R.id.title);  // ID를 설정해주세요.
        title.setText("영문장 발음하기 랭킹");

        rankingListView = root.findViewById(R.id.rankingList); // ListView의 ID를 설정해주세요
        rankingItemList = new ArrayList<>();
        rankingAdapter = new RankingAdapter(getContext(), rankingItemList);
        rankingListView.setAdapter(rankingAdapter);

        fetchRankings(); // 스쿼트 랭킹 데이터 가져오기

        return root;
    }
    private void fetchRankings() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://sw--zqbli.run.goorm.site/getSpeachSentencesRankings")
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



