package com.example.ar1.ui.graph;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import androidx.core.content.ContextCompat;

import com.example.ar1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SentencesListActivity extends AppCompatActivity {
    private Button btnBeginner, btnIntermediate, btnAdvanced;
    private ListView lvWordList;
    private ArrayAdapter<String> adapter;
    private List<String> wordsList;
    private TextToSpeech textToSpeech;
    private boolean isTtsSpeaking = false;
    private ImageView lastActiveSpeakerIcon; // 마지막으로 활성화된 스피커 아이콘 참조
    ImageButton btBack;
    TextView tvtitle;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentences_list);
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
        tvtitle = findViewById(R.id.tvtitle);
        runOnUiThread(() -> {
            if ("today".equals(timeFrame)) {
                tvtitle.setText("오늘의 영문장");
            } else if ("week".equals(timeFrame)) {
                tvtitle.setText("금주의 영문장");
            } else if ("month".equals(timeFrame)) {
                tvtitle.setText("금월의 영문장");
            }
        });
        wordsList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, R.layout.list_item_sentences, R.id.tvWord, wordsList) {
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

        // 난이도별 버튼 리스너 설정
        btnBeginner.setOnClickListener(v -> fetchWords(userId, "5", timeFrame));
        btnIntermediate.setOnClickListener(v -> fetchWords(userId, "10", timeFrame));
        btnAdvanced.setOnClickListener(v -> fetchWords(userId, "15", timeFrame));
        setupCategoryButtons(userId, timeFrame);
        fetchWords(userId, "5", timeFrame);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setupCategoryButtons(String userId, String timeFrame) {
        btnBeginner.setOnClickListener(v -> {
            fetchWords(userId, "5", timeFrame);
            highlightSelectedCategoryButton(btnBeginner);
        });
        btnIntermediate.setOnClickListener(v -> {
            fetchWords(userId, "10", timeFrame);
            highlightSelectedCategoryButton(btnIntermediate);
        });
        btnAdvanced.setOnClickListener(v -> {
            fetchWords(userId, "15", timeFrame);
            highlightSelectedCategoryButton(btnAdvanced);
        });

        // 초기 선택: 초급
        highlightSelectedCategoryButton(btnBeginner);
    }
    private void highlightSelectedCategoryButton(Button selectedButton) {
        // 각 버튼에 대한 스타일 적용
        btnBeginner.setBackground(ContextCompat.getDrawable(this, selectedButton == btnBeginner ? R.drawable.rounded_button_mint : R.drawable.default_button_style));
        btnIntermediate.setBackground(ContextCompat.getDrawable(this, selectedButton == btnIntermediate ? R.drawable.rounded_button_mint : R.drawable.default_button_style));
        btnAdvanced.setBackground(ContextCompat.getDrawable(this, selectedButton == btnAdvanced ? R.drawable.rounded_button_mint : R.drawable.default_button_style));
    }
    private void fetchWords(String userId, String missionCount, String timeFrame) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://sw--zqbli.run.goorm.site/getSentencesList";

        RequestBody formBody = new FormBody.Builder()
                .add("userId", userId)
                .add("missionCount", missionCount)
                .add("timeFrame", timeFrame)
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

                    // 전체 데이터를 리스트에 추가
                    Collections.addAll(wordsList, wordsArray);
                }
            }

            // 데이터가 없는 경우에 대한 처리
            if (wordsList.isEmpty()) {
                String noDataMessage = getNoDataMessage(missionCount);
                TextView tvNoDataMessage = findViewById(R.id.tvNoDataMessage);
                tvNoDataMessage.setText(noDataMessage);
                tvNoDataMessage.setVisibility(View.VISIBLE);
                lvWordList.setVisibility(View.GONE);
            } else {
                TextView tvNoDataMessage = findViewById(R.id.tvNoDataMessage);
                tvNoDataMessage.setVisibility(View.GONE);
                lvWordList.setVisibility(View.VISIBLE);
            }

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            // JSON 파싱 오류 처리
        }
    }

    private String getNoDataMessage(String missionCount) {
        switch (missionCount) {
            case "5":
                return "초급 영단어 학습내역이 없습니다.";
            case "10":
                return "중급 영단어 학습내역이 없습니다.";
            case "15":
                return "고급 영단어 학습내역이 없습니다.";
            default:
                return "";
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
