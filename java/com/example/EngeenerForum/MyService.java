package com.example.EngeenerForum;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;



public class MyService extends Service {
    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;
    private static final String NOTIFICATION_CHANNEL_ID ="1" ;

    String notif="0";
    String oldnotif="0";
    final String LOG_TAG = "myLogs";
    Handler handler;
    Runnable runnable;

    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        handler = new Handler();
        runnable =new Runnable() {
            @Override
            public void run() {
                someTask();
                handler.postDelayed(runnable, 5000);
            }
        };
        runnable.run();


        return super.onStartCommand(intent, flags, startId);
    }
    void someTask() {
//-----------------------------------------------------------------------------
        new Thread() {
            public void run() {
                final JSONObject json = RemoteFetch.getJSON();

                Log.d(LOG_TAG, "render: " + json);

        //----------------------------------------------------------------------
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getApplicationContext(),"нет данных",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            renderJson(json);

                        }
                    });
                }
            }
        }.start();


       // stopSelf();
    }
    private void renderJson(JSONObject json){
       String number ;
         try {
      number=json.getString("count");
      for(int count=0; count<Integer.parseInt(number); count++) {
          notif = json.getJSONObject(String.valueOf(count)).getString("sent");
          if (oldnotif.equals("0")   && notif.equals("1")  ) {
              oldnotif = "1";
              sendNotif();
          }
          if (notif.equals("0") ) {
              oldnotif = "0";
          }
      }
         }catch(Exception e){
             Log.e(LOG_TAG, "One or more fields not found in the JSON data");
         }
     }

    //-----------------------------уведомления----------------------------------------------------------


    void sendNotif() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 800, 300, 800});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
            Notification.Builder  builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("EngeenerForum")
                    .setContentText("Получено новое сообщение") // Текст уведомления
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true); // автоматически закрыть уведомление после нажатия
            Notification notification =builder.build();

            notificationManager.notify(NOTIFY_ID, notification);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Notification.Builder  builder = new Notification.Builder(this);
            builder.setContentIntent(contentIntent)

                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("EngeenerForum")
                    .setContentText("Получено новое сообщение") // Текст уведомления
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true); // автоматически закрыть уведомление после нажатия
            Notification notification = null;


                notification = builder.build();



            notificationManager.notify(NOTIFY_ID, notification);

        }
    }
        //-----------------------------уведомления---------------------------------------------------------


    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }


    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }
}
