package com.lwnotifyheadsup;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public class ReceiverHandler {
  private static final String TAG = "ReceiverHandler";
  private static boolean canPerformClick = true;
  private static boolean canOpenNotification = true;

  static void updateActionChecks(Boolean status){
    canPerformClick=status;
    canOpenNotification=status;
  }

  static void handleNotification(Context context, Intent intent) {
    handleNotificationIntent(context, intent);
  }


  private static void handleNotificationIntent(Context context, Intent intent) {
    if(!canPerformClick) return;
    String action= intent.getAction();
    switch (action) {
      case LwConstants.ACTION_ACCEPT:
        canPerformClick=false;
        handleNotificationActionIntent(context,intent);
        break;
    }
  }

  private static void handleNotificationActionIntent(Context context, Intent intent) {
    Bundle bundle = intent.getExtras();
    String uuid="";
    String eventName="";
    if (bundle != null) {
      if (bundle.containsKey("uuid")) {
        uuid = bundle.getString("uuid");
      }
      if(bundle.containsKey("eventName")){
        eventName=bundle.getString("eventName");
      }
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)  {
      Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
      context.sendBroadcast(it);
    }
    if (FullScreenNotification.isNotificationActive) {
      FullScreenNotification.getInstance().destroyActivity();
    }
    WritableMap params = Arguments.createMap();
    if(bundle.containsKey("payload")){
      params.putString("payload",bundle.getString("payload"));
    }
    params.putBoolean("accept", true);
    LwNotifyHeadsupModule.sendEventToJs(eventName, params);
    context.stopService(new Intent(context, FullScreenNotification.class));
  }

}
