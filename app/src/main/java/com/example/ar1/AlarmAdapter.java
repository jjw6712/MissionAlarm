package com.example.ar1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AlarmAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private int mResource;
    private List<String> mAlarms;

    public AlarmAdapter(@NonNull Context context, int resource, @NonNull List<String> alarms) {
        super(context, resource, alarms);
        mContext = context;
        mResource = resource;
        mAlarms = alarms;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }

        TextView alarmTextView = convertView.findViewById(R.id.time_textview);
        String alarm = mAlarms.get(position);
        alarmTextView.setText(alarm);
        // "알람 번호: " 부분을 "알람 시간: "으로 변경
        String updatedAlarmText = alarm.replaceFirst("알람 번호: \\d{2}", "알람 시간:");
        alarmTextView.setText(updatedAlarmText);

        // 텍스트를 중앙 정렬로 설정
        alarmTextView.setGravity(Gravity.CENTER);

        Switch toggleSwitch = convertView.findViewById(R.id.switch_btn);
        toggleSwitch.setChecked(true);
        toggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

//              클릭한 알람 정보를 가져옴
                String clickedAlarm = mAlarms.get(position);

                // 가져온 알람 정보를 시간과 분으로 분리

                String[] alarmParts = clickedAlarm.split(" "); // 공백을 기준으로 문자열을 분리하여 배열에 저장
                String idString = alarmParts[2].trim();
                String hourString = alarmParts[3].replace("시", ""); // 시간 부분에서 "시"를 제거하여 시간 문자열 추출
                String minuteString = alarmParts[4].replace("분", ""); // 분 부분에서 "분"를 제거하여 분 문자열 추출

                int id = 0;
                int hour = 0; // 초기값 0으로 설정
                int minute = 0; // 초기값 0으로 설정

                if (!hourString.isEmpty() && hourString.matches("\\d+")) {
                    hour = Integer.parseInt(hourString);
                }

                if (!minuteString.isEmpty() && minuteString.matches("\\d+")) {
                    minute = Integer.parseInt(minuteString);
                }

                if (!idString.isEmpty() && idString.matches("\\d+")) {
                    id = Integer.parseInt(idString);
                }
                // 토글 버튼 상태 변경 이벤트 처리 로직
                if (isChecked) {
                    // 토글 버튼이 켜진 경우 동작
                    Toast.makeText(mContext, "알람이 켜졌습니다.", Toast.LENGTH_SHORT).show();

                    // 현재 시간 가져오기
                    Calendar currentTime = Calendar.getInstance();
                    Calendar now = Calendar.getInstance();
                    int currentHour = now.get(Calendar.HOUR_OF_DAY);
                    int currentMinute = now.get(Calendar.MINUTE);

                    Calendar alarmTimeCalendar = Calendar.getInstance();
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour); // 시간 설정
                    calendar.set(Calendar.MINUTE, minute); // 분 설정

// alarmTimeCalendar 객체에도 동일한 시간 설정
                    alarmTimeCalendar.set(Calendar.HOUR_OF_DAY, hour); // 시간 설정
                    alarmTimeCalendar.set(Calendar.MINUTE, minute); // 분 설정

// 알람 시간과 현재 시간 비교
                    if (alarmTimeCalendar.before(currentTime) || alarmTimeCalendar.equals(currentTime)) {
                        // 만약 알람 시간이 현재 시간보다 같거나 이전이라면, 알람 시간에 하루를 더함
                        alarmTimeCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    // 알람매니저로 알람 설정
                    AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                    Intent alarmIntent = new Intent(mContext, AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);
                    }

                } else {
                    // 토글 버튼이 꺼진 경우 동작
                    Toast.makeText(mContext, "알람이 꺼졌습니다.", Toast.LENGTH_SHORT).show();

                    // 알람매니저에서 알람 삭제
                    AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                    Intent alarmIntent = new Intent(mContext, AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    alarmManager.cancel(pendingIntent);
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭한 알람 정보를 가져옴
                String clickedAlarm = mAlarms.get(position);

                // 클릭한 항목의 ID 값을 가져옴
                long clickedId = getItemId(position);

                // 가져온 알람 정보를 시간과 분으로 분리

                String[] alarmParts = clickedAlarm.split(" "); // 공백을 기준으로 문자열을 분리하여 배열에 저장
                String idString = alarmParts[2].trim();
                String hourString = alarmParts[3].replace("시", ""); // 시간 부분에서 "시"를 제거하여 시간 문자열 추출
                String minuteString = alarmParts[4].replace("분", ""); // 분 부분에서 "분"를 제거하여 분 문자열 추출

                int id = 0;
                int hour = 0; // 초기값 0으로 설정
                int minute = 0; // 초기값 0으로 설정

                if (!hourString.isEmpty() && hourString.matches("\\d+")) {
                    hour = Integer.parseInt(hourString);
                }

                if (!minuteString.isEmpty() && minuteString.matches("\\d+")) {
                    minute = Integer.parseInt(minuteString);
                }

                if (!idString.isEmpty() && idString.matches("\\d+")) {
                    id = Integer.parseInt(idString);
                }

                // "%d  %02d:%02d" 형식에 맞게 문자열 생성
                //String alarmTimeString = String.format("%s  %02d:%02d", id, hour, minute);

                // 인텐트 생성 및 알람 정보 전달
                Intent intent = new Intent(mContext, AlarmSettingActivity.class);
                intent.putExtra("alarm", clickedAlarm);
                intent.putExtra("cid", clickedId);
                intent.putExtra("id", id);
                intent.putExtra("hour", hour); // 시간 정보를 Intent에 추가
                intent.putExtra("minute", minute); // 분 정보를 Intent에 추가
                intent.putExtra("editmode", String.valueOf(true)); // "true" 문자열을 전달

                // mContext가 유효한 경우에만 알람 세팅 액티비티 띄우기
                if (mContext != null) {
                    mContext.startActivity(intent);
                    notifyDataSetChanged();
                }
            }
        });

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        // 해당 위치의 알람 정보에 대한 고유한 ID 값을 반환하도록 구현
        // 예시로 알람 정보가 String 타입인 경우, position을 그대로 반환
        return position;
    }

}