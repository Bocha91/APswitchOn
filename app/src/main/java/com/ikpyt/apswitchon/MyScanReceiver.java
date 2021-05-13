package com.ikpyt.apswitchon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyScanReceiver extends BroadcastReceiver {
    private final String TAG = "TerScan";

    //public static final int REQUEST_CODE = 1365;
    //public static final String ACTION = "com.ikpyt.apswitchon.alarm";
    public static final int EVENT = 4;

    // периодически срабатывает по тревоге (запускает службу для запуска задачи)
    @Override
    public void onReceive (Context context, Intent intent){
        Log.d(TAG, "MyScanReceiver");
        Intent i = new Intent(context, MyIntentService.class);
        i.putExtra("event", EVENT);
        context.startService(i);
    }
}
