package com.example.ar1;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StopWatchT extends AppCompatActivity {
    long i;
    private boolean isTimerRunning = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopwatch_title);

        TextView ttv = findViewById(R.id.T_tv);
        Button start = findViewById(R.id.T_start_btn);
        Button delete = findViewById(R.id.T_delete_btn);
        Button back = findViewById(R.id.exit_btn);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StopWatchT.this, BottomActivity.class);
                startActivities(new Intent[]{intent});
            }
        });
        //스톱워치 시작/정지
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start.getText()=="시  작"){
                    Timer(i);
                    start.setText("정  지");
                }else{
                    isTimerRunning = true;
                    Timer(i);
                    start.setText("시  작");
                }


            }

        });

        //스톱워치 초기화
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTimerRunning = true;
                Timer(0);
                i=0;
                ttv.setText("00:00:00:00");
            }
        });
        //스톱워치 시간설정(추후추가)

    }

    private void Timer(long in) {
        // 타이머 시간 표시 계속 받아오기
        if (!isTimerRunning) {
            isTimerRunning = true;
            TextView tv = findViewById(R.id.T_tv);
            long mil = SystemClock.elapsedRealtime();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isTimerRunning) {
                        long mi = SystemClock.elapsedRealtime() - mil + in;
                        final String time = String.format("%02d:%02d:%02d:%02d", (mi/3600000), ((mi/60000)%60), ((mi/1000)%60), ((mi/10)%100));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText(time);
                                i=mi;
                            }
                        });
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
    // 안좋은예
    /*private void Timer(int i) {
        // 타이머 시간 표시 계속 받아오기
        int mili = AlarmManager.ELAPSED_REALTIME;
        if (i == 0) {
            TextView tv = findViewById(R.id.T_tv);
            int mil = AlarmManager.ELAPSED_REALTIME;
            int mi = mil -mili;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    tv.setText( (mi/360000)+ ":" +(mi/60000)+ ":" + (mi/10));
                    // 타이머 정지
                    if (i == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setText((mi/360000) + ":" + (mi/60000) + ":" + (mi/10));
                                onStop();
                            }
                        });
                    }
                }
            }).start();



            }
        }*/
    }



