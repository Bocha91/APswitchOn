package com.ikpyt.apswitchon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import java.util.List;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;



/******** Известные проблемы ***************
  Если приложение (активити) смахнуть то сервис перестаёт получать сканы
 проверено на Андроид 7
********************************************/



/*
TYPT_BOOT   = 1;
TYPT_BUTTON = 2;
TYPT_TIME   = 3;
TYPT_SCAN   = 4;
*/

public class MainActivity extends AppCompatActivity {
    private final String TAG = "TerActivity";
    public static final int EVENT = 2;
    public static final int EVENT_STOP = 5;

    //long old_timest = 0;
    //Button button;
    //TextView mInfoTextView;
    EditText SSIDText;
    EditText PasswordText;
    EditText PauseText;
    SharedPreferences sPref;
    final static String SAVED_FILE = "TerSave";
    final static String SAVED_SSID = "saved_ssid";
    final static String SAVED_PASS = "saved_passvord";
    final static String SAVED_PERI = "saved_period";

    final static String DEF_SSID = "AP___setup";
    final static String DEF_PASS = "12345678";
    final static String DEF_PERI = "45";

/*

    // --------------- для приёма данных от MyIntentService -----------------------------------
    private Intent mMyServiceIntent;
    private MyBroadcastReceiver mMyBroadcastReceiver;
    private UpdateBroadcastReceiver mUpdateBroadcastReceiver;
*/
    public  static final boolean IS_PRE_M_ANDROID = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1000;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        SSIDText     = (EditText) findViewById(R.id.editText2);
        PasswordText = (EditText) findViewById(R.id.editText);
        PauseText    = (EditText) findViewById(R.id.editText3);
/*
        // --------------- приём данных от MyIntentService -----------------------------------
        mMyBroadcastReceiver = new MyBroadcastReceiver();
        mUpdateBroadcastReceiver = new UpdateBroadcastReceiver();
        // регистрируем BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter( MyIntentService.ACTION_MYINTENTSERVICE );
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mMyBroadcastReceiver, intentFilter);
        // Регистрируем второй приёмник Update
        IntentFilter updateIntentFilter = new IntentFilter( MyIntentService.ACTION_UPDATE );
        updateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mUpdateBroadcastReceiver, updateIntentFilter);
*/
        // данные формы
        loadText();
    }

    void saveText(String ssid,String passw,String period) {
        sPref = getSharedPreferences(SAVED_FILE, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_SSID, ssid);
        ed.putString(SAVED_PASS, passw);
        ed.putString(SAVED_PERI, period);
        ed.commit();
    }
    public void buttonDefault(View v) {
        saveText(DEF_SSID,DEF_PASS,DEF_PERI);
        loadText();
    }

    void loadText() {
        // файлы настроек хранятся в каталоге /data/data/имя_пакета/shared_prefs/

        sPref = getSharedPreferences(SAVED_FILE,MODE_PRIVATE);
        if( sPref.contains(SAVED_SSID)==true ){ // в базе нет данных
            SSIDText.    setText(sPref.getString(SAVED_SSID, DEF_SSID));
            PasswordText.setText(sPref.getString(SAVED_PASS, DEF_PASS));
            PauseText.   setText(sPref.getString(SAVED_PERI, DEF_PERI));
        }
        //Toast.makeText(this, "Text loaded", Toast.LENGTH_SHORT).show();
    }
    public void buttonSave(View v) {
/*
        sPref = getSharedPreferences(SAVED_FILE,MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_SSID, SSIDText    .getText().toString());
        ed.putString(SAVED_PASS, PasswordText.getText().toString());
        ed.putString(SAVED_PERI, PauseText   .getText().toString());
        ed.commit();
*/
        String ssid   = SSIDText.getText().toString();
        String passw  = PasswordText.getText().toString();
        String period = PauseText.getText().toString();



        saveText(SSIDText.getText().toString()
                ,PasswordText.getText().toString()
                ,PauseText.getText().toString());

        Toast.makeText(this, "Text saved", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Save: SSID="+ssid+" PASS="+passw+" period="+period
        );
    }

    @Override protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

        // вывожу список известных сетей
        StringBuilder info = new StringBuilder();
        info.append(" \nСписок известных сетей:");
        List<WifiConfiguration> list = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            //Log.d(TAG, "\n"+i.toString()+"\npassword:"+i.preSharedKey);
            info.append("\n").append(i.SSID);

                    /*if(i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                        wifiManager.disconnect();
                        networkOnePresent = wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        Log.d("wifi", "net id is = "+netOneId);
                        break;
                    }*/
        }
        Log.d(TAG, info.toString());
    }

    @Override protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //registerReceiver( wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) );

        if (!this.isFineOrCoarseLocationPermissionGranted()) {
            requestCoarseLocationPermission();
        } else if (isFineOrCoarseLocationPermissionGranted() || IS_PRE_M_ANDROID) {
            startWifiAccessPointsSubscription();
        }

        // сообщаем сервису что мы проснулись
        final Context context = getApplicationContext();
        Intent i = new Intent(context, MyIntentService.class);
        i.putExtra("event", EVENT);
        context.startService(i);
    }

    @Override protected void onPause() {
        Log.d(TAG, "onPause");
        //context.unregisterReceiver(receiver);
        super.onPause();
    }

    @Override protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }

    @Override protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
