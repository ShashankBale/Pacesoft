package com.pacesoft.sdk.network.heartbeat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.pacesoft.sdk.R
import x.code.util.XConst

class XHeartBeatNotification(val mContext: Context) {

    fun getNotificationObj(): Notification {
        val notification = createNotification()
        notification.flags = notification.flags or
                (Notification.FLAG_ONGOING_EVENT or Notification.FLAG_NO_CLEAR)
        return notification
    }

    private fun createNotification(): Notification {
        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        val manager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            XConst.NOTIFICATION_HEARTBEAT_CHANNEL_ID,
            XConst.NOTIFICATION_HEARTBEAT_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).let {
            it.description = XConst.NOTIFICATION_HEARTBEAT_CHANNEL_DESC
            //it.enableLights(true)
            //it.lightColor = Color.RED
            //it.enableVibration(true)
            //it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            it
        }
        manager.createNotificationChannel(channel)

        /*
        val pendingIntent: PendingIntent = Intent(this, Splash::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }
        */


        val builder: Notification.Builder =
            Notification.Builder(
                mContext,
                XConst.NOTIFICATION_HEARTBEAT_CHANNEL_ID
            )

        return builder
            .setContentTitle(XConst.NOTIFICATION_HEARTBEAT_TITLE)
            .setContentText(XConst.NOTIFICATION_HEARTBEAT_TEXT)
            //.setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_stat_project)
            .setTicker("")
            //.setPriority(Notification.PRIORITY_LOW) // for under android 26 compatibility
            .build()
    }

}