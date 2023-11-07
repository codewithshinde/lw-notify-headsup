package com.lwnotifyheadsup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ReceiverActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ReceiverHandler.handleNotification(this, getIntent());
    finish();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    ReceiverHandler.handleNotification(this, intent);
    finish();
  }
}
