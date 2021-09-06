package com.younetsi.wakelock_plugin


import android.util.Log
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** WakeupPlugin */
class WakeLockPlugin: FlutterPlugin, MethodCallHandler, ActivityAware, EventChannel.StreamHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var module : WakeLockModule

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    Log.d(TAG, "onAttachedToEngine")
    module = WakeLockModule(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger)

    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "wakelock_plugin")
    channel.setMethodCallHandler(this)

    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "screenState")
    eventChannel.setStreamHandler(this)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    Log.d(TAG, "onDetachedFromEngine")
    channel.setMethodCallHandler(null)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    }
    else if (call.method == "backToForeground") {
      module.backToForeground(result)
    }
    else if (call.method == "acquireWakelock") {
      module.acquireWakelock(result)
    }
    else if (call.method == "getScreenAndKeyguardState") {
      module.getScreenAndKeyguardState(result)
    }
    else {
      result.notImplemented()
    }
  }

  /**
   * Start ActivityAware callbacks
   */

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    module.setActivity(binding.activity)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    module.setActivity(null)
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    module.setActivity(binding.activity)
  }

  override fun onDetachedFromActivity() {
    module.setActivity(null)
  }

  /**
   * Start ActivityAware callback
   */

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    module.onScreenStateListen(arguments, events)
  }

  override fun onCancel(arguments: Any?) {
    module.onScreenStateCancel(arguments)
  }
}