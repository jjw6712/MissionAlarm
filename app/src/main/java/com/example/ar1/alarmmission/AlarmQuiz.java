package com.example.ar1.alarmmission;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ar1.R;
import com.example.ar1.ui.alarm.AlarmDBHelper;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlarmQuiz extends AppCompatActivity {
    Intent intent;
    OkHttpClient client;
    LinearLayout layoutEnglishWords, layoutKoreanWords;
    @Nullable
    private Button lastSelectedButton = null;
    private boolean isCheckingAnswer = false;
    private RelativeLayout loadingLayout;
    private TextToSpeech textToSpeech;
    TextView tvSelectedWord, tvlevel;
    List<String> englishWords = new ArrayList<>();
    List<String> koreanWords = new ArrayList<>();
    private LinearLayout layoutCorrectAnswers;
    private String firstSelectedWord = "";
    private String secondSelectedWord = "";
    // 클래스 레벨 변수로 사용자가 선택한 난이도를 저장
    private String currentLevel = "";

    private int alarmId;

    String selectedLevel;
    boolean isPowerModeEnabled;
    boolean isQuizEnd;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        isQuizEnd = false;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        englishWords = new ArrayList<>();
        koreanWords = new ArrayList<>();
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        tvlevel = findViewById(R.id.tvlevel);
        layoutEnglishWords = findViewById(R.id.layoutEnglishWords);
        layoutKoreanWords = findViewById(R.id.layoutKoreanWords);
        loadingLayout = findViewById(R.id.loadingLayout);
        tvSelectedWord = findViewById(R.id.tvSelectedWord);
        layoutCorrectAnswers = findViewById(R.id.layoutCorrectAnswers);

        // TextToSpeech 초기화
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        // 선택한 레벨을 받아옴
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        alarmId = getIntent().getIntExtra("alarm_id", -1); // 알람 ID 받아오기
        selectedLevel = sharedPreferences.getString("selected_stretching_count_" + alarmId, "default");
        isPowerModeEnabled = sharedPreferences.getBoolean("PowerMode", false);
            // 선택한 레벨에 따라 원하는 동작 수행
            if (selectedLevel != null) {
                switch (selectedLevel) {
                    case "elementary":
                        // 초급 레벨에 대한 처리
                        fetchQuizWords("elementary");
                        tvlevel.setText("난이도: 초급");
                        break;
                    case "middle":
                        // 중급 레벨에 대한 처리
                        fetchQuizWords("middle");
                        tvlevel.setText("난이도: 중급");
                        break;
                    case "college":
                        fetchQuizWords("college");
                        tvlevel.setText("난이도: 고급");
                        break;
                    default:
                        // 예외 처리
                        break;
                }
            }
        }


    void fetchQuizWords(String level) {
        currentLevel = level; // 사용자가 선택한 난이도 저장
        loadingLayout.setVisibility(View.VISIBLE); // 로딩 화면 표시
        getWordsFromServer(currentLevel);
    }

    private void getWordsFromServer(String level) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://sw--zqbli.run.goorm.site/getWordstospeachwords/" + level) // 서버의 엔드포인트 URL
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                // 오류 처리 로직
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "클라우드 리스폰스: "+responseData.trim());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            processQuizResponse(responseData.trim());
                        }
                    });
                } else {
                    // 오류 처리 로직
                }
            }
        });
    }
    private Map<String, String> translationMap = new HashMap<>();

    private void processQuizResponse(String response) {
        loadingLayout.setVisibility(View.GONE); // 로딩 화면 숨김
        //List<String> englishWords = new ArrayList<>();
        //List<String> koreanWords = new ArrayList<>();

        // 쉼표로 분리하기 전에 배열 표시를 제거합니다.
        response = response.replaceAll("^\\[|\\]$", "");

        String[] wordPairs = response.split("\",\"");
        for (String wordPair : wordPairs) {
            // 따옴표 제거
            wordPair = wordPair.trim().replaceAll("^\"|\"$", "");

            // 정규 표현식 수정: "영어 - 한글", "영어 (한글)", "영어: 한글" 형식을 모두 처리
            Matcher matcher = Pattern.compile("^(\\d+\\.)?\\s*([A-Za-z\\s]+)(?:\\s*-|\\s*\\(|\\s*:)\\s*([가-힣]+)").matcher(wordPair);
            if (matcher.find()) {
                String englishWord = matcher.group(2).trim();
                String koreanWord = matcher.group(3).trim();
                translationMap.put(englishWord, koreanWord);
                englishWords.add(englishWord);
                koreanWords.add(koreanWord);
            }
        }

        if (!englishWords.isEmpty() && !koreanWords.isEmpty()) {
            displayWordsAsButtons(englishWords, koreanWords);
        } else {
            // 오류 처리
        }
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

    private Button createWordButton(String word, String languageType) {
        Button btn = new Button(this);
        btn.setText(word);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // 텍스트 크기 설정
        btn.setTextColor(Color.WHITE); // 텍스트 색상 설정
        btn.setTypeface(null, Typeface.BOLD); // 굵은 텍스트 설정
        btn.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_button)); // 둥근 모서리 버튼 배경 설정

        // 버튼의 레이아웃 파라미터 설정
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // 버튼의 너비: 매치_부모
                LinearLayout.LayoutParams.WRAP_CONTENT  // 버튼의 높이: 내용에 맞춤
        );

        // 버튼 위아래 마진 설정
        int verticalMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()
        );
        layoutParams.setMargins(0, verticalMargin, 0, verticalMargin);
        btn.setLayoutParams(layoutParams);

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
                    .setDuration(1000)
                    .withEndAction(() -> {
                        resetSelectedWords();
                        tvSelectedWord.setVisibility(View.GONE);
                        tvSelectedWord.setAlpha(1.0f); // 텍스트의 투명도를 다시 원래대로 설정
                        tvSelectedWord.setTextColor(Color.WHITE);
                        updateSelectedWordText(); // 상태 초기화 후 텍스트 업데이트
                    })
                    .start();
            firstBtn.setEnabled(false);
            secondBtn.setEnabled(false);
            isCheckingAnswer = false;
            addNewAnswerText(tvSelectedWord.getText().toString());
            resetSelectedWords();
            if(checkAllWordsMatched()) showQuizComplete();
        } else {
            // 오답인 경우
            processIncorrectAnswer(firstBtn, secondBtn);
        }
        lastSelectedButton = null;
    }
    private void showQuizComplete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("퀴즈퍼즐 완료");
        isQuizEnd = true;
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_words_list, null);
        builder.setView(dialogView);

        TextView tvWordsList = dialogView.findViewById(R.id.tvWordsList);
        StringBuilder wordsListBuilder = new StringBuilder();
        for (int i = 0; i < englishWords.size(); i++) {
            wordsListBuilder.append(englishWords.get(i)).append(" - ").append(koreanWords.get(i)).append("\n");
        }
        tvWordsList.setText(wordsListBuilder.toString());
        Intent getintent = getIntent();
        if (getintent != null) {

            // 선택한 레벨에 따라 원하는 동작 수행
            if (selectedLevel != null) {
                switch (selectedLevel) {
                    case "elementary":
                        // 초급 레벨에 대한 처리
                        sendEnglishwordscore("5",wordsListBuilder.toString());
                        break;
                    case "middle":
                        // 중급 레벨에 대한 처리
                        sendEnglishwordscore("10", wordsListBuilder.toString());
                        break;
                    case "college":
                        sendEnglishwordscore("15", wordsListBuilder.toString());
                        break;
                    default:
                        // 예외 처리
                        break;
                }
            }
        }
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 확인 버튼 클릭시 처리
                finish(); // 혹은 다른 활동으로 이동
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void sendEnglishwordscore(String score,  String wordsList) { // 서버로 데이터 보내기
        OkHttpClient client = new OkHttpClient();
        SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        String userId = preferences.getString("userId", ""); //로그인한 유저 id 가져오기

        // 현재 날짜 가져오기
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        String currentDate = sdfDate.format(calendar.getTime());

        // AlarmDBHelper 인스턴스 생성
        AlarmDBHelper dbHelper = new AlarmDBHelper(this); //sqlite 로컬db 객체 초기화


// 현재 날짜와 시간을 구합니다.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        HashMap<String, String> data = new HashMap<>(); // 해쉬맵으로 데이터를 구성하기 위한 해쉬맵 객체 초기화
        data.put("userId", userId); // 로그인한 사용자 ID 삽입
        data.put("alarmId", String.valueOf(alarmId)); // 항상 null로 설정
        data.put("alarmTime", currentDateAndTime); // 현재 날짜와 시간 삽입
        data.put("missionName", "영단어 퀴즈퍼즐"); // 해당 알람 미션 종류 삽입
        data.put("missionCount", score); // 해당 알람 미션 완료 갯수 삽입
        data.put("wordsList", wordsList);
        Log.d(TAG, ("userId: " + userId + "\nalarmId: " + alarmId + "\nalarmTime: " + currentDateAndTime + "\nmissionMode: " + "영단어 발음하기" + "\nmissionCount: " + score+ "\nwordList"+wordsList));

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject(data);

        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        String url = "https://sw--zqbli.run.goorm.site/mission"; //앤드포인드  mission으로 리퀘스트 보냄
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
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
                .setDuration(1000)
                .withEndAction(() -> {
                    resetSelectedWords();
                    tvSelectedWord.setVisibility(View.GONE);
                    tvSelectedWord.setAlpha(1.0f); // 텍스트의 투명도를 다시 원래대로 설정
                    tvSelectedWord.setTextColor(Color.WHITE);
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
        }, 1000);
    }
    private void resetSelectedWords() {
        firstSelectedWord = "";
        secondSelectedWord = "";
    }
    private void changeButtonColor(Button btn, boolean isCorrect) {
        btn.setBackground(ContextCompat.getDrawable(this, isCorrect ? R.drawable.rounded_button_blue : R.drawable.rounded_button_red));
    }

    private void resetButtonColor(Button btn) {
        btn.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_button)); // 또는 기본 색상으로 변경
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
    @Override
    public void onBackPressed() {
        if (!isPowerModeEnabled) {
            super.onBackPressed(); // 파워 모드가 비활성화되어 있으면 기본 뒤로 가기 기능 수행
        } else {
            // 파워 모드가 활성화되어 있으면 토스트 메시지 표시
            Toast.makeText(this, "뒤로 가기 버튼이 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isQuizEnd) return;
        finish();
        if (isPowerModeEnabled) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }
}