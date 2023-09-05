package com.example.ar1.ui.mypage.userinfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangeSexActivity extends AppCompatActivity {

    private CheckBox cbMale, cbFemale, cbNone;
    private Button btChangeName;
    private ImageButton btBack;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_sex);

        // 상태바 색상 변경
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.white));

        SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String savedGender = prefs.getString("userSex", null); // 기존에 저장된 userSex 값을 가져옵니다.

        cbMale = findViewById(R.id.cbmale);
        cbFemale = findViewById(R.id.cbfemale);
        cbNone = findViewById(R.id.cbnull);
        btChangeName = findViewById(R.id.btChangeName); // 버튼의 id를 찾아 초기화합니다.
        btBack = findViewById(R.id.btBack);

        // 초기 체크 상태 설정
        if ("남성".equals(savedGender)) {
            cbMale.setChecked(true);
        } else if ("여성".equals(savedGender)) {
            cbFemale.setChecked(true);
        } else if ("성별을 선택해 주세요".equals(savedGender)) {
            cbNone.setChecked(true);
        }

        client = new OkHttpClient();

        cbMale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbFemale.setChecked(false);
                cbNone.setChecked(false);
            }
        });

        cbFemale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbMale.setChecked(false);
                cbNone.setChecked(false);
            }
        });

        cbNone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbMale.setChecked(false);
                cbFemale.setChecked(false);
            }
        });

        btChangeName.setOnClickListener(v -> {
            String selectedGender = null;
            if (cbMale.isChecked()) {
                selectedGender = "남성";
            } else if (cbFemale.isChecked()) {
                selectedGender = "여성";
            }else if (cbNone.isChecked()) {
                selectedGender = "성별을 선택해 주세요";
            }

            if (selectedGender != null) {
                sendGenderToServer(selectedGender);
            }

            // SharedPreferences에 userSex 업데이트
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userSex", selectedGender);
            editor.apply();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("userSex", selectedGender);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void sendGenderToServer(String gender) {
        SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        RequestBody formBody = new FormBody.Builder()
                .add("userId", userId)
                .add("userSex", gender)
                .build();

        Request request = new Request.Builder()
                .url("https://sw--zqbli.run.goorm.site/updateGender")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 에러 처리
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 성공적인 경우
                } else {
                    // 실패한 경우
                }
            }
        });
    }
}