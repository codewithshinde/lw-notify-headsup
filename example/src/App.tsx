import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import {
  multiply,
  displayNotification,
  displayFullScreenNotification,
} from 'react-native-lw-notify-headsup';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  const displayNotiee = () => {
    displayNotification('22221a97-8eb4-4ac2-b2cf-0a3c0b9100ad', null, 30000, {
      channelId: 'com.abc.incomingcall2',
      channelName: 'Incoming video call',
      notificationIcon: 'ic_launcher', //mipmap
      notificationTitle: 'Linh Vo',
      notificationBody: 'Incoming video call',
      answerText: 'Answer',
      declineText: 'Decline',
      notificationColor: 'colorAccent',
      notificationSound: 'default',
    });
  };

  const displayFullScreen = () => {
    displayFullScreenNotification();
  };

  return (
    <View style={styles.container}>
      <Text>Resultssssssss3: {result}</Text>
      <Button title="PRESS" onPress={displayNotiee} />
      <Button title="FULL SCREEN" onPress={displayFullScreen} />
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
