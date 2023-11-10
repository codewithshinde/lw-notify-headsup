# react-native-lw-notify-headsup

# Installation

`React-Native-lw-notify` can be installed using npm or yarn. Here are the steps to install it.

1. First, open your terminal and navigate to the root directory of your React Native project.
2. If you are using `npm`, run the following command:

```bash
npm install react-native-lw-notify-headsup

```

If you are using `yarn`, run the following command instead:

```bash
yarn add react-native-lw-notify-headsup

```

1. In  `android/app/build.gradle` under `dependencies {}`

```bash
dependencies {
    implementation("com.facebook.react:react-android")
    implementation platform('com.google.firebase:firebase-bom:32.5.0')
    implementation 'com.google.firebase:firebase-analytics'
    implementation project(':react-native-lw-notify-headsup') // Add this line
}

```

1. `android/settings.gradle`

```bash
include ':react-native-lw-notify-headsup'
project(':react-native-lw-notify-headsup').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-lw-notify-headsup/android')
```

1. AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />

<activity
		android:name="com.lwnotifyheadsup.FullScreenNotification"
    android:launchMode="singleTask"
    android:excludeFromRecents="true"
    android:exported="true"
    android:showWhenLocked="true"
    android:turnScreenOn="true"
 />
<service
  android:name="com.lwnotifyheadsup.LwNotifyService"
  android:enabled="true"
  android:stopWithTask="false"
  android:exported="true" 
/>
```

1. In `android/build.gradle`

```bash
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    ext {
        buildToolsVersion = "33.0.0"
        minSdkVersion = 21
        compileSdkVersion = 34 //change to 33
        targetSdkVersion = 33
        ndkVersion = "23.1.7779620"
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2") // add this version
        classpath("com.facebook.react:react-native-gradle-plugin")
    }
}
```

# Usage example

```jsx
import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import LWNotify from 'react-native-lw-notify-headsup';

export default function App() {
  const [result, setResult] = React.useState<string>();

  const displayNotiee = () => {
    const uniqueIds = 'C-04-channel';
    LWNotify.displayNotification({
      channelId: uniqueIds,
      channelName: uniqueIds,
      notificationSound: 'type1',
      notificationId: '3432drd',
      notificationTitle: 'ORDER ALERT',
      notificationInfo: 'You can 40 sec to choose',
      timeout: 300000,
      icon: 'ic_launcher',
      acceptText: 'Accept',
      rejectText: 'Reject',
      notificationColor: 'colorAccent',
      payload: {
        key1: 'value1',
        key2: 'value2',
      },
    });

    // Listen to headless action events
    LWNotify.addEventListener('endCall', (data: any) => {
      setResult(JSON.stringify(data));
    });

    LWNotify.addEventListener('answer', (data: any) => {
      setResult(JSON.stringify(data));
    });

    LWNotify.addFullScreenEventListener('accept', (data: any) => {
      setResult(JSON.stringify(data));
    });
  };

  return (
    <View style={styles.container}>
      <Text>Result payload: {result}</Text>
      <Button title="PRESS" onPress={displayNotiee} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
```
