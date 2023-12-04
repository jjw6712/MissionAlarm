package com.example.ar1.ui.mission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ar1.MLkit.MLkitMotionDemo;
import com.example.ar1.R;

public class PushupIntroductionActivity extends AppCompatActivity {
    ImageButton btBack;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pushup_introduction);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        ImageView imageView = findViewById(R.id.squatGifImageView);
        Glide.with(this).load(R.drawable.pushup).into(imageView);
        btBack = findViewById(R.id.btBack);
        Button startSquatMissionButton = findViewById(R.id.startSquatMissionButton);
        startSquatMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 미션 시작 다이얼로그 표시
                showStartMissionDialog();
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showStartMissionDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        final EditText inputMissionCount = dialogView.findViewById(R.id.editTextMissionCount);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        inputMissionCount.setFilters(new InputFilter[] { inputFilter });

        final AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            String missionCountText = inputMissionCount.getText().toString();
            if (!missionCountText.isEmpty()) {
                int missionCount = Integer.parseInt(missionCountText);
                startSquatMission(missionCount);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void startSquatMission(int missionCount) {
        // 미션 횟수를 저장하고, 미션 수행 액티비티 시작
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selected_defalut_stretching_count_", String.valueOf(missionCount));
        editor.apply();
        // 선택된 미션을 시작
        String selectedOption = "푸쉬업";
        SharedPreferences.Editor editorOption = sharedPreferences.edit();
        editorOption.putString("selected_stretching_default_mode_", selectedOption);
        editorOption.apply();
        Intent intent = new Intent(this, MLkitMotionDemo.class);
        startActivity(intent);
        finish();
    }

    InputFilter inputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (input >= 1 && input <= 999) return null;
                else return "";
            } catch (NumberFormatException e) {
                return "";
            }
        }
    };
}
