package com.example.ar1;


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
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class AlarmSettingActivity extends AppCompatActivity {
    private TimePicker timePicker;
    private Button setAlarmButton;
    private Button cancelAlarmButton;

    private AlarmDBHelper databaseHelper;
    private Spinner stretchingOptionSpinner;

    private boolean isEditMode;
    private int editId;
    private int editHour;
    private int editMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_setting_layout);

        timePicker = findViewById(R.id.time_picker);
        setAlarmButton = findViewById(R.id.set_alarm_btn);
        cancelAlarmButton = findViewById(R.id.cancel_alarm_btn);

// TimePicker의 NumberPicker를 스피너 모드로 설정
        timePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        stretchingOptionSpinner = findViewById(R.id.stretching_option_spinner);
        // 스트레칭 옵션 배열
        String[] stretchingOptions = {"스쿼트", "푸쉬업"};

        // 어댑터 생성 및 스트레칭 옵션 배열 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stretchingOptions);

        // 드롭다운 스피너 스타일 설정
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 스피너에 어댑터 설정
        stretchingOptionSpinner.setAdapter(adapter);

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

        //스트레칭 초기 횟수 부정입력 방지를 위한 텍스트 와쳐 매서드
        EditText editText = findViewById(R.id.st_count_init);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 텍스트가 변경되기 전에 호출됨
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 텍스트가 변경되는 동안 호출됨
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 텍스트가 변경된 후에 호출됨
                String editTextValue = s.toString(); // 에딧 텍스트의 값을 문자열로 받아옴
                int numberOfStretching;

                try {
                    numberOfStretching = Integer.parseInt(editTextValue); // 에딧 텍스트의 값을 숫자로 변환
                } catch (NumberFormatException e) {
                    // 에딧 텍스트의 값이 숫자가 아닌 경우
                    Toast.makeText(AlarmSettingActivity.this, "숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 사용자가 입력한 스트레칭 횟수가 5 미만이거나 너무 큰 값인 경우
                if (numberOfStretching < 5 || numberOfStretching > 100) {
                    Toast.makeText(AlarmSettingActivity.this, "5 이상 99 이하의 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
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
                EditText editText = findViewById(R.id.st_count_init); // 에딧 텍스트 참조
                String editTextValue = editText.getText().toString(); // 에딧 텍스트의 값을 문자열로 받아옴
                int numberOfStretching;

                try {
                    numberOfStretching = Integer.parseInt(editTextValue); // 에딧 텍스트의 값을 숫자로 변환
                } catch (NumberFormatException e) {
                    // 에딧 텍스트의 값이 숫자가 아닌 경우
                    Toast.makeText(AlarmSettingActivity.this, "유효한 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 사용자가 입력한 스트레칭 횟수가 5 미만이거나 너무 큰 값인 경우
                if (numberOfStretching < 5 || numberOfStretching > 100) {
                    Toast.makeText(AlarmSettingActivity.this, "5 이상 100 이하의 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
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
    private void setAlarm(int hour, int minute) {
        EditText editText = findViewById(R.id.st_count_init);
        Spinner stretchingOptionSpinner = findViewById(R.id.stretching_option_spinner);
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
            Alarm alarmModel = new Alarm(0, hour, minute);
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
            String selectedOption = stretchingOptionSpinner.getSelectedItem().toString();
            String selectedCount = editText.getText().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("selected_stretching_mode_" + alarmId, selectedOption);
            editor.putString("selected_stretching_count_" + alarmId, selectedCount);
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
            Alarm alarmModel = new Alarm(0, hour, minute);
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
            String selectedOption = stretchingOptionSpinner.getSelectedItem().toString();
            String selectedCount = editText.getText().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("selected_stretching_mode_" + alarmId, selectedOption);
            editor.putString("selected_stretching_count_" + alarmId, selectedCount);
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