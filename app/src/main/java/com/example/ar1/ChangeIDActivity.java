package com.example.ar1;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangeIDActivity extends AppCompatActivity {
    private EditText etName;
    private ImageButton btcleartext, btBack;
    private Button btChangeName;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_id);

        etName = findViewById(R.id.etName);
        btcleartext = findViewById(R.id.btcleartext);
        btChangeName = findViewById(R.id.btChangeName);
        btBack = findViewById(R.id.btBack);

        SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userName = prefs.getString("userName", null);
        String userId = prefs.getString("userId", null);
        if (userName != null) {
            etName.setText(userId);
        }

        btcleartext.setOnClickListener(v -> etName.setText(""));

        btChangeName.setOnClickListener(v -> {
            String newUserId = etName.getText().toString();

            // AlertDialog를 만듭니다.
            new AlertDialog.Builder(this)
                    .setTitle("아이디 변경")
                    .setMessage("아이디를 " + newUserId + "로 바꾸시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // '예'를 선택했을 때 할 일
                            // SharedPreferences에 userName 업데이트
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("userId", newUserId);
                            editor.apply();

                            // 서버에 userName 업데이트 (userId 기준)
                            updateUserIdOnServer(userId, newUserId);

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("newUserId", newUserId);
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

    private void updateUserIdOnServer(String userId, String newUserId) {
        Log.d(TAG, "아이디변경: "+userId+newUserId);
        OkHttpClient client = new OkHttpClient();
        String url = "https://sw--zqbli.run.goorm.site/updateUserId";

        RequestBody body = new FormBody.Builder()
                .add("oldUserId", userId)
                .add("newUserId", newUserId)
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