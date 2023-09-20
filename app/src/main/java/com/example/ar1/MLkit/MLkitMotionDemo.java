package com.example.ar1.MLkit;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ar1.Alarm;
import com.example.ar1.R;
import com.example.ar1.ui.alarm.AlarmActivity;
import com.example.ar1.ui.alarm.AlarmDBHelper;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)

public class MLkitMotionDemo extends AppCompatActivity{
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int COUNTDOWN_DURATION = 10000; // 카운트 다운 시간 (밀리초)
    private static final int Reapit_Alarm_Count = 60000; //알람 반복 되기까지의 제한시간
    private static final int COUNTDOWN_INTERVAL = 1000; // 카운트 다운 간격 (밀리초)
    private boolean isReapitAlarm = false; //알람액티비티를 재호출 할때만 true가 되는 플래그
    private boolean stop_motion = false;
    private PreviewView viewFinder;
    private TextView tvSquatCount, stoption, tvGoalCount;
    private GraphicOverlay graphicOverlay;
    private float hipInitialPosition = -1;
    private float shoulderInitialPosition;
    private boolean isSquatting = false;
    private LinearLayout layoutbottom;

    private  int initialCount = 0;
    private int stCount = 0; // 공통으로 사용할 스트레칭 카운트
    private int lastStCount = 0; // 마지막 스트레칭 카운트를 추적하는 변수를 추가
    private PoseDetector poseDetector;
    private TextView tvFps;
    private static final long FPS_UPDATE_INTERVAL = 0   ; // 1초마다 FPS 업데이트
    private long lastFpsUpdateTime = 0;
    private int frameCount = 0;
    private boolean isCountdownFinished = false;
    private boolean isDeviceStill = false;
    private static final long STILL_THRESHOLD = 2000; // the device is considered still after 1 second of no movement
    private static final float MOVEMENT_THRESHOLD = 1.2f; // change this value based on your needs
    private long lastMovementTime;
    private boolean isSquat = false; // 스쿼트 모드 여부를 저장하는 변수
    private boolean isPushingUp = false;

    private boolean isPushingUping = false;

