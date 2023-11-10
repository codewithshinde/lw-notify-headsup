import { NativeModules, Platform, NativeEventEmitter } from 'react-native';

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

let eventEmitter: any;
if (isAndroid) {
  eventEmitter = new NativeEventEmitter(LwNotifyHeadsup);
}

enum RNNotifyActions {
  RNNotifyAcceptAction = 'RNNotifyAcceptAction',
  RNNotifyRejectAction = 'RNNotifyRejectAction',
  RNNotifyFullScreenAcceptAction = 'RNNotifyFullScreenAcceptAction',
  RNNotifyFullScreenRejectAction = 'RNNotifyFullScreenRejectAction',
}

interface NotificationData {
  channelId: string;
  channelName: string;
  notificationSound: string;
  notificationId: string;
  notificationTitle: string;
  notificationInfo: string;
  timeout: number;
  icon?: string;
  acceptText: string;
  rejectText: string;
  notificationColor?: string;
  payload?: any;
}

class LWNotify {
  private eventsHandler;
  isAndroid = Platform.OS === 'android';

  constructor() {
    this.eventsHandler = new Map();
  }

  displayNotification = (notifyOptions: NotificationData) => {
    if (!isAndroid) return;
    LwNotifyHeadsup.displayNotification(notifyOptions);
  };

  addEventListener = (type: string, handler: any) => {
    if (!isAndroid) return;
    let listener;
    if (type === 'answer') {
      listener = eventEmitter.addListener(
        RNNotifyActions.RNNotifyAcceptAction,
        (eventPayload: any) => {
          handler(eventPayload);
        }
      );
    } else if (type === 'endCall') {
      listener = eventEmitter.addListener(
        RNNotifyActions.RNNotifyRejectAction,
        (eventPayload: any) => {
          handler(eventPayload);
        }
      );
    } else {
      return;
    }
    this.eventsHandler.set(type, listener);
  };

  addFullScreenEventListener = (type: string, handler: any) => {
    if (!isAndroid) return;
    let listener;
    if (type === 'accept') {
      listener = eventEmitter.addListener(
        RNNotifyActions.RNNotifyFullScreenAcceptAction,
        (eventPayload: any) => {
          handler(eventPayload);
        }
      );
    } else if (type === 'reject') {
      listener = eventEmitter.addListener(
        RNNotifyActions.RNNotifyFullScreenRejectAction,
        (eventPayload: any) => {
          handler(eventPayload);
        }
      );
    } else {
      return;
    }
    this.eventsHandler.set(type, listener);
  };

  removeEventListener = (type: any) => {
    if (!isAndroid) return;
    const listener = this.eventsHandler.get(type);
    if (!listener) {
      return;
    }
    listener.remove();
    this.eventsHandler.delete(type);
  };
}

export default new LWNotify();
