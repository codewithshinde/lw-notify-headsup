package com.lwnotifyheadsup;

import static com.lwnotifyheadsup.LwNotifyUtils.convertMapToJson;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;
import org.json.JSONObject;

@ReactModule(name = LwNotifyHeadsupModule.NAME)
public class LwNotifyHeadsupModule extends ReactContextBaseJavaModule {
  public static final String NAME = "LwNotifyHeadsup";
  public static final String TAG = "LwNotifyHeadsup";
  public static ReactApplicationContext reactContext;

  public LwNotifyHeadsupModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  /*HELPERS*/
  private Context getAppContext() {
    return reactContext.getApplicationContext();
  }

  public Activity getCurrentReactActivity() {
    return this.reactContext.getCurrentActivity();
  }

  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a * b);
  }

  @ReactMethod
  public void displayFullScreenNotification() {
    Intent notificationIntent = new Intent(getAppContext(), FullScreenNotification.class);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
      | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    notificationIntent.setAction(LwConstants.ACTION_ON_PRESS_NOTIFICATION);
    notificationIntent.putExtra("notificationId", "TEST_123");
    getAppContext().startActivity(notificationIntent);
  }

  @ReactMethod
  public void openNotificationActivity(ReadableMap payload) {
    Intent notificationIntent = new Intent(getAppContext(), FullScreenNotification.class);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
      | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    notificationIntent.setAction(LwConstants.ACTION_ON_PRESS_NOTIFICATION);//TODO
    notificationIntent.putExtra("notificationId", payload.getString("notificationId"));
    notificationIntent.putExtra("requestId", payload.getString("requestId"));
    getAppContext().startActivity(notificationIntent);
  }


  @ReactMethod
  public static void sendEventToJs(String eventName, @Nullable WritableMap params) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }


  @ReactMethod
  public void addListener(String eventName) {
    // Keep: Required for RN built in Event Emitter Calls.
  }

  @ReactMethod
  public void removeListeners(Integer count) {
    // Keep: Required for RN built in Event Emitter Calls.
  }


  @ReactMethod
  public void redirectToApp() {
    Context context = getAppContext();
    if (context == null) {
      return;
    }

    String packageName = context.getApplicationContext().getPackageName();
    Intent focusIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

    if (focusIntent != null) {
      focusIntent.addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK |
          Intent.FLAG_ACTIVITY_CLEAR_TASK |
          Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT |
          Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
          Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
      );

      Activity currentActivity = getCurrentActivity();

      if (currentActivity != null) {
        currentActivity.startActivity(focusIntent);
      } else {
        getReactApplicationContext().startActivity(focusIntent);
      }
    }
  }

  @ReactMethod
  public void displayNotification(ReadableMap foregroundOptions) throws JSONException {
    if (foregroundOptions == null) {
      return;
    }
    Log.d(TAG, "displayNotification: invoked");
    Intent intent = new Intent(getReactApplicationContext(), LwNotifyService.class);

    intent.putExtra(LwConstants.KEY_NOTIFICATION_ID, foregroundOptions.getString(LwConstants.KEY_NOTIFICATION_ID));
    intent.putExtra(LwConstants.KEY_NOTIFICATION_TITLE, foregroundOptions.getString(LwConstants.KEY_NOTIFICATION_TITLE));
    intent.putExtra(LwConstants.KEY_NOTIFICATION_INFO, foregroundOptions.getString(LwConstants.KEY_NOTIFICATION_INFO));
    intent.putExtra(LwConstants.KEY_CHANNEL_ID, foregroundOptions.getString(LwConstants.KEY_CHANNEL_ID));
    intent.putExtra(LwConstants.KEY_CHANNEL_NAME, foregroundOptions.getString(LwConstants.KEY_CHANNEL_NAME));
    intent.putExtra(LwConstants.KEY_TIMEOUT, foregroundOptions.getInt(LwConstants.KEY_TIMEOUT));
    intent.putExtra(LwConstants.KEY_ICON, foregroundOptions.getString(LwConstants.KEY_ICON));
    intent.putExtra(LwConstants.KEY_ACCEPT_TEXT, foregroundOptions.getString(LwConstants.KEY_ACCEPT_TEXT));
    intent.putExtra(LwConstants.KEY_REJECT_TEXT, foregroundOptions.getString(LwConstants.KEY_REJECT_TEXT));
    intent.putExtra(LwConstants.KEY_NOTIFICATION_COLOR, foregroundOptions.getString(LwConstants.KEY_NOTIFICATION_COLOR));
    intent.putExtra(LwConstants.KEY_NOTIFICATION_SOUND, foregroundOptions.getString(LwConstants.KEY_NOTIFICATION_SOUND));

    if (foregroundOptions.hasKey(LwConstants.KEY_PAYLOAD)) {
      JSONObject payload = LwNotifyUtils.convertMapToJson(foregroundOptions.getMap(LwConstants.KEY_PAYLOAD));
      intent.putExtra(LwConstants.KEY_PAYLOAD, payload.toString());
    }

    intent.setAction(LwConstants.ACTION_SHOW_NOTIFICATION);
    getReactApplicationContext().startService(intent);
  }

  @ReactMethod
  public void hideNotification() {
    if (FullScreenNotification.isNotificationActive) {
      FullScreenNotification.getInstance().destroyActivity();
    }
    Intent intent = new Intent(getReactApplicationContext(), LwNotifyService.class);
    intent.setAction(LwConstants.ACTION_HIDE_NOTIFICATION);
    getReactApplicationContext().stopService(intent);
  }

}
