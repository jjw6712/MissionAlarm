package com.example.ar1.ui.mypage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.ar1.Login;
import com.example.ar1.MyInfoActivity;
import com.example.ar1.R;
import com.example.ar1.WebViewActivity;
import com.example.ar1.databinding.MyInfoActivityBinding;

import java.util.List;

public class MyPageListAdapter extends ArrayAdapter<String> {

    private Context context;

    public MyPageListAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.mypage_list_layout, parent, false);
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
                if ("개발자노트".equals(currentItem)) { //개발자 노트 버튼 클릭 이벤트 처리
                    Intent intent = new Intent(context, WebViewActivity.class); // WebView를 표시하는 액티비티
                    context.startActivity(intent);
                } else if ("내계정".equals(currentItem)) {
                    Intent intent = new Intent(context, MyInfoActivity.class);
                    context.startActivity(intent);
                } else if ("로그아웃".equals(currentItem)) {
                    // 로그인 상태 확인
                    SharedPreferences preferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
                    boolean isLoggedIn = preferences.getBoolean("is_logged_in", false);

                    // 로그아웃 처리 및 Login 액티비티로 이동
                    if (isLoggedIn) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("is_logged_in", false);
                        editor.clear();
                        editor.apply();

                        // 여기서 Login 액티비티로 이동하는 코드를 작성하면서 기존 액티비티 스택을 클리어합니다.
                        Intent intent = new Intent(context, Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                        // 현재 액티비티를 종료합니다.
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    }
                }
            }
        });

        // "로그아웃" 버튼의 텍스트 색상 변경
        if ("로그아웃".equals(currentItem)) {
            itemButton.setTextColor(Color.RED);
        } else {
            itemButton.setTextColor(Color.BLACK);
        }

        return itemView;
    }

}