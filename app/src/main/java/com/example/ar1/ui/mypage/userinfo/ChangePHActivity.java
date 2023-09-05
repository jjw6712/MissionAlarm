package com.example.ar1.ui.mypage.userinfo;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
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

public class ChangePHActivity extends AppCompatActivity {

    private EditText etName;
    private ImageButton btcleartext, btBack;
    private Button btChangeName;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_ph);

        // 상태바 색상 변경
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.white));

        etName = findViewById(R.id.etName);
        btcleartext = findViewById(R.id.btcleartext);
        btChangeName = findViewById(R.id.btChangeName);
        btBack = findViewById(R.id.btBack);

        SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userName = prefs.getString("userPhoneNum", null);
        String userId = prefs.getString("userId", null);
        if (userName != null) {
            etName.setText(userName);
        }

        btcleartext.setOnClickListener(v -> etName.setText(""));

        btChangeName.setOnClickListener(v -> {
            String newUserPhoneNum = etName.getText().toString();

            // AlertDialog를 만듭니다.
            new AlertDialog.Builder(this)
                    .setTitle("번호 변경")
                    .setMessage("번호를 " + newUserPhoneNum + "로 바꾸시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // '예'를 선택했을 때 할 일

                            // SharedPreferences에 userName 업데이트
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("userPhoneNum", newUserPhoneNum);
                            editor.apply();

                            // 서버에 userName 업데이트 (userId 기준)
                            updateUserPHOnServer(userId, newUserPhoneNum);

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("newUserPhoneNum", newUserPhoneNum);
                            setResult(RESULT_OK, resultIntent);

                            // 액티비티 종료
                            finish();
                        }
                    })
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // '아니요'를 선택했을 때 할 일, 여기서는 아무것도 하지 않습니다.
                        }
                    })
                    .show();
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void updateUserPHOnServer(String userId, String userPhoneNum) {
        Log.d(TAG, "번호변경: "+userId+userPhoneNum);
        OkHttpClient client = new OkHttpClient();
        String url = "https://sw--zqbli.run.goorm.site/updateUserPH";

        RequestBody body = new FormBody.Builder()
                .add("userId", userId)
                .add("userPhoneNum", userPhoneNum)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 에러 처리
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        // 성공적인 처리 (필요한 경우)
                    }
                } finally {
                    response.body().close(); // response body를 닫아줍니다.
                }
            }
        });
    }
}