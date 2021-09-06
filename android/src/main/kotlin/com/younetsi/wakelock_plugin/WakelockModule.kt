package com.younetsi.wakelock_plugin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import android.util.Log
import io.flutter.plugin.common.EventChannel
import android.app.KeyguardManager

class WakeLockModule(val appContext: Context, val binaryMessenger: BinaryMessenger) {
    private var activity: Activity? = null
    private var screenReceiver: ScreenReceiver? = null

    fun setActivity(activity: Activity?) {
        this.activity = activity;
    }

    @SuppressLint("WrongConstant")
    fun backToForeground(result: MethodChannel.Result) {
        val context = appContext.applicationContext
        val packageName = context.packageName

        val focusIntent = context.packageManager.getLaunchIntentForPackage(packageName)!!.cloneFilter()
        val _activity = this.activity

        if (_activity != null) {
            focusIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity!!.startActivity(focusIntent);

            result.success(false)
        } else {
            focusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK +
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            context.startActivity(focusIntent)

            result.success(true)
        }
    }

    fun acquireWakelock(result: MethodChannel.Result) {
        val intent = Intent(appContext, WakeLockService::class.java)
        val componentName = appContext.startService(intent)

        if (componentName != null) {
            Log.d(TAG, "componentName != null")
            val requested = WakeLockService.acquireWakelock(appContext)
            result.success(requested)

            return;
        }

        Log.d(TAG, "componentName = null")
        result.success(false)
    }

    fun onScreenStateListen(arguments: Any?, events: EventChannel.EventSink?) {
        if (events != null) {
            screenReceiver = ScreenReceiver(events)

            /// Create IntentFilter with the screen actions
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON) // Turn on screen
            filter.addAction(Intent.ACTION_SCREEN_OFF) // Turn off Screen
            filter.addAction(Intent.ACTION_USER_PRESENT) // Unlock screen

            /// Register
            appContext.registerReceiver(screenReceiver, filter)
        }
    }

    fun onScreenStateCancel(arguments: Any?) {
        appContext.unregisterReceiver(screenReceiver)
    }

    fun getScreenAndKeyguardState(result: MethodChannel.Result) {
        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager;
        val keyguardManager = appContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager;

        Log.e(TAG, "isInteractive: ${powerManager.isInteractive}")
        Log.e(TAG, "isDeviceLocked: ${keyguardManager.isDeviceLocked}")

        var intResult = 0;


        intResult = intResult or (if (powerManager.isInteractive) 1 shl 0 else 0)
        intResult = intResult or (if (keyguardManager.isDeviceLocked) 1 shl 1 else 0)

        result.success(intResult)
    }
}