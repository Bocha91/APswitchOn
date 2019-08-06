package com.ikpyt.apswitchon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class MyTimeReceiver extends BroadcastReceiver {
    private final String TAG = "TerTime";
    public static final int REQUEST_CODE = 32180;
    public static final String ACTION = "com.ikpyt.apswitchon.alarm";
    public static final int EVENT = 3;

    // периодически срабатывает по тревоге (запускает службу для запуска задачи)
    @Override
    public void onReceive (Context context, Intent intent){
        Log.d(TAG, "MyTimeReceiver");
        Intent i = new Intent(context, MyIntentService.class);
        i.putExtra("event", EVENT);
        context.startService(i);
/* похоже перестаёт работать если сканить отсюда
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
*/
    }
}
