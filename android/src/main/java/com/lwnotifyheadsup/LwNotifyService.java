package com.lwnotifyheadsup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.io.File;

public class LwNotifyService extends Service {
  private static final String TAG = "LwNotifyService";
  private static Runnable handleTimeout;
  public static Handler callhandle;
  private Integer TIME_OUT_LIMIT= 5000;
  private Bundle bundleData;
  private String uuid = "";
  private boolean isRegistered = false;

  public LwNotifyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

  @Override
  public void onCreate() {
    super.onCreate();
  }


  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);
    stopSelf();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy service");
    cancelTimer();
    stopForeground(true);
    unregisterBroadcastPressEvent();
  }

  public void registerBroadcastPressEvent() {
    if (isRegistered) return;
    IntentFilter filter = new IntentFilter();
    filter.addAction(LwConstants.ACTION_REJECT);
    getApplicationContext().registerReceiver(mReceiver, filter);
    isRegistered = true;
  }

  public void unregisterBroadcastPressEvent() {
    if (!isRegistered) return;
    getApplicationContext().unregisterReceiver(mReceiver);
    isRegistered = false;
  }
  public int onStartCommand(Intent intent, int flags, int startId) {
    String action = intent.getAction();
    if (action != null) {
      if(action.equals(LwConstants.ACTION_SHOW_NOTIFICATION)) {
        ReceiverHandler.updateActionChecks(true);
        registerBroadcastPressEvent();
        Bundle bundle = intent.getExtras();
        uuid= bundle.getString("uuid");
        if(bundle.containsKey("timeout")){
          TIME_OUT_LIMIT=bundle.getInt("timeout");
        }
        Notification notification = buildNotification(getApplicationContext(), intent);
        startForeground(1, notification);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)  {
          sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
      }else if(action.equals(LwConstants.ACTION_HIDE_NOTIFICATION)) {
        stopSelf();
      }
    }
    return START_NOT_STICKY;
  }
  private Notification buildNotification(Context context, Intent intent) {
    Intent emptyScreenIntent = new Intent(context, ReceiverActivity.class);
    Bundle bundle = intent.getExtras();
    bundleData=bundle;
    emptyScreenIntent.putExtras(bundle);
    emptyScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    emptyScreenIntent.setAction(LwConstants.ACTION_ON_PRESS_NOTIFICATION);
    String channelId=bundle.getString("channelId");
    PendingIntent emptyPendingIntent = PendingIntent.getActivity(context, 0, emptyScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    String customSound=bundle.getString("notificationSound");
    Uri sound= RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
    if(customSound != null){
      sound= Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + File.separator + getPackageName() + "/raw/" + customSound);
    }
    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel=new NotificationChannel(channelId, bundle.getString("channelName"), NotificationManager.IMPORTANCE_HIGH);
      notificationChannel.setSound(sound,
        new AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_NOTIFICATION)
          .build());
      notificationChannel.enableLights(true);
      notificationChannel.enableVibration(true);
      notificationChannel.setLightColor(Color.WHITE);
      notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
      notificationChannel.setVibrationPattern(new long[] { 0, 1000, 800});
      notificationManager.createNotificationChannel(notificationChannel);
    }
    NotificationCompat.Builder notificationBuilder;
    notificationBuilder = new NotificationCompat.Builder(context,channelId);
    notificationBuilder.setContentTitle(bundle.getString("name"))
      .setContentText(bundle.getString("info"))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_CALL)
      .setContentIntent(emptyPendingIntent)
      .setSmallIcon(R.mipmap.ic_launcher)
      .addAction(
        0,
        bundle.getString("declineText"),
        onButtonNotificationClick(0,LwConstants.ACTION_REJECT, LwConstants.ACTION_REJECT)
      )
      .addAction(
        0,
        bundle.getString("answerText"),
        onButtonNotificationClick(1,LwConstants.ACTION_ACCEPT,LwConstants.ACTION_ACCEPT)
      )
      .setAutoCancel(true)
      .setOngoing(true)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setVibrate(new long[] { 0, 1000, 800})
      //.setSound(sound)
      .setFullScreenIntent(emptyPendingIntent, true);
//    if(bundle.getString("notificationColor")!=null){
//      notificationBuilder.setColor(getColorForResourceName(context,bundle.getString("notificationColor")));
//    }
//    String iconName = bundle.getString("icon");
//    if (iconName != null) {
//      notificationBuilder.setSmallIcon(getResourceIdForResourceName(context, iconName));
//    }
    if(TIME_OUT_LIMIT > 0){
      setTimeOutEndCall(uuid);
    }
    Notification notification = notificationBuilder.build();
    notification.flags |= Notification.FLAG_INSISTENT;
    return notification;
  }

  public  void setTimeOutEndCall(String uuid) {
    callhandle=new Handler();
    handleTimeout=new Runnable() {
      public void run() {
        if (FullScreenNotification.isNotificationActive) {
          FullScreenNotification.getInstance().destroyActivity();
        }
        WritableMap params = Arguments.createMap();
        if(bundleData.containsKey("payload")){
          params.putString("payload",bundleData.getString("payload"));
        }
        params.putString("callUUID", uuid);
        params.putString("endAction", LwConstants.ACTION_HIDE_NOTIFICATION);
        LwNotifyHeadsupModule.sendEventToJs(LwConstants.ACTION_HIDE_NOTIFICATION,params);
        cancelTimer();
        stopForeground(true);
      }
    };
    callhandle.postDelayed(handleTimeout, TIME_OUT_LIMIT);
  }

  public void cancelTimer(){
    if(handleTimeout != null){
      callhandle.removeCallbacks(handleTimeout);
    }
  }

  private int getResourceIdForResourceName(Context context, String resourceName) {
    int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    if (resourceId == 0) {
      resourceId = context.getResources().getIdentifier(resourceName, "mipmap", context.getPackageName());
    }
    return resourceId;
  }

  private int getColorForResourceName(Context context, String colorPath){
    // java
    Resources res = context.getResources();
    String packageName = context.getPackageName();

    int colorId = res.getIdentifier(colorPath, "color", packageName);
    int desiredColor = ContextCompat.getColor(context, colorId);

    return desiredColor;
  }

  private PendingIntent onButtonNotificationClick(int id, String action,String eventName) {
    if(action == LwConstants.ACTION_REJECT){
      Intent  buttonIntent= new Intent();
      buttonIntent.setAction(action);
      return PendingIntent.getBroadcast(this,id , buttonIntent,PendingIntent.FLAG_UPDATE_CURRENT  | PendingIntent.FLAG_IMMUTABLE);
    }
    Intent emptyScreenIntent = new Intent(this, ReceiverActivity.class);
    emptyScreenIntent.setAction(action);
    emptyScreenIntent.putExtras(bundleData);
    emptyScreenIntent.putExtra("eventName",eventName);
    return PendingIntent.getActivity(this, 0, emptyScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }


  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action != null && action.equals(LwConstants.ACTION_REJECT)) {
            ReceiverHandler.updateActionChecks(true);
      }
    }
  };

}
