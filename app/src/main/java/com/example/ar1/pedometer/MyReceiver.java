package com.example.ar1.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyReceiver extends BroadcastReceiver {
    /**
     * Context : 애플리케이션별 리소스 및 서비스에 액세스하는 방법을 제공
     * Intent : 애플리케이션의 다른 구성 요소에서 작업을 요청하는데 사용되는 메시징 개체,
     * 활동이나 서비스를 시작하거나 애플리케이션의 다른 부분이나 다른 애플리케이션에 메시지를 브로드캐스트하는데 사용
     **/
    private TextView count;

    public MyReceiver(TextView count){
        this.count = count;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 지정된 시간에 적용할 알고리즘 작성
        Log.d("my receiver", "만보계 카운트 초기화 실행 됨");
        resetCounts(context);
        if (count != null) {
            count.setText("0");
        }
    }

    private void resetCounts(Context context) {

        // Get the shared preferences to store counts
        SharedPreferences preferences = context.getSharedPreferences("StepCountPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Reset the counts to 0
        editor.putInt("stepCount", 0);
        editor.apply();

        // Update the UI (you may want to send a broadcast or use other mechanisms to update the UI in your activity)
        Intent updateIntent = new Intent("STEP_COUNT_UPDATED");
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);

    }
}
