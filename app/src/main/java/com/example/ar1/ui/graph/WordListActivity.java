package com.example.ar1.ui.graph;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WordListActivity extends AppCompatActivity {
    private Button btnBeginner, btnIntermediate, btnAdvanced;
    private ListView lvWordList;
    private ArrayAdapter<String> adapter;
    private List<String> wordsList;
    private TextToSpeech textToSpeech;
    private boolean isTtsSpeaking = false;
    private ImageView lastActiveSpeakerIcon; // 마지막으로 활성화된 스피커 아이콘 참조
    ImageButton btBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        String timeFrame = getIntent().getStringExtra("TIME_FRAME");

        // TTS 초기화
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });
        btBack = findViewById(R.id.btBack);
        btnBeginner = findViewById(R.id.btnBeginner);
        btnIntermediate = findViewById(R.id.btnIntermediate);
        btnAdvanced = findViewById(R.id.btnAdvanced);
        lvWordList = findViewById(R.id.lvWordList);

        wordsList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, R.layout.list_item_word, R.id.tvWord, wordsList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.tvWord);
                ImageView ivSpeaker = view.findViewById(R.id.ivSpeaker);
                String word = getItem(position).split(" - ")[0]; // 영어 단어 추출

                ivSpeaker.setOnClickListener(v -> {
                    if (textToSpeech != null && !textToSpeech.isSpeaking()) {
                        lastActiveSpeakerIcon = ivSpeaker;
                        ivSpeaker.setImageResource(R.drawable.ic_speaker_on);
                        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID");
                    }
                });

                return view;
            }
        };
        lvWordList.setAdapter(adapter);

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // TTS 시작
            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> {
                    if (lastActiveSpeakerIcon != null) {
                        lastActiveSpeakerIcon.setImageResource(R.drawable.ic_speaker_off);
                        lastActiveSpeakerIcon = null; // 참조 초기화
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                // 오류 처리
            }
        });

        SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        // 초기 화면 설정: 초급 단어 목록 로드
        fetchWords(userId, "5");

        btnBeginner.setOnClickListener(v -> fetchWords(userId, "5"));
        btnIntermediate.setOnClickListener(v -> fetchWords(userId, "10"));
        btnAdvanced.setOnClickListener(v -> fetchWords(userId, "15"));

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void fetchDataBasedOnTimeFrame(String timeFrame) {
        SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        switch (timeFrame) {
            case "today":
                fetchWords(userId, "today");
                break;
            case "week":
                fetchWords(userId, "week");
                break;
            case "month":
                fetchWords(userId, "month");
                break;
            default:
                fetchWords(userId, "today"); // 기본값 설정
                break;
        }
    }

    private void fetchWords(String userId, String missionCount) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://sw--zqbli.run.goorm.site/getWordsList"; // 서버의 API 엔드포인트 URL

        // 현재 날짜 가져오기
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        RequestBody formBody = new FormBody.Builder()
                .add("userId", userId)
                .add("date", currentDate)
                .add("missionName", "영단어 발음하기")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // 네트워크 오류 처리
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    Log.d(TAG, "오늘의 단어장: "+responseData);
                    runOnUiThread(() -> updateUI(responseData, missionCount));
                } else {
                    // 서버 응답 오류 처리
                }
            }
        });
    }

    private void updateUI(String data, String missionCount) {
        try {
            JSONArray jsonArray = new JSONArray(data);
            wordsList.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int count = jsonObject.getInt("missionCount");
                if (Integer.toString(count).equals(missionCount)) {
                    String words = jsonObject.getString("wordsList");
                    String[] wordsArray = words.split("\n");
                    Collections.addAll(wordsList, wordsArray);
                }
            }

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            // JSON 파싱 오류 처리
        }
    }
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
