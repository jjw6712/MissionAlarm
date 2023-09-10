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

import com.example.ar1.Login;
import com.example.ar1.R;
import com.example.ar1.WebViewActivity;
import com.example.ar1.ui.mypage.userinfo.MyInfoActivity;

import java.util.List;

public class MissionListAdapter extends ArrayAdapter<String> {

    private Context context;

    public MissionListAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.mission_list, parent, false);
        }

        // 현재 위치(position)에 해당하는 데이터 가져오기
        String currentItem = getItem(position);

        // 버튼 초기화 및 설정
        Button itemButton = itemView.findViewById(R.id.itemButton);
        itemButton.setText(currentItem); // 버튼 텍스트 설정

        // 로그아웃 버튼 클릭 이벤트 처리
        itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("스쿼트".equals(currentItem)) { //개발자 노트 버튼 클릭 이벤트 처리
                    Intent intent = new Intent(context, SquatInfo.class); // WebView를 표시하는 액티비티
                    context.startActivity(intent);
                } else if ("푸쉬업".equals(currentItem)) {
                    Intent intent = new Intent(context, SquatInfo.class);
                    context.startActivity(intent);
                }
            }
        });

        return itemView;
    }

}