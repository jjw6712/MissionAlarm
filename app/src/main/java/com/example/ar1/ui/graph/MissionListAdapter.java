package com.example.ar1.ui.graph;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.ar1.Login;
import com.example.ar1.R;
import com.example.ar1.WebViewActivity;
import com.example.ar1.ui.mypage.userinfo.MyInfoActivity;

import java.util.List;

public class MissionListAdapter extends ArrayAdapter<String> {

    private Context context;
    private int squatCount = 0; // 스쿼트 카운트
    private int pushUpCount = 0; // 푸쉬업 카운트
    private  int speachWordsCount = 0;
    private  int speachSentencesCount = 0;

    public MissionListAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
    }

    // 카운트 업데이트 메서드
    public void updateSquatCount(int newCount) {
        this.squatCount = newCount;
        notifyDataSetChanged();
    }
    public void updatePushUpCount(int newCount) {
        this.pushUpCount = newCount;
        notifyDataSetChanged();
    }
    public void updateSpeachWordsCount(int newCount) {
        this.speachWordsCount = newCount;
        notifyDataSetChanged();
    }
    public void updateSpeachSentencesCount(int newCount) {
        this.speachSentencesCount = newCount;
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.mission_list, parent, false);
        }

        String currentItem = getItem(position);

        Button itemButton = itemView.findViewById(R.id.itemButton);
        TextView tvCount = itemView.findViewById(R.id.tvCount); // tvCount는 각 리스트 아이템의 텍스트 뷰의 ID

        itemButton.setText(currentItem);

        if ("스쿼트".equals(currentItem)) {
            tvCount.setText(String.valueOf(squatCount));
        }
        if ("푸쉬업".equals(currentItem)) {
            tvCount.setText(String.valueOf(pushUpCount));
        }
        if ("영단어 발음하기".equals(currentItem)) {
            tvCount.setText(String.valueOf(speachWordsCount));
        }
        if ("영문장 발음하기".equals(currentItem)) {
            tvCount.setText(String.valueOf(speachSentencesCount));
        }

        itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("스쿼트".equals(currentItem)) {
                    Intent intent = new Intent(context, SquatInfo.class);
                    context.startActivity(intent);
                } else if ("푸쉬업".equals(currentItem)) {
                    Intent intent = new Intent(context, PushUpInfo.class); // 푸쉬업 정보 액티비티로 변경해야 함
                    context.startActivity(intent);
                }else if ("영단어 발음하기".equals(currentItem)) {
                    Intent intent = new Intent(context, SpeachWordsInfo.class); // 푸쉬업 정보 액티비티로 변경해야 함
                    context.startActivity(intent);
                }
            }
        });

        return itemView;
    }
}