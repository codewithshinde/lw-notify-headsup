package com.lwnotifyheadsup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

public class FullScreenNotification extends AppCompatActivity {

  private static final String TAG = "FullScreenNotification";
  private static final String TAG_KEYGUARD = "FullScreenNotification:unLock";
  private TextView notificationId;
  private Button acceptButton;
  private Button rejectButton;

  static boolean isNotificationActive = false;

  static FullScreenNotification instance;

  public static FullScreenNotification getInstance() {
    return instance;
  }


  @Override
  public void onStart() {
    super.onStart();
    isNotificationActive = true;
    instance = this;
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  @Override
  public void onDestroy() {
    if (isNotificationActive) {
      //dismissIncoming(Constants.ACTION_REJECTED_CALL);
    }
    super.onDestroy();
  }

  public void destroyActivity() {
    isNotificationActive = false;
    if (android.os.Build.VERSION.SDK_INT >= 21) {
      finishAndRemoveTask();
    } else {
      finish();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true);
      setTurnScreenOn(true);
      KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
      if (keyguardManager.isDeviceLocked()) {
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(TAG_KEYGUARD);
        keyguardLock.disableKeyguard();
      }
    }
    getWindow().addFlags(
      WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

    Bundle bundle = getIntent().getExtras();

    super.onCreate(savedInstanceState);

    if (bundle.containsKey("customComponent") && bundle.getString("customComponent") != null) {
      //handle custom notification
    } else {
      setContentView(R.layout.activity_full_screen_notification);
    }

    //TODO: add multiple fields to the UI
    if (bundle != null) {
      notificationId = findViewById(R.id.notificationId);
      if (bundle.containsKey("notificationId")) {
        String name = bundle.getString("notificationId");
        notificationId.setText(name);
      }
    }

    acceptButton = findViewById(R.id.acceptButton);
    rejectButton = findViewById(R.id.rejectButton);

    acceptButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

      }
    });
  }

  private void acceptActionHandler() {
    WritableMap params = Arguments.createMap();
    Bundle bundle = getIntent().getExtras();
    if (bundle.containsKey("payload")) {
      params.putString("payload", bundle.getString("payload"));
    }
    params.putString("notificationId", "TEST_123");
  }

}
