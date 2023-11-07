package com.lwnotifyheadsup;

import static com.lwnotifyheadsup.LwNotifyUtils.convertMapToJson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    notificationIntent.setAction(LwConstants.ACTION_ON_PRESS_NOTIFICATION);
    notificationIntent.putExtra("notificationId", "TEST_123");
    getAppContext().startActivity(notificationIntent);
  }

  @ReactMethod
  public static void sendEventToJs(String eventName,@Nullable WritableMap params) {
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
  public void displayNotification(String uuid, @Nullable String avatar,@Nullable int timeout, ReadableMap foregroundOptions) throws JSONException {
    if(foregroundOptions == null){
      return;
    }
    Log.d(TAG, "displayNotification: invoked");
    Intent intent = new Intent(getReactApplicationContext(), LwNotifyService.class);
    intent.putExtra("uuid", uuid);
    intent.putExtra("name", "SHAZAMMMM...." );
    intent.putExtra("avatar", avatar);
    intent.putExtra("info", foregroundOptions.getString("notificationBody"));
    intent.putExtra("channelId", foregroundOptions.getString("channelId"));
    intent.putExtra("channelName", foregroundOptions.getString("channelName"));
    intent.putExtra("timeout", timeout);
    intent.putExtra("icon",foregroundOptions.getString("notificationIcon"));
    intent.putExtra("answerText",foregroundOptions.getString("answerText"));
    intent.putExtra("declineText",foregroundOptions.getString("declineText"));
    intent.putExtra("notificationColor",foregroundOptions.getString("notificationColor"));
    intent.putExtra("notificationSound",foregroundOptions.getString("notificationSound"));
    intent.putExtra("mainComponent",foregroundOptions.getString("mainComponent"));
    if(foregroundOptions.hasKey("payload")){
      JSONObject payload= LwNotifyUtils.convertMapToJson(foregroundOptions.getMap("payload"));
      intent.putExtra("payload",payload.toString());
    }
    intent.setAction(LwConstants.ACTION_SHOW_NOTIFICATION);
    getReactApplicationContext().startService(intent);
  }

  @ReactMethod
  public void hideNotification() {
//    if (IncomingCallActivity.active) {
//      IncomingCallActivity.getInstance().destroyActivity(false);
//    }
    Intent intent = new Intent(getReactApplicationContext(), LwNotifyService.class);
    intent.setAction(LwConstants.ACTION_HIDE_NOTIFICATION);
    getReactApplicationContext().stopService(intent);
  }

}
