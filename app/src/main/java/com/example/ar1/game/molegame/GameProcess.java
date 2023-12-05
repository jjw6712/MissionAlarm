package com.example.ar1.game.molegame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameProcess extends AppCompatActivity {

    private ImageView[] moleImageViews;
    private TextView scoreTextView;
    private int score = 0;
    private int moleCount = 0;
    private int currentIndex = 0;
    private boolean isMoleHurt = false;

    private Handler handler;

    {
        handler = new Handler();
    }

    private int[] moleImageIds = {
            com.example.ar1.R.drawable.mole1,
            R.drawable.mole2,
            R.drawable.mole3,
            R.drawable.mole4,
            R.drawable.mole5,
            R.drawable.mole6,
            R.drawable.mole7,
            R.drawable.mole8,
            R.drawable.mole9
    };
    private String difficulty; // 사용자가 선택한 난이도를 저장하는 변수
    private final int BEGINNER_SCORE_LIMIT = 1000;
    private final int INTERMEDIATE_SCORE_LIMIT = 1500;
    private final int ADVANCED_SCORE_LIMIT = 2000;
    private MediaPlayer upSound;
    private MediaPlayer dieSound;
    private MediaPlayer hitSound;
    private MediaPlayer backgroundMusic;
    private Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_process);

        // 상태바 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        // 배경음악 로드 및 재생
        backgroundMusic = MediaPlayer.create(this, R.raw.gamebgm);
        backgroundMusic.setLooping(true); // 음악 반복 설정
        backgroundMusic.start();
        // 사운드 로드
        upSound = MediaPlayer.create(this, R.raw.up);
        dieSound = MediaPlayer.create(this, R.raw.die);
        hitSound = MediaPlayer.create(this, R.raw.hit);
        // Vibrator 서비스 초기화
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        moleImageViews = new ImageView[]{
                findViewById(R.id.moleImageView1),
                findViewById(R.id.moleImageView2),
                findViewById(R.id.moleImageView3),
                findViewById(R.id.moleImageView4),
                findViewById(R.id.moleImageView5),
                findViewById(R.id.moleImageView6),
                findViewById(R.id.moleImageView7),
                findViewById(R.id.moleImageView8),
                findViewById(R.id.moleImageView9)
        };

        // 난이도 받아오기
        difficulty = getIntent().getStringExtra("difficulty");

        scoreTextView = findViewById(R.id.scoreTextView);

        // 초기에는 두더지가 나오기 전까지의 대기 시간을 설정
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulateMoleHiding();
            }
        }, getRandomDelay());

        // 각 두더지 이미지 뷰에 대한 터치 리스너 설정
        for (final ImageView moleImageView : moleImageViews) {
            moleImageView.setOnTouchListener(new View.OnTouchListener() {
                private boolean isMoleHurt = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && !isMoleHurt) {
                        moleImageView.setImageResource(R.drawable.mole_hurt);
                        moleImageView.setClickable(false);
                        isMoleHurt = true;
                        // 진동 실행
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(100); // API 26 미만에서의 진동 실행
                        }

                        // 터치 시 사운드 재생
                        playSound(hitSound);

                        score += 100;
                        updateScore();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideMoleImage();
                                isMoleHurt = false;
                                moleImageView.setClickable(true);
                            }
                        }, 1000);

                        // 사운드 재생이 길어져도 겹쳐서 재생됨
                        playSound(dieSound);

                        if ((difficulty.equals("초급") && score >= BEGINNER_SCORE_LIMIT) ||
                                (difficulty.equals("중급") && score >= INTERMEDIATE_SCORE_LIMIT) ||
                                (difficulty.equals("고급") && score >= ADVANCED_SCORE_LIMIT)) {
                            endGame();
                        }
                    }
                    return true;
                }
            });
        }
    }

    // MediaPlayer 객체 생성 및 초기화를 위한 메소드
    private MediaPlayer createMediaPlayer(int resourceId) {
        MediaPlayer mp = MediaPlayer.create(this, resourceId);
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.release(); // 오류 발생 시 리소스 해제
                return true;
            }
        });
        return mp;
    }

    private void playSound(MediaPlayer sound) {
        if (sound != null) {
            try {
                if (sound.isPlaying()) {
                    sound.stop();
                    sound.prepare(); // MediaPlayer 재설정
                }
                sound.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void simulateMoleHiding() {
        showMoleImage();

    }

    private void endGame() {

            Intent intent = new Intent(this, game_end.class);
            intent.putExtra("difficulty", difficulty);
            intent.putExtra("score", score);
            startActivity(intent);
            finish();

    }

    private void showMoleImage() {
        List<Integer> moleImageIndexes = getRandomImageIndexes(1); // 1개의 이미지를 랜덤으로 선택
        Collections.shuffle(moleImageIndexes);

        int totalMoleCount = moleImageViews.length;
        List<Integer> visibleIndexes = new ArrayList<>();
        int delay = getDelayBasedOnDifficulty(); // 난이도에 따른 지연 시간을 받아옴
        // 이미지를 랜덤으로 나타나게 하기
        for (int i = 0; i < moleImageIndexes.size(); i++) {
            int randomImageIndex = moleImageIndexes.get(i);
            moleImageViews[i].setImageResource(moleImageIds[randomImageIndex]);
            visibleIndexes.add(i);

            final int finalI = i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playSound(upSound);
                    moleImageViews[finalI].setVisibility(View.VISIBLE);
                }
            }, delay);
        }

    }
    private void hideMoleImage() {
        for (ImageView moleImageView : moleImageViews) {
            moleImageView.setImageResource(R.drawable.mole); // 두더지 이미지를 초기 상태로 설정
            moleImageView.setVisibility(View.INVISIBLE); // 이미지를 숨김 처리
        }

        // 랜덤한 위치에 두더지 이미지 나타내기
        List<Integer> indexes = getRandomImageIndexes(1); // 3개의 이미지를 랜덤으로 선택
        currentIndex = indexes.get(0);

        moleImageViews[currentIndex].setVisibility(View.VISIBLE);

        // 일정 시간 후에 두더지 이미지를 숨기는 코드
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showMoleImage();
            }
        }, 3000);
    }
    private int getDelayBasedOnDifficulty() {
        switch (difficulty) {
            case "초급":
                return 4000;
            case "중급":
                return 3000;
            case "고급":
                return 2000;
            default:
                return 4000; // 기본값은 초급 난이도로 설정
        }
    }

    private List<Integer> getRandomImageIndexes(int count) {
        List<Integer> randomIndexes = new ArrayList<>();
        List<Integer> availableIndexes = new ArrayList<>();
        for (int i = 0; i < moleImageIds.length; i++) {
            availableIndexes.add(i);
        }
        Collections.shuffle(availableIndexes);

        for (int i = 0; i < count; i++) {
            if (i < availableIndexes.size()) {
                randomIndexes.add(availableIndexes.get(i));
            }
        }

        return randomIndexes;
    }

    private void updateScore() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dieSound.start();
                scoreTextView.setText("Score: " + score);
            }
        });
    }

    private int getRandomDelay() {
        Random random = new Random();
        return random.nextInt(500) + 1000; // 예시로 1000에서 3000까지의 범위로 수정
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 사운드 리소스 해제
        if (upSound != null) {
            upSound.release();
        }
        if (dieSound != null) {
            dieSound.release();
        }
        if (hitSound != null) {
            hitSound.release();
        }
        if (backgroundMusic != null) {
            if (backgroundMusic.isPlaying()) {
                backgroundMusic.stop();
            }
            backgroundMusic.release();
        }
    }
    private void releaseMediaPlayer(MediaPlayer mp) {
        if (mp != null) {
            mp.release();
        }
    }

}
