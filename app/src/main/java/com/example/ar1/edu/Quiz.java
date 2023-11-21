package com.example.ar1.edu;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Quiz extends AppCompatActivity {

    OkHttpClient client;
    LinearLayout layoutEnglishWords, layoutKoreanWords;
    Button btnSelectDifficulty;
    @Nullable
    private Button lastSelectedButton = null;
    private boolean isCheckingAnswer = false;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private RelativeLayout loadingLayout;
    private TextToSpeech textToSpeech;
    TextView tvSelectedWord;
    private LinearLayout layoutCorrectAnswers;
    private String firstSelectedWord = "";
    private String secondSelectedWord = "";
    // 클래스 레벨 변수로 사용자가 선택한 난이도를 저장
    private String currentLevel = "";
    private static final String MY_SECRET_KEY = "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";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        layoutEnglishWords = findViewById(R.id.layoutEnglishWords);
        layoutKoreanWords = findViewById(R.id.layoutKoreanWords);
        btnSelectDifficulty = findViewById(R.id.btnSelectDifficulty);
        loadingLayout = findViewById(R.id.loadingLayout);
        tvSelectedWord = findViewById(R.id.tvSelectedWord);
        layoutCorrectAnswers = findViewById(R.id.layoutCorrectAnswers);

        // TextToSpeech 초기화
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        btnSelectDifficulty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDifficultyDialog();
            }
        });

        // 난이도 선택 다이얼로그를 처음에 표시
        showDifficultyDialog();
    }


    private void showDifficultyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("난이도 선택");
        String[] levels = {"초급 - Elementary", "중급 - Middle", "고급 - College"};
        builder.setItems(levels, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // 초급
                        fetchQuizWords("elementary school level");
                        break;
                    case 1: // 중급
                        fetchQuizWords("high school level");
                        break;
                    case 2: // 고급
                        fetchQuizWords("college level");
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void fetchQuizWords(String level) {
        currentLevel = level; // 사용자가 선택한 난이도 저장
        loadingLayout.setVisibility(View.VISIBLE); // 로딩 화면 표시
        String message = "Please provide 5 English words along with Korean for " + level + " students without additional explanation";
        callAPI(message);
    }

    void callAPI(String question){
        JSONArray arr = new JSONArray();
        JSONObject baseAi = new JSONObject();
        JSONObject userMsg = new JSONObject();
        try {
            baseAi.put("role", "user");
            baseAi.put("content", "Quizbot that provides various levels of English words with Korean Result");
            userMsg.put("role", "user");
            userMsg.put("content", question);
            arr.put(baseAi);
            arr.put(userMsg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JSONObject object = new JSONObject();
        try {
            object.put("model", "gpt-3.5-turbo");
            object.put("messages", arr);
        } catch (JSONException e){
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer "+MY_SECRET_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 오류 처리 로직
                        fetchQuizWords(currentLevel);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String result;
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                        Log.d(TAG, "onResponse: "+result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processQuizResponse(result.trim());
                            }
                        });
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 오류 처리 로직
                            fetchQuizWords(currentLevel);
                        }
                    });
                }
            }
        });
    }
    private Map<String, String> translationMap = new HashMap<>();

    private void processQuizResponse(String response) {
        loadingLayout.setVisibility(View.GONE); // 로딩 화면 숨김
        List<String> englishWords = new ArrayList<>();
        List<String> koreanWords = new ArrayList<>();

        String[] lines = response.split("\n");
        for (String line : lines) {
            // 줄이 유효한 단어 쌍을 포함하는지 확인
            if (line.matches("^\\d+\\. .*")) {
                String englishWord, koreanWord;

                // 긴 하이픈(" – ")으로 분할 시도
                String[] parts = line.split(" – ");
                if (parts.length == 2) {
                    // 새로운 형식 처리
                    englishWord = parts[0].replaceAll("^[0-9]+\\.", "").trim();
                    koreanWord = parts[1].split(" \\(")[0].trim(); // 괄호 앞까지만 선택
                } else {
                    // 기존 형식 처리('-' 기준으로 분할)
                    parts = line.split(" - ");
                    if (parts.length == 2) {
                        englishWord = parts[0].replaceAll("^[0-9]+\\.", "").trim();
                        koreanWord = parts[1].replaceAll("\\(.*?\\)", "").trim();
                    } else {
                        continue; // 형식이 맞지 않으면 다음 줄로 넘어감
                    }
                }

                englishWords.add(englishWord);
                koreanWords.add(koreanWord);
                translationMap.put(englishWord, koreanWord);
            }
        }

        // 유효한 단어 쌍이 충분한지 확인
        if (englishWords.size() < 5 || koreanWords.size() < 5) {
            // 유효한 단어 쌍이 충분하지 않을 경우, fetchQuizWords 함수를 다시 호출
            fetchQuizWords(currentLevel);
            return;
        }

        // 추출한 단어로 버튼 생성 및 표시
        displayWordsAsButtons(englishWords, koreanWords);
    }

    void displayWordsAsButtons(List<String> englishWords, List<String> koreanWords) {
        Collections.shuffle(englishWords);
        Collections.shuffle(koreanWords);

        // 영어 단어 버튼 추가
        for (String word : englishWords) {
            Button btn = createWordButton(word, "English");
            layoutEnglishWords.addView(btn);
        }

        // 한글 단어 버튼 추가
        for (String word : koreanWords) {
            Button btn = createWordButton(word, "Korean");
            layoutKoreanWords.addView(btn);
        }
    }

    private String findEnglishWordForKorean(String koreanWord, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(koreanWord)) {
                return entry.getKey();
            }
        }
        return null; // 적절한 영어 단어가 없는 경우
    }

    private Button createWordButton(String word, String languageType) {
        Button btn = new Button(this);
        btn.setText(word);
        btn.setOnClickListener(view -> {
            if (languageType.equals("English")) {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            }
            onWordButtonClicked(btn, word);
        });
        return btn;
    }

    private void onWordButtonClicked(Button btn, String selectedWord) {
        if (isCheckingAnswer) {
            return; // 이미 처리 중인 경우 무시
        }

        if (lastSelectedButton == null) {
            // 첫 번째로 선택된 단어 처리
            firstSelectedWord = selectedWord;
            lastSelectedButton = btn;
            lastSelectedButton.setEnabled(false); // 비활성화
            updateSelectedWordText();
        } else {
            // 두 번째로 선택된 단어 처리
            secondSelectedWord = selectedWord;
            updateSelectedWordText();
            checkAnswer(lastSelectedButton, btn);
            lastSelectedButton = null;
        }
    }

    private void updateSelectedWordText() {
        String textToShow = (firstSelectedWord.isEmpty() ? "" : firstSelectedWord + " - ") + secondSelectedWord;
        tvSelectedWord.setText(textToShow);
        tvSelectedWord.setVisibility(View.VISIBLE);
    }
    private void checkAnswer(Button firstBtn, Button secondBtn) {
        String firstBtnText = firstBtn.getText().toString();
        String secondBtnText = secondBtn.getText().toString();

        String firstBtnTranslation = translationMap.get(firstBtnText);
        String secondBtnTranslation = translationMap.get(secondBtnText);

        boolean isFirstBtnCorrect = firstBtnTranslation != null && firstBtnTranslation.equals(secondBtnText);
        boolean isSecondBtnCorrect = secondBtnTranslation != null && secondBtnTranslation.equals(firstBtnText);

        if (isFirstBtnCorrect || isSecondBtnCorrect) {
            // 정답인 경우
            MediaPlayer correctSound = MediaPlayer.create(this, R.raw.currect);
            correctSound.start();
            correctSound.setOnCompletionListener(MediaPlayer::release);
            changeButtonColor(firstBtn, true);
            changeButtonColor(secondBtn, true);
            tvSelectedWord.setTextColor(Color.BLUE);
            tvSelectedWord.animate()
                    .alpha(0.0f)
                    .setDuration(2000)
                    .withEndAction(() -> {
                        resetSelectedWords();
                        tvSelectedWord.setVisibility(View.GONE);
                        tvSelectedWord.setAlpha(1.0f); // 텍스트의 투명도를 다시 원래대로 설정
                        tvSelectedWord.setTextColor(Color.BLACK);
                        updateSelectedWordText(); // 상태 초기화 후 텍스트 업데이트
                    })
                    .start();
            firstBtn.setEnabled(false);
            secondBtn.setEnabled(false);
            isCheckingAnswer = false;
            addNewAnswerText(tvSelectedWord.getText().toString());
            resetSelectedWords();
            if(checkAllWordsMatched()) finish();
        } else {
            // 오답인 경우
            processIncorrectAnswer(firstBtn, secondBtn);
        }
        lastSelectedButton = null;
    }

    private void processIncorrectAnswer(Button firstBtn, Button secondBtn) {
        MediaPlayer incorrectSound = MediaPlayer.create(this, R.raw.wrong);
        incorrectSound.start();
        incorrectSound.setOnCompletionListener(MediaPlayer::release);
        // 진동 발생
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            // API 26 이상에서는 VibrationEffect를 사용
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // 이전 버전에서는 기본 진동 메소드 사용
                vibrator.vibrate(500); // 500ms 동안 진동
            }
        }
        // 오답 애니메이션
        tvSelectedWord.setTextColor(Color.RED);
        tvSelectedWord.animate()
                .alpha(0.0f)
                .setDuration(2000)
                .withEndAction(() -> {
                    resetSelectedWords();
                    tvSelectedWord.setVisibility(View.GONE);
                    tvSelectedWord.setAlpha(1.0f); // 텍스트의 투명도를 다시 원래대로 설정
                    tvSelectedWord.setTextColor(Color.BLACK);
                    updateSelectedWordText(); // 상태 초기화 후 텍스트 업데이트
                })
                .start();
        changeButtonColor(firstBtn, false);
        changeButtonColor(secondBtn, false);
        new Handler().postDelayed(() -> {
            resetButtonColor(firstBtn);
            resetButtonColor(secondBtn);
            firstBtn.setEnabled(true);
            secondBtn.setEnabled(true);
            isCheckingAnswer = false;
        }, 2000);
    }
    private void resetSelectedWords() {
        firstSelectedWord = "";
        secondSelectedWord = "";
    }
    private void changeButtonColor(Button btn, boolean isCorrect) {
        btn.setBackgroundColor(isCorrect ? Color.BLUE : Color.RED);
    }

    private void resetButtonColor(Button btn) {
        btn.setBackgroundColor(Color.WHITE); // 또는 기본 색상으로 변경
    }
    private boolean checkAllWordsMatched() {
        for (int i = 0; i < layoutEnglishWords.getChildCount(); i++) {
            Button btn = (Button) layoutEnglishWords.getChildAt(i);
            if (btn.isEnabled()) {
                return false;
            }
        }
        for (int i = 0; i < layoutKoreanWords.getChildCount(); i++) {
            Button btn = (Button) layoutKoreanWords.getChildAt(i);
            if (btn.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    private void addNewAnswerText(String answerText) {
        TextView newAnswerTextView = new TextView(this);
        newAnswerTextView.setText(answerText);
        newAnswerTextView.setTextColor(Color.BLUE);
        newAnswerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        // 추가적인 스타일 설정
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL; // 수평 중앙 정렬

        int topMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics()); // dp를 픽셀로 변환
        int bottomMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()); // dp를 픽셀로 변환
        layoutParams.setMargins(0, topMargin, 0, bottomMargin); // 상하 마진 설정

        newAnswerTextView.setLayoutParams(layoutParams);

        layoutCorrectAnswers.addView(newAnswerTextView);
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