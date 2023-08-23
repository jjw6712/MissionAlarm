package com.example.ar1;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        if (allPermissionsGranted()) {

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }
        // 로그인 상태 확인
        SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            // 로그인된 상태라면 바로 BottomActivity로 이동
            Intent intent = new Intent(Login.this, BottomActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        Button signUpButton = findViewById(R.id.SignUpButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // AsyncTask를 사용하여 네트워크 작업을 백그라운드에서 수행
                new LoginTask().execute(username, password);
            }
        });
    }
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private class LoginTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            int responseCode = 0;

            try {
                URL url = new URL("https://sw--zqbli.run.goorm.site/login");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("userId", username);
                jsonParam.put("userPw", password);

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.close();

                responseCode = httpURLConnection.getResponseCode();

            } catch (Exception e) {
                Log.e("LoginError", "Error:", e);
            }

            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == 200) {
                Toast.makeText(Login.this, "로그인이 완료 되었습니다.", Toast.LENGTH_SHORT).show();

                // 로그인 상태 및 사용자 ID 저장
                SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("is_logged_in", true);
                editor.putString("userId", usernameEditText.getText().toString());
                editor.apply();

                // 홈으로 이동
                Intent intent = new Intent(Login.this, BottomActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Login.this, "ID 또는 PW를 확인하세요", Toast.LENGTH_SHORT).show();
            }
        }
    }
}