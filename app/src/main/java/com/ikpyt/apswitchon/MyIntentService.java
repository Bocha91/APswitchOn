package com.ikpyt.apswitchon;

import android.app.AlarmManager;
import android.app.IntentService;
//import android.app.Notification;
//import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import java.util.List;
//import android.widget.Toast;

import static android.os.SystemClock.elapsedRealtime;
/*
RedmiTer
xtyhfde467
*/

public class MyIntentService extends IntentService {
    static final String TAG = "TerService";
    // уведомления которыее выдвигаются пальцем из под верхней части экрана
    //private static final int NOTIFICATION_ID = 1;
    //private NotificationManager mNotificationManager;
    // для WiFi
    boolean success = false;
    int len;

    public MyIntentService()
    {
        super("MyIntentService");
        //Log.d(TAG, "MyIntentService");
    }
    public void onCreate() {
        super.onCreate();
        //Log.d(TAG, "onCreate");
        //mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    @Override
    public void onDestroy() {
        //Log.d(TAG, "onDestroy");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        int event = intent.getIntExtra("event",0);
        Log.d(TAG, "onHandleIntent("+event+")");
        SharedPreferences sPref;
        WifiManager wifiManager;
        WifiConfiguration wifiConfig;
        String ssid;
        WifiInfo wifiInfo;

        switch(event)
        {
            case MyBootReceiver.EVENT: // 1 BOOT
                // снять блокировку пробуждения, чтобы после завершения сервиса система могла заснуть
                WakefulBroadcastReceiver.completeWakefulIntent(intent);
                //scheduleAlarm(30000);
                Log.d(TAG, "стартовали после BOOT");
                //break;

            case MainActivity.EVENT: // 2 onResume MainActivity
                //scheduleAlarm(30000);
                //break;

            case MyTimeReceiver.EVENT: // 3 TIME
                scheduleAlarm(30000);

                // проверим проверим что задана сеть для подключения
                sPref = getSharedPreferences(MainActivity.SAVED_FILE,MODE_PRIVATE);
                if( sPref.contains(MainActivity.SAVED_SSID)==false ) { // в базе нет данных
                    Log.d(TAG, "не задана сеть для подключения!");
                    break;
                }
                // проверить включен ли WiFi и включить если не велючен
                // проверить к какой AP подключен?
                //  - к нашей то ничего не делаем
                //  -  не к нашей то запускаем поиск

                // проверить включен ли WiFi и включить если не велючен
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()==false) {
                    if (wifiManager.setWifiEnabled(true) == false) {
                        Log.d(TAG, "Немогу включить WiFi!");
                        break; // не включается
                    }
                }
                // проверить к какой AP подключен?
                //  - к нашей то ничего не делаем
                ssid = sPref.getString(MainActivity.SAVED_SSID, "");
                // создадим конфигурацию сети куда хотим подключиться (только имя)
                wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", ssid);
                // проверить к какой AP подключены?
                wifiInfo = wifiManager.getConnectionInfo();
                //Log.d(TAG, "\nSSID:"+wifiInfo.getSSID()+"== "+wifiConfig.SSID);
                if( wifiInfo.getSSID().equals(wifiConfig.SSID) ){
                    Log.d(TAG,"уже подключены куда надо:"+wifiInfo.getSSID() );
                    break; // уже подключены куда надо, уходим
                }
/*
                // ----------- проверить включен ли редим AP точка доступа ------------
                Boolean fl =ApManager.isApOn(getApplicationContext());
                Log.d(TAG, "Точка доступа включена?:"+fl.toString());
                if(fl==false)  ...
*/
                //  -  не к нашей то запускаем поиск
                success = wifiManager.startScan();
                if(success==false){
                    Log.d(TAG,"startScan отказался работать!" );
                }
/*s
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // поиск сети в эфире
                List<ScanResult> wifiScanList1 = wifiManager.getScanResults();
                StringBuilder info = new StringBuilder();
                // вывод списка обнаруженных WiFi сетей
                info.append("\n \nИщем: "+ssid);
                len = wifiScanList1.size();
                for (int i = 0; i < len; i++) {
                    ScanResult ap = wifiScanList1.get(i);
                    info.append(String.format("\n %6d ", ap.level)).append(ap.SSID);
                }
                info.append("\n");
                Log.d(TAG,info.toString() );
*/

                break;
            case MyScanReceiver.EVENT: // 4 SCAN

                // проверим проверим что задана сеть для подключения
                sPref = getSharedPreferences(MainActivity.SAVED_FILE,MODE_PRIVATE);
                if( sPref.contains(MainActivity.SAVED_SSID)==false ) { // в базе нет данных
                    Log.d(TAG, "не задана сеть для подключения!");
                    return;
                }
                ssid = sPref.getString(MainActivity.SAVED_SSID, "");

                // поиск сети в эфире
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                List<ScanResult> wifiScanList = wifiManager.getScanResults();
                //StringBuilder info = new StringBuilder();
                Boolean scanOK = false;
                // вывод списка обнаруженных WiFi сетей
                //info.append("\n \n???"+ssid+"\n");
                len = wifiScanList.size();
                for (int i = 0; i < len; i++) {
                    ScanResult ap = wifiScanList.get(i);
                    if(ap.SSID.equals(ssid)) {
                        //info.append("\n  !!!!!!!!   "); // вижу требуемую сеть
                        scanOK=true;
                    }
                    //info.append(String.format("\n %6d ", ap.level)).append(ap.SSID)/*.append(" ").append(ap.BSSID)*/;
                }
                //info.append("\n");
                //Log.d(TAG,info.toString() );

                if( scanOK == false ) {
                    Log.d(TAG,"Сети не видно" );
                    break; // валим
                }
                // подключаемся к нашей сети

                // создаём конфигурацию сети куда хотим подключиться (только имя)
                wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", ssid);

                // проверить к какой AP подключены?
                wifiInfo = wifiManager.getConnectionInfo();
                //Log.d(TAG, "\nwifiInfo.getSSID() "+wifiInfo.getSSID());
                if( wifiInfo.getSSID().equals(wifiConfig.SSID) ){
                    Log.d(TAG,"уже подключены куда надо: "+wifiInfo.getSSID() );
                    break; // уже подключены куда надо, уходим
                }

                // проверить setup - наличие требуемой сети
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                int networkId = -1;
                for( WifiConfiguration i : list ) {
                    Log.d(TAG, "\nSSID="+i.SSID+" networkId="+i.networkId);
                    if(i.SSID != null && i.SSID.equals(wifiConfig.SSID)) {
                        networkId = i.networkId;
                        break;
                    }
                }

                // если требуемой сети нет в setup то добавим её туда
                if(networkId == -1){
                    //Log.d(TAG, wifiConfig.toString());
                    // вспомним пароль
                    String password = sPref.getString(MainActivity.SAVED_PASS, "");
                    wifiConfig.preSharedKey = String.format("\"%s\"", password);
                    // добавляем сеть
                    networkId = wifiManager.addNetwork(wifiConfig);
                    Log.d(TAG, "addNetwork= "+ networkId);
                }

                // подключиться к требуемой сети
                wifiManager.disconnect();
                Boolean res = wifiManager.enableNetwork(networkId, true);
                Log.d(TAG, "\nПодключение!!! "+res);
                //wifiManager.reconnect();

                //notif();
                break;
            case MainActivity.EVENT_STOP: // 5 onResume MainActivity
                cancelAlarm();
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

                break;

            default:
                break;
        }
    }
/*
    public void notif()
    {
        //----------------------- уведомление --------------------------------------------
        // формируем уведомление
        //int note = ((i==0)||(i==9))? Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE : Notification.BADGE_ICON_NONE ;
        int note = Notification.DEFAULT_SOUND;
        //String notificationText = String.valueOf( (100 * i / 10))+ " %";
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Progress :" + len)
                //.setContentText(notificationText)
                .setTicker("Notification!")
                .setWhen(System.currentTimeMillis())
                //.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE )
                .setDefaults(note)
                .setAutoCancel(true).setSmallIcon(R.mipmap.ic_launcher)
                .build();
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }
*/
    //----------------------------------Переодический таймер---------------------------------------------
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
        //alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime(), period, pIntent);
        alarm.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsedRealtime()+period, pIntent);
    }
    // После установки будильника, если мы хотим отменить будильник, мы можем сделать это с помощью:
    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), MyTimeReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyTimeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
}