    private int alarmId;
    private String userId;
    private String stretchingCount;
    private String stretchingMode;
    private int targetCount = 0;
    private Handler handler;
    private Runnable runnable;
    private MediaPlayer mediaPlayer;
    private MediaPlayer motionStartMediaPlayer;
    private MediaPlayer motionEndMediaPlayer;
    private Alarm alarm;
    private Handler stCountCheckHandler = new Handler();
    // 스트레칭 카운트가 증가하는 것을 감시하는 Runnable을 생성 스트레칭 카운트가 1분동안 증가하지 않으면 스트레칭 횟수를 추가해서 다 시 알람을 울림


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion);

        viewFinder = findViewById(R.id.viewFinder);
        tvSquatCount = findViewById(R.id.tvSquatCount);
        graphicOverlay = findViewById(R.id.graphicOverlay);
        tvFps = findViewById(R.id.tvFps);
        layoutbottom = findViewById(R.id.layoutBottom);
        stoption = findViewById(R.id.tvStOption);
        Button startButton = findViewById(R.id.startButton);
        TextView countdownTextView = findViewById(R.id.countdownTextView);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        setMediaVolume(100);

        // stCount 방향 감지 및 회전 설정
        final TextView textView = findViewById(R.id.tvSquatCount);
        final OrientationEventListener orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                float newRotation;
                if (orientation >= 315 || orientation < 45) {
                    newRotation = 0f;
                } else if (orientation >= 45 && orientation < 135) {
                    newRotation = -90f;
                } else if (orientation >= 135 && orientation < 225) {
                    newRotation = 180f;
                } else if (orientation >= 225 && orientation < 315) {
                    newRotation = 90f;
                } else {
                    return;
                }
                textView.setRotation(newRotation);
            }
        };

        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        }

        // 이 코드가 화면이 자동으로 꺼지는 것을 막음
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        stretchingCount = sharedPreferences.getString("selected_defalut_stretching_count_", "default"); // 알람 ID 사용하여 스트레칭 횟수 불러오기
        Log.d("StretchingMode", "Selected count: " +stretchingCount); // 확인용 로그 문

        initialCount = Integer.parseInt(stretchingCount);// stCount의 갯수가 줄어들때 음성 안내는 하나부터 시작하도록 하기위한 기존 목표횟수를 저장
        stCount = Integer.parseInt(stretchingCount);
        tvSquatCount.setText(String.valueOf(stCount)); // 초기 진행횟수 텍스트뷰 0으로 초기화
        handler = new Handler();
        // Initialize countdown timer
        CountDownTimer countDownTimer = new CountDownTimer(COUNTDOWN_DURATION, COUNTDOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / COUNTDOWN_INTERVAL;
                String message = secondsUntilFinished + "\n초 후 스트래칭을 시작합니다.";

                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(message);
                int startSpan = String.valueOf(secondsUntilFinished).length() + 1; // Plus one to account for the space
                int endSpan = message.length();
                spannableStringBuilder.setSpan(new RelativeSizeSpan(0.1f), startSpan, endSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                countdownTextView.setText(spannableStringBuilder);
                countdownTextView.setVisibility(View.VISIBLE);
            }

            public void onFinish() {
                countdownTextView.setText("Start!");
                countdownTextView.setVisibility(View.GONE);

                // Find the squatLayout and set its visibility
                LinearLayout squatLayout = findViewById(R.id.squatLayout);
                squatLayout.setVisibility(View.VISIBLE);

                isCountdownFinished = true;
            }
        };

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 스트레칭 시작 버튼 숨김
                startButton.setVisibility(View.GONE);
                // 카운트 다운 레이아웃을 보이도록 함
                countdownTextView.setVisibility(View.VISIBLE);
                // 카운트 다운 실행
                countDownTimer.start();
                // 지연된 활동 종료 취소
                handler.removeCallbacks(runnable);

                // 스트레칭 시작 버튼을 누르면 mp3 파일을 재생합니다.
                if (mediaPlayer != null) {
                    mediaPlayer.stop();  // st_start 재생 중지
                    mediaPlayer.release();  // MediaPlayer 해제
                }

                motionStartMediaPlayer = MediaPlayer.create(MLkitMotionDemo.this, R.raw.motion_start);
                motionStartMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // motion_start 재생이 완료되면 motion_start MediaPlayer 해제
                        motionStartMediaPlayer.release();
                        motionStartMediaPlayer = null;
                    }
                });
                motionStartMediaPlayer.start();
            }
        });

        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();

        poseDetector = PoseDetection.getClient(options);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Camera permission is already granted, start the camera
            startCamera();
        }
    }

    private void setMediaVolume(int volume) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    // 스트레칭 진행 시마다 호출될 메서드
    public void incrementStCount() {
        stCount--;
        lastStCount = stCount;

        // 현재 진행된 스트레칭 횟수 계산
        int currentCount = initialCount - stCount;

        // 스트레칭 카운트에 따른 음성파일 재생 (1 ~ 10 사이의 숫자로 반복)
        int modulatedCount = ((currentCount - 1) % 10) + 1;
        playCountSound(modulatedCount);
    }
    private MediaPlayer mp; // 카운트를 재생하기 위한 MediaPlayer

    private void playCountSound(int count) {
        // 기존 MediaPlayer 해제
        if (mp != null) {
            mp.release();
            mp = null;
        }

        // count에 따른 음성 파일 이름
        String fileName = "num_" + count;

        // MediaPlayer 초기화 및 설정
        try {
            int resId = getResources().getIdentifier(fileName, "raw", getPackageName());
            mp = MediaPlayer.create(this, resId);

            if (mp == null) {
                Log.e("MediaPlayer", "Failed to create MediaPlayer for " + fileName);
                return;
            }

            // 음성 파일 재생
            mp.start();

            // 재생이 끝나면 MediaPlayer 자원 해제
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                    mp = null;

                    // 스트레칭 카운트가 목표치에 도달했는지 확인
                    if (stCount <= 0) {
                        // 'motion_end' 재생
                            motionEndMediaPlayer = MediaPlayer.create(MLkitMotionDemo.this, R.raw.motion_end); // ActivityName은 현재 Activity의 이름으로 변경해야 합니다.
                        motionEndMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                setMediaVolume(AudioManager.USE_DEFAULT_STREAM_TYPE);
                                finish();
                            }
                        });
                        motionEndMediaPlayer.start();
                        stop_motion = true;
                    }
                }
            });

        } catch (Exception e) {
            Log.e("MediaPlayer", "Error playing sound: " + e.getMessage());
        }
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (x*x + y*y + z*z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

            if (acceleration > MOVEMENT_THRESHOLD) {
                // device is moving
                isDeviceStill = false;
                lastMovementTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastMovementTime > STILL_THRESHOLD) {
                // device is still
                isDeviceStill = true;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // We don't need to handle this case for this example
        }
    };


    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        alarmId = getIntent().getIntExtra("alarm_id", -1); // 알람 ID 받아오기
        stretchingMode = sharedPreferences.getString("selected_stretching_default_mode_", "default"); // 알람 ID 사용하여 스트레칭 모드 불러오기
        Log.d("StretchingMode", "Selected mode: " + stretchingMode); // 확인용 로그 문

        if (stretchingMode != null) {
            if (stretchingMode.equals("스쿼트")) {
                isSquat = true; // 스쿼트 모드로 설정
                stoption.setText("미션: 스쿼트");
            } else if (stretchingMode.equals("푸쉬업")) {
                isPushingUp = true; // 푸쉬업 모드로 설정
                stoption.setText("미션: 푸쉬업");
            }
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(new android.util.Size(deviceWidth, deviceHeight))
                        .build();

        imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    //이미지 분석 로직
                    Image mediaImage = imageProxy.getImage();
                    if (mediaImage != null) {
                        InputImage image =
                                InputImage.fromMediaImage(
                                        mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                        poseDetector.process(image)
                                .addOnSuccessListener(pose -> {
                                    if (isSquat && isCountdownFinished) {
                                        squat(pose);  // 스쿼트 모드인 경우 squat() 메서드 호출
                                    } else if(isPushingUp && isCountdownFinished) {
                                        pushup(pose);  // 푸쉬업 모드인 경우 pushup() 메서드 호출
                                    }
                                    drawPose(pose);
                                })
                                .addOnFailureListener(Throwable::printStackTrace)
                                .addOnCompleteListener(result -> {
                                    imageProxy.close(); // Close the imageProxy here after completion
                                    // FPS 계산
                                    long currentTime = System.currentTimeMillis();
                                    frameCount++;

                                    if (currentTime - lastFpsUpdateTime >= FPS_UPDATE_INTERVAL) {
                                        float fps = (float) frameCount / ((currentTime - lastFpsUpdateTime) / 1000.0f);
                                        lastFpsUpdateTime = currentTime;
                                        frameCount = 0;

                                        // FPS를 표시할 TextView 업데이트
                                        runOnUiThread(() -> tvFps.setText(String.format(Locale.getDefault(), "FPS: %.2f", fps)));
                                    }
                                });
                    }
                    // Don't close the imageProxy here, as it could cause a double close situation
                });

        CameraSelector cameraSelector =
                new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

        cameraProvider.unbindAll();
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        graphicOverlay.setCameraInfo(camera.getCameraInfo());
    }

    private void updateSquatCountWithAnimation(final TextView textView, final String newText) { // stCount 업데이트될 때마다 애니매이션 적용

        // Fade-out and move to the left
        ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);
        ObjectAnimator moveOutAnimator = ObjectAnimator.ofFloat(textView, "translationX", 0f, -textView.getWidth());
        fadeOutAnimator.setDuration(500);
        moveOutAnimator.setDuration(500);

        // Fade-in and move from the right to the center
        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
        ObjectAnimator moveInAnimator = ObjectAnimator.ofFloat(textView, "translationX", textView.getWidth(), 0f);
        fadeInAnimator.setDuration(500);
        moveInAnimator.setDuration(500);

        // Animation set for old text
        AnimatorSet oldTextAnimationSet = new AnimatorSet();
        oldTextAnimationSet.play(fadeOutAnimator).with(moveOutAnimator);
        oldTextAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                textView.setText(newText);
                textView.setTranslationX(0); // Reset translation
            }
        });

        // Animation set for new text
        AnimatorSet newTextAnimationSet = new AnimatorSet();
        newTextAnimationSet.play(fadeInAnimator).with(moveInAnimator);
        newTextAnimationSet.setStartDelay(500); // Start after old text animation ends
        newTextAnimationSet.addListener(new AnimatorListenerAdapter() {

        });

        // Start the animations
        oldTextAnimationSet.start();
        newTextAnimationSet.start();
    }
    public boolean areLandmarksInView(PoseLandmark... landmarks) {
        int imageWidth = viewFinder.getWidth();
        int imageHeight = viewFinder.getHeight();
        for (PoseLandmark landmark : landmarks) {
            PointF landmarkPosition = landmark.getPosition();
            // 가정: 0,0은 왼쪽 상단, 오른쪽 하단은 imageWidth, imageHeight
            if (landmark == null ||
                    landmarkPosition.x < 0 ||
                    landmarkPosition.y < 0 ||
                    landmarkPosition.x > imageWidth ||
                    landmarkPosition.y > imageHeight) {
                return false;
            }
        }
        return true;
    }

    public void squat(Pose pose) {
        if(!isCountdownFinished && !isDeviceStill) {
            return;
        }
        // Left landmarks
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        // Right landmarks
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        if ((leftHip == null || leftKnee == null || leftAnkle == null) &&
                (rightHip == null || rightKnee == null || rightAnkle == null)) {
            return;
        }

        // Check if all landmarks are in view
        if (!areLandmarksInView(leftHip, leftKnee, leftAnkle, rightHip, rightKnee, rightAnkle)) {
            return;
        }

        // Calculate angles for both legs
        float leftAngle = (leftHip != null && leftKnee != null && leftAnkle != null) ? calculateAngle(leftHip, leftKnee, leftAnkle) : 0;
        float rightAngle = (rightHip != null && rightKnee != null && rightAnkle != null) ? calculateAngle(rightHip, rightKnee, rightAnkle) : 0;

        if (leftAngle < 90 || rightAngle < 90) {
            if (!isSquatting) {
                isSquatting = true;
                hipInitialPosition = Math.min(
                        leftHip != null ? leftHip.getPosition().y : Float.MAX_VALUE,
                        rightHip != null ? rightHip.getPosition().y : Float.MAX_VALUE
                );
            }
        } else {
            float currentHipPosition = Math.min(
                    leftHip != null ? leftHip.getPosition().y : Float.MAX_VALUE,
                    rightHip != null ? rightHip.getPosition().y : Float.MAX_VALUE
            );

            if (isSquatting && (hipInitialPosition - currentHipPosition) > 0.03f * viewFinder.getHeight()) {
                incrementStCount(); //incrementStCount() 메서드를 사용하여 운동 횟수를 일관되게 관리 1분동안 카운트가 안올라가면 알람 재실행
                runOnUiThread(() -> updateSquatCountWithAnimation(tvSquatCount, String.valueOf(stCount))); //애니매이션으로 stCount 업데이트
            }
            isSquatting = false;


        }

    }
    private float torsoInitialPosition;
    public void pushup(Pose pose) {
        if (!isCountdownFinished || !isDeviceStill) {
            return;
        }

        float bentArmThreshold = 100f;
        float straightArmThreshold = 160f;

        // Get all necessary landmarks
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);

        if (leftShoulder == null || rightShoulder == null || leftElbow == null || rightElbow == null || leftWrist == null || rightWrist == null || leftHip == null || rightHip == null) {
            return;
        }

        // Check if all landmarks are in view
        if (!areLandmarksInView(leftShoulder, rightShoulder, leftElbow, rightElbow, leftWrist, rightWrist, leftHip, rightHip)) {
            return;
        }

        float leftArmAngle = calculateAngle(leftShoulder, leftElbow, leftWrist);
        float rightArmAngle = calculateAngle(rightShoulder, rightElbow, rightWrist);

        // Check if the person is in push-up down position
        if (leftArmAngle > bentArmThreshold && rightArmAngle > bentArmThreshold) {
            if (!isPushingUping) {
                torsoInitialPosition = Math.min(leftHip.getPosition().y, rightHip.getPosition().y);
                isPushingUping = true;
            }
        }

        // Check if the person is in push-up up position
        else if (leftArmAngle < straightArmThreshold && rightArmAngle < straightArmThreshold) {
            if (isPushingUping) {
                float currentTorsoPosition = Math.min(leftHip.getPosition().y, rightHip.getPosition().y);
                if ((torsoInitialPosition - currentTorsoPosition) > 0.02f * viewFinder.getHeight()) {
                    incrementStCount();
                    runOnUiThread(() -> updateSquatCountWithAnimation(tvSquatCount, String.valueOf(stCount))); //애니매이션으로 stCount 업데이트
                    isPushingUping = false;
                    torsoInitialPosition = 0;
                }
            }
        }
    }
    private float calculateAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        if (firstPoint == null || midPoint == null || lastPoint == null) {
            return 0.0f; // 또는 적절한 기본값을 반환하세요.
        }

        double result = Math.abs(Math.atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                lastPoint.getPosition().x - midPoint.getPosition().x)
                - Math.atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                firstPoint.getPosition().x - midPoint.getPosition().x));
        result = Math.abs(result);
        if (result > Math.PI)
            result = (2 * Math.PI) - result;
        return (float) Math.toDegrees(result);
    }

    private void drawPose(Pose pose) {
        graphicOverlay.clear();

        boolean showInFrameLikelihood = true;  // 예시 값
        boolean visualizeZ = true;  // 예시 값
        boolean rescaleZForVisualization = true;  // 예시 값
        List<String> poseClassification = new ArrayList<>();  // 예시 값

        PoseGraphic poseGraphic =
                new PoseGraphic(
                        graphicOverlay,
                        pose,
                        showInFrameLikelihood,
                        visualizeZ,
                        rescaleZForVisualization,
                        poseClassification);

        graphicOverlay.add(poseGraphic);
        graphicOverlay.postInvalidate();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission is granted, start the camera
                startCamera();
            } else {
                // Camera permission is denied, handle accordingly (e.g., display a message, disable camera functionality)
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        if(stop_motion ==true)
            SendToServer();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // MediaPlayer를 해제합니다.
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (motionStartMediaPlayer != null) {
            motionStartMediaPlayer.release();
            motionStartMediaPlayer = null;
        }
        if (motionEndMediaPlayer != null) {
            motionEndMediaPlayer.release();
            motionEndMediaPlayer = null;
        }
    }
    private void SendToServer() { // 서버로 데이터 보내기
        OkHttpClient client = new OkHttpClient();
        SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        String userId = preferences.getString("userId", ""); //로그인한 유저 id 가져오기

        // 현재 날짜 가져오기
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        String currentDate = sdfDate.format(calendar.getTime());

        // AlarmDBHelper 인스턴스 생성
        AlarmDBHelper dbHelper = new AlarmDBHelper(this); //sqlite 로컬db 객체 초기화

        // alarmId를 항상 null로 설정
        String alarmId = "기본모드";

// 현재 날짜와 시간을 구합니다.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        HashMap<String, String> data = new HashMap<>(); // 해쉬맵으로 데이터를 구성하기 위한 해쉬맵 객체 초기화
        data.put("userId", userId); // 로그인한 사용자 ID 삽입
        data.put("alarmId", alarmId); // 항상 null로 설정
        data.put("alarmTime", currentDateAndTime); // 현재 날짜와 시간 삽입
        data.put("missionName", stretchingMode); // 해당 알람 미션 종류 삽입
        data.put("missionCount", stretchingCount); // 해당 알람 미션 완료 갯수 삽입
        Log.d(TAG, ("userId: " + userId + "\nalarmId: " + alarmId + "\nalarmTime: " + currentDateAndTime + "\nmissionMode: " + stretchingMode + "\nmissionCount: " + stretchingCount));

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

}
