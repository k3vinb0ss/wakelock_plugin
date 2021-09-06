
import 'dart:async';

import 'package:flutter/services.dart';

enum ScreenStateEvent { SCREEN_UNLOCKED, SCREEN_ON, SCREEN_OFF }

/// Android only
class WakeLockPlugin {
  static const MethodChannel _channel =
      const MethodChannel('wakelock_plugin');
  static Stream<ScreenStateEvent>? _screenStateStream;

  static const SCREENON_MASK = (1 << 0);
  static const DEVICE_LOCK_MASK = (1 << 0);

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> backToForeground() async {
    final isOpened = await _channel.invokeMethod('backToForeground');
    return isOpened;
  }

  static Future<bool> accquireWakelock() async {
    final requestWakeLock = await _channel.invokeMethod('acquireWakelock');
    return requestWakeLock;
  }

  static Stream<ScreenStateEvent> registerScreenState() {
    final eventChannel = EventChannel('screenState');

    if (_screenStateStream == null) {
      _screenStateStream = eventChannel
          .receiveBroadcastStream()
          .map((event) => _parseScreenStateEvent(event));
    }
    return _screenStateStream!;
  }

  /// int value, can be used with [SCREENON_MASK] and [DEVICE_LOCK_MASK]
  static Future<int> getScreenAndKeyguardState() async {
    final state = await _channel.invokeMethod('getScreenAndKeyguardState');
    return state;
  }
}

ScreenStateEvent _parseScreenStateEvent(String event) {
  switch (event) {
  /** Android **/
    case 'android.intent.action.SCREEN_OFF':
      return ScreenStateEvent.SCREEN_OFF;
    case 'android.intent.action.SCREEN_ON':
      return ScreenStateEvent.SCREEN_ON;
    case 'android.intent.action.USER_PRESENT':
      return ScreenStateEvent.SCREEN_UNLOCKED;
    default:
      throw new ArgumentError('$event was not recognized.');
  }
}
