package com.younetsi.wakelock_plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.flutter.plugin.common.EventChannel

class ScreenReceiver(private val eventSink: EventChannel.EventSink) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        eventSink.success(intent?.action ?: "")
    }
}