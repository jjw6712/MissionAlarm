package com.example.ar1.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MidnightResetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity activity = (MainActivity) context;
        activity.resetData();
    }
}

