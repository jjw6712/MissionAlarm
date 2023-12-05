package com.example.ar1.alarmmission;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.ar1.R;
import com.example.ar1.ui.mission.PushupIntroductionActivity;
import com.example.ar1.ui.mission.QuizIntroductionActivity;
import com.example.ar1.ui.mission.SpeachSentencesIntroductionActivity;
import com.example.ar1.ui.mission.SpeachWordsIntroductionActivity;
import com.example.ar1.ui.mission.SquatIntroductionActivity;

import java.util.List;

public class AlarmMissionListAdapter extends ArrayAdapter<String> {
    private static final int REQUEST_CODE_SQUAT = 1;
    private static final int REQUEST_CODE_PUSHUP = 2;
    private static final int REQUEST_CODE_Speachwords = 3;
    private static final int REQUEST_CODE_Speachsentences = 4;
    private static final int REQUEST_CODE_Quiz = 5;
    private static final int REQUEST_CODE_Pedometer = 6;
    private static final int REQUEST_CODE_Molegame = 7;
    private Activity activity;
    private Context context;
    SharedPreferences sharedPreferences;

    public AlarmMissionListAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
    }
    public AlarmMissionListAdapter(Activity activity, List<String> items) {
        super(activity, 0, items);
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.dafault_mission_list, parent, false);
        }

        String currentItem = getItem(position);

        Button itemButton = itemView.findViewById(R.id.itemButton);
        TextView tvCount = itemView.findViewById(R.id.tvCount); // tvCount는 각 리스트 아이템의 텍스트 뷰의 ID

        itemButton.setText(currentItem);

        itemButton.setOnClickListener(view -> {
            if ("스쿼트".equals(currentItem)) {
                Intent intent = new Intent(context, AlarmSquatIntroductionActivity.class);
                activity.startActivityForResult(intent, REQUEST_CODE_SQUAT); // 예시 요청 코드
            }else if ("푸쉬업".equals(currentItem)) {
                Intent intent = new Intent(context, AlarmPushupIntroductionActivity.class);
                activity.startActivityForResult(intent, REQUEST_CODE_PUSHUP);
            }else if("영단어 발음하기".equals(currentItem)){
                Intent intent = new Intent(context, AlarmSpeachWordsIntroductionActivity.class);
                activity.startActivityForResult(intent, REQUEST_CODE_Speachwords);
            }else if("영문장 발음하기".equals(currentItem)){
                Intent intent = new Intent(context, AlarmSpeachSentencesIntroductionActivity.class);
                activity.startActivityForResult(intent, REQUEST_CODE_Speachsentences);
            }else if("영단어 퀴즈퍼즐".equals(currentItem)){
                Intent intent = new Intent(context, AlarmQuizIntroductionActivity.class);
                activity.startActivityForResult(intent, REQUEST_CODE_Quiz);
            }else if("만보계".equals(currentItem)){
                Intent intent = new Intent(context, AlarmPedometerIntroductionActivity.class);
                activity.startActivityForResult(intent, REQUEST_CODE_Pedometer);
            }else if("두더지게임".equals(currentItem)){
                Intent intent = new Intent(context, AlarmMoleGameIntroductionActivity.class);
                activity.startActivityForResult(intent, REQUEST_CODE_Molegame);
            }
        });

        return itemView;
    }

}