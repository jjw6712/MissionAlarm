package com.example.ar1.ui.alarm;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.ar1.Alarm;
import com.example.ar1.BottomActivity;
import com.example.ar1.R;
import com.example.ar1.alarmmission.AlarmMissionActivity;


public class AlarmSettingActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_MISSION = 1; // 미션 요청 코드 상수 정의

    private TimePicker timePicker;
    private Button setAlarmButton;
    private Button cancelAlarmButton;
    TextView curruntcount;

    private AlarmDBHelper databaseHelper;
    private Button btmissionselect;
    String missionName, missionLevel;
    String missionCount;
    private CheckBox cbPowerMode;
    private boolean isPowerModeEnabled;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_setting_layout);

        // 상태바 색상 변경
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        cbPowerMode = findViewById(R.id.cbpowermode);
        timePicker = findViewById(R.id.time_picker);
        setAlarmButton = findViewById(R.id.set_alarm_btn);
        cancelAlarmButton = findViewById(R.id.cancel_alarm_btn);
        curruntcount = findViewById(R.id.currunt_count);
// 체크박스 상태 변경 리스너 설정
        cbPowerMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isPowerModeEnabled = isChecked; // 체크박스의 상태에 따라 변수 업데이트
            }
        });

        // 초기 상태 업데이트
        isPowerModeEnabled = cbPowerMode.isChecked();

// TimePicker의 NumberPicker를 스피너 모드로 설정
        timePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        btmissionselect = findViewById(R.id.btmissionselect);
        btmissionselect.setOnClickListener(v -> {
            // AlarmMissionActivity 호출
            Intent intent = new Intent(AlarmSettingActivity.this, AlarmMissionActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MISSION);
        });


