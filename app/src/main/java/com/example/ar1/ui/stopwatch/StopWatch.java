package com.example.ar1.ui.stopwatch;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ar1.databinding.FragmentStopwatchBinding;

public class StopWatch extends Fragment {

    private FragmentStopwatchBinding binding;
    private boolean isTimerRunning = false;
    private int i = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StopWatchViewModel dashboardViewModel =
                new ViewModelProvider(this).get(StopWatchViewModel.class);

        binding = FragmentStopwatchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView ttv = binding.TTv;
        Button start = binding.TStartBtn;
        Button delete = binding.TDeleteBtn;

        // 스톱워치 시작/정지
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start.getText().equals("시  작")) {
                    Timer(i);
                    start.setText("정  지");
                } else {
                    isTimerRunning = true;
                    Timer(i);
                    start.setText("시  작");
                }
            }
        });

        // 스톱워치 초기화
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTimerRunning = true;
                Timer(0);
                i = 0;
                ttv.setText("00:00:00:00");
            }
        });

        return root;
    }

    private void Timer(long in) {
        // 타이머 시간 표시 계속 받아오기
        if (!isTimerRunning) {
            isTimerRunning = true;
            TextView tv = binding.TTv;
            long mil = SystemClock.elapsedRealtime();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isTimerRunning) {
                        long mi = SystemClock.elapsedRealtime() - mil + in;
                        final String time = String.format("%02d:%02d:%02d:%02d", (mi / 3600000), ((mi / 60000) % 60), ((mi / 1000) % 60), ((mi / 10) % 100));
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(time);
                                    i = (int) mi;
                                }
                            });
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } else {
            isTimerRunning = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}