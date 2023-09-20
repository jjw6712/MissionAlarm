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

        itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("스쿼트".equals(currentItem) || "푸쉬업".equals(currentItem)) {
                    // 다이얼로그 레이아웃을 인플레이트
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View dialogView = inflater.inflate(R.layout.custom_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(dialogView);

                    // 레이아웃 내의 뷰 참조
                    final EditText inputMissionCount = dialogView.findViewById(R.id.editTextMissionCount);
                    Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
                    Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
                    // EditText에 InputFilter 적용
                    inputMissionCount.setFilters(new InputFilter[] { inputFilter });

                    final AlertDialog dialog = builder.create();

                    buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss(); // 다이얼로그 닫기
                        }
                    });

                    buttonConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String missionCountText = inputMissionCount.getText().toString();
                            if (!missionCountText.isEmpty()) {
                                int missionCount = Integer.parseInt(missionCountText);
                                // 미션 횟수를 저장
                                SharedPreferences sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("selected_defalut_stretching_count_", String.valueOf(missionCount));
                                editor.apply(); // 변경사항 저장

                                // 선택된 미션을 시작
                                String selectedOption = currentItem;
                                SharedPreferences.Editor editorOption = sharedPreferences.edit();
                                editorOption.putString("selected_stretching_default_mode_", selectedOption);
                                editorOption.apply();

                                // 액티비티 시작
                                Intent intent = new Intent(context, MLkitMotionDemo.class);
                                context.startActivity(intent);

                                dialog.dismiss(); // 다이얼로그 닫기
                            }
                        }
                    });

                    dialog.show();
                }
            }
        });

        return itemView;
    }
    // EditText에 입력할 수 있는 범위를 1부터 999로 제한하는 InputFilter 생성
    InputFilter inputFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                // 입력된 값과 기존 값(혹은 빈 값)을 합쳐서 숫자로 파싱
                int input = Integer.parseInt(dest.toString() + source.toString());

                // 범위를 1부터 999로 제한
                if (input >= 1 && input <= 999) {
                    return null; // 입력 허용
                } else {
                    return ""; // 입력 무시
                }
            } catch (NumberFormatException e) {
                // 숫자로 파싱할 수 없는 경우, 입력 무시
                return "";
            }
        }
    };
}