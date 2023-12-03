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
  import android.graphics.Color;
  import android.media.AudioAttributes;
  import android.media.RingtoneManager;
  import android.net.Uri;
  import android.os.Build;
  import android.os.Bundle;
  import android.os.Handler;
  import android.os.IBinder;
  import android.util.Log;

  import androidx.core.app.NotificationCompat;

  import com.facebook.react.bridge.Arguments;
  import com.facebook.react.bridge.WritableMap;

  import java.io.File;
  import java.util.HashMap;
  import java.util.Map;

public class CallNotification extends Service {
  private static final String TAG = "LwNotifyService";
  private static Runnable handleTimeout;
  public static Handler callhandle;
  private Integer DEFAULT_TIME_OUT_LIMIT = 5000;
  private Bundle bundleData;
  private boolean isRegistered = false;

  private Map<String, Integer> soundTypes = new HashMap<String, Integer>() {
    {
      put("type1", R.raw.type1);
      put("type2", R.raw.type2);
      put("type3", R.raw.type3);
      put("type4", R.raw.type4);
    }
  };

  public CallNotification() {
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
    filter.addAction(LwConstants.ACTION_ACCEPT);
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
      if (action.equals(LwConstants.ACTION_SHOW_NOTIFICATION)) {
        ReceiverHandler.updateActionChecks(true);
        registerBroadcastPressEvent();
        showNotification(getApplicationContext(), intent);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
          sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
      } else if (action.equals(LwConstants.ACTION_HIDE_NOTIFICATION)) {
        stopSelf();
      }
    }
    return START_NOT_STICKY;
  }


  private WritableMap getOutputParams() {
    WritableMap params = Arguments.createMap();
    if (bundleData.containsKey(LwConstants.KEY_PAYLOAD)) {
      params.putString(LwConstants.KEY_PAYLOAD, bundleData.getString(LwConstants.KEY_PAYLOAD));
    }
    return params;
  }

  private void showFullScreenNotification(Context context, Intent intent) {
    Intent fullScreenIntent = new Intent(context, FullScreenNotification.class);
    PendingIntent fullScreenPendingIntent = getModifiedIntents(context, intent, fullScreenIntent);

    Intent emptyScreenIntent = new Intent(context, ReceiverActivity.class);
    PendingIntent emptyScreenPendingIntent = getModifiedIntents(context, intent, emptyScreenIntent);

    Notification notification = getNotification(context, fullScreenPendingIntent, fullScreenPendingIntent);
    startForeground(1, notification);
  }

  private void showNotification(Context context, Intent intent) {
    Intent emptyScreenIntent = new Intent(context, ReceiverActivity.class);
    PendingIntent pendingIntent = getModifiedIntents(context, intent, emptyScreenIntent);
    Notification notification = getNotification(context, pendingIntent, pendingIntent);
    startForeground(1, notification);
  }


  private PendingIntent getModifiedIntents(Context context, Intent originalIntent, Intent newIntent) {
    Bundle bundle = originalIntent.getExtras();
    bundleData = bundle;
    newIntent.putExtras(bundle);
    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    newIntent.setAction(LwConstants.ACTION_ON_PRESS_NOTIFICATION);
    return PendingIntent.getActivity(context, 0, newIntent,
      PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }

  private Uri getNotificationSound() {
    try {
      String customSound = bundleData.getString(LwConstants.KEY_NOTIFICATION_SOUND);
      Uri sound = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
      if (soundTypes.containsKey(customSound)) {
        sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + File.separator + getPackageName() + "/raw/" + soundTypes.get(customSound));
      } else if (sound == null) {
        sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      }
      return sound;
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
    }
    return null;
  }

  private void createNewNotificationChannel() {
    String CHANNEL_ID = bundleData.getString(LwConstants.KEY_CHANNEL_ID);
    String CHANNEL_NAME = bundleData.getString(LwConstants.KEY_CHANNEL_NAME);
    createNotificationChannel(CHANNEL_ID, CHANNEL_NAME);
  }

  public void createNotificationChannel(String CHANNEL_ID, String CHANNEL_NAME) {
    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
      Uri notificationSound = getNotificationSound();

      if (notificationSound != null) {
        notificationChannel.setSound(notificationSound,
          new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build());
      }

      notificationChannel.enableLights(true);
      notificationChannel.enableVibration(true);
      notificationChannel.setLightColor(Color.WHITE);
      notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
      notificationChannel.setVibrationPattern(new long[]{0, 1000, 800});
      notificationManager.createNotificationChannel(notificationChannel);
    }
  }

  private Notification getNotification(Context context, PendingIntent initialIntent, PendingIntent finalIntent) {
    String CHANNEL_ID = bundleData.getString(LwConstants.KEY_CHANNEL_ID);
    String NOTIFICATION_TITLE = bundleData.getString(LwConstants.KEY_NOTIFICATION_TITLE);
    String NOTIFICATION_INFO = bundleData.getString(LwConstants.KEY_NOTIFICATION_INFO);
    String ACCEPT_TEXT = bundleData.getString(LwConstants.KEY_ACCEPT_TEXT);
    String REJECT_TEXT = bundleData.getString(LwConstants.KEY_REJECT_TEXT);
    String SHOW_DETAILS  = bundleData.getString(LwConstants.SHOW_VIEW_DETAILS);

    int KEY_TIMEOUT = bundleData.getInt(LwConstants.KEY_TIMEOUT);
    Uri notificationSound = getNotificationSound();

    /*Creating Channel*/
    createNewNotificationChannel();

    /*Notification builder*/
    NotificationCompat.Builder notificationBuilder;
    notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
    notificationBuilder.setContentTitle(NOTIFICATION_TITLE)
      .setContentText(NOTIFICATION_INFO)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_CALL)
      .setContentIntent(initialIntent)
      .setSmallIcon(R.mipmap.ic_launcher)
      .addAction(
        0,
        REJECT_TEXT,
        onButtonNotificationClick(0, LwConstants.ACTION_REJECT, LwConstants.ACTION_REJECT)
      )
      .addAction(
        0,
        ACCEPT_TEXT,
        onButtonNotificationClick(1, LwConstants.ACTION_ACCEPT, LwConstants.ACTION_ACCEPT)
      )
      .setAutoCancel(false)
      .setOngoing(true)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setVibrate(new long[]{0, 1000, 800})
      .setSound(notificationSound);

    if (SHOW_DETAILS.equals("true")) {
      notificationBuilder.addAction(
        0,
        "Show details",
        onButtonNotificationClick(2, LwConstants.ACTION_DETAILS, LwConstants.ACTION_DETAILS)
      );
    }

    if (KEY_TIMEOUT > 0) {
      setTimeOutEndCall();
    }

    Notification notification = notificationBuilder.build();
    notification.flags |= Notification.FLAG_INSISTENT;
    return notification;
  }


  public void setTimeOutEndCall() {
    String NOTIFICATION_ID = bundleData.getString(LwConstants.KEY_NOTIFICATION_ID);
    int KEY_TIMEOUT = bundleData.getInt(LwConstants.KEY_TIMEOUT);
    int TIME_OUT = KEY_TIMEOUT > DEFAULT_TIME_OUT_LIMIT ? KEY_TIMEOUT : DEFAULT_TIME_OUT_LIMIT;

    callhandle = new Handler();
    handleTimeout = new Runnable() {
      public void run() {
        if (FullScreenNotification.isNotificationActive) {
          FullScreenNotification.getInstance().destroyActivity();
        }
        WritableMap params = getOutputParams();
        params.putString(LwConstants.KEY_NOTIFICATION_ID, NOTIFICATION_ID);
        //TODO
        params.putString(LwConstants.END_ACTION, LwConstants.ACTION_HIDE_NOTIFICATION);
        LwNotifyHeadsupModule.sendEventToJs(LwConstants.ACTION_HIDE_NOTIFICATION, params);
        cancelTimer();
        stopForeground(true);
      }
    };
    callhandle.postDelayed(handleTimeout, TIME_OUT);
  }

  public void cancelTimer() {
    if (handleTimeout != null) {
      callhandle.removeCallbacks(handleTimeout);
    }
  }

  private PendingIntent onButtonNotificationClick(int id, String action, String eventName) {
    /*OPEN_APP, ACCEPT & REJECT ACTIONS*/
    Log.println(Log.INFO, TAG, "OPEN_APP, ACCEPT, REJECT_ACTION");
    Intent buttonIntent = new Intent();
    buttonIntent.setAction(action);
    return PendingIntent.getBroadcast(this, id, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }


  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action != null) {
        String NOTIFICATION_ID = bundleData.getString(LwConstants.KEY_NOTIFICATION_ID);
        WritableMap params = getOutputParams();
        params.putString(LwConstants.KEY_NOTIFICATION_ID, NOTIFICATION_ID);
        params.putString(LwConstants.END_ACTION, action);
        cancelTimer();
        if(action.equals(LwConstants.ACTION_REJECT)) {
          LwNotifyHeadsupModule.sendEventToJs(LwConstants.RNNotifyRejectAction, params);
        } else if (action.equals(LwConstants.ACTION_ACCEPT)) {
          LwNotifyHeadsupModule.sendEventToJs(LwConstants.RNNotifyAcceptAction, params);
        } else  if(action == LwConstants.ACTION_DETAILS) {
          LwNotifyHeadsupModule.sendEventToJs(LwConstants.RNNotifyOpenAppAction, params);
        }
        stopForeground(true);
      }
    }
  };

}
