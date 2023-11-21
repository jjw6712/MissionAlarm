package com.example.ar1.edu;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ar1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

public class SpeachSentences extends AppCompatActivity {
    private boolean isSttListening = false;
    private boolean isTtsSpeaking = false;
    final int PERMISSION = 1;
    SpeechRecognizer mRecognizer;
    Intent intent;
    OkHttpClient client;
    LinearLayout layoutEnglishWords, layoutKoreanWords;
    ImageButton btMic;
    @Nullable
    private Button lastSelectedButton = null;
    private boolean isCheckingAnswer = false;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private RelativeLayout loadingLayout;
    private TextToSpeech textToSpeech;
    TextView tvSelectedWord;
    private TextView tvWordPair, tvUserSpeech;
    private List<String> englishWords, koreanWords;
    private int currentWordIndex = 0;
    private LinearLayout layoutCorrectAnswers;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 100; // 음성 인식 요청 코드 정의
    private String firstSelectedWord = "";
    private String secondSelectedWord = "";
    // 클래스 레벨 변수로 사용자가 선택한 난이도를 저장
    private String currentLevel = "";
    ImageButton btnSpeak;
    private static final String MY_SECRET_KEY = "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";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speach_words);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        if ( Build.VERSION.SDK_INT >= 23 ){
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }
        englishWords = new ArrayList<>();
        koreanWords = new ArrayList<>();
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        layoutEnglishWords = findViewById(R.id.layoutEnglishWords);
        layoutKoreanWords = findViewById(R.id.layoutKoreanWords);
        loadingLayout = findViewById(R.id.loadingLayout);
        tvSelectedWord = findViewById(R.id.tvSelectedWord);
        layoutCorrectAnswers = findViewById(R.id.layoutCorrectAnswers);
        tvWordPair = findViewById(R.id.tvWordPair);
        tvUserSpeech = findViewById(R.id.tvUserSpeech);
        btMic = findViewById(R.id.btnMic);
        btnSpeak = findViewById(R.id.btnSpeak);
        // TextToSpeech 초기화
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                isTtsSpeaking = true;
                updateTtsButtonImage(); // TTS 버튼 이미지 업데이트
            }

            @Override
            public void onDone(String utteranceId) {
                isTtsSpeaking = false;
                updateTtsButtonImage(); // TTS 버튼 이미지 업데이트
            }

            @Override
            public void onError(String utteranceId) {
                isTtsSpeaking = false;
                updateTtsButtonImage(); // TTS 버튼 이미지 업데이트
            }
        });

        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");

        // 난이도 선택 다이얼로그를 처음에 표시
        showDifficultyDialog();
        btMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTtsSpeaking) {
                    mRecognizer = SpeechRecognizer.createSpeechRecognizer(SpeachSentences.this);
                    mRecognizer.setRecognitionListener(listener);
                    mRecognizer.startListening(intent);
                    mRecognizer.startListening(intent);
                    isSttListening = true; // STT가 시작되면 상태 변경
                    updateSttButtonImage(); // STT 버튼 이미지 업데이트
                    // STT 버튼 비활성화
                    btnSpeak.setEnabled(false);
                    // 2초 후에 STT 버튼 다시 활성화
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btnSpeak.setEnabled(true);
                        }
                    }, 2000);

                } else {
                    Toast.makeText(SpeachSentences.this, "듣기가 끝난 후에 시도하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void stopSttSession() {
        if (mRecognizer != null) {
            mRecognizer.stopListening();
            isSttListening = false;
        }
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
        String message = "Please provide 5 English sentences along with Korean for " + level + " students without additional explanation ";
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

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            isSttListening = true;
            Toast.makeText(getApplicationContext(),"영어 문장의 발음을 정확하게 말하세요.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {isSttListening = false;updateSttButtonImage();}

        @Override
        public void onError(int error) {
            String message;
            isSttListening = false;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "다시 한번 말하세요";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "영어 문장의 발음을 정확하게 말하세요.";
                    break;
            }

            Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String recognizedText = matches.get(0);
                tvUserSpeech.setText(recognizedText);

                if (currentWordIndex < englishWords.size() && recognizedText.equalsIgnoreCase(englishWords.get(currentWordIndex))) {
                    Log.d(TAG, "currentWordIndex: "+ currentWordIndex);
                    Log.d(TAG, "englishWords: "+ englishWords);
                    // 정답인 경우
                    processCorrectAnswer();
                } else {
                    // 오답인 경우
                    processIncorrectAnswer();
                    Log.d(TAG, "currentWordIndex: "+ currentWordIndex);
                    Log.d(TAG, "englishWords: "+ englishWords);
                    Log.d(TAG, "matches: "+ matches);
                }
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };
    private void processCorrectAnswer() {
        playSoundCorrect();
        changeTextTemporarily(tvUserSpeech, Color.BLUE, 2000, new Runnable() {
            @Override
            public void run() {
                // 애니메이션이 끝나고 나면 텍스트 초기화
                tvUserSpeech.setText("");

                currentWordIndex++;
                if (currentWordIndex < englishWords.size()) {
                    displayWordPair(englishWords.get(currentWordIndex), koreanWords.get(currentWordIndex));
                } else {
                    // 모든 단어 표시 완료 - 결과 표시
                    showQuizComplete();
                }
            }
        });
    }

    private void processIncorrectAnswer() {
        vibrateAndPlayIncorrectSound();
        changeTextTemporarily(tvUserSpeech, Color.RED, 2000, new Runnable() {
            @Override
            public void run() {
                // 오답 애니메이션 후 텍스트 지우기
                tvUserSpeech.setText("");
            }
        });
    }

    private void vibrateAndPlayIncorrectSound() {
        MediaPlayer incorrectSound = MediaPlayer.create(this, R.raw.wrong);
        incorrectSound.start();
        incorrectSound.setOnCompletionListener(MediaPlayer::release);

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }
    }

    private void playSoundCorrect() {
        MediaPlayer correctSound = MediaPlayer.create(this, R.raw.currect);
        correctSound.start();
        correctSound.setOnCompletionListener(MediaPlayer::release);
    }

    private void changeTextTemporarily(TextView textView, int color, long duration, Runnable endAction) {
        final int originalColor = textView.getCurrentTextColor();
        textView.setTextColor(color);

        new Handler().postDelayed(() -> {
            textView.setTextColor(originalColor);
            if (endAction != null) {
                endAction.run();
            }
        }, duration);
    }

    private void showQuizComplete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("발음하기 완료");

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sentences_list, null);
        builder.setView(dialogView);

        TextView tvWordsList = dialogView.findViewById(R.id.tvWordsList);
        StringBuilder wordsListBuilder = new StringBuilder();
        for (int i = 0; i < englishWords.size(); i++) {
            wordsListBuilder.append(englishWords.get(i)).append(" - ").append(koreanWords.get(i)).append("\n");
        }
        tvWordsList.setText(wordsListBuilder.toString());

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

    // 퀴즈를 리셋하는 메소드 (옵션)
    private void resetQuiz() {
        currentWordIndex = 0;
        if (!englishWords.isEmpty() && !koreanWords.isEmpty()) {
            displayWordPair(englishWords.get(0), koreanWords.get(0));
        }
    }

    private Map<String, String> translationMap = new HashMap<>();

    private void processQuizResponse(String response) {
        loadingLayout.setVisibility(View.GONE); // 로딩 화면 숨김

        String[] lines = response.split("\n");
        for (String line : lines) {
            // 숫자와 점 제거
            String cleanedLine = line.replaceFirst("^\\d+\\.\\s*", "");

            String englishWord = "", koreanWord = "";

            // 레이블 형식 처리
            if (cleanedLine.contains("English:") && cleanedLine.contains("Korean:")) {
                String[] parts = cleanedLine.split("Korean:");
                englishWord = parts[0].replace("English:", "").trim();
                koreanWord = parts[1].trim();
            }
            // 괄호 형식 처리
            else if (cleanedLine.contains("(")) {
                String[] parts = cleanedLine.split("\\(");
                englishWord = parts[0].trim();
                koreanWord = parts[1].replace(")", "").trim();
            }
            // 하이픈 형식 처리
            else if (cleanedLine.contains(" - ")) {
                String[] parts = cleanedLine.split(" - ");
                englishWord = parts[0].trim();
                koreanWord = parts[1].trim();
            }

            // 문장 끝의 마침표 제거
            englishWord = englishWord.replaceAll("\\.$", "");
            koreanWord = koreanWord.replaceAll("\\.$", "");

            if (!englishWord.isEmpty() && !koreanWord.isEmpty()) {
                englishWords.add(englishWord);
                koreanWords.add(koreanWord);
                translationMap.put(englishWord, koreanWord);
            }
        }

        if (englishWords.size() >= 5 && koreanWords.size() >= 5) {
            displayWordPair(englishWords.get(0), koreanWords.get(0));
        } else {
            fetchQuizWords(currentLevel); // 유효한 단어 쌍이 충분하지 않으면 다시 호출
        }
    }

    private void displayWordPair(String englishWord, String koreanWord) {
        TextView tvWordPair = findViewById(R.id.tvWordPair);
        tvWordPair.setText(englishWord + " - " + koreanWord);

        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId");
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // STT 세션이 활성화된 경우 중지
                if (isSttListening) {
                    stopSttSession();
                }

                // TTS 기능 실행
                if (textToSpeech != null) {
                    isTtsSpeaking = true; // TTS가 시작되면 상태 변경
                    textToSpeech.speak(englishWord, TextToSpeech.QUEUE_FLUSH, null, "utteranceId");
                }
            }
        });
        Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 애니메이션 시작 시 필요한 작업
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 현재 단어 쌍 제거 후 새 단어 쌍 표시
                tvWordPair.setText(englishWord + " - " + koreanWord);
                tvWordPair.startAnimation(slideInRight);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 반복 애니메이션에 필요한 작업 (해당 없음)
            }
        });

        // 현재 단어 쌍에 애니메이션 적용
        tvWordPair.startAnimation(slideOutLeft);
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
    // STT 버튼 이미지 업데이트를 위한 메소드
    private void updateSttButtonImage() {
        if (isSttListening) {
            btMic.setImageResource(R.drawable.ic_mic_on); // 활성화 이미지로 변경
        } else {
            btMic.setImageResource(R.drawable.ic_mic_off); // 비활성화 이미지로 변경
        }
    }

    // TTS 버튼 이미지 업데이트를 위한 메소드
    private void updateTtsButtonImage() {
        if (isTtsSpeaking) {
            btnSpeak.setImageResource(R.drawable.ic_speaker_on); // 활성화 이미지로 변경
        } else {
            btnSpeak.setImageResource(R.drawable.ic_speaker_off); // 비활성화 이미지로 변경
        }
    }
}
