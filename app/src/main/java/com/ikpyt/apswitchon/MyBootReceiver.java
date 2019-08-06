package com.ikpyt.apswitchon;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

// ПРИМЕЧАНИЕ: начиная с Android 8 так работать не будет возможно

// WakefulBroadcastReceiver гарантирует, что устройство не переходит в спящий режим во время запуска службы
public class MyBootReceiver extends WakefulBroadcastReceiver {
    private final String TAG = "MyBoot";
    public static final int EVENT = 1;

    @Override public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "TerBootReceiver");
        // Запускаем указанную службу при получении этого сообщения
        Intent i = new Intent(context, MyIntentService.class);
        i.putExtra("event", EVENT);
        startWakefulService(context, i);
    }
}