/*
        // --------------- приём данных от MyIntentService -----------------------------------
        // отключаем регистрацию Broadcast
        unregisterReceiver(mMyBroadcastReceiver);
        unregisterReceiver(mUpdateBroadcastReceiver);
*/
    }
/*
    // --------------- приём данных от MyIntentService -----------------------------------
    // принимаем результат от MyIntentService
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(MyIntentService.EXTRA_KEY_OUT);
            Long timest = intent.getLongExtra(MyIntentService.EXTRA_KEY_TIME, 0);
            mInfoTextView.setText(mInfoTextView.getText() + result + "\ntime=" + (timest - old_timest) / 1000 + "\n");
            if (mMyServiceIntent != null) {
                stopService(mMyServiceIntent);
                mMyServiceIntent = null;
            }
            old_timest = timest;
        }
    }
    // принимам промежутечные данные от MyIntentService
    public class UpdateBroadcastReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            int update = intent.getIntExtra(MyIntentService.EXTRA_KEY_UPDATE, 0);
            mInfoTextView.setText(mInfoTextView.getText() + "\nUpdate=" + update);
        }
    }
*/
/*
    //----------------------------------Переодический таймер---------------------------------------------
    // настраиваем повторяющийся будильник на каждые ХХ секунд который будеи запускать наш сервис MyIntentService
    public void scheduleAlarm(long period) {
        // Создаем намерение, которое будет выполнять AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), MyTimeReceiver.class);
        // Создать PendingIntent, который будет срабатывать по истечении времени будильника
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyTimeReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // С этого момента настраиваем периодическую сигнализацию каждые XX секунд
        //long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // Первый параметр - это тип: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Интервал может быть INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        //alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_HALF_HOUR, pIntent);
        //alarm.setExact(AlarmManager.RTC_WAKEUP, firstMillis+15000, pIntent); // будит один раз через 15 секунд
        //alarm.setRepeating (AlarmManager.RTC_WAKEUP, firstMillis,15000, pIntent); // разбудит сразу и потом каждые 15 секунд, реально интервалы от балды
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime(), period, pIntent);
        //alarm.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime()+period, pIntent);
    }
    // После установки будильника, если мы хотим отменить будильник, мы можем сделать это с помощью:
    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), MyTimeReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyTimeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
*/
    public void buttonStop(View v) {
        // сообщаем сервису остановить всё
        final Context context = getApplicationContext();
        Intent i = new Intent(context, MyIntentService.class);
        i.putExtra("event", EVENT_STOP);
        context.startService(i);


        //cancelAlarm();
        //
        //unregisterReceiver(MyTimeReceiver.class);
        //MyBootReceiver
        //MyScanReceiver
        /* // похоже что программу нужно будет переустанавливать после такого
        final Context context = getApplicationContext();
        disableBroadcastReceiver(MyBootReceiver.class,context);
        disableBroadcastReceiver(MyTimeReceiver.class,context);
        disableBroadcastReceiver(MyScanReceiver.class,context);
        */
    }
    public void disableBroadcastReceiver(Class<? extends BroadcastReceiver> cls,final Context context){
        final ComponentName receiver = new ComponentName(this, cls);
        final int status = context.getPackageManager().getComponentEnabledSetting(receiver);
        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            context.getPackageManager()
                    .setComponentEnabledSetting(receiver,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
            Toast.makeText(this, "Disabled " + cls.getName(), Toast.LENGTH_LONG).show();
        }
    }
    //------------------Получение разрешения от пользователя---------------------------
    private void startWifiAccessPointsSubscription() {

        boolean fineLocationPermissionNotGranted = ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED;
        boolean coarseLocationPermissionNotGranted = ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED;

        if (fineLocationPermissionNotGranted && coarseLocationPermissionNotGranted) {
            return;
        }

        if (!AccessRequester.isLocationEnabled(this)) {
            AccessRequester.requestLocationAccess(this);
            return;
        }
    }
    private void requestCoarseLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
    }
    private boolean isFineOrCoarseLocationPermissionGranted() {
        boolean isAndroidMOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        boolean isFineLocationPermissionGranted = isGranted(ACCESS_FINE_LOCATION);
        boolean isCoarseLocationPermissionGranted = isGranted(ACCESS_COARSE_LOCATION);

        return isAndroidMOrHigher && (isFineLocationPermissionGranted
                || isCoarseLocationPermissionGranted);

    }
    private boolean isGranted(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED;
    }
/*
    public static void getWpaConfig(Context context, String SSID, String Passphrase, boolean Hidden)
    {
        WifiManager wfMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // Включаем WiFi при необходимости
        if (!wfMgr.isWifiEnabled())
            wfMgr.setWifiEnabled(true);
        // Не продолжать, если мы не можем войти в хорошее состояние или если на этом устройстве уже есть хотя бы одна конфигурация WifiConfiguration
        List<WifiConfiguration> cfgList = wfMgr.getConfiguredNetworks();
        //if (!wfMgr.isWifiEnabled() || cfgList == null || cfgList.size() > 0)
            //return;

        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = "\"".concat(SSID).concat("\"");
        wfc.preSharedKey = "\"".concat(Passphrase).concat("\"");
        wfc.hiddenSSID = Hidden;

        int networkId = wfMgr.addNetwork(wfc);
        if (networkId != -1) {
            wfMgr.enableNetwork(networkId, true);
            // Use this to permanently save this network
            // wfMgr.saveConfiguration();
        }
    }
*/



}


