package com.example.ar1.ui.mypage.userinfo;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.ar1.R;

import java.util.List;

import okhttp3.OkHttpClient;

public class MyInfoListAdapter extends ArrayAdapter<String> {

    private Context context;
    private OkHttpClient client;


    public MyInfoListAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
        client = new OkHttpClient();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.myinfo_list_layout, parent, false);
        }

        // 리스트 아이템의 텍스트를 설정
        Button button = itemView.findViewById(R.id.itemButton); // 텍스트뷰 ID를 itemText로 변경했습니다.
        String text = getItem(position);
        button.setText(text);
        // 텍스트뷰 참조
        TextView tvInfo = itemView.findViewById(R.id.tvinfo);

        // SharedPreferences에서 userId 가져오기
        SharedPreferences prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String userName = prefs.getString("userName", null);
        String userPhoneNum = prefs.getString("userPhoneNum", null);
        String userSex = prefs.getString("userSex", null);
        Log.d(TAG, "내계정 userId: "+userId);
        Log.d(TAG, "내계정 폰번: "+userPhoneNum);

        // position에 따른 정보 적용
        switch (position) {
            case 0:
                tvInfo.setText(userName);
                break;
            case 1:
                tvInfo.setText(userId);
                break;
            case 2:
                tvInfo.setText(userPhoneNum);
                break;
            case 3:
                tvInfo.setText(userSex);
                break;
        }

        // 현재 위치(position)에 해당하는 데이터 가져오기
        String currentItem = getItem(position);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if("이름".equals(currentItem)) {
                    Intent intent = new Intent(context, ChangeNameActivity.class);
                    ((Activity) context).startActivityForResult(intent, MyInfoActivity.REQUEST_CODE_CHANGE_NAME);
                } else if ("아이디".equals(currentItem)) {
                    Intent intent = new Intent(context, ChangeIDActivity.class);
                    ((Activity) context).startActivityForResult(intent, MyInfoActivity.REQUEST_CODE_CHANGE_ID);
                } else if ("번호".equals(currentItem)) {
                    Intent intent = new Intent(context, ChangePHActivity.class);
                    ((Activity) context).startActivityForResult(intent, MyInfoActivity.REQUEST_CODE_CHANGE_PH);
                } else if ("성별".equals(currentItem)) {
                    Intent intent = new Intent(context, ChangeSexActivity.class);
                    ((Activity) context).startActivityForResult(intent, MyInfoActivity.REQUEST_CODE_CHANGE_SEX);
                }
            }
        });


        return itemView;
    }

}