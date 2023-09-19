package com.example.ar1.ui.alarm;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ar1.BottomActivity;
import com.example.ar1.R;
import com.example.ar1.databinding.FragmentAlarmBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Alarm extends Fragment {

    private FragmentAlarmBinding binding;
    private TextView alarmTimeTv;
    private TextView ampmTv;
    private Button setAlarmBtn, timerBtn, toggleButton, motionBtn;
    private ListView alarmListView;
    private ArrayList<String> alarmList;
    private AlarmDBHelper dbHelper;
    private AlarmAdapter adapter;

    int OVERLAY_PERMISSION_REQ_CODE = 1000;
    int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
    private SimpleDateFormat ampmFormat = new SimpleDateFormat("a", Locale.getDefault());
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateTime();
            handler.postDelayed(this, 1000);
        }
    };

    public Alarm() {
        // Required empty public constructor
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlarmViewModel homeViewModel =
                new ViewModelProvider(this).get(AlarmViewModel.class);

        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 프래그먼트에서 상태바 색상 변경
        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.black));
        }


        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }


        alarmTimeTv = binding.alarmTimeTv;
        ampmTv = binding.ampmTv;
        setAlarmBtn = binding.setAlarmBtn;
        alarmListView = binding.alarmListView;

        dbHelper = new AlarmDBHelper(getContext());
        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(getContext(), R.layout.alarm_list_item, alarmList);
        alarmListView.setAdapter(adapter);
        String PackageName = getActivity().getPackageName();
        // 오버레이 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + PackageName));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            // 오버레이 권한이 이미 있는 경우, 카메라 권한 확인
            checkCameraPermission();
        }

        List<com.example.ar1.Alarm> alarms = dbHelper.getAllAlarms();
        for (com.example.ar1.Alarm alarm : alarms) {
            String alarmInfo = String.format(Locale.getDefault(), "알람 번호: %02d %02d시 %02d분",
                    alarm.getId(), alarm.getHour(), alarm.getMinute());
            alarmList.add(alarmInfo);
        }


        adapter.notifyDataSetChanged();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM alarms", null);

        if (cursor.moveToFirst()) {
            do {
                int timeIndex = cursor.getColumnIndex("time");
                int repeatIndex = cursor.getColumnIndex("repeat");
                if (timeIndex != -1 && repeatIndex != -1) {
                    String time = cursor.getString(timeIndex);
                    boolean repeat = cursor.getInt(repeatIndex) == 1;
                    String alarmInfo = time + " (Repeat: " + repeat + ")";
                    alarmList.add(alarmInfo);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // 알람 설정 버튼 클릭 이벤트 처리
        setAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 알람 세팅 액티비티로 이동
                Intent intent = new Intent(getActivity(), AlarmSettingActivity.class);
                // 기본값 전달
                intent.putExtra("hour", 6);
                intent.putExtra("minute", 0);
                startActivity(intent);
            }
        });

        ListView listView = binding.alarmListView; // findViewById 대신 binding 사용
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 해당 항목의 ID를 사용하여 DB에서 시간 정보를 가져와서 알람 세팅 액티비티로 이동
                int[] time = dbHelper.loadTime(id);
                if (time != null) {
                    int hour = time[0];
                    int minute = time[1];
                    Intent intent = new Intent(getActivity(), AlarmSettingActivity.class);
                    intent.putExtra("hour", hour);
                    intent.putExtra("minute", minute);
                    startActivity(intent);
                }
            }
        });

        //리스트뷰의 항목을 길게 누르면 팝업메뉴가 뜬다
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 팝업 메뉴로 변경
                final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.delete_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete_item:
                                // 삭제 버튼 클릭 시 동작
                                com.example.ar1.Alarm alarm = dbHelper.getAllAlarms().get((int) id); // id를 기반으로 DB에서 알람 정보 가져오기
                                if (alarm != null) {
                                    dbHelper.deleteAlarm(alarm.getId()); // 알람 ID를 기준으로 DB에서 알람 삭제
                                    alarmList.remove(position); // 리스트뷰에서 해당 항목 제거
                                    adapter.notifyDataSetChanged(); // 어댑터에 데이터 변경을 알려 리스트뷰 갱신

                                    // 알람 취소
                                    Intent intent = new Intent(getActivity(), AlarmReceiver.class);
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), alarm.getId(), intent, PendingIntent.FLAG_IMMUTABLE);
                                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                                    alarmManager.cancel(pendingIntent);
                                }
                                return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();

                return true; // true를 반환하여 롱클릭 이벤트가 소비되었음을 알려줍니다.
            }
        });

        updateTime();
        handler.postDelayed(updateRunnable, 1000);

        return root;
    }

    private void updateTime() {
        Calendar calendar = Calendar.getInstance();
        String time = timeFormat.format(calendar.getTime());
        String ampm = ampmFormat.format(calendar.getTime());

        alarmTimeTv.setText(time);
        ampmTv.setText(ampm);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        handler.removeCallbacks(updateRunnable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(getActivity())) {

                // 메인 액티비티를 실행하는 코드
                Intent mainActivityIntent = new Intent(getActivity(), BottomActivity.class);
                startActivity(mainActivityIntent);
                getActivity().finish();
            }
        }

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String time = data.getStringExtra("time");
            boolean repeat = data.getBooleanExtra("repeat", false);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("time", time);
            values.put("repeat", repeat);
            db.insert("alarms", null, values);
            db.close();

            String alarmInfo = time + " (Repeat: " + repeat + ")";
            alarmList.add(alarmInfo);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM alarms", null);


        if (cursor.moveToFirst()) {
            do {
                int timeIndex = cursor.getColumnIndex("time");
                int repeatIndex = cursor.getColumnIndex("repeat");
                if (timeIndex != -1 && repeatIndex != -1) {
                    String time = cursor.getString(timeIndex);
                    boolean repeat = cursor.getInt(repeatIndex) == 1;

                    // 알람 시간이 현재 시간보다 이전이 아니면 알람을 울립니다.
                    if (isAlarmTimePassed(time)) {
                        if (!repeat) {
                            db.delete("alarms", "time=?", new String[]{time});
                        }
                        startAlarmActivity(time);
                    }
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    // 알람 시간이 현재 시간보다 이전인지 확인하는 메소드
    private boolean isAlarmTimePassed(String time) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date alarmTime = format.parse(time);
            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.setTime(alarmTime);
            Calendar nowCalendar = Calendar.getInstance();
            nowCalendar.set(Calendar.SECOND, 0);
            nowCalendar.set(Calendar.MILLISECOND, 0);

            // 현재 시간이 알람 시간보다 이후인지 확인합니다.
            return nowCalendar.after(alarmCalendar);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 알람 액티비티를 시작하는 메소드
    private void startAlarmActivity(String time) {
        String[] splitTime = time.split(":");
        int hour = Integer.parseInt(splitTime[0]);
        int minute = Integer.parseInt(splitTime[1]);
        Intent intent = new Intent(getActivity(), AlarmActivity.class);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void checkCameraPermission() {
        // 카메라 권한 확인
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우, 권한 요청
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 이미 있는 경우, 카메라 기능을 초기화하거나 다음 단계로 진행
            // ...
        }
    }

}