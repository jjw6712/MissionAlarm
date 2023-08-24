package com.example.ar1;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {

    private EditText userIdEditText, userPwEditText, userNameEditText, userPhoneNumEditText;
    ImageButton btBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Change status bar color to white
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.white));

        userIdEditText = findViewById(R.id.userId);
        userPwEditText = findViewById(R.id.userPw);
        userNameEditText = findViewById(R.id.userName);
        userPhoneNumEditText = findViewById(R.id.userPhoneNum);
        btBack = findViewById(R.id.btBack);

        Button signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = userIdEditText.getText().toString();
                String userPw = userPwEditText.getText().toString();
                String userName = userNameEditText.getText().toString();
                String userPhoneNum = userPhoneNumEditText.getText().toString();

                // Save user information in SharedPreferences
                SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("userId", userId);
                editor.putString("userPw", userPw);
                editor.putString("userName", userName);
                editor.putString("userPhoneNum", userPhoneNum);
                editor.apply();

                // AsyncTask를 사용하여 회원가입 요청을 백그라운드에서 수행
                new SignUpTask().execute(userId, userPw, userName, userPhoneNum);
            }
        });
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private class SignUpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String userId = params[0];
            String userPw = params[1];
            String userName = params[2];
            String userPhoneNum = params[3];
            int responseCode = 0;

            try {
                URL url = new URL("https://sw--zqbli.run.goorm.site/signup");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userId", userId);
                jsonParam.put("userPw", userPw);
                jsonParam.put("userName", userName);
                jsonParam.put("userPhoneNum", userPhoneNum);

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.close();

                responseCode = httpURLConnection.getResponseCode();

            } catch (Exception e) {
                Log.e("SignUpError", "Error:", e);
            }

            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == 201) {
                Toast.makeText(SignUpActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                finish(); // 회원가입 액티비티 종료
            } else {
                Toast.makeText(SignUpActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
            if(responseCode == 409){
                Toast.makeText(SignUpActivity.this, "이미 사용중이 아이디입니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}