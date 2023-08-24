package com.example.ar1;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

        // Change status bar color to white
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.white));

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

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(Login.this, SignUpActivity.class);
                startActivity(in);
            }
        });
    }
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private class LoginTask extends AsyncTask<String, Void, Pair<Integer, String>> {

        @Override
        protected Pair<Integer, String> doInBackground(String... params) {
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

                if (responseCode == 200) {
                    String userName = getUserNameFromServer(username);
                    return new Pair<>(responseCode, userName);
                }

            } catch (Exception e) {
                Log.e("LoginError", "Error:", e);
            }

            return new Pair<>(responseCode, null);
        }

        @Override
        protected void onPostExecute(Pair<Integer, String> result) {
            Integer responseCode = result.first;
            String userName = result.second;

            if (responseCode == 200 && userName != null) {
                String userId = usernameEditText.getText().toString();
                SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("is_logged_in", true);
                editor.putString("userId", userId);
                editor.putString("userName", userName); // 사용자 이름 저장
                editor.apply();

                Toast.makeText(Login.this, "로그인이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                // 홈으로 이동
                Intent intent = new Intent(Login.this, BottomActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Login.this, "ID 또는 PW를 확인하세요", Toast.LENGTH_SHORT).show();
            }
        }

        // 서버로부터 사용자 이름을 가져오는 함수
        private String getUserNameFromServer(String userId) {
            try {
                // 여기서 사용자 이름 조회 API를 호출
                // 해당 API의 엔드포인트 URL을 사용하세요
                String apiUrl = "https://sw--zqbli.run.goorm.site/getUserName?userId=" + userId;
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getString("userName"); // userName 반환
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null; // 에러 발생 시 null 반환
        }
    }
}