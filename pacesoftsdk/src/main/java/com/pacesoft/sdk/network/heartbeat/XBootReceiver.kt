package com.pacesoft.sdk.network.heartbeat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class XBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && !XHeartbeatService.sIsHeartbeatServiceRunning)
            XHeartbeatService.startHeartbeatService(context)
    }
}
