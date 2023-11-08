import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import LWNotify from 'react-native-lw-notify-headsup';

export default function App() {
  const [result, setResult] = React.useState<string>();

  const displayNotiee = () => {
    LWNotify.displayNotification({
      channelId: 'Channel123',
      channelName: 'ChannelName',
      notificationSound: 'default',
      notificationId: 'Notification123',
      notificationTitle: 'ORDER ALERT',
      notificationInfo: 'You can 30 sec to choose',
      timeout: 6000,
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
