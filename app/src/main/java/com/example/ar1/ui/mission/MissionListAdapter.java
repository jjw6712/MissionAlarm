package com.example.ar1.ui.mission;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.ar1.MLkit.MLkitMotionDemo;
import com.example.ar1.R;
import com.example.ar1.edu.Quiz;
import com.example.ar1.edu.SpeachSentences;
import com.example.ar1.edu.SpeachWords;
import com.example.ar1.pedometer.MainActivity;
import com.example.ar1.ui.graph.PushUpInfo;
import com.example.ar1.ui.graph.SquatInfo;

import java.util.List;

public class MissionListAdapter extends ArrayAdapter<String> {

    private Context context;
    SharedPreferences sharedPreferences;

    public MissionListAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
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
                // 스쿼트 미션 수행 방법 소개 화면으로 이동
                Intent intent = new Intent(context, SquatIntroductionActivity.class);
                context.startActivity(intent);
            } else if ("푸쉬업".equals(currentItem)) {
                Intent intent = new Intent(context, PushupIntroductionActivity.class);
                context.startActivity(intent);
            } else if ("파리잡기게임".equals(currentItem)) {
                Intent intent = new Intent(context, FlyingGameIntroductionActivity.class);
                context.startActivity(intent);
            }else if("영단어 발음하기".equals(currentItem)){
                Intent intent = new Intent(context, SpeachWordsIntroductionActivity.class);
                context.startActivity(intent);
            }else if("영문장 발음하기".equals(currentItem)){
                Intent intent = new Intent(context, SpeachSentencesIntroductionActivity.class);
                context.startActivity(intent);
            }else if("영단어 퀴즈퍼즐".equals(currentItem)){
                Intent intent = new Intent(context, QuizIntroductionActivity.class);
                context.startActivity(intent);
            }else if("만보계".equals(currentItem)){
                Intent intent = new Intent(context, PedometerIntroductionActivity.class);
                context.startActivity(intent);
            }else if("두더지게임".equals(currentItem)){
                Intent intent = new Intent(context, MoleGameIntroductionActivity.class);
                context.startActivity(intent);
            }
        });

        return itemView;
    }

}