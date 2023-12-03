package com.example.ar1.alarmmission;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ar1.R;
import com.example.ar1.edu.SpeachSentences;

public class AlarmSpeachSentencesIntroductionActivity extends AppCompatActivity {
    ImageButton btBack;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speach_sentences_introduction);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.black));
        VideoView videoView = findViewById(R.id.videoView);
        String videoFileName = "speachwords"; // 동영상 파일 이름

        int videoResId = getResources().getIdentifier(videoFileName, "raw", getPackageName());
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videoResId));

// 동영상 재생이 완료될 때 호출되는 리스너 설정
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 동영상 재생이 완료되면 다시 시작
                videoView.start();
            }
        });

// 동영상 재생 시작
        videoView.start();
        btBack = findViewById(R.id.btBack);
        Button startSquatMissionButton = findViewById(R.id.startSquatMissionButton);
        startSquatMissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 미션 시작 다이얼로그 표시
                showDifficultyDialog();
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void showDifficultyDialog() {
        // 커스텀 다이얼로그 레이아웃을 설정
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.edu_custom_dialog);

        // 커스텀 다이얼로그 내부의 버튼 및 라디오 그룹 등의 요소를 찾아옴
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        Button buttonConfirm = dialog.findViewById(R.id.buttonConfirm);

        // "확인" 버튼 클릭 시 다이얼로그를 닫음
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //취소 버튼 클릭 시
        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        // "확인" 버튼 클릭 시 선택한 난이도 처리
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 여기에서 선택한 난이도에 따라 작업을 수행할 수 있습니다.
                // 선택한 난이도를 사용하여 다음 액티비티로 전달할 수도 있습니다.
                RadioGroup radioGroupLevels = dialog.findViewById(R.id.radioGroupLevels);
                int selectedId = radioGroupLevels.getCheckedRadioButtonId();
                String selectedLevel = "";

                switch (selectedId) {
                    case R.id.radioElementary:
                        selectedLevel = "elementary";
                        break;
                    case R.id.radioMiddle:
                        selectedLevel = "middle";
                        break;
                    case R.id.radioCollege:
                        selectedLevel = "college";
                        break;
                }

                // 결과를 반환하고 액티비티 종료
                Log.d("SelectedLevel", "Selected level: " + selectedLevel); // 로그 추가

                Intent returnIntent = new Intent();
                returnIntent.putExtra("missionName", "영문장 발음하기");
                returnIntent.putExtra("missionCount", selectedLevel.toString()); // toString() 제거
                setResult(RESULT_OK, returnIntent);
                finish();


                dialog.dismiss();
            }
        });

        // 커스텀 다이얼로그를 표시
        dialog.show();
    }
}