// TimePicker에 더블 클릭 이벤트 리스너 등록
        timePicker.setOnTouchListener(new View.OnTouchListener() {
            private long lastTouchTime = -1;
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // 더블 클릭 간격 (ms)

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    long touchTime = System.currentTimeMillis();
                    if (lastTouchTime != -1 && touchTime - lastTouchTime < DOUBLE_CLICK_TIME_DELTA) {
                        // 더블 클릭 시 이벤트 무시
                        return true;
                    }
                    lastTouchTime = touchTime;
                }
                // 이벤트 처리를 계속 진행
                return false;
            }
        });



        databaseHelper = new AlarmDBHelper(this);
        // onCreate() 메소드 내부에서 Intent에서 시간과 분 값을 추출하는 예시 코드
        Intent intent = getIntent();

        String modeValue = getIntent().getStringExtra("editmode"); // getStringExtra() 메서드를 사용하여 "mode" 키로 전달된 문자열 값을 받음

        boolean isEditMode = Boolean.parseBoolean(modeValue); // 받은 문자열 값을 boolean 값으로 변환하여 처리
        // 인텐트에서 시간과 분 값을 추출하여 타임 피커에 설정
        int editid = intent.getIntExtra("id", 0);
        int edithour = getIntent().getIntExtra("hour", 0);
        int editminute = getIntent().getIntExtra("minute", 0);

        if (isEditMode) {
            // 추출한 시간과 분 값을 타임피커에 설정
            timePicker.setHour(edithour);
            timePicker.setMinute(editminute);
        }

        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    setAlarm(hour, minute);
                    updateTimeText(hour, minute);

                    Intent intent = new Intent(AlarmSettingActivity.this, BottomActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 이전 액티비티들을 모두 스택에서 제거
                    startActivity(intent);

                    finish();
                }

        });

        cancelAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelAlarm();
                updateTimeText(0, 0);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MISSION && resultCode == RESULT_OK && data != null) {
            // 결과 데이터에서 미션 정보 가져오기
            missionName = data.getStringExtra("missionName");
            missionCount = data.getStringExtra("missionCount");
           //missionLevel = data.getStringExtra("missionLevel");
            Log.d(TAG, "난이도: "+ missionCount);

            // 여기서 미션 정보를 사용하거나 저장
            // 예: TextView에 미션 이름과 횟수 표시
            if("푸쉬업".equals(missionName) || "스쿼트".equals(missionName)) {
                curruntcount.setText("선택 미션: " + missionName + " \n횟수: " + missionCount + "회");
            } else if ("영단어 발음하기".equals(missionName)||"영문장 발음하기".equals(missionName)||"영단어 퀴즈퍼즐".equals(missionName)) {
                if("elementary".equals(missionCount)) {
                    curruntcount.setText("선택 미션: " + missionName + " \n난이도: " + "초급");
                }else if("middle".equals(missionCount)) {
                    curruntcount.setText("선택 미션: " + missionName + " \n난이도: " + "중급");
                }else if("college".equals(missionCount)) {
                    curruntcount.setText("선택 미션: " + missionName + " \n난이도: " + "고급");
                }
            } else if ("만보계".equals(missionName)) {
                curruntcount.setText("선택 미션: " + missionName + " \n횟수: " + missionCount + "보");
            }
        }

    }
    private void setAlarm(int hour, int minute) {

        btmissionselect = findViewById(R.id.btmissionselect);
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        Intent intent = getIntent();

        String modeValue = getIntent().getStringExtra("editmode"); // getStringExtra() 메서드를 사용하여 "mode" 키로 전달된 문자열 값을 받음
        boolean isEditMode = Boolean.parseBoolean(modeValue); // 받은 문자열 값을 boolean 값으로 변환하여 처리
        int editid = intent.getIntExtra("id", 0);

        if(isEditMode){
            // 알람 매니저에게 알람 삭제 요청
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, editid, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);

            Log.d("TAG", "In Edit Mode");

            // 기존 행 삭제
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.delete("alarms", "_id=?", new String[]{String.valueOf(editid)});

            db.close();

            // 현재 시간 가져오기
            Calendar currentTime = Calendar.getInstance();

            // 알람 시간용 캘린더 인스턴스 생성
            Calendar alarmTimeCalendar = Calendar.getInstance();
            alarmTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
            alarmTimeCalendar.set(Calendar.MINUTE, minute);
            alarmTimeCalendar.set(Calendar.SECOND, 0);

            // 알람 시간과 현재 시간 비교
            if (alarmTimeCalendar.before(currentTime)) {
                // 만약 알람 시간이 현재 시간보다 이전이라면, 알람 시간에 하루를 더함
                alarmTimeCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // 알람 정보를 데이터베이스에 저장
            com.example.ar1.Alarm alarmModel = new com.example.ar1.Alarm(0, hour, minute);
            long alarmId = databaseHelper.addAlarm(alarmModel);

            // 알람 매니저를 통해 알람 등록
            alarmManager = getSystemService(AlarmManager.class); // getSystemService 메서드를 통해 AlarmManager 객체를 얻음
            intent = new Intent(AlarmSettingActivity.this, AlarmReceiver.class);
            intent.putExtra("id", (int) alarmId); // Ensure alarmId is correctly put as an extra
            pendingIntent = PendingIntent.getBroadcast(AlarmSettingActivity.this, (int) alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);
            }
            // MLkitMotion 클래스로 선택된 모드 플래그 전송

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("selected_stretching_mode_" + alarmId, missionName);
            editor.putString("selected_stretching_count_" + alarmId, String.valueOf(missionCount));
            editor.putBoolean("PowerMode", isPowerModeEnabled);
            editor.putLong("alarmId", alarmId);
            Log.d(TAG, "파워모드: "+isPowerModeEnabled);
            editor.apply();
            String stretchingOptionSaved = sharedPreferences.getString("selected_stretching_mode_" + alarmId, "default");
            Log.d("TAG", "Saved stretching option edit: " + stretchingOptionSaved);

            // 토스트 메시지로 알람 설정 완료 메시지 표시
            Toast.makeText(this, "알람이 수정되었습니다.", Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d("TAG", "In Run Mode");

            // 현재 시간 가져오기
            Calendar currentTime = Calendar.getInstance();

            // 알람 시간용 캘린더 인스턴스 생성
            Calendar alarmTimeCalendar = Calendar.getInstance();
            alarmTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
            alarmTimeCalendar.set(Calendar.MINUTE, minute);
            alarmTimeCalendar.set(Calendar.SECOND, 0);

            // 알람 시간과 현재 시간 비교
            if (alarmTimeCalendar.before(currentTime)) {
                // 만약 알람 시간이 현재 시간보다 이전이라면, 알람 시간에 하루를 더함
                alarmTimeCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // 알람 정보를 데이터베이스에 저장
            com.example.ar1.Alarm alarmModel = new Alarm(0, hour, minute);
            long alarmId = databaseHelper.addAlarm(alarmModel);

            // 알람 매니저를 통해 알람 등록
            AlarmManager alarmManager = getSystemService(AlarmManager.class); // getSystemService 메서드를 통해 AlarmManager 객체를 얻음
            intent = new Intent(AlarmSettingActivity.this, AlarmReceiver.class);
            intent.putExtra("id", (int) alarmId); // Ensure alarmId is correctly put as an extra
            PendingIntent pendingIntent = PendingIntent.getBroadcast(AlarmSettingActivity.this, (int) alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);
            }

            // MLkitMotion 클래스로 선택된 모드 플래그 전송

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("selected_stretching_mode_" + alarmId, missionName);
            editor.putString("selected_stretching_count_" + alarmId, String.valueOf(missionCount));
            editor.putBoolean("PowerMode", isPowerModeEnabled);
            editor.putLong("alarmId", alarmId);
            Log.d(TAG, "파워모드: "+isPowerModeEnabled);
            editor.apply();
            String stretchingOptionSaved = sharedPreferences.getString("selected_stretching_mode_" + alarmId, "default");

            Log.d("TAG", "Saved stretching option: " + stretchingOptionSaved);

            // 토스트 메시지로 알람 설정 완료 메시지 표시
            Toast.makeText(this, "알람이 설정되었습니다.", Toast.LENGTH_SHORT).show();
        }

    }
    private void cancelAlarm() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) alarmId, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //alarmManager.cancel(pendingIntent);
        // 알람 세팅 액티비티를 종료한다.
        finish();
    }
    private void updateTimeText(int hour, int minute) {
        String ampm = "AM";
        int hour12 = hour;
        if (hour > 12) {
            ampm = "PM";
            hour12 = hour - 12;
        }
        String timeText = String.format("%02d:%02d", hour12, minute);
    }

}