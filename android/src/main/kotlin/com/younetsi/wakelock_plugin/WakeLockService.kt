package com.younetsi.wakelock_plugin

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log

class WakeLockService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "WakeLockService onBind");
        return null;
    }

    override fun onDestroy() {
        super.onDestroy()

        iWakelock?.release()
    }

    companion object {
        private var iWakelock: PowerManager.WakeLock? = null

        fun acquireWakelock(context: Context) : Boolean {
            if (iWakelock == null || !iWakelock!!.isHeld) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                iWakelock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, WakeLockService::class.java.canonicalName)
                iWakelock!!.setReferenceCounted(false)
                iWakelock!!.acquire(3*1000L /*3 minutes*/)
                Log.d(TAG, "accquireWakelock from Service -> done")
                return true
            }

            return false
        }
    }
}