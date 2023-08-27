package com.example.ar1;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangeNameActivity extends AppCompatActivity {

    private EditText etName;
    private ImageButton btcleartext;
    private Button btChangeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        etName = findViewById(R.id.etName);
        btcleartext = findViewById(R.id.btcleartext);
        btChangeName = findViewById(R.id.btChangeName);

        SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userName = prefs.getString("userName", null);
        String userId = prefs.getString("userId", null);
        if (userName != null) {
            etName.setText(userName);
        }

        btcleartext.setOnClickListener(v -> etName.setText(""));

        btChangeName.setOnClickListener(v -> {
            String newUserName = etName.getText().toString();

            // SharedPreferences에 userName 업데이트
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userName", newUserName);
            editor.apply();

            // 서버에 userName 업데이트 (userId 기준)
            updateUserNameOnServer(userId, newUserName);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("newUserName", newUserName);
            setResult(RESULT_OK, resultIntent);
            // 액티비티 종료
            finish();
        });
    }

    private void updateUserNameOnServer(String userId, String userName) {
        Log.d(TAG, "이름변경: "+userId+userName);
        OkHttpClient client = new OkHttpClient();
        String url = "https://sw--zqbli.run.goorm.site/updateUserName";

        RequestBody body = new FormBody.Builder()
                .add("userId", userId)
                .add("userName", userName)
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