//import { NativeEventEmitter } from 'react-native';
import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-lw-notify-headsup' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const isAndroid = Platform.OS === 'android';

const LwNotifyHeadsup = NativeModules.LwNotifyHeadsup
  ? NativeModules.LwNotifyHeadsup
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

// let eventEmitter: any;
// if (isAndroid) {
//   eventEmitter = new NativeEventEmitter(LwNotifyHeadsup);
// }

export interface foregroundOptionsModel {
  channelId: string;
  channelName: string;
  notificationIcon: string;
  notificationTitle: string;
  notificationBody: string;
  answerText: string;
  declineText: string;
  notificationColor?: string;
  notificationSound?: string;
  mainComponent?: string;
  payload?: any;
}

export function multiply(a: number, b: number): Promise<number> {
  return LwNotifyHeadsup.multiply(a, b);
}

const displayNotification = (
  uuid: string,
  avatar: string | null,
  timeout: number | null,
  foregroundOptions: foregroundOptionsModel
) => {
  console.log('isAndroid', isAndroid);
  if (!isAndroid) return;
  LwNotifyHeadsup.displayNotification(
    uuid,
    avatar,
    timeout ? timeout : 5000,
    foregroundOptions
  );
};

const displayFullScreenNotification = () => {
  LwNotifyHeadsup.displayFullScreenNotification();
};

export { displayNotification, displayFullScreenNotification };
